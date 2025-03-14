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

package org.orecruncher.lib.math;

import javax.annotation.Nonnull;

/** Specialization of the EMA based on measuring time. The time unit it expects
 * to deal with is nanoseconds. */
public class TimerEMA extends EMA {
    
    public TimerEMA(@Nonnull final String name) {
        super(name);
    }
    
    public TimerEMA(@Nonnull final String name, final int periods) {
        super(name, periods);
    }
    
    public double getMSecs() {
        return super.get() / 1000000;
    }
    
    @Override
    public String toString() {
        return String.format("%s:%7.3fms", name(), getMSecs());
    }
    
}
