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
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.vgu.sqlsi.main.Injector;
import org.vgu.sqlsi.main.InjectorExpression;
import org.vgu.sqlsi.main.InjectorFromItem;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;

/**
 * The core of a "SELECT" statement (no UNION, no ORDER BY)
 */
public class PlainSelect extends ASTNodeAccessImpl implements SelectBody {

    private Distinct distinct = null;
    private List<SelectItem> selectItems;
    private List<Table> intoTables;
    private FromItem fromItem;
    private List<Join> joins;
    private Expression where;
    private List<Expression> groupByColumnReferences;
    private List<OrderByElement> orderByElements;
    private Expression having;
    private Limit limit;
    private Offset offset;
    private Fetch fetch;
    private Skip skip;
    private First first;
    private Top top;
    private OracleHierarchicalExpression oracleHierarchical = null;
    private OracleHint oracleHint = null;
    private boolean oracleSiblings = false;
    private boolean forUpdate = false;
    private Table forUpdateTable = null;
    private boolean useBrackets = false;
    private Wait wait;
    private boolean mySqlSqlCalcFoundRows = false;

    public boolean isUseBrackets() {
        return useBrackets;
    }

    public void setUseBrackets(boolean useBrackets) {
        this.useBrackets = useBrackets;
    }

    /**
     * The {@link FromItem} in this query
     *
     * @return the {@link FromItem}
     */
    public FromItem getFromItem() {
        return fromItem;
    }

    public List<Table> getIntoTables() {
        return intoTables;
    }

    /**
     * The {@link SelectItem}s in this query (for example the A,B,C in "SELECT A,B,C")
     *
     * @return a list of {@link SelectItem}s
     */
    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public Expression getWhere() {
        return where;
    }

    public void setFromItem(FromItem item) {
        fromItem = item;
    }

    public void setIntoTables(List<Table> intoTables) {
        this.intoTables = intoTables;
    }

    public void setSelectItems(List<SelectItem> list) {
        selectItems = list;
    }

    public void addSelectItems(SelectItem... items) {
        if (selectItems == null) {
            selectItems = new LinkedList<SelectItem>();
        }
        Collections.addAll(selectItems, items);
    }
    
    public void addSelectItem(SelectItem item) {
        if (selectItems == null) {
            selectItems = new LinkedList<SelectItem>();
        }
        selectItems.add(item);
    }
    
    public void setWhere(Expression where) {
        this.where = where;
    }

    /**
     * The list of {@link Join}s
     *
     * @return the list of {@link Join}s
     */
    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> list) {
        joins = list;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    public Top getTop() {
        return top;
    }

    public void setTop(Top top) {
        this.top = top;
    }

    public Skip getSkip() {
        return skip;
    }

    public void setSkip(Skip skip) {
        this.skip = skip;
    }

    public First getFirst() {
        return first;
    }

    public void setFirst(First first) {
        this.first = first;
    }

    public Distinct getDistinct() {
        return distinct;
    }

    public void setDistinct(Distinct distinct) {
        this.distinct = distinct;
    }

    public Expression getHaving() {
        return having;
    }

    public void setHaving(Expression expression) {
        having = expression;
    }

    /**
     * A list of {@link Expression}s of the GROUP BY clause. It is null in case there is no GROUP BY
     * clause
     *
     * @return a list of {@link Expression}s
     */
    public List<Expression> getGroupByColumnReferences() {
        return groupByColumnReferences;
    }

    public void setGroupByColumnReferences(List<Expression> list) {
        groupByColumnReferences = list;
    }

    public void addGroupByColumnReference(Expression expr) {
        if (groupByColumnReferences == null) {
            groupByColumnReferences = new ArrayList<Expression>();
        }
        groupByColumnReferences.add(expr);
    }

    public OracleHierarchicalExpression getOracleHierarchical() {
        return oracleHierarchical;
    }

    public void setOracleHierarchical(OracleHierarchicalExpression oracleHierarchical) {
        this.oracleHierarchical = oracleHierarchical;
    }

    public boolean isOracleSiblings() {
        return oracleSiblings;
    }

    public void setOracleSiblings(boolean oracleSiblings) {
        this.oracleSiblings = oracleSiblings;
    }

    public boolean isForUpdate() {
        return forUpdate;
    }

    public void setForUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
    }

    public Table getForUpdateTable() {
        return forUpdateTable;
    }

    public void setForUpdateTable(Table forUpdateTable) {
        this.forUpdateTable = forUpdateTable;
    }

    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
    }

    /**
     * Sets the {@link Wait} for this SELECT
     *
     * @param wait the {@link Wait} for this SELECT
     */
    public void setWait(final Wait wait) {
        this.wait = wait;
    }

    /**
     * Returns the value of the {@link Wait} set for this SELECT
     *
     * @return the value of the {@link Wait} set for this SELECT
     */
    public Wait getWait() {
        return wait;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        if (useBrackets) {
            sql.append("(");
        }
        sql.append("SELECT ");

        if (oracleHint != null) {
            sql.append(oracleHint).append(" ");
        }

        if (skip != null) {
            sql.append(skip).append(" ");
        }

        if (first != null) {
            sql.append(first).append(" ");
        }

        if (distinct != null) {
            sql.append(distinct).append(" ");
        }
        if (top != null) {
            sql.append(top).append(" ");
        }
        if (mySqlSqlCalcFoundRows) {
            sql.append("SQL_CALC_FOUND_ROWS").append(" ");
        }
        sql.append(getStringList(selectItems));

        if (intoTables != null) {
            sql.append(" INTO ");
            for (Iterator<Table> iter = intoTables.iterator(); iter.hasNext();) {
                sql.append(iter.next().toString());
                if (iter.hasNext()) {
                    sql.append(", ");
                }
            }
        }

        if (fromItem != null) {
            sql.append(" FROM ").append(fromItem);
            if (joins != null) {
                Iterator<Join> it = joins.iterator();
                while (it.hasNext()) {
                    Join join = it.next();
                    if (join.isSimple()) {
                        sql.append(", ").append(join);
                    } else {
                        sql.append(" ").append(join);
                    }
                }
            }
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
            if (oracleHierarchical != null) {
                sql.append(oracleHierarchical.toString());
            }
            sql.append(getFormatedList(groupByColumnReferences, "GROUP BY"));
            if (having != null) {
                sql.append(" HAVING ").append(having);
            }
            sql.append(orderByToString(oracleSiblings, orderByElements));
            if (limit != null) {
                sql.append(limit);
            }
            if (offset != null) {
                sql.append(offset);
            }
            if (fetch != null) {
                sql.append(fetch);
            }
            if (isForUpdate()) {
                sql.append(" FOR UPDATE");

                if (forUpdateTable != null) {
                    sql.append(" OF ").append(forUpdateTable);
                }

                if (wait != null) {
                    // Wait's toString will do the formatting for us
                    sql.append(wait);
                }
            }
        } else {
            //without from
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
        }
        if (useBrackets) {
            sql.append(")");
        }
        return sql.toString();
    }

    public static String orderByToString(List<OrderByElement> orderByElements) {
        return orderByToString(false, orderByElements);
    }

    public static String orderByToString(boolean oracleSiblings, List<OrderByElement> orderByElements) {
        return getFormatedList(orderByElements, oracleSiblings ? "ORDER SIBLINGS BY" : "ORDER BY");
    }

    public static String getFormatedList(List<?> list, String expression) {
        return getFormatedList(list, expression, true, false);
    }

    public static String getFormatedList(List<?> list, String expression, boolean useComma, boolean useBrackets) {
        String sql = getStringList(list, useComma, useBrackets);

        if (sql.length() > 0) {
            if (expression.length() > 0) {
                sql = " " + expression + " " + sql;
            } else {
                sql = " " + sql;
            }
        }

        return sql;
    }

    /**
     * List the toString out put of the objects in the List comma separated. If the List is null or
     * empty an empty string is returned.
     *
     * The same as getStringList(list, true, false)
     *
     * @see #getStringList(List, boolean, boolean)
     * @param list list of objects with toString methods
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list) {
        return getStringList(list, true, false);
    }

    /**
     * List the toString out put of the objects in the List that can be comma separated. If the List
     * is null or empty an empty string is returned.
     *
     * @param list list of objects with toString methods
     * @param useComma true if the list has to be comma separated
     * @param useBrackets true if the list has to be enclosed in brackets
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list, boolean useComma, boolean useBrackets) {
        StringBuilder ans = new StringBuilder();
//        String ans = "";
        String comma = ",";
        if (!useComma) {
            comma = "";
        }
        if (list != null) {
            if (useBrackets) {
                ans.append("(");
//                ans += "(";
            }

            for (int i = 0; i < list.size(); i++) {
                ans.append(list.get(i)).append((i < list.size() - 1) ? comma + " " : "");
//                ans += "" + list.get(i) + ((i < list.size() - 1) ? comma + " " : "");
            }

            if (useBrackets) {
                ans.append(")");
//                ans += ")";
            }
        }

        return ans.toString();
    }

    public void setMySqlSqlCalcFoundRows(boolean mySqlCalcFoundRows) {
        this.mySqlSqlCalcFoundRows = mySqlCalcFoundRows;
    }

    public boolean getMySqlSqlCalcFoundRows() {
        return this.mySqlSqlCalcFoundRows;
    }

    @Override
	public void accept(Injector injector) {
    	
    	PlainSelect pselect = new PlainSelect();
    	// list of tables from which columns may come
    	List<String> tables = new ArrayList<String>();
    	
    	// FromItem
    	if (this.getFromItem() != null) {
		
		FromItem fromItem = this.getFromItem();
		if (fromItem.getClass().getSimpleName().equals("Table")) {
			tables.add(((Table) fromItem).getName());
		};
		if (this.getJoins() != null) {
			for(Join join : this.getJoins()) {
				switch(join.getRightItem().getClass().getSimpleName()) {
				case "Table" : {
					tables.add(((Table) join.getRightItem()).getName());
					break;
				}
				case "SubSelect" : {
					tables.add(((Alias) join.getRightItem().getAlias()).getName());
					break;
				}
				default : {
					break;
				}
				}
			}
		}
		
		pselect.setFromItem(fromItem);
    	}
    	// SelectedItems
    	// CRITICALLY: nothing change with the selected items
    	// ALL the "logic" goes into the WHERE
		pselect.setSelectItems(this.getSelectItems());
			
		
		// Join
		List<Join> joins = new ArrayList<Join>();
		if (this.getJoins() != null) {
		
		for(Join join : this.getJoins()) 
		{
			Join siJoin = new Join();
			// Type
			siJoin.setInner(join.isInner());
			
			siJoin.setLeft(join.isLeft());
			siJoin.setRight(join.isRight());
			// RighItem
			InjectorFromItem injectorFromItem = new InjectorFromItem();
			injectorFromItem.setAction(injector.getAction());
			injectorFromItem.setContext(injector.getContext());
			injectorFromItem.setParameters(injector.getParameters());
			join.getRightItem().accept(injectorFromItem);
			siJoin.setRightItem(injectorFromItem.getResult());
			// On
			siJoin.setOnExpression(join.getOnExpression());
			//
			joins.add(siJoin);
		}
		pselect.setJoins(joins);
		}
		// WHERE
		List<Expression> whereList = new ArrayList<Expression>();
		
		// it adds a call to the corresponding auth-function for each
		// column appearing in the selected items (possibly, ``inside''
		// a function call

		for(SelectItem selitem : this.getSelectItems()) {
			Expression expItem = ((SelectExpressionItem) selitem).getExpression();
			InjectorExpression injectorExpression = new InjectorExpression();
			injectorExpression.setContext(injector.getContext());
			injectorExpression.setAction(injector.getAction());
			injectorExpression.setParameters(injector.getParameters());
			injectorExpression.setTables(tables);
			expItem.accept(injectorExpression);
			//
			List<Expression> list = injectorExpression.getResult();
			
			for(Expression checkexp : list) {
				//if (checkexp.getClass().getSimpleName().equals("Function")){
					BinaryExpression check = new EqualsTo();
					//check.setLeftExpression(((CaseExpression) checkexp).getSwitchExpression());
					check.setLeftExpression(checkexp);
					check.setRightExpression(new LongValue(1));
					whereList.add(check);
				//}
			}
		}
		
		// NEW ADDING CHECKS ALSO BECAUSE EXPRESSIONS IN WHERE CLAUSES 
		// begin
		
		if (this.getWhere() != null) {
			Expression owexp = this.getWhere();
			
			InjectorExpression injectorExpression = new InjectorExpression();
			injectorExpression.setContext(injector.getContext());
			injectorExpression.setAction(injector.getAction());
			injectorExpression.setParameters(injector.getParameters());
			injectorExpression.setTables(tables);
			owexp.accept(injectorExpression);
			
			for(Expression checkexp : injectorExpression.getResult()) {
				//if (checkexp.getClass().getSimpleName().equals("Function")){
					BinaryExpression check = new EqualsTo();
					//check.setLeftExpression(((CaseExpression) checkexp).getSwitchExpression());
					check.setLeftExpression(checkexp);
					check.setRightExpression(new LongValue(1));
					whereList.add(check);
				//}
			}
		} 
		
		// end

		//
		if (this.getFromItem() != null) {
		Function checkAuthFun = new Function();
		checkAuthFun.setName("checkAuth");
		ExpressionList pars = new ExpressionList();
		List<Expression> parsList = new ArrayList<Expression>();

		if (this.getWhere() != null) {
			parsList.add(this.getWhere());
		} else {
			parsList.add(new LongValue(1));
		};
		if (whereList.size() > 0) {
			MultiAndExpression where = new MultiAndExpression(whereList);
			parsList.add(where);
		} else {
			parsList.add(new LongValue(1));
		}
		pars.setExpressions(parsList);	
		checkAuthFun.setParameters(pars);
		pselect.setWhere(checkAuthFun);
		}
		// INTO TABLES
		pselect.setIntoTables(this.getIntoTables());
		
		
  		injector.setResult(pselect);
		}
    

	
}

