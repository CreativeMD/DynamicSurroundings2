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

package org.orecruncher.dsurround;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.config.Config;
import org.orecruncher.dsurround.config.ConfigMenuBuilder;
import org.orecruncher.dsurround.gui.Keys;
import org.orecruncher.lib.compat.ModEnvironment;
import org.orecruncher.lib.config.ConfigGui;
import org.orecruncher.lib.fml.ClientLoginChecks;
import org.orecruncher.lib.fml.ConfigUtils;
import org.orecruncher.lib.fml.UpdateChecker;
import org.orecruncher.lib.logging.ModLog;

import net.minecraftforge.fml.common.Mod;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.ICreativeLoader;
import team.creative.creativecore.client.ClientLoader;

@Mod(DynamicSurroundings.MODID)
public final class DynamicSurroundings implements ClientLoader {
    
    public static final String MODID = "dsurround";
    public static final ModLog LOGGER = new ModLog(DynamicSurroundings.class);
    
    /** Path to the mod's configuration directory */
    public static final Path CONFIG_PATH = ConfigUtils.getConfigPath(MODID);
    
    /** Path to the external config data cache for user customization */
    public static final File DATA_PATH = Paths.get(CONFIG_PATH.toString(), "configs").toFile();
    
    /** Path to the external folder for dumping data */
    public static final File DUMP_PATH = Paths.get(CONFIG_PATH.toString(), "dumps").toFile();
    
    public DynamicSurroundings() {
        ICreativeLoader loader = CreativeCore.loader();
        loader.registerClient(this);
    }
    
    public static void doConfigMenuSetup() {
        // If ClothAPI is available, use that.  Otherwise post a message to install it.
        if (ModEnvironment.ClothAPI.isLoaded())
            ConfigGui.registerConfigGui(new ConfigMenuBuilder());
        else
            ConfigGui.registerConfigGui(new ConfigGui.InstallClothGuiFactory());
    }
    
    private static void createPath(@Nonnull final File path) {
        if (!path.exists()) {
            try {
                path.mkdirs();
            } catch (@Nonnull final Throwable t) {
                LOGGER.error(t, "Unable to create data path %s", path.toString());
            }
        }
    }
    
    @Override
    public void onInitializeClient() {
        // Since we are 100% client side
        ICreativeLoader loader = CreativeCore.loader();
        loader.registerDisplayTest(() -> loader.ignoreServerNetworkConstant(), (a, b) -> true);
        
        // Initialize our configuration
        Config.setup();
        
        // Create additional data paths if needed
        createPath(DATA_PATH);
        createPath(DUMP_PATH);
        
        doConfigMenuSetup();
        
        Keys.register();
        if (Config.CLIENT.logging.onlineVersionCheck.get())
            ClientLoginChecks.register(new UpdateChecker(DynamicSurroundings.MODID));
    }
    
}
