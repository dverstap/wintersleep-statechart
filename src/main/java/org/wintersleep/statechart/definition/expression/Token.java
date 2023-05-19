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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Token {
    int position;
    TokenType type;
    String name;

    public static Token name(int position, String name) {
        return new Token(position, TokenType.NAME, name);
    }

    public static Token nonName(int position, TokenType type) {
        Preconditions.checkArgument(type != TokenType.NAME);
        return new Token(position, type, null);
    }

    public String getName() {
        Preconditions.checkNotNull(name);
        return name;
    }

    public String format() {
        if (name != null) {
            return name;
        }
        return type.toString();
    }

    @Override
    public String toString() {
        return position + ":" + format();
    }

}
