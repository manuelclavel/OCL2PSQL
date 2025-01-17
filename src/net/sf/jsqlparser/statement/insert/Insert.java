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
package net.sf.jsqlparser.statement.insert;

import java.util.ArrayList;
import java.util.List;

import org.vgu.sqlsi.main.InjectorStatement;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

/**
 * The insert statement. Every column name in <code>columnNames</code> matches an item in
 * <code>itemsList</code>
 */
public class Insert implements Statement {

    private Table table;
    private List<Column> columns;
    private ItemsList itemsList;
    private boolean useValues = true;
    private Select select;
    private boolean useSelectBrackets = true;
    private boolean useDuplicate = false;
    private List<Column> duplicateUpdateColumns;
    private List<Expression> duplicateUpdateExpressionList;
    private InsertModifierPriority modifierPriority = null;
    private boolean modifierIgnore = false;

    private boolean returningAllColumns = false;

    private List<SelectExpressionItem> returningExpressionList = null;
    
    /* these lines of codes are used to handle SET syntax in the insert part. 
     * the SET syntax is based on this: https://dev.mysql.com/doc/refman/5.6/en/insert.html. */
    private boolean useSet = false;
    private List<Column> setColumns;
    private List<Expression> setExpressionList;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table name) {
        table = name;
    }

    /**
     * Get the columns (found in "INSERT INTO (col1,col2..) [...]" )
     *
     * @return a list of {@link net.sf.jsqlparser.schema.Column}
     */
    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> list) {
        columns = list;
    }

    /**
     * Get the values (as VALUES (...) or SELECT)
     *
     * @return the values of the insert
     */
    public ItemsList getItemsList() {
        return itemsList;
    }

    public void setItemsList(ItemsList list) {
        itemsList = list;
    }

    public boolean isUseValues() {
        return useValues;
    }

    public void setUseValues(boolean useValues) {
        this.useValues = useValues;
    }

    public boolean isReturningAllColumns() {
        return returningAllColumns;
    }

    public void setReturningAllColumns(boolean returningAllColumns) {
        this.returningAllColumns = returningAllColumns;
    }

    public List<SelectExpressionItem> getReturningExpressionList() {
        return returningExpressionList;
    }

    public void setReturningExpressionList(List<SelectExpressionItem> returningExpressionList) {
        this.returningExpressionList = returningExpressionList;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public boolean isUseSelectBrackets() {
        return useSelectBrackets;
    }

    public void setUseSelectBrackets(boolean useSelectBrackets) {
        this.useSelectBrackets = useSelectBrackets;
    }

    public boolean isUseDuplicate() {
        return useDuplicate;
    }

    public void setUseDuplicate(boolean useDuplicate) {
        this.useDuplicate = useDuplicate;
    }

    public List<Column> getDuplicateUpdateColumns() {
        return duplicateUpdateColumns;
    }

    public void setDuplicateUpdateColumns(List<Column> duplicateUpdateColumns) {
        this.duplicateUpdateColumns = duplicateUpdateColumns;
    }

    public List<Expression> getDuplicateUpdateExpressionList() {
        return duplicateUpdateExpressionList;
    }

    public void setDuplicateUpdateExpressionList(List<Expression> duplicateUpdateExpressionList) {
        this.duplicateUpdateExpressionList = duplicateUpdateExpressionList;
    }

    public InsertModifierPriority getModifierPriority() {
        return modifierPriority;
    }

    public void setModifierPriority(InsertModifierPriority modifierPriority) {
        this.modifierPriority = modifierPriority;
    }

    public boolean isModifierIgnore() {
        return modifierIgnore;
    }

    public void setModifierIgnore(boolean modifierIgnore) {
        this.modifierIgnore = modifierIgnore;
    }
    
    public void setUseSet(boolean useSet) {
        this.useSet = useSet;
    }
    
    public boolean isUseSet() {
        return useSet;
    }
    
    public void setSetColumns(List<Column> setColumns) {
        this.setColumns = setColumns;
    }
    
    public List<Column> getSetColumns() {
        return setColumns;
    }
    
    public void setSetExpressionList(List<Expression> setExpressionList) {
        this.setExpressionList = setExpressionList;
    }
    
    public List<Expression> getSetExpressionList() {
        return setExpressionList;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT ");
        if (modifierPriority != null) {
            sql.append(modifierPriority.name()).append(" ");
        }
        if (modifierIgnore) {
            sql.append("IGNORE ");
        }
        sql.append("INTO ");
        sql.append(table).append(" ");
        if (columns != null) {
            sql.append(PlainSelect.getStringList(columns, true, true)).append(" ");
        }

        if (useValues) {
            sql.append("VALUES ");
        }

        if (itemsList != null) {
            sql.append(itemsList);
        } else {
            if (useSelectBrackets) {
                sql.append("(");
            }
            if (select != null) {
                sql.append(select);
            }
            if (useSelectBrackets) {
                sql.append(")");
            }
        }
        
        if (useSet) {
            sql.append("SET ");
            for (int i = 0; i < getSetColumns().size(); i++) {
                if (i != 0) {
                    sql.append(", ");
                }
                sql.append(setColumns.get(i)).append(" = ");
                sql.append(setExpressionList.get(i));
            }
        }

        if (useDuplicate) {
            sql.append(" ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < getDuplicateUpdateColumns().size(); i++) {
                if (i != 0) {
                    sql.append(", ");
                }
                sql.append(duplicateUpdateColumns.get(i)).append(" = ");
                sql.append(duplicateUpdateExpressionList.get(i));
            }
        }

        if (isReturningAllColumns()) {
            sql.append(" RETURNING *");
        } else if (getReturningExpressionList() != null) {
            sql.append(" RETURNING ").append(PlainSelect.
                    getStringList(getReturningExpressionList(), true, false));
        }

        return sql.toString();
    }

	@Override
	public void accept(InjectorStatement injectorStatement) {
		/*
		List<Column> update_cols =  ((Insert) statement).getColumns();
		List<Expression> update_values =  
				((ExpressionList) ((Insert) statement).getItemsList()).getExpressions();

		List<String> checks = new ArrayList<String>();
		for(int column_index = 0; column_index < update_cols.size(); column_index++) {
			checks.add("auth_update_" + ((Insert) statement).getTable().getName()
					+ "_" + update_cols.get(column_index)
					+ "(" + "-1" + "," + "kcaller" + "," +  "krole"
					+ "," + update_values.get(column_index)+ ")");
		}
		String checksIf = "";
		for(int check_index = 0; check_index < checks.size(); check_index++) {
			checksIf += checks.get(check_index);
			checksIf += " " + "AND" + " ";
		};

		s +=  "IF" + " " 
				+ checksIf
				+ "auth_create_" + ((Insert) statement).getTable().getName()
				+ "(kcaller, krole)\n"
				+ "THEN" + " " 

				+ statement.toString()  + ";" + "\n"
				+ "ELSE" + "\n" 
				+ "ROLLBACK;\n"
				+ "SIGNAL SQLSTATE '45000'" + "\n"
				+ "SET MESSAGE_TEXT = 'Security exception';" + "\n"
				//
				//+ ((Insert) statement).getColumns().toString()
				//+ ":" + ((Insert) statement).getItemsList().toString()
				//
				+ "END IF";
		*/
		
		injectorStatement.setResult(this);	
		
	}
    
}
