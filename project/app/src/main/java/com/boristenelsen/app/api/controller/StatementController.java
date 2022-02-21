package com.boristenelsen.app.api.controller;

import com.boristenelsen.app.api.model.IndexResponse;
import com.boristenelsen.app.api.model.Statement;
import com.boristenelsen.app.api.services.SqlMethodProvider;
import com.boristenelsen.app.database.services.DumpReader;
import net.sf.jsqlparser.JSQLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
public class StatementController {
    //Erstellt einen Table nach Init
    DumpReader dumpReader;

    @Autowired
    SqlMethodProvider sqlMethodProvider;

    @GetMapping("/initDatabase")
    public ResponseEntity<String> initDatabase() throws FileNotFoundException {
        //Initialisiere Datenbank mit Dump. Dump liegt vorerst an vorbestimmter Stelle. Hier macht es keinen Sinn einen Dump über Rest zu senden, da in der Praxis der Dump zu groß ist.
        File file = ResourceUtils.getFile("classpath:dump.sql");
        dumpReader = new DumpReader(file);
        try {
            dumpReader.initDatabase();
        } catch (IOException e) {
            return new ResponseEntity<String>("Fehler beim lesen des Files",HttpStatus.BAD_REQUEST);
        } catch (JSQLParserException e) {
            e.printStackTrace();
            return new ResponseEntity<String>("JSQLParser konnte Dump nicht erfolgreich parsen",HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("Datenbank wurde erfolgreich initialisiert",HttpStatus.CREATED);
    }
    @PostMapping("/statement")
    public ResponseEntity<IndexResponse> sendStatement(@RequestBody Statement statement) throws JSQLParserException {

        //Prüfe ob überhaupt ein Table-Objekt initiliasiert wurde ansonsten return Error
        //parse Statement through SqlMethodPicker
        IndexResponse  indexResponse = sqlMethodProvider.checkStatementToMethod(statement,dumpReader.getTable());
        return new ResponseEntity<IndexResponse>(indexResponse, HttpStatus.OK);
    }
}
