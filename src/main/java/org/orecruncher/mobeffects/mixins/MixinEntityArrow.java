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
package org.orecruncher.mobeffects.mixins;

import org.orecruncher.mobeffects.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityType;

@Mixin(AbstractArrowEntity.class)
public abstract class MixinEntityArrow extends Entity {
    
    public MixinEntityArrow(EntityType<? extends AbstractArrowEntity> type, World worldIn) {
        super(type, worldIn);
    }
    
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/AbstractArrowEntity;getIsCritical()Z"))
    private boolean isCriticalCheck(AbstractArrowEntity self) {
        return self.getIsCritical() && Config.CLIENT.effects.showArrowTrail.get();
    }
}