package com.boristenelsen.app.api.services;

import com.boristenelsen.app.api.model.IndexResponse;
import com.boristenelsen.app.api.model.Statement;
import com.boristenelsen.app.database.models.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.arrow.vector.FieldVector;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SqlMethodProvider {

    public IndexResponse checkStatementToMethod(Statement statement,Table table) throws JSQLParserException {
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

            //Wenn SelectStatement ein * enthält (AllColumns)
            if(firstItem instanceof AllColumns){
                return selectEverything(table);
            }else if(firstItem instanceof SelectExpressionItem){
                List<String> columnNames = selectItemList.stream().map(SelectItem::toString).collect(Collectors.toList());
                return selectCol(columnNames,table);
            }

            //Wenn SelectStatement Spalten enthält
        }
        return null;
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
}
