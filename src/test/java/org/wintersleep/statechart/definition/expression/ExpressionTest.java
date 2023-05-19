package org.wintersleep.statechart.definition.expression;

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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.wintersleep.statechart.definition.expression.ExpressionParser;
import org.wintersleep.statechart.definition.guard.CompositeGuardExpression;
import org.wintersleep.statechart.definition.guard.ConditionGuardExpression;
import org.wintersleep.statechart.definition.guard.GuardExpression;
import org.wintersleep.statechart.definition.guard.NotGuardExpression;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ExpressionTest {


    @Test
    void testSimple() {
        ExpressionParser parser = new ExpressionParser("x");
        GuardExpression def = parser.parse();
        assertEquals("x", def.toString());
        assertThat(def).isInstanceOf(ConditionGuardExpression.class);
        ConditionGuardExpression conditionGuardDef = (ConditionGuardExpression) def;
        assertThat(conditionGuardDef.getName()).isEqualTo("x");
    }

    @Test
    void testNot() {
        ExpressionParser parser = new ExpressionParser("not x");
        GuardExpression def = parser.parse();
        assertEquals("not x", def.toString());
        assertThat(def).isInstanceOf(NotGuardExpression.class);
    }

    @Test
    void testAnd() {
        ExpressionParser parser = new ExpressionParser("x and y");
        GuardExpression def = parser.parse();
        assertThat(def.toString()).isEqualTo("x and y");
        assertThat(def).isInstanceOf(CompositeGuardExpression.class);
        CompositeGuardExpression and = (CompositeGuardExpression) def;
        assertThat(and.getType()).isEqualTo(CompositeGuardExpression.Type.AND);
    }

    @Test
    void testOr() {
        ExpressionParser parser = new ExpressionParser("x or y");
        GuardExpression def = parser.parse();
        assertThat(def.toString()).isEqualTo("x or y");
        assertThat(def).isInstanceOf(CompositeGuardExpression.class);
        CompositeGuardExpression or = (CompositeGuardExpression) def;
        assertThat(or.getType()).isEqualTo(CompositeGuardExpression.Type.OR);
    }

    @TestFactory
    Stream<DynamicTest> testVariableNamesWithAO() {
        return Stream.of(
                        "a",
                        "(not a)",
                        "o",
                        "(not o)"
                )
                .map(formatTest());
    }

    @TestFactory
    Stream<DynamicTest> combinations() {
        return Stream.of(
                        "(not x)",
                        "not x and y",
                        "(not x) and y",
                        "not (x and y)",
                        "x or not y",
                        "x or not (y and z)",
                        "x or (not y and z)",
                        "x and y and z",
                        "x or y or z"
                )
                .map(formatTest());
    }

    private static Function<String, DynamicTest> formatTest() {
        return str -> dynamicTest(str, () -> {
            ExpressionParser parser = new ExpressionParser(str);
            GuardExpression def = parser.parse();
            assertThat(def.toString()).isEqualTo(str);
        });
    }
}
