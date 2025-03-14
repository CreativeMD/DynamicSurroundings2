/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.dsurround.config;

import java.io.File;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.huds.lightlevel.LightLevelHUD;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = DynamicSurroundings.MODID + File.separator + DynamicSurroundings.MODID + "-client.toml";
    public static final ForgeConfigSpec SPEC;
    
    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
    
    private Config() {}
    
    private static void applyConfig() {
        DynamicSurroundings.LOGGER.setDebug(Config.CLIENT.logging.enableLogging.get());
        DynamicSurroundings.LOGGER.setTraceMask(Config.CLIENT.logging.flagMask.get());
    }
    
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        applyConfig();
        DynamicSurroundings.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }
    
    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        DynamicSurroundings.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }
    
    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, CLIENT_CONFIG);
    }
    
    public static class Flags {
        public static int ALLOW_LIGHTLEVEL_HUD = 0x01;
        public static int ALLOW_CHUNK_BORDER_HUD = 0x02;
    }
    
    public static class Client {
        
        public final Logging logging;
        public final LightLevel lightLevel;
        
        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.lightLevel = new LightLevel(builder);
        }
        
        public static class Logging {
            
            public final BooleanValue enableLogging;
            public final BooleanValue onlineVersionCheck;
            public final IntValue flagMask;
            
            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how Sound Control logging will behave").push("Logging Options");
                
                this.enableLogging = builder.comment("Enables/disables debug logging of the mod").translation("dsurround.cfg.logging.EnableDebug").define("Debug Logging", false);
                
                this.onlineVersionCheck = builder.comment("Enables/disables display of version check information").translation("dsurround.cfg.logging.VersionCheck")
                        .define("Online Version Check Result", true);
                
                this.flagMask = builder.comment("Bitmask for toggling various debug traces").translation("dsurround.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);
                
                builder.pop();
            }
        }
        
        public static class LightLevel {
            
            public final ForgeConfigSpec.EnumValue<LightLevelHUD.ColorSet> colorSet;
            public final ForgeConfigSpec.EnumValue<LightLevelHUD.Mode> mode;
            public final IntValue range;
            public final BooleanValue hideSafe;
            public final IntValue lightSpawnThreshold;
            
            LightLevel(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for configuring the Light Level HUD").push("Light Level HUD Options");
                
                this.colorSet = builder.comment("Coloring style to use for numbering").translation("dsurround.cfg.lightlevel.ColorSet")
                        .defineEnum("Color Set", LightLevelHUD.ColorSet.BRIGHT);
                
                this.mode = builder.comment("The type of light level to display").translation("dsurround.cfg.lightlevel.Mode").defineEnum("Display Mode", LightLevelHUD.Mode.BLOCK);
                
                this.range = builder.comment("Block range for light level analysis").translation("dsurround.cfg.lightlevel.Range").defineInRange("Block Range", 16, 8, 64);
                
                this.hideSafe = builder.comment("Do not show light level for blocks where mobs will not spawn").translation("dsurround.cfg.lightlevel.HideSafe")
                        .define("Hide Safe Blocks", false);
                
                this.lightSpawnThreshold = builder.comment("Light level at which mobs can spawn").translation("dsurround.cfg.lightlevel.SpawnThreshold")
                        .defineInRange("Mob Spawn Light Threshold", 7, 0, 15);
            }
        }
    }
}
