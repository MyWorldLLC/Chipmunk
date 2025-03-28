/*
 * Copyright (C) 2025 MyWorld, LLC
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

import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

import java.math.BigDecimal;
import java.util.Iterator;

@AllowChipmunkLinkage
public class BigDecimalRange implements Range<BigDecimal, BigDecimal> {

    private final BigDecimal start;
    private final BigDecimal end;
    private final BigDecimal step;
    private final boolean inclusive;

    public BigDecimalRange(BigDecimal start, BigDecimal end, BigDecimal step, boolean inclusive){
        this.start = start;
        this.end = end;
        this.step = step;
        this.inclusive = inclusive;
    }

    @Override
    public BigDecimal getStart() {
        return start;
    }

    @Override
    public BigDecimal getEnd() {
        return end;
    }

    @Override
    public BigDecimal getStep() {
        return step;
    }

    @Override
    public boolean isInclusive() {
        return inclusive;
    }

    @AllowChipmunkLinkage
    protected class BigDecimalRangeIterator implements Iterator<BigDecimal> {

        private BigDecimal current;

        public BigDecimalRangeIterator(){
            current = start;
        }

        @Override
        public BigDecimal next() {
            if(!hasNext()){
                throw new IllegalStateException("Iteration past end of range");
            }

            BigDecimal value = current;
            current = current.add(step);

            return value;
        }

        @Override
        public boolean hasNext() {
            if(inclusive){
                return current.compareTo(end) <= 0;
            }else{
                return current.compareTo(end) < 0;
            }
        }
    }
}
