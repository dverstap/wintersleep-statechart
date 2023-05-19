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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

@ToString
@Getter
public class StateChartDef {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("events")
    private LinkedHashMap<String, String> events = new LinkedHashMap<>();

    @JsonProperty("conditions")
    private LinkedHashMap<String, String> conditions = new LinkedHashMap<>();

    @JsonProperty("actions")
    private LinkedHashMap<String, String> actions = new LinkedHashMap<>();

    @JsonProperty("root state")
    private StateDef rootStateDef;

    public List<StateDef> getAllStateDefs() {
        List<StateDef> result = new ArrayList<>();
        result.add(rootStateDef);
        result.addAll(rootStateDef.getAllStateDefs());
        return result;
    }

    public Descriptions getEventDescriptions() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        rootStateDef.collectEventNames(names);
        return new Descriptions(events, names);
    }

    public Descriptions getConditionDescriptions() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        rootStateDef.collectConditionNames(names);
        return new Descriptions(conditions, names);
    }

    public Descriptions getActionDescriptions() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        rootStateDef.collectActionNames(names);
        return new Descriptions(actions, names);
    }

    @SuppressWarnings("unused")
    protected void setEvents(LinkedHashMap<String, String> events) {
        if (events != null) {
            this.events = events;
        }
    }

    @SuppressWarnings("unused")
    protected void setConditions(LinkedHashMap<String, String> conditions) {
        if (conditions != null) {
            this.conditions = conditions;
        }
    }

    @SuppressWarnings("unused")
    protected void setActions(LinkedHashMap<String, String> actions) {
        if (actions != null) {
            this.actions = actions;
        }
    }

}
