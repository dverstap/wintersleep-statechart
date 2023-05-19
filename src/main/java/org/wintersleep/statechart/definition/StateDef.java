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
import lombok.Getter;
import lombok.ToString;
import org.wintersleep.statechart.definition.guard.GuardExpression;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
@Getter
public class StateDef {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("states")
    private List<StateDef> childStateDefs = new ArrayList<>();

    @JsonProperty("transitions")
    private List<TransitionDef> transitionDefs = new ArrayList<>();

    public List<StateDef> getAllStateDefs() {
        List<StateDef> result = new ArrayList<>();
        for (StateDef childStateDef : childStateDefs) {
            result.add(childStateDef);
            result.addAll(childStateDef.getAllStateDefs());
        }
        return result;
    }

    public List<TransitionDef> getInternalTransitionDefs() {
        return transitionDefs
                .stream()
                .filter(t -> t.getTarget() == null)
                .collect(Collectors.toList());
    }

    public List<TransitionDef> getOutgoingTransitionDefs() {
        return transitionDefs
                .stream()
                .filter(t -> t.getTarget() != null)
                .collect(Collectors.toList());
    }

    public void collectEventNames(Set<String> names) {
        for (TransitionDef transitionDef : getInternalTransitionDefs()) {
            names.add(transitionDef.getEvent());
        }
        for (TransitionDef transitionDef : getOutgoingTransitionDefs()) {
            names.add(transitionDef.getEvent());
        }
        for (StateDef childStateDef : childStateDefs) {
            childStateDef.collectEventNames(names);
        }
    }

    public void collectConditionNames(Set<String> names) {
        Set<GuardExpression> expressions = new LinkedHashSet<>();
        collectGuardExpressions(expressions);
        expressions
                .stream()
                .flatMap(expr -> expr.getAllConditionNames().stream())
                .forEach(names::add);
    }

    public void collectGuardExpressions(Set<GuardExpression> guards) {
        collectGuardExpressions(getInternalTransitionDefs(), guards);
        collectGuardExpressions(getOutgoingTransitionDefs(), guards);
        childStateDefs.forEach(cs -> cs.collectGuardExpressions(guards));
    }

    private static void collectGuardExpressions(List<TransitionDef> transitionDefs, Set<GuardExpression> guards) {
        transitionDefs
                .stream()
                .map(TransitionDef::parseGuard)
                .filter(Objects::nonNull)
                .forEach(guards::add);
    }

    public Stream<EventGuardExpression> eventGuardExpressions() {
        return Stream.concat(
                transitionDefs.stream()
                        .map(TransitionDef::getEventGuard)
                        .filter(Objects::nonNull),
                childStateDefs.stream()
                        .flatMap(StateDef::eventGuardExpressions)
        );
    }

    public Stream<EventCondition> eventConditions() {
        return Stream.concat(
                transitionDefs.stream()
                        .flatMap(TransitionDef::eventConditions)
                        .filter(Objects::nonNull),
                childStateDefs.stream()
                        .flatMap(StateDef::eventConditions)
        );
    }

    public void collectActionNames(Set<String> names) {
        for (TransitionDef transitionDef : getInternalTransitionDefs()) {
            names.addAll(transitionDef.getActions());
        }
        for (TransitionDef transitionDef : getOutgoingTransitionDefs()) {
            names.addAll(transitionDef.getActions());
        }
        for (StateDef childStateDef : childStateDefs) {
            childStateDef.collectActionNames(names);
        }
    }

}
