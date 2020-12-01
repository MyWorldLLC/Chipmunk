/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.vm.invoke.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface PolicyEntry {

    default AccessEvaluation allowInstantiation(Class<?> targetClass, Object[] params){
        return AccessEvaluation.UNSPECIFIED;
    }

    default AccessEvaluation allowMethodCall(Object target, Method method, Object[] params) {
        return AccessEvaluation.UNSPECIFIED;
    }

    default AccessEvaluation allowFieldSet(Object target, Field field, Object value) {
        return AccessEvaluation.UNSPECIFIED;
    }

    default AccessEvaluation allowFieldGet(Object target, Field field) {
        return AccessEvaluation.UNSPECIFIED;
    }

}
