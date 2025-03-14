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

package org.orecruncher.environs.mixins;

import javax.annotation.Nonnull;

import org.orecruncher.environs.effects.particles.ParticleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.particle.DripParticle;

@Mixin(targets = { "net.minecraft.client.particle.DripParticle$FallingLiquidParticle" })
public class MixinDripParticleDripping {
    
    @Inject(method = "updateMotion()V", at = @At("HEAD"))
    public void onHitGround(@Nonnull final CallbackInfo ci) {
        ParticleHooks.dripHandler((DripParticle) (Object) this);
    }
}
