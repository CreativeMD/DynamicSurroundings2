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

package org.orecruncher.environs.fog;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

@OnlyIn(Dist.CLIENT)
public class FixedFogRangeCalculator extends VanillaFogRangeCalculator {
    
    protected final FogResult cached;
    
    public FixedFogRangeCalculator(final float minDistance, final float maxDistance) {
        super("FixedFogRangeCalculator");
        this.cached = new FogResult(minDistance, maxDistance);
    }
    
    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        return this.cached;
    }
    
}