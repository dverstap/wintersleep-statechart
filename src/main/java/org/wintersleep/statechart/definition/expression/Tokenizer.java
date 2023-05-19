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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.isWhitespace;


public class Tokenizer {
    final String input;
    int pos = 0;
    String var;

    public Tokenizer(String input) {
        this.input = input;
    }

    public List<Token> readAll() {
        List<Token> result = new ArrayList<>();
        Token token;
        do {
            token = getNext();
            result.add(token);
        } while (token.getType() != TokenType.END);
        return result;
    }

    private Token getNext() {
        var = null;
        while (pos < input.length() && isWhitespace(input.charAt(pos))) {
            ++pos;
        }
        if (pos >= input.length()) {
            return Token.nonName(pos, TokenType.END);
        }
        int start = pos++;
        switch (input.charAt(start)) {
            case '(':
                return Token.nonName(pos, TokenType.LP);
            case ')':
                return Token.nonName(pos, TokenType.RP);
            case 'n':
                if (input.startsWith("ot", pos)) {
                    pos += 2;
                    return Token.nonName(pos, TokenType.NOT);
                }
                break;
            case 'a':
                if (input.startsWith("nd", pos)) {
                    pos += 2;
                    return Token.nonName(pos, TokenType.AND);
                }
                //break; // fall-through for named conditions starting with 'a'
            case 'o':
                if (input.startsWith("r", pos)) {
                    pos += 1;
                    return Token.nonName(pos, TokenType.OR);
                }
                //break; // fall-through for named conditions starting with 'o'
            default:
                while (pos < input.length() && isLetterOrDigit(input.charAt(pos))) {
                    ++pos;
                }
                var = input.substring(start, pos);
                return Token.name(pos, var);
        }
        throw new AssertionError("Can't tokenize: " + input.substring(start));
    }
}
