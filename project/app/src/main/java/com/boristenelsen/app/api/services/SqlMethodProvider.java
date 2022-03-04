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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SqlMethodProvider {

    public IndexResponse checkStatementToMethod(Statement statement,Table table) throws JSQLParserException, GandivaException {
        /*
        Abhängig vom Statement rufe private Methode auf und returne die Indexresponse an Controller -> An Client
         */
        if(statement.getStatement().equals("gandivatest")){

        }
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
                }
            } else if(firstItem instanceof SelectExpressionItem){
                List<String> columnNames = selectItemList.stream().map(SelectItem::toString).collect(Collectors.toList());
                return selectCol(columnNames,table);
            }

            //Wenn SelectStatement Spalten enthält
        }
        return null;
    }

    private IndexResponse selectWhereLessThan(String columnName, int value,Table table) throws GandivaException {
        Field column = table.getFieldByName(columnName);
        if(column == null){
            System.out.println("Kein Field in Datenbank mit dem Namen: " + columnName);
        }

        TreeNode literal = TreeBuilder.makeLiteral(value);

        List<TreeNode> args = Lists.newArrayList(TreeBuilder.makeField(column),literal);
        TreeNode lessThanFuncNode = TreeBuilder.makeFunction("less_than",args, new ArrowType.Bool());
        Condition lessthanCond = TreeBuilder.makeCondition(lessThanFuncNode);

        Schema schema = new Schema(Lists.newArrayList(column));
        Filter filter = Filter.make(schema,lessthanCond);

        int numRows = table.getRowSize();
        ArrowBuf colBuf = table.vectorSchemaRoot.getVector(columnName).getDataBuffer();
        ArrowBuf valBuf = table.vectorSchemaRoot.getVector(columnName).getValidityBuffer();
        ArrowRecordBatch batch = new ArrowRecordBatch(
                numRows,
                Lists.newArrayList(new ArrowFieldNode(numRows,0)),
                Lists.newArrayList(valBuf,colBuf));
        ArrowBuf selectionBuffer = table.allocator.buffer(numRows*3);
        SelectionVectorInt32 selVecInt32 = new SelectionVectorInt32(selectionBuffer);
        filter.evaluate(batch,selVecInt32);

        int [] indices = MemoryUtil.selectionVectorToArray(selVecInt32);

        //Prepare IndexResponse
        List<String> memoryAdressList = new ArrayList<>();
        memoryAdressList.add(table.getMemoryadress(columnName));
        List<String> types = new ArrayList<>();
        List<Integer> offets = Arrays.stream(indices).boxed().collect(Collectors.toList());
        types.add(column.getType().toString());
        return new IndexResponse(memoryAdressList,offets,types);


    }

    //Deckt ein SELECT * FROM Table ab
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
    //Deckt ein SELECT * FROM Table WHERE ab. -> GandivaService
    private IndexResponse selectCondtion(){
        return null;
    }

    //Deckt ein SELECT Spalte FROM Table ab
    private IndexResponse selectCol(List<String> colNames,Table table){
        return null;
    }

    // ACHTUNG: Die Spezifische Column muss noch in den Code eingarbeitet werden-> nur Grundgerüst
    private IndexResponse testGandiva(Table table) throws GandivaException {
        /*
            Gandiva Regeln:
            - Referenziere Felder aus Datenbank welche im Statement Bedingung/Funktion benutzen
            - Filtere aus Statement die Condition bsp : Select col from table WHERE col < 2
        */

        //Field nach ColName aus Select instanzieren
        Field col = table.fieldList.get(1);
        TreeNode number = TreeBuilder.makeLiteral(2);

        //Prepare Gandiva Expression Structure
        List<TreeNode> args = Lists.newArrayList(TreeBuilder.makeField(col),number);
        TreeNode lessThanLiteralFunction = TreeBuilder.makeFunction("less_than",args, new ArrowType.Bool());
        Condition lessthanCond = TreeBuilder.makeCondition(lessThanLiteralFunction);

        Schema schema = new Schema(Lists.newArrayList(col));
        Filter filter = Filter.make(schema,lessthanCond);


        //Um Filter zu evaluieren brauchen wir Daten und einen ArrowRecordBatch. Daten holen wir aus unserem Table
        int numRows = table.fieldList.size();
        ArrowBuf colBuf = table.vectorSchemaRoot.getVector("col").getDataBuffer();
        ArrowBuf valBuf = table.vectorSchemaRoot.getVector("col").getValidityBuffer();
        ArrowRecordBatch batch = new ArrowRecordBatch(
                numRows,
                Lists.newArrayList(new ArrowFieldNode(numRows,0)),
                Lists.newArrayList(valBuf,colBuf));
        ArrowBuf selectionBuffer = table.allocator.buffer(numRows);
        SelectionVectorInt32 selVecInt32 = new SelectionVectorInt32(selectionBuffer);
        filter.evaluate(batch,selVecInt32);

        int [] indices = MemoryUtil.selectionVectorToArray(selVecInt32);

        //Prepare IndexResponse
        List<FieldVector> fieldVectorList = table.vectorSchemaRoot.getFieldVectors();

        List<String> memoryAdressList = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<Integer> offets = Arrays.stream(indices).boxed().collect(Collectors.toList());
        types.add(col.getFieldType().toString());


        return new IndexResponse(memoryAdressList,offets,types);
    }
}
