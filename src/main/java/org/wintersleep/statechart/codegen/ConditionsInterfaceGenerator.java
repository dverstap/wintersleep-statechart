package org.wintersleep.statechart.codegen;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.squareup.javapoet.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.wintersleep.statechart.definition.Definition;
import org.wintersleep.statechart.definition.EventCondition;
import org.wintersleep.statechart.definition.StateChartDef;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class ConditionsInterfaceGenerator {

    private final SortedSet<EventCondition> eventConditions;

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Definition definition = mapper.readValue(new File("test.sc.yml"), Definition.class);
        StateChartDef stateChartDef = definition.getStateChartDef();

        SortedSet<EventCondition> eventConditions = stateChartDef.getRootStateDef().eventConditions().collect(Collectors.toCollection(TreeSet::new));
        for (EventCondition eventCondition : eventConditions) {
            System.out.println(eventCondition);
        }
        Map<String, List<EventCondition>> map = eventConditions.stream()
                .collect(Collectors.groupingBy(EventCondition::getConditionName));
        for (Map.Entry<String, List<EventCondition>> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            for (EventCondition eventCondition : entry.getValue()) {
                System.out.println("  " + eventCondition.getEvent());
            }
        }
        new ConditionsInterfaceGenerator(eventConditions).generate();
    }

    @SneakyThrows
    public void generate() {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder("Conditions")
                .addModifiers(Modifier.PUBLIC);
/*
                .addField(FieldSpec.builder(String.class, "ONLY_THING_THAT_IS_CONSTANT")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", "change")
                        .build())
                .addMethod(MethodSpec.methodBuilder("beep")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
*/
        Map<String, List<EventCondition>> map = eventConditions.stream()
                .collect(Collectors.groupingBy(EventCondition::getConditionName));
        for (Map.Entry<String, List<EventCondition>> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            for (EventCondition guard : entry.getValue()) {
                //System.out.println("  " + guard.getEvent());
                builder.addMethod(MethodSpec.methodBuilder(guard.getConditionName())
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(TypeName.BOOLEAN)
                        .addParameter(ClassName.get("test", StringUtils.capitalize(guard.getEvent()) + "Event"),
                                "event")
                        .build());
            }
        }


        JavaFile javaFile = JavaFile.builder("com.example.helloworld", builder.build())
                .build();
        javaFile.writeTo(Paths.get("generated"));
    }

}
