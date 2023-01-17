/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package org.orecruncher.lib.particles;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleRenderType implements IParticleRenderType {
    
    private final ResourceLocation texture;
    
    public ParticleRenderType(@Nonnull final ResourceLocation texture) {
        this.texture = texture;
    }
    
    @Nonnull
    protected VertexFormat getVertexFormat() {
        return DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP;
    }
    
    @Override
    public void beginRender(@Nonnull final BufferBuilder buffer, @Nonnull final TextureManager textureManager) {
        RenderHelper.disableStandardItemLighting();
        textureManager.bindTexture(getTexture());
        buffer.begin(GL11.GL_QUADS, getVertexFormat());
    }
    
    protected ResourceLocation getTexture() {
        return this.texture;
    }
    
    @Override
    public void finishRender(@Nonnull final Tessellator tessellator) {
        tessellator.draw();
    }
    
    @Override
    public String toString() {
        return this.texture.toString();
    }
}
