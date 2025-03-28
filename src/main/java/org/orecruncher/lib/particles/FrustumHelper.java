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

package org.orecruncher.lib.particles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrustumHelper {
    
    private FrustumHelper() {}
    
    private static Frustum helper = null;
    
    /** Called by a Mixin to set the current clipping helper
     * 
     * @param clippingHelper
     *            Current clipping helper */
    public static void setFrustum(@Nullable final Frustum clippingHelper) {
        helper = clippingHelper;
    }
    
    /** Determines if the position is within the frustum
     * 
     * @param pos
     *            Position to check
     * @return true if in the frustum, or there is no frustum, false otherwise */
    public static boolean isLocationInFrustum(@Nonnull final Vec3 pos) {
        return isBoundingBoxInFrustum(new AABB(pos, pos));
    }
    
    public static boolean isBoundingBoxInFrustum(@Nonnull final AABB bb) {
        if (helper == null)
            return true;
        return helper.isVisible(bb);
    }
}
