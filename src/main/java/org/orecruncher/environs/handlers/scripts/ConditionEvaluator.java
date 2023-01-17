/*
 *  Dynamic Surroundings: Environs
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

package org.orecruncher.environs.handlers.scripts;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.orecruncher.lib.scripting.ExecutionContext;
import org.orecruncher.lib.scripting.sets.BiomeVariables;
import org.orecruncher.lib.scripting.sets.DimensionVariables;
import org.orecruncher.lib.scripting.sets.DiurnalCycleVariables;
import org.orecruncher.lib.scripting.sets.PlayerVariables;
import org.orecruncher.lib.scripting.sets.SeasonVariables;
import org.orecruncher.lib.scripting.sets.WeatherVariables;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ConditionEvaluator {
    
    public static final ConditionEvaluator INSTANCE = new ConditionEvaluator();
    
    private final ExecutionContext context = new ExecutionContext("Conditions");
    
    private ConditionEvaluator() {
        this.context.add(new BiomeVariables());
        this.context.add(new DimensionVariables());
        this.context.add(new DiurnalCycleVariables());
        this.context.add(new PlayerVariables());
        this.context.add(new WeatherVariables());
        this.context.add(new StateVariables());
        this.context.add(new SeasonVariables());
    }
    
    public void tick() {
        this.context.update();
    }
    
    public boolean check(@Nonnull final String conditions) {
        final Object result = eval(conditions);
        return result instanceof Boolean && (boolean) result;
    }
    
    public Object eval(@Nonnull final String conditions) {
        if (StringUtils.isNullOrEmpty(conditions))
            return true;
        final Optional<Object> result = this.context.eval(conditions);
        return result.orElse(false);
    }
}
