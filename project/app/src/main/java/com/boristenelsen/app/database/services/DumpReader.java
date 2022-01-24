package com.boristenelsen.app.database.services;

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

    public DumpReader(File dumpFile){
        this.dumpFile = dumpFile;
    }



    public void initDatabase() throws IOException, JSQLParserException {
        Scanner scanner = new Scanner(dumpFile);
        scanner.useDelimiter(";");
        while(scanner.hasNext()){
            String statement = scanner.next();
            System.out.println(statement);
            System.out.println("New Loop");
            //Ueberpruefe statement auf Create Table Statement.
            if(SqlUtil.isCreateTableStmt(statement)){
                this.dumpSchema = SqlUtil.parseCreateTableStmt(statement);
                break;
            }
            if(SqlUtil.isInsertStmt(statement)){

            }


        }
    }


}
