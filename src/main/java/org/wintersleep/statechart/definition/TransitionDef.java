package org.wintersleep.statechart.definition;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.wintersleep.statechart.definition.expression.ExpressionParser;
import org.wintersleep.statechart.definition.guard.GuardExpression;

import java.util.List;
import java.util.stream.Stream;

@ToString
@Getter
public class TransitionDef {

    @JsonProperty("description")
    private String description;

    @JsonProperty("event")
    private String event;

    @JsonProperty("guard")
    private String guard;

    @JsonProperty("action")
    private String action;

    @JsonProperty("actions")
    private List<String> actions;

    @JsonProperty("target")
    private String target;

    public List<String> getActions() {
        Preconditions.checkArgument(!(action != null && actions != null));
        if (action != null) {
            return List.of(action);
        }
        if (actions != null) {
            return actions;
        }
        return List.of();
    }

    public GuardExpression parseGuard() {
        if (guard == null) {
            return null;
        }
        return new ExpressionParser(guard).parse();
    }

    public EventGuardExpression getEventGuard() {
        if (guard == null) {
            return null;
        }
        return new EventGuardExpression(event, parseGuard());
    }

    public Stream<EventCondition> eventConditions() {
        if (guard == null) {
            return Stream.of();
        }
        GuardExpression guard = parseGuard();
        return guard.getAllConditionNames()
                .stream()
                .map(name -> new EventCondition(event, name));
    }

}
