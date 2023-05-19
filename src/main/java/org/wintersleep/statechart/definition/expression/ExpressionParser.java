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


import org.wintersleep.statechart.definition.guard.*;

import java.util.ArrayList;
import java.util.List;

import static org.wintersleep.statechart.definition.expression.TokenType.AND;
import static org.wintersleep.statechart.definition.expression.TokenType.OR;

// exp    -> term {OR term};
// term   -> factor {AND factor};
// factor -> id;
// factor -> NOT factor;
// factor -> LPAREN exp RPAREN;
public class ExpressionParser implements GuardExpressionParser {

    private final TokenStream tokenStream;
    TokenType currentType;

    public ExpressionParser(String input) {
        this(new TokenStream(input));
    }

    public ExpressionParser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
        advance();
    }

    GuardExpression die(String msg) {
        throw new AssertionError(msg + "\nCurrent:" + tokenStream.current() + "\n" + tokenStream.all());
    }

    void advance() {
        currentType = tokenStream.next();
    }

    void match(TokenType tokenType) {
        if (currentType != tokenType) {
            die("Expected " + tokenType + ", found " + currentType);
        }
        if (currentType != TokenType.END) {
            advance();
        }
    }

    public GuardExpression parse() {
        GuardExpression exprVal = expr();
        match(TokenType.END);
        return exprVal;
    }

    GuardExpression expr() {
        List<GuardExpression> expressions = new ArrayList<>();
        GuardExpression left = term();
        expressions.add(left);
        while (currentType == OR) {
            advance();
            GuardExpression right = term();
            expressions.add(right);
        }
        if (expressions.size() == 1) {
            return left;
        }
        return new CompositeGuardExpression(CompositeGuardExpression.Type.OR, expressions);
    }

    GuardExpression term() {
        List<GuardExpression> expressions = new ArrayList<>();
        GuardExpression left = factor();
        expressions.add(left);
        while (currentType == AND) {
            advance();
            GuardExpression right = factor();
            expressions.add(right);
        }
        if (expressions.size() == 1) {
            return left;
        }
        return new CompositeGuardExpression(CompositeGuardExpression.Type.AND, expressions);
    }

    GuardExpression factor() {
        switch (currentType) {
            case NAME:
                ConditionGuardExpression result = new ConditionGuardExpression(tokenStream.current().getName());
                advance();
                return result;
            case NOT:
                advance();
                return new NotGuardExpression(expr());
            case LP:
                advance();
                GuardExpression exprVal = expr();
                match(TokenType.RP);
                return new NestedGuardExpression(exprVal);
            default:
                return die("Expected variable, not or (");
        }
    }

}

