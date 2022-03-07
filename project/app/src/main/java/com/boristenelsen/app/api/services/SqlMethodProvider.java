package com.boristenelsen.app.api.services;

import com.boristenelsen.app.api.model.IndexResponse;
import com.boristenelsen.app.api.model.Statement;
import com.boristenelsen.app.database.models.Table;
import com.boristenelsen.app.database.utils.MemoryUtil;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.arrow.gandiva.evaluator.Filter;
import org.apache.arrow.gandiva.evaluator.SelectionVectorInt32;
import org.apache.arrow.gandiva.exceptions.GandivaException;
import org.apache.arrow.gandiva.expression.Condition;
import org.apache.arrow.gandiva.expression.TreeBuilder;
import org.apache.arrow.gandiva.expression.TreeNode;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.ipc.message.ArrowFieldNode;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SqlMethodProvider {

    @Autowired
    GandivaProvider gandivaProvider;

    public IndexResponse checkStatementToMethod(Statement statement,Table table) throws JSQLParserException, GandivaException {
        /*
        Abhängig vom Statement rufe private Methode auf und returne die Indexresponse an Controller -> An Client
         */
        net.sf.jsqlparser.statement.Statement jsqlStatement = CCJSqlParserUtil.parse(statement.getStatement());
        if(jsqlStatement instanceof Select) {

            Select select = (Select) jsqlStatement;
            PlainSelect plainSelect = (PlainSelect)select.getSelectBody();
            List<SelectItem> selectItemList = plainSelect.getSelectItems();

            //Item für SQL-Statement-Art
            SelectItem firstItem = selectItemList.get(0);
            Expression expression = plainSelect.getWhere();

            //Wenn SelectStatement ein * enthält (AllColumns)
            if(firstItem instanceof AllColumns){
                return selectEverything(table);
            }else if(expression!=null){
                if(expression instanceof MinorThan){
                    MinorThan minorThan = (MinorThan)expression;
                    Column col = (Column) minorThan.getLeftExpression();
                    LongValue rightExpr = (LongValue) minorThan.getRightExpression();
                    int value = Integer.parseInt(rightExpr.getStringValue());
                    return selectWhereLessThan(col.getColumnName(),value,table);
                }else if(expression instanceof GreaterThan){
                    GreaterThan greaterThan = (GreaterThan) expression;
                    Column col = (Column) greaterThan.getLeftExpression();
                    LongValue rightExpr = (LongValue) greaterThan.getRightExpression();
                    int value = Integer.parseInt(rightExpr.getStringValue());
                    return selectWhereGreaterThan(col.getColumnName(),value,table);
                }else if(expression instanceof EqualsTo) {
                    EqualsTo equalsTo = (EqualsTo) expression;
                    Column col = (Column) equalsTo.getLeftExpression();
                    LongValue rightExpr = (LongValue) equalsTo.getRightExpression();
                    int value = Integer.parseInt(rightExpr.getStringValue());
                    return selectWhereEqualsTo(col.getColumnName(), value, table);
                }
            } else if(firstItem instanceof SelectExpressionItem){
                List<String> columnNames = selectItemList.stream().map(SelectItem::toString).collect(Collectors.toList());
                return selectEverythingFromCols(columnNames,table);
            }

            //Wenn SelectStatement Spalten enthält
        }
        return null;
    }

    private IndexResponse selectWhereEqualsTo(String columnName, int value, Table table) throws GandivaException {
        Filter filter = gandivaProvider.equalsTo_NumberFilter(table,columnName,value);
        ArrowRecordBatch batch = gandivaProvider.createBatch(table,columnName);
        SelectionVectorInt32 selectionVectorInt32 = gandivaProvider.createSelectionVector(table);

        filter.evaluate(batch,selectionVectorInt32);

        int[] indices = MemoryUtil.selectionVectorToArray(selectionVectorInt32);

        //Prepare IndexResponse
        List<String> memoryAdressList = new ArrayList<>();
        memoryAdressList.add(table.getMemoryadress(columnName));
        List<String> types = new ArrayList<>();
        List<Integer> offets = Arrays.stream(indices).boxed().collect(Collectors.toList());
        types.add(table.getTypeByName(columnName));
        return new IndexResponse(memoryAdressList,offets,types);

    }


    private IndexResponse selectWhereGreaterThan(String columnName, int value, Table table) throws GandivaException {
        Filter filter = gandivaProvider.greaterThan_NumberFilter(table,columnName,value);
        ArrowRecordBatch batch = gandivaProvider.createBatch(table,columnName);
        SelectionVectorInt32 selectionVectorInt32 = gandivaProvider.createSelectionVector(table);

        filter.evaluate(batch,selectionVectorInt32);

        int[] indices = MemoryUtil.selectionVectorToArray(selectionVectorInt32);

        //Prepare IndexResponse
        List<String> memoryAdressList = new ArrayList<>();
        memoryAdressList.add(table.getMemoryadress(columnName));
        List<String> types = new ArrayList<>();
        List<Integer> offets = Arrays.stream(indices).boxed().collect(Collectors.toList());
        types.add(table.getTypeByName(columnName));
        return new IndexResponse(memoryAdressList,offets,types);

    }

    /**
     * Evaluates a less-than SQL-Statement with Gandiva in 4 Steps:
     * - Create Gandiva Treestructure
     * - Fill ArrowBuffers with Data from @param table and encapsulate an ArrowRecordBatch
     * - evaluate Filter
     * - Prepare IndexResponse
     * @param columnName
     * @param value
     * @param table
     * @return
     * @throws GandivaException
     */
    private IndexResponse selectWhereLessThan(String columnName, int value,Table table) throws GandivaException {

        Filter filter = gandivaProvider.lessThan_NumberFilter(table,columnName,value);
        ArrowRecordBatch batch = gandivaProvider.createBatch(table,columnName);
        SelectionVectorInt32 selectionVectorInt32 = gandivaProvider.createSelectionVector(table);

        filter.evaluate(batch,selectionVectorInt32);

        int [] indices = MemoryUtil.selectionVectorToArray(selectionVectorInt32);

        //Prepare IndexResponse
        List<String> memoryAdressList = new ArrayList<>();
        memoryAdressList.add(table.getMemoryadress(columnName));
        List<String> types = new ArrayList<>();
        List<Integer> offets = Arrays.stream(indices).boxed().collect(Collectors.toList());
        types.add(table.getTypeByName(columnName));
        return new IndexResponse(memoryAdressList,offets,types);


    }


    /**
     * Evaluates SELECT * FROM table
     * This Statement is handly written with no Gandiva backend-logic
     * @param table
     * @return
     */
    private IndexResponse selectEverything(Table table){

        List<FieldVector> fieldVectorList = table.vectorSchemaRoot.getFieldVectors();
        List<String> memoryAdressList = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<Integer> offets = new ArrayList<>();
        for(FieldVector fieldVector : fieldVectorList){
            memoryAdressList.add(Long.toHexString(fieldVector.getDataBufferAddress()));
            types.add(fieldVector.getField().getType().toString());
        }
        return new IndexResponse(memoryAdressList,offets,types);
    }

    /**
     * Evaluates SELECT col1,col2 FROM table
     * No offsets in IndexResponse means that every Address should be iterated
     * @param colNames
     * @param table
     * @return
     */
    private IndexResponse selectEverythingFromCols(List<String> colNames,Table table){
        List<FieldVector> fieldVectorList = table.vectorSchemaRoot.getFieldVectors();
        List<String> memoryAdressList = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<Integer> offets = new ArrayList<>();
        for(String col : colNames){
            for(FieldVector fieldVector: fieldVectorList){
                if(fieldVector.getField().getName().equals(col)){
                    memoryAdressList.add(Long.toHexString(fieldVector.getDataBufferAddress()));
                    types.add(fieldVector.getField().getType().toString());
                }
            }
        }
        return new IndexResponse(memoryAdressList,offets,types);
    }


}
