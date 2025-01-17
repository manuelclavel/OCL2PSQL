/*
 * Copyright (c) 2009 by Robert Herschke,
 * Hauptstrasse 30, 65760 Eschborn, rh@ocl.herschke.de.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Robert Herschke. ("Confidential Information").
 */
package org.vgu.sqlsi.ocl.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vgu.sqlsi.ocl.context.OclContext;
import org.vgu.sqlsi.ocl.exception.OclEvaluationException;
import org.vgu.sqlsi.ocl.visitor.OCL2SQLParser;
import org.vgu.sqlsi.sql.statement.select.ResSelectExpression;
import org.vgu.sqlsi.sql.statement.select.ValSelectExpression;
import org.vgu.sqlsi.sql.statement.select.VarSelectExpression;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.MyPlainSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Class PropertyCallExp
 */
public final class PropertyCallExp extends NavigationCallExp {

    private final String name;

    public String getName() {
        return name;
    }

    public PropertyCallExp(OclExpression newsource, String name, OclExpression... qualifier) {
        super(newsource, qualifier);
        this.name = name;
    }

    @Override
    public Object eval(OclContext context) throws OclEvaluationException {
        try {
            return context.getFieldValue(source.eval(context), this.name);
        } catch (Exception e) {
            throw new OclEvaluationException("cannot get the field value: " + this.name, e);
        }
    }

    @Override
    public Statement accept(StmVisitor visitor) {
        // 1. get the "source" of the variable

        // 1. save the current iterator source list (outer-iterators), and alias
        // in a temporal variable
        List<IteratorSource> cur_iters = new ArrayList<IteratorSource>();
        cur_iters.addAll(visitor.getVisitorContext());
        //
        // 3. visit the source and save the result
        SubSelect subselect_Source = new SubSelect();
        subselect_Source.setSelectBody(((Select) visitor.visit(this.getSource())).getSelectBody());
        visitor.setVisitorContext(cur_iters);
        Alias alias_Source = new Alias(Utilities.genAliasName(visitor));
        subselect_Source.setAlias(alias_Source);

        String prop = this.name.substring(this.name.indexOf(":") + 1, this.name.length());
        String propClass = this.name.substring(0, this.name.indexOf(":"));

        // System.out.println(propClass + " " + prop);

        if (org.vgu.sqlsi.main.Utilities.isAttribute(visitor.getPlainUMLContext(), propClass, prop)) {
            /*
             * attribute
             */

            // 1. find the attribute
            // UMLAttribute attr = null;
            // for (UMLAttribute item_attr : visitor.getUMLContext().getAttributes()) {
            // if (item_attr.getName().equals(prop)
            // && item_attr.getSource().getName().equals(propClass)) {
            // attr = item_attr;
            // break;
            // }
            // }

            // build the expression
            PlainSelect pselect = new PlainSelect();

            SelectExpressionItem item = new SelectExpressionItem();
            // item.setExpression(new Column(attr.getName()));
            item.setExpression(new Column(prop));
            item.setAlias(new Alias("item"));
            pselect.addSelectItems(item);

            // FROM ITEM
            SubSelect lselect = new SubSelect();
            Table table = new Table();
            PlainSelect body = new PlainSelect();
            // table.setName(attr.getSource().getName());
            table.setName(propClass);
            body.setFromItem(table);
            body.addSelectItems(new AllColumns());
            lselect.setSelectBody(body);
            Alias alias_Class = new Alias(Utilities.genAliasName(visitor));
            lselect.setAlias(alias_Class);

            pselect.setFromItem(lselect);

            // RIGHT JOIN
            Join join = new Join();
            join.setRight(true);
            join.setRightItem(subselect_Source);
            // set the expected on expression
            BinaryExpression bexpr = new EqualsTo();
            bexpr.setLeftExpression(new Column(alias_Class.getName().concat(".").concat(propClass).concat("_id")));
            bexpr.setRightExpression(new Column(alias_Source.getName().concat(".").concat("item")));
            join.setOnExpression(bexpr);

            List<Join> joins = new ArrayList<Join>();
            joins.add(join);
            pselect.setJoins(joins);

            // variables rippling up
            Utilities.ripplingUpVariables(pselect, null, subselect_Source, null);

            Select select = new Select();
            select.setSelectBody(pselect);

            return select;

        } else
        /*
         * association-end
         */

        {
            // 1. find the association-end
            // UMLAssociationEnd end = null;
            // for (UMLAssociationEnd item_end : visitor.getPlainUMLContext()) {
            // if (item_end.getName().equals(prop)
            // && item_end.getSource().getName().equals(propClass)) {
            // end = item_end;
            // break;
            // }
            // }

            // 2. find and add the product table in the result-table
            // String nameEndTable = null;
            // String source = end.getSource().getName();
            // String target = end.getTarget().getName();
            // if (source.compareTo(target) > 0) {
            // nameEndTable = target.concat("_").concat(source);
            // } else {
            // nameEndTable = source.concat("_").concat(target);
            // }

            // build the expression
            PlainSelect pselect = new PlainSelect();

            SelectExpressionItem item = new SelectExpressionItem();
            item.setExpression(new Column(prop));
            item.setAlias(new Alias("item"));
            pselect.addSelectItems(item);

            // FROM ITEM
            SubSelect lselect = new SubSelect();
            Table table = new Table();
            PlainSelect body = new PlainSelect();
            // table.setName(nameEndTable);
            table.setName(org.vgu.sqlsi.main.Utilities.getAssociation(visitor.getPlainUMLContext(), propClass, prop));
            body.setFromItem(table);
            body.addSelectItems(new AllColumns());
            lselect.setSelectBody(body);
            Alias alias_Table = new Alias(Utilities.genAliasName(visitor));
            lselect.setAlias(alias_Table);
            pselect.setFromItem(lselect);

            // RIGHT JOIN
            Join join = new Join();
            join.setRight(true);
            join.setRightItem(subselect_Source);

            // set the expected on expression
            BinaryExpression bexpr = new EqualsTo();
            bexpr.setLeftExpression(new Column(alias_Table.getName().concat(".").concat(org.vgu.sqlsi.main.Utilities
                    .getAssociationOpposite(visitor.getPlainUMLContext(), propClass, prop))));
            bexpr.setRightExpression(new Column(alias_Source.getName().concat(".").concat("item")));

            join.setOnExpression(bexpr);

            List<Join> joins = new ArrayList<Join>();
            joins.add(join);
            pselect.setJoins(joins);

            // variables rippling up
            Utilities.ripplingUpVariables(pselect, null, subselect_Source, null);

            Select select = new Select();
            select.setSelectBody(pselect);

            return select;

        }
    }

    @Override
    public Statement map(StmVisitor visitor) {
        Variable currentVariable = VariableUtils.getCurrentVariable(this);
        SubSelect tempVar = new SubSelect();
        tempVar.setSelectBody(((Select) visitor.visit(this.getSource())).getSelectBody());
//        Alias aliasSubSelectCurrentVar = new Alias("TEMP_".concat(currentVariable.getName()));
        Alias aliasSubSelectCurrentVar = new Alias("TEMP_obj");
        tempVar.setAlias(aliasSubSelectCurrentVar);
        MyPlainSelect finalPlainSelect = new MyPlainSelect();
        Select finalSelect = new Select(finalPlainSelect);
        String propertyName = this.name.substring(this.name.indexOf(":") + 1, this.name.length());
        String propertyClass = this.name.substring(0, this.name.indexOf(":"));

//        CaseExpression caseExpression = new CaseExpression();
//        BinaryExpression isValValid = new EqualsTo();
//        isValValid.setLeftExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".val")));
//        isValValid.setRightExpression(new LongValue(0L));
//        WhenClause whenClause = new WhenClause();
//        whenClause.setWhenExpression(isValValid);
//        whenClause.setThenExpression(new NullValue());
//        caseExpression.setWhenClauses(Arrays.asList(whenClause));
//        caseExpression.setElseExpression(new Column(propertyName));
        
        ResSelectExpression resSelectExpression = new ResSelectExpression();
//        resSelectExpression.setExpression(caseExpression);
        resSelectExpression.setExpression(new Column(propertyName));
        
        Table table = new Table();
        AndExpression andEx = new AndExpression();
        
        Select currentVarSource = null;
        
        for(IteratorSource v : visitor.getVisitorContext()) {
            MyIteratorSource myV = (MyIteratorSource) v;
            if(myV.getIterator().getName().equals(currentVariable.getName())) {
                currentVarSource = myV.getSourceWithoutIter();
            }
        }
        
        if (org.vgu.sqlsi.main.Utilities.isAttribute(visitor.getPlainUMLContext(), propertyClass, propertyName)) {

            String tableRefColumn = propertyClass.concat(".").concat(propertyClass).concat("_id");

            table.setName(propertyClass);

            if(VariableUtils.isSourceAClassAllInstances(currentVarSource.getSelectBody(), propertyClass)) {
                finalPlainSelect.setRes(resSelectExpression);
                finalPlainSelect.setFromItem(table);
                VarSelectExpression newVar = new VarSelectExpression(currentVariable.getName());
                newVar.setRefExpression(new Column(table.getName().concat("_id")));
                finalPlainSelect.addVar(newVar);
                return finalSelect;
            }
            
            BinaryExpression bexpr = new EqualsTo();
            bexpr.setLeftExpression(new Column(tableRefColumn));
            bexpr.setRightExpression(
                    new Column(aliasSubSelectCurrentVar.getName().concat(".ref_").concat(currentVariable.getName())));
            andEx.setLeftExpression(bexpr);
            BinaryExpression varEx = new EqualsTo();
            varEx.setLeftExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".val")));
            varEx.setRightExpression(new LongValue(1L));
            andEx.setRightExpression(varEx);
            
            Join join = new Join();
            join.setLeft(true);
            join.setRightItem(table);
            join.setOnExpression(andEx);
            finalPlainSelect.addSelectItems(resSelectExpression);
            finalPlainSelect.setVal(new ValSelectExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".val"))));
            finalPlainSelect.setFromItem(tempVar);
            finalPlainSelect.setJoins(Arrays.asList(join));
            
            List<String> SVarsSource = VariableUtils.SVars((MyPlainSelect) tempVar.getSelectBody(), visitor.getVisitorContext());
            for(String v : SVarsSource) {
                VarSelectExpression newVar = new VarSelectExpression(v);
                newVar.setRefExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".ref_").concat(v)));
                finalPlainSelect.addVar(newVar);
            }
            
            return finalSelect;
        } 
        else if(org.vgu.sqlsi.main.Utilities.isAssociation(visitor.getPlainUMLContext(), propertyClass, propertyName)) {
            String assocClass = org.vgu.sqlsi.main.Utilities.getAssociation(visitor.getPlainUMLContext(), propertyClass,
                    propertyName);
            String oppositeEnd = org.vgu.sqlsi.main.Utilities.getAssociationOpposite(visitor.getPlainUMLContext(),
                    propertyClass, propertyName);

            table.setName(assocClass);
            
//            if(VariableUtils.isSourceHasRequiredClass(tempVar, propertyClass)) {
//                PlainSelect plainSelectVar = (PlainSelect) tempVar.getSelectBody();
//                finalPlainSelect.addSelectItem(resSelectExpression);
//                finalPlainSelect.setFromItem(table);
//                for(SelectItem item : plainSelectVar.getSelectItems()) {
//                    if(item instanceof RefSelectExpression) {
//                        RefSelectExpression refExpression = (RefSelectExpression) item;
//                        if(refExpression.getVar().equals(currentVariable.getName())) {
//                            RefSelectExpression newRef = new RefSelectExpression(currentVariable.getName());
//                            newRef.setExpression(new Column(oppositeEnd));
//                            finalPlainSelect.addSelectItem(newRef);
//                        } else {
//                            finalPlainSelect.addSelectItem(item);
//                        }
//                    }
//                }
//                return finalSelect;
//            }
            if(VariableUtils.isSourceAClassAllInstances(currentVarSource.getSelectBody(), propertyClass)) {
                String tableName = ((Table) ((MyPlainSelect) currentVarSource.getSelectBody()).getFromItem()).getName();
                finalPlainSelect.setRes(resSelectExpression);
                finalPlainSelect.setFromItem(new Table(tableName));
                
                Join join = new Join();
                join.setLeft(true);
                join.setRightItem(table);
                BinaryExpression onCondition = new EqualsTo();
                onCondition.setLeftExpression(new Column(tableName.concat("_id")));
                onCondition.setRightExpression(new Column(assocClass.concat(".").concat(oppositeEnd)));
                join.setOnExpression(onCondition);
                finalPlainSelect.setJoins(Arrays.asList(join));
                
                CaseExpression caseValExpression = new CaseExpression();
                IsNullExpression isOppEndNull = new IsNullExpression();
                isOppEndNull.setLeftExpression(new Column(assocClass.concat(".").concat(oppositeEnd)));
                WhenClause whenValClause = new WhenClause();
                whenValClause.setWhenExpression(isOppEndNull);
                whenValClause.setThenExpression(new LongValue(0L));
                caseValExpression.setWhenClauses(Arrays.asList(whenValClause));
                caseValExpression.setElseExpression(new LongValue(1L));
                
                finalPlainSelect.setVal(new ValSelectExpression(caseValExpression));
                
                VarSelectExpression newVar = new VarSelectExpression(currentVariable.getName());
                newVar.setRefExpression(new Column(tableName.concat("_id")));
                finalPlainSelect.addVar(newVar);
                return finalSelect;
            }
            
            BinaryExpression bexpr = new EqualsTo();
            bexpr.setLeftExpression(new Column(assocClass.concat(".").concat(oppositeEnd)));
            bexpr.setRightExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".ref_")
                    .concat(currentVariable.getName())));
            andEx.setLeftExpression(bexpr);
            BinaryExpression varEx = new EqualsTo();
            varEx.setLeftExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".val")));
            varEx.setRightExpression(new LongValue(1L));
            andEx.setRightExpression(varEx);
            
            CaseExpression caseValExpression = new CaseExpression();
            IsNullExpression isOppEndNull = new IsNullExpression();
            isOppEndNull.setLeftExpression(new Column(assocClass.concat(".").concat(oppositeEnd)));
            WhenClause whenValClause = new WhenClause();
            whenValClause.setWhenExpression(isOppEndNull);
            whenValClause.setThenExpression(new LongValue(0L));
            caseValExpression.setWhenClauses(Arrays.asList(whenValClause));
            caseValExpression.setElseExpression(new LongValue(1L));
            
            Join join = new Join();
            join.setLeft(true);
            join.setRightItem(table);
            join.setOnExpression(andEx);
            finalPlainSelect.addSelectItems(resSelectExpression);
            finalPlainSelect.setVal(new ValSelectExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".val"))));
            finalPlainSelect.setFromItem(tempVar);
            finalPlainSelect.setJoins(Arrays.asList(join));
            
            List<String> SVarsSource = VariableUtils.SVars((MyPlainSelect) tempVar.getSelectBody(), visitor.getVisitorContext());
            for(String v : SVarsSource) {
                VarSelectExpression newVar = new VarSelectExpression(v);
                newVar.setRefExpression(new Column(aliasSubSelectCurrentVar.getName().concat(".ref_").concat(v)));
                finalPlainSelect.addVar(newVar);
            }
            
            finalPlainSelect.setVal(new ValSelectExpression(caseValExpression));
            
//            for(VarSelectExpression var : finalPlainSelect.getVars()) {
//                if(currentVariable.getName().equals(var.getVar())) {
//                    IsNullExpression isNullExp = new IsNullExpression();
//                    isNullExp.setNot(true);
//                    isNullExp.setLeftExpression(new Column(assocClass.concat(".").concat(oppositeEnd)));
//                    var.setValExpression(isNullExp);
//                    break;
//                }
//            }
            
            ((OCL2SQLParser) visitor).increaseLevelOfSet();

            return finalSelect;
        } else {
            if(!org.vgu.sqlsi.main.Utilities.isClass(visitor.getPlainUMLContext(), propertyClass)) {
                throw new NullPointerException("Invalid class: ".concat(propertyClass));
            }
            throw new NullPointerException("Invalid attribute or association: ".concat(propertyName));
        }
    }

}
