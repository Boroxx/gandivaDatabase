package com.boristenelsen.app.database.utils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import java.util.ArrayList;
import java.util.List;

public class SqlUtil {

    // Parse SQL-Statement in ein Schema welches für Apache Arrow zugeschnitten ist. Dieses Schema wird später für die Klasse VectorSchemaRoot benötigt.
    public static Schema parseCreateTableStmt(String stmt) throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(stmt);
        CreateTable ct = (CreateTable)parse;
        System.out.println("table=" + ct.getTable().getFullyQualifiedName());

        List<Field> fieldList = new ArrayList<>();

        for (ColumnDefinition colDef : ct.getColumnDefinitions()) {
            System.out.println("column=" + colDef.getColumnName() + " " + colDef.getColDataType() + " ");
            try {
                Field parsedField = prepareField(colDef.getColumnName(),colDef.getColDataType().toString());
                fieldList.add(parsedField);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return new Schema(fieldList);


    }

    public static Field prepareField(String colName, String colDataType) throws Exception {
        String [] colType = colDataType.split(" ");
        switch (colType[0]){
            case "char":
                return new Field(colName, FieldType.nullable(new ArrowType.Utf8()),null);
            case "varchar":
                return new Field(colName, FieldType.nullable(new ArrowType.Utf8()),null);
            case "mediumint":
                return new Field(colName,FieldType.nullable(new ArrowType.Int(32,false)),null);

        }
        throw new Exception();
    }

    public static boolean isCreateTableStmt(String stmt) throws JSQLParserException {
        return CCJSqlParserUtil.parse(stmt) instanceof CreateTable;

    }

    public static boolean isInsertStmt(String statement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(statement) instanceof Insert;
    }
}
