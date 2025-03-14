/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.library;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.SoundTypeUtils;
import org.orecruncher.lib.blockstate.BlockStateMatcher;
import org.orecruncher.lib.blockstate.BlockStateMatcherMap;
import org.orecruncher.lib.blockstate.BlockStateParser;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.service.IModuleService;
import org.orecruncher.lib.service.ModuleServiceManager;
import org.orecruncher.lib.tags.TagUtils;
import org.orecruncher.lib.validation.MapValidator;
import org.orecruncher.lib.validation.Validators;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.config.Config;
import org.orecruncher.mobeffects.footsteps.Generator;
import org.orecruncher.mobeffects.footsteps.GeneratorQP;
import org.orecruncher.mobeffects.footsteps.Substrate;
import org.orecruncher.mobeffects.footsteps.Variator;
import org.orecruncher.mobeffects.library.config.FootstepConfig;
import org.orecruncher.mobeffects.library.config.VariatorConfig;
import org.orecruncher.mobeffects.misc.IMixinFootstepData;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.audio.acoustic.NullAcoustic;
import org.orecruncher.sndctrl.events.BlockInspectionEvent;
import org.orecruncher.sndctrl.library.Primitives;

import com.google.gson.reflect.TypeToken;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.tags.ITag;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FootstepLibrary {
    
    private static final String TEXT_FOOTSTEPS = TextFormatting.DARK_PURPLE + "<Footsteps>";
    private static final Map<Substrate, BlockAcousticMap> substrateMap = new EnumMap<>(Substrate.class);
    private static final List<SoundType> FOOTPRINT_SOUND_PROFILE = Arrays.asList(SoundType.SAND, SoundType.GROUND, SoundType.SLIME, SoundType.SNOW);
    private static final Set<Material> FOOTPRINT_MATERIAL = new ReferenceOpenHashSet<>();
    private static final BlockStateMatcherMap<Boolean> FOOTPRINT_STATES = new BlockStateMatcherMap<>();
    private static final Map<String, List<MacroEntry>> macros = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Variator> variators = new Object2ObjectOpenHashMap<>();
    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(FootstepLibrary.class);
    private static Variator defaultVariator;
    private static Variator childVariator;
    private static Variator playerVariator;
    private static Variator playerQuadrupedVariator;
    
    private static IAcoustic SPLASH;
    private static IAcoustic SWIM;
    private static IAcoustic WATERLOGGED;
    
    static {
        
        // Initialize our substrate maps
        for (final Substrate s : Substrate.values())
            substrateMap.put(s, new BlockAcousticMap());
        substrateMap.put(Substrate.NORMAL, new BlockAcousticMap(FootstepLibrary::primitiveResolver));
        
        // Initialize the known materials that leave footprints
        FOOTPRINT_MATERIAL.add(Material.CLAY);
        FOOTPRINT_MATERIAL.add(Material.EARTH);
        FOOTPRINT_MATERIAL.add(Material.SPONGE);
        FOOTPRINT_MATERIAL.add(Material.SAND);
        FOOTPRINT_MATERIAL.add(Material.SNOW_BLOCK);
        FOOTPRINT_MATERIAL.add(Material.SNOW);
        FOOTPRINT_MATERIAL.add(Material.CAKE);
        FOOTPRINT_MATERIAL.add(Material.ORGANIC);
        
        final MacroEntry MESSY = new MacroEntry("messy", "messy_ground");
        final MacroEntry NOT_EMITTER = new MacroEntry(null, "not_emitter");
        
        List<MacroEntry> entries = new ArrayList<>(3);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#sapling", entries);
        macros.put("#reed", entries);
        
        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "leaves"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush"));
        macros.put("#plant", entries);
        
        entries = new ArrayList<>(3);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush"));
        macros.put("#tallplant", entries);
        
        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "leaves"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush_straw_transition"));
        macros.put("#bush", entries);
        
        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "not_emitter"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#deadbush", entries);
        
        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "bluntwood"));
        macros.put("#woodfence", entries);
        
        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "brickstone"));
        macros.put("#stonewall", entries);
        
        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "sandstone"));
        macros.put("#sandstonewall", entries);
        
        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("foliage", "rails"));
        macros.put("#rail", entries);
        
        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "straw"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#vine", entries);
        
        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "nether_vines"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "nether_vines"));
        macros.put("#nethervines", entries);
        
        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("carpet", "rug"));
        macros.put("#moss", entries);
        
        entries = new ArrayList<>(10);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "brush"));
        entries.add(new MacroEntry("age", "3", "foliage", "brush"));
        entries.add(new MacroEntry("age", "4", "foliage", "brush_straw_transition"));
        entries.add(new MacroEntry("age", "5", "foliage", "brush_straw_transition"));
        entries.add(new MacroEntry("age", "6", "foliage", "straw"));
        entries.add(new MacroEntry("age", "7", "foliage", "straw"));
        macros.put("#wheat", entries);
        
        entries = new ArrayList<>(10);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "3", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "4", "foliage", "brush"));
        entries.add(new MacroEntry("age", "5", "foliage", "brush"));
        entries.add(new MacroEntry("age", "6", "foliage", "brush"));
        entries.add(new MacroEntry("age", "7", "foliage", "brush"));
        macros.put("#crop", entries);
        
        entries = new ArrayList<>(6);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "brush"));
        entries.add(new MacroEntry("age", "3", "foliage", "brush"));
        macros.put("#beets", entries);
    }
    
    private FootstepLibrary() {
        
    }
    
    static void initialize() {
        ModuleServiceManager.instance().add(new FootstepLibraryService());
    }
    
    static void initFromConfig(@Nonnull final FootstepConfig mod) {
        
        // Load up configured primitives.  These will overwrite existing ones.
        for (final Map.Entry<String, String> kvp : mod.primitives.entrySet()) {
            if (SoundTypeUtils.getSoundType(kvp.getKey()) == null) {
                LOGGER.warn("'%s' is not a known SoundType; adding and praying...", kvp.getKey());
            }
            final ResourceLocation loc = Primitives.createFootstepResource(kvp.getKey());
            Library.resolve(loc, kvp.getValue(), true);
        }
        
        // Apply acoustics based on configured tagging
        for (final Map.Entry<String, String> kvp : mod.blockTags.entrySet()) {
            registerTag(kvp.getKey(), kvp.getValue());
        }
        
        // Now do the regular block stuff
        for (final Map.Entry<String, String> kvp : mod.footsteps.entrySet()) {
            register(kvp.getKey(), kvp.getValue());
        }
        
        // Register special blocks for footprints
        for (final String print : mod.footprints) {
            final BlockStateMatcher matcher = BlockStateMatcher.create(print);
            if (matcher != BlockStateMatcher.AIR)
                FOOTPRINT_STATES.put(matcher, true);
        }
    }
    
    /** This is pretty heavy - it will blow out all blockstates to get their values */
    public static Stream<String> dump() {
        return ForgeUtils.getBlockStates().stream().map(state -> {
            try {
                final StringBuilder builder = new StringBuilder();
                builder.append(BlockStateMatcher.create(state).toString()).append(" -> [");
                IAcoustic[] cached = getCachedAcoustics(state);
                if (cached != null) {
                    for (final Substrate sub : Substrate.values()) {
                        IAcoustic acoustic = cached[sub.ordinal()];
                        builder.append(sub.name()).append("=");
                        if (acoustic != null)
                            builder.append(acoustic.toString());
                        else
                            builder.append("NOT SET");
                        builder.append(", ");
                    }
                } else {
                    builder.append("No acoustics defined");
                }
                builder.append("]");
                return builder.toString();
            } catch (@Nonnull final Throwable ignore) {}
            return "ERROR";
        }).sorted();
    }
    
    @SubscribeEvent
    public static void onInspectionEvent(@Nonnull final BlockInspectionEvent evt) {
        evt.data.add(TEXT_FOOTSTEPS);
        collectData(evt.state, evt.data);
    }
    
    @Nonnull
    public static IAcoustic getRainSplashAcoustic() {
        if (SPLASH == null)
            SPLASH = Library.resolve(new ResourceLocation(MobEffects.MOD_ID, "waterfine"));
        return SPLASH;
    }
    
    @Nonnull
    public static IAcoustic getSwimAcoustic() {
        if (SWIM == null)
            SWIM = Library.resolve(new ResourceLocation(MobEffects.MOD_ID, "_swim"));
        return SWIM;
    }
    
    public static IAcoustic getWaterLoggedAcoustic() {
        if (WATERLOGGED == null)
            WATERLOGGED = Library.resolve(new ResourceLocation(MobEffects.MOD_ID, "_waterlogged"));
        return WATERLOGGED;
    }
    
    @Nonnull
    private static Variator getVariator(@Nonnull final String varName) {
        return variators.getOrDefault(varName, defaultVariator);
    }
    
    @Nonnull
    public static IAcoustic getBlockAcoustics(@Nonnull final BlockState state) {
        return getBlockAcoustics(state, Substrate.NORMAL);
    }
    
    @Nonnull
    public static IAcoustic getBlockAcoustics(@Nonnull final BlockState state, @Nonnull final Substrate substrate) {
        // Walking an edge of a block can produce this
        if (state.getMaterial() == Material.AIR)
            return Constants.NOT_EMITTER;
        // Get our cached entries
        IAcoustic[] cached = ((IMixinFootstepData) state).getAcoustics();
        if (cached == null) {
            ((IMixinFootstepData) state).setAcoustics(cached = new IAcoustic[Substrate.values().length]);
        }
        IAcoustic result = cached[substrate.ordinal()];
        if (result == null) {
            result = cached[substrate.ordinal()] = substrateMap.get(substrate).getBlockAcoustics(state);
        }
        return result;
    }
    
    private static IAcoustic[] getCachedAcoustics(@Nonnull final BlockState state) {
        IAcoustic[] cached = ((IMixinFootstepData) state).getAcoustics();
        if (cached == null) {
            ((IMixinFootstepData) state).setAcoustics(cached = new IAcoustic[Substrate.values().length]);
        }
        for (final Substrate sub : Substrate.values()) {
            IAcoustic result = cached[sub.ordinal()];
            if (result == null) {
                cached[sub.ordinal()] = substrateMap.get(sub).getBlockAcoustics(state);
            }
        }
        return cached;
    }
    
    private static void put(@Nonnull final BlockStateMatcher info, @Nullable final String substrate, @Nonnull final String acousticList) {
        
        final IAcoustic acoustics = Library.resolve(MobEffects.MOD_ID, acousticList, r -> {
            if (r.getPath().equals("not_emitter"))
                return Constants.NOT_EMITTER;
            if (r.getPath().equals("messy_ground"))
                return Constants.MESSY_GROUND;
            return null;
        });
        
        substrateMap.get(Substrate.get(substrate)).put(info, acoustics);
    }
    
    private static void register0(@Nonnull final String key, @Nonnull final String acousticList) {
        
        final Optional<BlockStateParser.ParseResult> parseResult = BlockStateParser.parseBlockState(key);
        if (parseResult.isPresent()) {
            final BlockStateParser.ParseResult name = parseResult.get();
            final BlockStateMatcher matcher = BlockStateMatcher.create(name);
            if (matcher.isEmpty()) {
                LOGGER.warn("Unable to identify block state '%s'", key);
            } else {
                final String substrate = name.getExtras();
                put(matcher, substrate, acousticList);
            }
        } else {
            LOGGER.warn("Malformed key in blockMap '%s'", key);
        }
    }
    
    private static void registerTag(@Nonnull String tagName, @Nonnull final String acousticList) {
        String substrate = null;
        final int idx = tagName.indexOf('+');
        
        if (idx >= 0) {
            substrate = tagName.substring(idx + 1);
            tagName = tagName.substring(0, idx);
        }
        
        final ITag<Block> blockTag = TagUtils.getBlockTag(tagName);
        if (blockTag != null) {
            final List<Block> elements = blockTag.getAllElements();
            if (elements.size() == 0) {
                LOGGER.debug("No blocks associated with tag '%s'", tagName);
            } else {
                for (final Block b : blockTag.getAllElements()) {
                    String blockName = Objects.requireNonNull(b.getRegistryName()).toString();
                    if (substrate != null)
                        blockName = blockName + "+" + substrate;
                    register(blockName, acousticList);
                }
            }
        } else {
            LOGGER.debug("Unable to identify block tag '%s'", tagName);
        }
    }
    
    private static void register(@Nonnull final String key, @Nonnull final String acousticList) {
        if (acousticList.startsWith("#")) {
            final List<MacroEntry> macro = macros.get(acousticList);
            if (macro != null) {
                macro.stream().map(m -> m.expand(key)).forEach(t -> register0(t.getLeft(), t.getRight()));
            } else {
                LOGGER.debug("Unknown macro '%s'", acousticList);
            }
        } else {
            register0(key, acousticList);
        }
    }
    
    @Nonnull
    public static Generator createGenerator(@Nonnull final LivingEntity entity) {
        Variator var;
        if (entity.isChild()) {
            var = childVariator;
        } else if (entity instanceof PlayerEntity) {
            var = Config.CLIENT.footsteps.footstepsAsQuadruped.get() ? playerQuadrupedVariator : playerVariator;
        } else {
            var = getVariator(entity.getType().getRegistryName().toString());
        }
        
        return var.QUADRUPED ? new GeneratorQP(var) : new Generator(var);
    }
    
    private static void collectData(@Nonnull final BlockState state, @Nonnull final List<String> data) {
        
        final int s = data.size();
        final IAcoustic temp = getBlockAcoustics(state);
        if (temp != Constants.EMPTY)
            data.add(temp.toString());
        
        for (final Map.Entry<Substrate, BlockAcousticMap> e : substrateMap.entrySet()) {
            final IAcoustic acoustics = e.getValue().getBlockAcoustics(state);
            if (acoustics != Constants.EMPTY)
                data.add(e.getKey() + ":" + acoustics.toString());
        }
        
        if (s == data.size()) {
            data.add("** NONE **");
        }
    }
    
    @Nonnull
    private static IAcoustic primitiveResolver(@Nonnull final BlockState state) {
        // If the state does not block movement or is liquid, like grass and plants, then it is not an
        // emitter.  Otherwise we get strange effects when edge walking on blocks with a plant
        // to the side.
        final Material mat = state.getMaterial();
        if (mat == null || !mat.blocksMovement() || mat.isLiquid())
            return Constants.NOT_EMITTER;
        final IAcoustic acoustic = Primitives.getFootstepAcoustic(state);
        return acoustic == NullAcoustic.INSTANCE ? Constants.NOT_EMITTER : acoustic;
    }
    
    public static boolean hasFootprint(@Nonnull final BlockState state) {
        Boolean result = ((IMixinFootstepData) state).hasFootprint();
        if (result != null)
            return result;
        
        result = FOOTPRINT_STATES.get(state);
        if (result == null) {
            result = FOOTPRINT_MATERIAL.contains(state.getMaterial()) || FOOTPRINT_SOUND_PROFILE.contains(state.getSoundType());
        }
        ((IMixinFootstepData) state).setHasFootprint(result);
        return result;
    }
    
    private static class MacroEntry {
        public final String propertyName;
        public final String propertyValue;
        public final String substrate;
        public final String value;
        
        public MacroEntry(@Nullable final String substrate, @Nonnull final String value) {
            this(null, null, substrate, value);
        }
        
        public MacroEntry(@Nullable final String propertyName, @Nullable final String propertyValue, @Nullable final String substrate, @Nonnull final String value) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
            this.substrate = substrate;
            this.value = value;
        }
        
        @Nonnull
        public Pair<String, String> expand(@Nonnull final String base) {
            final StringBuilder builder = new StringBuilder();
            builder.append(base);
            if (this.propertyName != null) {
                builder.append('[');
                builder.append(this.propertyName).append('=').append(this.propertyValue);
                builder.append(']');
            }
            
            if (this.substrate != null) {
                builder.append('+').append(this.substrate);
            }
            
            return Pair.of(builder.toString(), this.value);
        }
    }
    
    private static class FootstepLibraryService implements IModuleService {
        
        private static final Type variatorType = TypeToken.getParameterized(Map.class, String.class, VariatorConfig.class).getType();
        
        static {
            Validators.registerValidator(variatorType, new MapValidator<String, VariatorConfig>());
        }
        
        @Override
        public String name() {
            return "FootstepLibrary";
        }
        
        @Override
        public void start() {
            
            Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "variators.json");
            
            IResourceAccessor.process(configs, accessor -> {
                final Map<String, VariatorConfig> cfg = accessor.as(variatorType);
                for (final Map.Entry<String, VariatorConfig> kvp : cfg.entrySet()) {
                    variators.put(kvp.getKey(), new Variator(kvp.getValue()));
                }
            });
            
            defaultVariator = getVariator("default");
            childVariator = getVariator("child");
            playerVariator = getVariator(Config.CLIENT.footsteps.firstPersonFootstepCadence.get() ? "player_slow" : "player");
            playerQuadrupedVariator = getVariator(Config.CLIENT.footsteps.firstPersonFootstepCadence.get() ? "quadruped_slow" : "quadruped");
            
            configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "footsteps.json");
            
            IResourceAccessor.process(configs, accessor -> initFromConfig(accessor.as(FootstepConfig.class)));
            
            substrateMap.forEach((key, value) -> value.trim());
        }
        
        @Override
        public void log() {
            if (Config.CLIENT.logging.enableLogging.get()) {
                LOGGER.info("Registered Variators");
                LOGGER.info("====================");
                
                for (final String v : variators.keySet()) {
                    LOGGER.info(v);
                }
            }
        }
        
        @Override
        public void stop() {
            variators.clear();
            FOOTPRINT_STATES.clear();
            
            for (final BlockAcousticMap m : substrateMap.values())
                m.clear();
            
            ForgeUtils.getBlockStates().forEach(state -> ((IMixinFootstepData) state).setAcoustics(null));
        }
    }
}
