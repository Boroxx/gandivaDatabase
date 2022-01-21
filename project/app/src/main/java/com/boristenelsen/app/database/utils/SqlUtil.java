package com.boristenelsen.app.database.utils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.arrow.vector.types.pojo.Schema;

public class SqlUtil {

    // Sql-Statement wird aufgesplitted um Schema aus SqlDump für Arrow ValueVector zu erstellen.
    public static Schema parseCreateTableStmt(String stmt) throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(stmt);
        CreateTable ct = (CreateTable)parse;
        System.out.println("table=" + ct.getTable().getFullyQualifiedName());
        for (ColumnDefinition colDef : ct.getColumnDefinitions()) {
            System.out.println("column=" + colDef.getColumnName() + " " + colDef.getColDataType() + " ");
        }
        return null;


    }

    public static boolean isCreateTableStmt(String stmt) throws JSQLParserException {
        return CCJSqlParserUtil.parse(stmt) instanceof CreateTable;

    }
}
