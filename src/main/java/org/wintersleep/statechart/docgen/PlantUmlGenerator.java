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

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.wintersleep.statechart.definition.StateChartDef;
import org.wintersleep.statechart.definition.StateDef;
import org.wintersleep.statechart.definition.TransitionDef;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlantUmlGenerator {

    private final StateChartDef stateChartDef;

    public PlantUmlGenerator(StateChartDef stateChartDef) {
        this.stateChartDef = stateChartDef;
    }

    public void generateSpec(PrintWriter w) {
        w.println("@startuml");
        w.println("title " + stateChartDef.getName());
        write(w, "", stateChartDef.getRootStateDef());
        w.print("@enduml"); // TODO println
    }

    private void write(PrintWriter w, String indent, StateDef stateDef) {
        String name = stateDef.getName();
        w.format("%sstate \"%s\" as %s {\n", indent, name, name);
        for (TransitionDef transitionDef : stateDef.getInternalTransitionDefs()) {
            write(w, indent + "  ", stateDef, transitionDef);
        }
        for (TransitionDef transitionDef : stateDef.getOutgoingTransitionDefs()) {
            write(w, indent + "  ", stateDef, transitionDef);
        }
        // sismic appears to do this, so we do this too, to make it easy to compare the output.
        // But it definitely gives a better result: otherwise the plot is reversed.
        List<StateDef> children = new ArrayList<>(stateDef.getChildStateDefs());
        Collections.reverse(children);
        for (StateDef childStateDef : children) {
            write(w, indent + "  ", childStateDef);
        }
        w.format("%s}\n", indent);
    }

    private void write(PrintWriter w, String indent, StateDef sourceStateDef, TransitionDef transitionDef) {
        String source = sourceStateDef.getName();
        String target = transitionDef.getTarget();
        w.print(indent);
        w.print(source);
        if (target != null) {
            w.print(" --> " + target);
            w.print(" : " + transitionDef.getEvent());
        } else {
            w.print(" : **" + transitionDef.getEvent() + "**");
        }
        String guard = transitionDef.getGuard();
        if (guard != null) {
            w.print(" [" + guard + "]");
        }
        String singleAction = transitionDef.getAction();
        if (singleAction != null) {
            w.print(" / " + singleAction);
        } else {
            List<String> actions = transitionDef.getActions();
            if (!actions.isEmpty()) {
                w.print(" / " + actions.stream().map(a -> "\\n " + a).collect(Collectors.joining("; ")));
            }
        }
        w.println();
    }


    public void generateImage(Path plantUmlFile, OutputStream os, FileFormat fileFormat) throws IOException {
        // PlantUML has a limited API when dealing with files, so we re-read the PlantUML spec in RAM:
        // https://plantuml.com/api
        String plantUmlSpec = Files.readString(plantUmlFile);
        SourceStringReader reader = new SourceStringReader(plantUmlSpec);
        DiagramDescription desc = reader.outputImage(os, new FileFormatOption(fileFormat));
        System.out.println(desc);
    }

}
