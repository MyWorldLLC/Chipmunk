/*
 * Copyright (C) 2026 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.runtime;

public class CRange {

    protected final double start;
    protected final double end;
    protected final double step;
    protected final boolean inclusive;

    public CRange(double start, double end, double step, boolean inclusive) {
        this.start = start;
        this.end = end;
        this.step = step;
        this.inclusive = inclusive;
    }

    public double start() {
        return start;
    }

    public double end() {
        return end;
    }

    public double step() {
        return step;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public RangeIterator iterator() {
        return new RangeIterator();
    }

    public class RangeIterator {
        double current = start;

        public double hasNext(){
            return (current < end || (inclusive && current == end)) ? 1 : 0;
        }

        public double next(){
            var v = current;
            current += step;
            return v;
        }
    }
}
