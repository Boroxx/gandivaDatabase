package com.boristenelsen.app.api.services;

import com.boristenelsen.app.database.models.Table;
import com.google.common.collect.Lists;
import org.apache.arrow.gandiva.evaluator.Filter;
import org.apache.arrow.gandiva.evaluator.SelectionVectorInt32;
import org.apache.arrow.gandiva.exceptions.GandivaException;
import org.apache.arrow.gandiva.expression.Condition;
import org.apache.arrow.gandiva.expression.TreeBuilder;
import org.apache.arrow.gandiva.expression.TreeNode;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.ipc.message.ArrowFieldNode;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GandivaProvider implements IGandivaProvider {

    public Filter lessThan_NumberFilter(Table table,String columnName,int value) {
        Field column = table.getFieldByName(columnName);
        if(column == null){
            System.out.println("Kein Field in Datenbank mit dem Namen: " + columnName);
        }

        TreeNode literal = TreeBuilder.makeLiteral(value);

        List<TreeNode> args = Lists.newArrayList(TreeBuilder.makeField(column),literal);
        TreeNode lessThanFuncNode = TreeBuilder.makeFunction("less_than",args, new ArrowType.Bool());
        Condition lessthanCond = TreeBuilder.makeCondition(lessThanFuncNode);

        Schema schema = new Schema(Lists.newArrayList(column));
        try {
            return Filter.make(schema,lessthanCond);
        } catch (GandivaException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Filter greaterThan_NumberFilter(Table table, String columnName, int value) {
        Field column = table.getFieldByName(columnName);
        if(column == null){
            System.out.println("Kein Field in Datenbank mit dem Namen: " + columnName);
        }

        TreeNode literal = TreeBuilder.makeLiteral(value);

        List<TreeNode> args = Lists.newArrayList(TreeBuilder.makeField(column),literal);
        TreeNode greaterThanFuncNode = TreeBuilder.makeFunction("greater_than",args, new ArrowType.Bool());
        Condition greaterThanCond = TreeBuilder.makeCondition(greaterThanFuncNode);

        Schema schema = new Schema(Lists.newArrayList(column));
        try {
            return Filter.make(schema,greaterThanCond);
        } catch (GandivaException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Filter equalsTo_NumberFilter(Table table, String columnName, int value) {
        Field column = table.getFieldByName(columnName);
        if(column == null){
            System.out.println("Kein Field in Datenbank mit dem Namen: " + columnName);
        }

        TreeNode literal = TreeBuilder.makeLiteral(value);

        List<TreeNode> args = Lists.newArrayList(TreeBuilder.makeField(column),literal);
        TreeNode equalsToFuncNode = TreeBuilder.makeFunction("equal",args, new ArrowType.Bool());
        Condition equalsToCond = TreeBuilder.makeCondition(equalsToFuncNode);

        Schema schema = new Schema(Lists.newArrayList(column));
        try {
            return Filter.make(schema,equalsToCond);
        } catch (GandivaException e) {
            e.printStackTrace();
        }

        return null;
    }


    public ArrowRecordBatch createBatch(Table table, String colName){
        int numRows = table.getRowSize();
        ArrowBuf colBuf = table.vectorSchemaRoot.getVector(colName).getDataBuffer();
        ArrowBuf valBuf = table.vectorSchemaRoot.getVector(colName).getValidityBuffer();

        return new ArrowRecordBatch(
                numRows,
                Lists.newArrayList(new ArrowFieldNode(numRows,0)),
                Lists.newArrayList(valBuf,colBuf));
    }

    public SelectionVectorInt32 createSelectionVector(Table table){
        int numRows = table.getRowSize();
        ArrowBuf selectionBuffer = table.allocator.buffer(numRows*table.getCounter());
        return new SelectionVectorInt32(selectionBuffer);
    }

}
