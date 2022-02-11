package com.boristenelsen.app.database.services;

import com.boristenelsen.app.database.models.Table;
import com.boristenelsen.app.database.utils.MemoryUtil;
import com.boristenelsen.app.database.utils.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.UInt4Vector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;


public class DumpReader {

    private File dumpFile;
    private Schema dumpSchema;
    private Table table;

    public DumpReader(File dumpFile){
        this.dumpFile = dumpFile;
    }



    public void initDatabase() throws IOException, JSQLParserException {
        Scanner scanner = new Scanner(dumpFile);
        scanner.useDelimiter(";");

        while(scanner.hasNext()){
            String statement = scanner.next();
            if(statement.equals("\n"))break;

            //Ueberpruefe statement auf Create Table Statement und erstelle Table aus SQLDump.
            if(isCreateTableStmt(statement)){
                table = parseCreateTableStmt(statement);
            }
            //Fülle für jeden Insert SQL-Dump
            if(isInsertStmt(statement)){
                try {
                    prepareDataForMemory(statement,table);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Benutzer Speicher in Bytes: " + memory);
        System.out.println("Benutzer Speicher in MB: " + bytesToMegabytes(memory));
    }
    public static long bytesToMegabytes(long bytes) {
        return bytes / (1024L * 1024L);
    }

    public Table parseCreateTableStmt(String stmt) throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(stmt);
        CreateTable ct = (CreateTable) parse;
        System.out.println("table=" + ct.getTable().getFullyQualifiedName());

        List<Field> fieldList = new ArrayList<>();

        for (ColumnDefinition colDef : ct.getColumnDefinitions()) {
            try {
                Field parsedField = prepareField(colDef.getColumnName(), colDef.getColDataType().toString());
                fieldList.add(parsedField);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return new Table(new Schema(fieldList),fieldList);


    }

    public Field prepareField(String colName, String colDataType) throws Exception {
        String[] colType = colDataType.split(" ");
        switch (colType[0]) {
            case "char":
                return new Field(colName, FieldType.nullable(new ArrowType.Utf8()), null);
            case "varchar":
                return new Field(colName, FieldType.nullable(new ArrowType.Utf8()), null);
            case "mediumint":
                return new Field(colName, FieldType.nullable(new ArrowType.Int(32, false)), null);

        }
        throw new Exception();
    }

    // Parse SQL-Statement in ein Schema welches für Apache Arrow zugeschnitten ist. Dieses Schema wird später für die Klasse VectorSchemaRoot benötigt.
    public  void prepareDataForMemory(String stmt,Table table) throws Exception {
        Statement parse = CCJSqlParserUtil.parse(stmt);
        Insert insert = (Insert) parse;
        insert.getColumns().forEach(System.out::println);
        ItemsList items = insert.getItemsList();

        //Insert Statement enthält mehrere Expressions, welche Schema abhängig sind. Schema ist immer größer als Column im Insert Statement da ID Spalte zusätzlich hinzukommt
        if(items instanceof MultiExpressionList){
            MultiExpressionList multiExpressionList = ((MultiExpressionList) items);
            List<ExpressionList> expressionLists = multiExpressionList.getExpressionLists();
            for(ExpressionList expressionList : expressionLists){
                List<Expression> exp = expressionList.getExpressions();
                List <Column> columns = insert.getColumns();

                //Pro Eintrag muss sich der Index automatisch erhöhen
                ((UInt4Vector) table.vectorSchemaRoot.getVector("`id`")).setSafe(table.getCounter(),table.getCounter());
                //Iteriere über jeden einzelnen Wert in der Expression und fülle diesen Wert in die richtige Spalte. Anders gesagt fülle Apache Arrow Table mit Daten aus SQLDump
                for(int i=0; i <  columns.size();i++){
                    Column col = columns.get(i);
                    String colName = col.getColumnName();
                    FieldVector field = table.vectorSchemaRoot.getVector(colName);

                    if(field instanceof UInt4Vector){
                        ((UInt4Vector) table.vectorSchemaRoot.getVector(colName)).setSafe(table.getCounter(),Integer.parseInt(exp.get(i).toString()));
                        MemoryUtil.printLongAsAdress(table.vectorSchemaRoot.getVector(colName).getDataBufferAddress());
                    }else if(field instanceof VarCharVector){
                        ((VarCharVector) table.vectorSchemaRoot.getVector(colName)).setSafe(table.getCounter(),exp.get(i).toString().getBytes());
                        MemoryUtil.printLongAsAdress(table.vectorSchemaRoot.getVector(colName).getDataBufferAddress());
                    }
                    else throw new Exception();

                }
                //Am Ende wir der Table counter erhöht damit an den richtigen Index geschrieben werden kann.
                table.incrementCounter();
            }
        }

    }



    public  boolean isCreateTableStmt(String stmt) throws JSQLParserException {
        return CCJSqlParserUtil.parse(stmt) instanceof CreateTable;

    }

    public  boolean isInsertStmt(String statement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(statement) instanceof Insert;
    }

    public Table getTable(){
        return this.table;
    }

}
