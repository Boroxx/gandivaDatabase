package com.boristenelsen.app.database.utils;

import com.boristenelsen.app.database.models.Table;
import com.boristenelsen.app.enums.DataType;
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
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {

    public static Schema parseCreateTableStmt(String stmt) throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(stmt);
        CreateTable ct = (CreateTable) parse;
        System.out.println("table=" + ct.getTable().getFullyQualifiedName());

        List<Field> fieldList = new ArrayList<>();

        for (ColumnDefinition colDef : ct.getColumnDefinitions()) {
            System.out.println("column=" + colDef.getColumnName() + " " + colDef.getColDataType() + " ");
            try {
                Field parsedField = prepareField(colDef.getColumnName(), colDef.getColDataType().toString());
                fieldList.add(parsedField);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return new Schema(fieldList);


    }

    public static Field prepareField(String colName, String colDataType) throws Exception {
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
    public static void prepareDataForMemory(String stmt,Table table) throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(stmt);
        Insert insert = (Insert) parse;
        insert.getColumns().stream().forEach(System.out::println);
        ItemsList items = insert.getItemsList();

        //Insert Statement enthält mehrere Expressions, welche Schema abhängig sind. Schema ist immer größer als Column im Insert Statement da ID Spalte zusätzlich hinzukommt
        if(items instanceof MultiExpressionList){
            MultiExpressionList multiExpressionList = ((MultiExpressionList) items);
            List<ExpressionList> expressionLists = multiExpressionList.getExpressionLists();
            for(ExpressionList expressionList : expressionLists){
                List<Expression> exp = expressionList.getExpressions();
                List <Column> columns = insert.getColumns();

                System.out.println(exp.get(0));
                System.out.println(insert.getColumns().size());
                System.out.println(table.vectorSchemaRoot.getFieldVectors().size());

                ((IntVector) table.vectorSchemaRoot.getVector("id")).setSafe(table.getCounter(),table.getCounter());

                //Speichere Daten mithilfe von Arrow Pojo, dazu wird aus dem Schema der Datentyp geladen
                for(Column column: columns){
                  //Für jede Col soll ein Item aus der Expression im Table mithilfe von vectorSchemaRoot gespeichert werden.
                }
                //Am Ende wir der Table counter erhöht damit an den richtigen Index geschrieben werden kann.
                table.incrementCounter();
            }
        }

    }



    public static boolean isCreateTableStmt(String stmt) throws JSQLParserException {
        return CCJSqlParserUtil.parse(stmt) instanceof CreateTable;

    }

    public static boolean isInsertStmt(String statement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(statement) instanceof Insert;
    }

    /*
     Es wird unterschieden zwischen einem FieldType und einem ArrowDatatype. Dies kann man wie ein Mapping verstehen.
     Aus dem Dump wird ein SQL-Datentyp gelesen. Für das Schema wird ein ArrowFieldType benutzt.
     Um die Daten dann mithilfe von vectorSchemaRoot speichern zu können, benötigen wir die nötige BaseValueVectorKlasse.
     Das heißt wir haben ArrowType und brauchen basierend darauf eine BaseValueVector-Klasse um die Daten in Memory zulegen.
     */
    public static DataType getDataTypeFromFieldType(){
        return null;
    }
}
