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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.sourceforge.plantuml.FileFormat;
import org.apache.commons.io.FilenameUtils;
import org.wintersleep.statechart.definition.Definition;
import org.wintersleep.statechart.definition.StateChartDef;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

// TODO README
// TODO examples

public class Main {
    public static void main(String[] args) throws IOException {
        new Main(Boolean.parseBoolean(args[0]), Path.of(args[1]), Path.of(args[2])).run();
    }

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
    private final boolean force;
    private final Path inputDir;
    private final Path outputDir;

    public Main(boolean force, Path inputDir, Path outputDir) {
        this.force = force;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
    }

    private void run() throws IOException {
        System.out.println(inputDir);
        System.out.println(outputDir);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.sc.yml");
        try (Stream<Path> stream = Files.list(inputDir)) {
            stream
                    //.peek(System.err::println)
                    .filter(matcher::matches)
                    .forEach(inputFile -> {
                        try {
                            processFile(inputFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

    }

    private void processFile(Path inputFile) throws IOException {
        System.out.println("Processing " + inputFile);
        String baseName = FilenameUtils.getBaseName(inputFile.getFileName().toString());

        Definition definition = mapper.readValue(inputFile.toFile(), Definition.class);
        StateChartDef stateChartDef = definition.getStateChartDef();
        PlantUmlGenerator plantUmlGenerator = new PlantUmlGenerator(stateChartDef);

        Path plantUmlFile = outputDir.resolve(baseName + ".plantuml");
        if (isGenerationNeeded(inputFile, plantUmlFile)) {
            try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(plantUmlFile))) {
                plantUmlGenerator.generateSpec(w);
            }
        }

        Path imageFile = outputDir.resolve(baseName + ".svg");
        if (isGenerationNeeded(plantUmlFile, imageFile)) {
            try (OutputStream os = Files.newOutputStream(imageFile)) {
                plantUmlGenerator.generateImage(plantUmlFile, os, FileFormat.SVG);
            }
        }

        Path asciiDocFile = outputDir.resolve(baseName + ".adoc");
        if (isGenerationNeeded(inputFile, asciiDocFile)) {
            try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(asciiDocFile))) {
                new AsciiDocGenerator(stateChartDef, imageFile.getFileName().toString()).generate(w);
            }
        }
    }

    private boolean isGenerationNeeded(Path src, Path dst) throws IOException {
        if (force) {
            System.out.println("Generating (forced): " + dst);
            return true;
        }
        if (isUpToDate(src, dst)) {
            System.out.println("Not generating (up-to-date): " + dst);
            return false;
        } else {
            System.out.println("Generating (out-of-date): " + dst);
            return true;
        }
    }

    private static boolean isUpToDate(Path src, Path dst) throws IOException {
        if (!Files.exists(dst)) {
            return false;
        }
        return Files.getLastModifiedTime(src).compareTo(Files.getLastModifiedTime(dst)) < 0;
    }
}
