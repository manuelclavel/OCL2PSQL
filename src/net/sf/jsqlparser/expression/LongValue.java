/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
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
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.vgu.sqlsi.main.InjectorExpression;

/**
 * Every number without a point or an exponential format is a LongValue.
 */
public class LongValue extends ASTNodeAccessImpl implements Expression {

    private String stringValue;

    public LongValue(final String value) {
        String val = value;
        if (val.charAt(0) == '+') {
            val = val.substring(1);
        }
        this.stringValue = val;
    }

    public LongValue(long value) {
        stringValue = String.valueOf(value);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public long getValue() {
        return Long.valueOf(stringValue);
    }

    public BigInteger getBigIntegerValue() {
        return new BigInteger(stringValue);
    }

    public void setValue(long d) {
        stringValue = String.valueOf(d);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String string) {
        stringValue = string;
    }

    @Override
    public String toString() {
        return getStringValue();
    }

	@Override
	public void accept(InjectorExpression injectorExpression) {
		// TODO Auto-generated method stub
		List<Expression> list = new ArrayList<Expression>();
		//list.add(this);
		injectorExpression.setResult(list);
	}
}
