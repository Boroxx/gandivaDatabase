package com.boristenelsen.app.database.services;

import com.boristenelsen.app.database.models.Table;
import com.boristenelsen.app.database.utils.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.arrow.vector.types.pojo.Schema;

import java.io.*;
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
            if(SqlUtil.isCreateTableStmt(statement)){
                table = SqlUtil.parseCreateTableStmt(statement);
            }
            //Fülle für jeden Insert SQL-Dump
            if(SqlUtil.isInsertStmt(statement)){
                try {
                    SqlUtil.prepareDataForMemory(statement,table);
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


}
