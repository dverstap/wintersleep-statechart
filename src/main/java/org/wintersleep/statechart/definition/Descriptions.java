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

import lombok.Value;

import java.util.*;

@Value
public class Descriptions {

    Map<String, String> declaredDescriptions;
    Set<String> usedNames;

    // We require LinkedHashMap and LinkedHashSet, because we want to keep the order they appear in the spec
    public Descriptions(LinkedHashMap<String, String> declaredDescriptions, LinkedHashSet<String> usedNames) {
        this.declaredDescriptions = Collections.unmodifiableMap(declaredDescriptions);
        this.usedNames = Collections.unmodifiableSet(usedNames);
    }

    public Map<String, String> getDeclaredDescriptions() {
        return declaredDescriptions;
    }

    public Set<String> getUsedNames() {
        return usedNames;
    }

    public Map<String, String> getAll() {
        Map<String, String> result = new LinkedHashMap<>(declaredDescriptions);
        for (String name : usedNames) {
            if (!result.containsKey(name)) {
                result.put(name, null);
            }
        }
        return result;
    }

    // Returns the declared event/condition/action descriptions that are actually used,
    // plus the actually used event/condition/action names that have no description.
    public Map<String, String> getAllUsed() {
        Map<String, String> result = new LinkedHashMap<>();
        for (var entry : declaredDescriptions.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue();
            if (usedNames.contains(name)) {
                result.put(name, desc);
            }
        }
        for (String name : usedNames) {
            if (!result.containsKey(name)) {
                result.put(name, null);
            }
        }
        return result;
    }

    // Returns the declared event/condition/action descriptions that are not used.
    public Map<String, String> getAllUnused() {
        Map<String, String> result = new LinkedHashMap<>();
        for (var entry : declaredDescriptions.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue();
            if (!usedNames.contains(name)) {
                result.put(name, desc);
            }
        }
        return result;
    }

}
