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

package org.orecruncher.mobeffects.footsteps.accents;

import javax.annotation.Nonnull;

import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FootstepAccents {
    
    private FootstepAccents() {
        
    }
    
    private static final ObjectArray<IFootstepAccentProvider> providers = new ObjectArray<>();
    
    static {
        providers.add(new ArmorAccents());
        providers.add(new RainSplashAccent());
        providers.add(new WaterLoggedAccent());
    }
    
    public static void provide(@Nonnull final LivingEntity entity, @Nonnull final BlockPos pos, @Nonnull final ObjectArray<IAcoustic> in) {
        final BlockState state = entity.getEntityWorld().getBlockState(pos);
        providers.forEach(provider -> {
            if (provider.isEnabled())
                provider.provide(entity, pos, state, in);
        });
    }
}