/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.util.cnfexpression;

import java.util.List;

import org.vgu.sqlsi.main.InjectorExpression;

import net.sf.jsqlparser.expression.Expression;

/**
 * This helper class is mainly used for handling the CNF conversion.
 * @author messfish
 *
 */
public final class MultiAndExpression extends MultipleExpression {

    public MultiAndExpression(List<Expression> childlist) {
        super(childlist);
    }

    @Override
    public String getStringExpression() {
        return "AND";
    }

	@Override
	public void accept(InjectorExpression injectorExpression) {
		// TODO Auto-generated method stub
		
	}

}