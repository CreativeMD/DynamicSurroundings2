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

package org.orecruncher.dsurround.mixins;

import javax.annotation.Nullable;

import org.orecruncher.dsurround.huds.lightlevel.LightLevelHUD;
import org.orecruncher.environs.handlers.AuroraHandler;
import org.orecruncher.lib.particles.FrustumHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine {
    
    // Capture the frustrum and store away for use by the particle system
    @Inject(method = "render", at = @At("HEAD"), remap = false, require = 1)
    public void captureFrustum(PoseStack pose, MultiBufferSource.BufferSource buffer, LightTexture light, Camera cam, float partialTicks, @Nullable Frustum clippingHelper, CallbackInfo ci) {
        FrustumHelper.setFrustum(clippingHelper);
    }
    
    // Hook the tail of particle rendering so we can do our various render world last type things.
    @Inject(method = "render", at = @At("RETURN"), remap = false, require = 1)
    public void renderHook(PoseStack pose, MultiBufferSource.BufferSource buffer, LightTexture light, Camera cam, float partialTicks, @Nullable Frustum clippingHelper, CallbackInfo ci) {
        LightLevelHUD.render(pose, partialTicks);
        AuroraHandler.renderHook(pose, partialTicks);
    }
    
}
