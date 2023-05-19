package org.wintersleep.statechart.definition.guard;

/*-
 * #%L
 * wintersleep-statechart
 * %%
 * Copyright (C) 2023 Davy Verstappen
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Value
public class CompositeGuardExpression implements GuardExpression {

    public enum Type {
        AND,
        OR,
    }

    Type type;
    List<GuardExpression> guardExpressions;

    @Override
    public Set<String> getAllConditionNames() {
        Set<String> result = new LinkedHashSet<>();
        for (GuardExpression guardExpression : guardExpressions) {
            result.addAll(guardExpression.getAllConditionNames());
        }
        return result;
    }

    @Override
    public String toString() {
        Preconditions.checkArgument(!guardExpressions.isEmpty());
        return guardExpressions
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(" " + type.name().toLowerCase() + " "));
    }
}
