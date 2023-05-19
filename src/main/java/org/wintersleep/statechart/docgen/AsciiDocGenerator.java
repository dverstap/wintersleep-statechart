package org.wintersleep.statechart.docgen;

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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.wintersleep.statechart.definition.Descriptions;
import org.wintersleep.statechart.definition.StateChartDef;
import org.wintersleep.statechart.definition.StateDef;
import org.wintersleep.statechart.definition.TransitionDef;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AsciiDocGenerator {

    private final StateChartDef stateChartDef;
    private final String imageFileName;

    public AsciiDocGenerator(StateChartDef stateChartDef, String imageFileName) {
        this.stateChartDef = stateChartDef;
        this.imageFileName = imageFileName;
    }

    public void generate(PrintWriter w) {
        w.println("= " + stateChartDef.getName() + " state chart");
        w.println();
        stateChart(w);
        w.println();
        events(w);
        w.println();
        conditions(w);
        w.println();
        actions(w);
        w.println();
        states(w);
        w.println();
    }

    private void stateChart(PrintWriter w) {
        if (stateChartDef.getDescription() != null) {
            w.println(stateChartDef.getDescription());
        }
        w.println();
        w.println("image::" + imageFileName + "[]");
        w.println();
    }

    private void events(PrintWriter w) {
        descriptions(w, "Events", stateChartDef.getEventDescriptions());
    }

    private void conditions(PrintWriter w) {
        descriptions(w, "Conditions", stateChartDef.getConditionDescriptions());
    }

    private void actions(PrintWriter w) {
        descriptions(w, "Actions", stateChartDef.getActionDescriptions());
    }

    private void descriptions(PrintWriter w, String title, Descriptions descriptions) {
        w.println("== " + title);
        w.println();

        Map<String, String> events = descriptions.getAllUsed();
        descriptionList(w, events);
        w.println();

        Map<String, String> unused = descriptions.getAllUnused();
        if (!unused.isEmpty()) {
            w.println("WARNING: These " + title.toLowerCase() + " are not actually used:");
            w.println();
            descriptionList(w, unused);
            w.println();
        }
        w.println();
    }

    private static void descriptionList(PrintWriter w, Map<String, String> events) {
        for (var entry : events.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue();
            if (desc != null) {
                w.println(name + ":: " + desc);
            } else {
                w.println(name + "::");
            }
        }
        w.println("{nbsp}");
        w.println();
    }

    private void states(PrintWriter w) {
        w.println("== States");
        w.println();
        for (StateDef stateDef : stateChartDef.getAllStateDefs()) {
            state(w, stateDef);
            w.println();
        }
        w.println();
    }

    private void state(PrintWriter w, StateDef stateDef) {
        w.println("=== " + stateDef.getName());
        w.println();
        if (stateDef.getDescription() != null) {
            w.println(stateDef.getDescription());
            w.println();
        }
        if (stateDef.getChildStateDefs().isEmpty()) {
            w.println("Type: Leaf state");
            w.println();
        } else {
            w.println("Type: Non-leaf state containing:");
            w.println();
            for (StateDef childStateDef : stateDef.getChildStateDefs()) {
                w.println("* " + childStateDef.getName());
            }
            w.println();
        }
        if (!stateDef.getInternalTransitionDefs().isEmpty()) {
            table(w, "Internal transitions", transitionTable(stateDef, true),
                    TransitionCol.Event, TransitionCol.Guard, TransitionCol.Actions);
            w.println();
        }
        if (!stateDef.getOutgoingTransitionDefs().isEmpty()) {
            table(w, "Outgoing transitions", transitionTable(stateDef, false),
                    TransitionCol.Event, TransitionCol.Guard, TransitionCol.Target, TransitionCol.Actions);
            w.println();
        }
        // TODO List incoming transitions
        w.println();
    }

    private Table<Integer, TransitionCol, Object> transitionTable(StateDef stateDef, boolean internal) {
        Table<Integer, TransitionCol, Object> result = HashBasedTable.create();
        List<TransitionDef> transitionDefs;
        if (internal) {
            transitionDefs = stateDef.getInternalTransitionDefs();
        } else {
            transitionDefs = stateDef.getOutgoingTransitionDefs();
        }
        int i = 0;
        for (TransitionDef transitionDef : transitionDefs) {
            result.put(i, TransitionCol.Event, transitionDef.getEvent());
            if (transitionDef.getGuard() != null) {
                result.put(i, TransitionCol.Guard, transitionDef.getGuard());
            }
            if (transitionDef.getTarget() != null) {
                result.put(i, TransitionCol.Target, transitionDef.getTarget());
            }
            if (transitionDef.getAction() != null) {
                result.put(i, TransitionCol.Actions, transitionDef.getAction());
            } else if (!transitionDef.getActions().isEmpty()) {
                result.put(i, TransitionCol.Actions, transitionDef.getActions());
            }
            i++;
        }
        return result;
    }

    private <C extends Enum<C>> void table(PrintWriter w, String title, Table<Integer, C, Object> table, C... cols) {
        w.println("." + title);
        w.println("|===");

        for (C col : cols) {
            w.print("|" + col);
        }
        w.println();
        w.println();
        for (Integer key : new TreeSet<>(table.rowKeySet())) {
            for (C col : cols) {
                Object cell = table.get(key, col);
                if (cell != null) {
                    if (cell instanceof List) {
                        w.println("a|");
                        List<?> entries = (List<?>) cell;
                        for (Object entry : entries) {
                            w.println("* " + entry);
                        }
                    } else {
                        w.println("|" + cell);
                    }
                } else {
                    w.println("|");
                }
            }
            w.println();
        }
        w.println("|===");
    }

    private enum TransitionCol {
        Event,
        Guard,
        Target,
        Actions,
        // TODO? Description,
    }

}
