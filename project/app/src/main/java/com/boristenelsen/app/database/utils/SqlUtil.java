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
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {



    public static void isSelectEverythingStmt(String statement){

    }

    public static void isSelectConditionStmt(String statement){

    }


    /*
     Es wird unterschieden zwischen einem FieldType und einem ArrowDatatype. Dies kann man wie ein Mapping verstehen.
     Aus dem Dump wird ein SQL-Datentyp gelesen. Für das Schema wird ein ArrowFieldType benutzt.
     Um die Daten dann mithilfe von vectorSchemaRoot speichern zu können, benötigen wir die nötige BaseValueVectorKlasse.
     Das heißt wir haben ArrowType und brauchen basierend darauf eine BaseValueVector-Klasse um die Daten in Memory zulegen.
     */
    public static Class getDataTypeFromFieldType(FieldVector fieldVector) throws Exception {
        if(fieldVector.getField().getType().toString().startsWith("Int")){
            return IntVector.class;
        }
        else if(fieldVector.getField().getType().toString().startsWith("Varchar")){
            return VarCharVector.class;
        }
        else throw new Exception();

    }
}
