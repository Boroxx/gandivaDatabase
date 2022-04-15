package com.boristenelsen.app.api.controller;

import com.boristenelsen.app.api.model.IndexResponse;
import com.boristenelsen.app.api.model.Statement;
import com.boristenelsen.app.api.services.SqlMethodProvider;
import com.boristenelsen.app.database.services.DumpReader;
import com.google.common.base.Stopwatch;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.arrow.gandiva.exceptions.GandivaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StatementController {
    //Erstellt einen Table nach Init
    DumpReader dumpReader;

    @Autowired
    SqlMethodProvider sqlMethodProvider;

    @Value("${db.root}")
    public String dumpFolder;

    public int dumpID;

    public List<File> dumpFiles;

    @GetMapping("/initDatabase")
    public ResponseEntity<String> initDatabase() throws FileNotFoundException {
        //Initialisiere Datenbank mit Dump. Dump liegt vorerst an vorbestimmter Stelle. Hier macht es keinen Sinn einen Dump über Rest zu senden, da in der Praxis der Dump zu groß ist.
        File file = new File(this.dumpFiles.get(this.dumpID).getAbsolutePath());
        String timer= "";
        dumpReader = new DumpReader(file);
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            dumpReader.initDatabase();
            timer = stopwatch.stop().toString();
            System.out.println("Init-Databasetime: " + timer);
        } catch (IOException e) {
            return new ResponseEntity<String>("Fehler beim lesen des Files",HttpStatus.BAD_REQUEST);
        } catch (JSQLParserException e) {
            e.printStackTrace();
            return new ResponseEntity<String>("JSQLParser konnte Dump nicht erfolgreich parsen",HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("Datenbank wurde erfolgreich initialisiert. " + timer,HttpStatus.CREATED);
    }
    @PostMapping("/statement")
    public ResponseEntity<IndexResponse> sendStatement( @RequestBody Statement statement) throws JSQLParserException, GandivaException {

        System.out.println(statement.getStatement());
        //Prüfe ob überhaupt ein Table-Objekt initiliasiert wurde ansonsten return Error
        //parse Statement through SqlMethodPicker
        Stopwatch stopwatch = Stopwatch.createStarted();
        IndexResponse  indexResponse = sqlMethodProvider.checkStatementToMethod(statement,dumpReader.getTable());
        System.out.println("Time: " + stopwatch.stop());
        return new ResponseEntity<>(indexResponse, HttpStatus.OK);
    }

    @GetMapping("/information")
    public ResponseEntity<String> tableInformation() throws FileNotFoundException {
        if(dumpReader==null){
            return new ResponseEntity<String>("Es wurde keinen Datenbank initialisiert.", HttpStatus.FAILED_DEPENDENCY);
        }
        return  new ResponseEntity<String>(dumpReader.getTable().getInfo(), HttpStatus.OK);
    }

    @GetMapping("/listDumps")
    public ResponseEntity<String> listDumps(){
        String dumpList="";
        this.dumpFiles = getDumpList();
        if(this.dumpFiles ==null){
            return new ResponseEntity<String>("Kein Dumpfile gefunden.", HttpStatus.BAD_REQUEST);

        }
        for(int i= 0; i< this.dumpFiles .size();i++){
            dumpList += i + " " + this.dumpFiles .get(i).getName() + "\r\n";
        }

        return new ResponseEntity<String>(dumpList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> chooseDump(@PathVariable String id){
        int dumpIDtemp = Integer.parseInt(id);
        if(this.dumpFiles ==null){
            return new ResponseEntity<>("SQL-Dump konnte nicht gefunden werden", HttpStatus.BAD_REQUEST);
        }

        //waehle Dump aus zu von Client aus gesendeter ID
        if(dumpIDtemp>=0 && dumpIDtemp < this.dumpFiles.size()){
            this.dumpID = dumpIDtemp;
        }else{
            return new ResponseEntity<>("Dump-ID ist nicht vorhanden.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Dump: " + this.dumpFiles.get(this.dumpID) + " wurde ausgewählt.", HttpStatus.OK);

    }

    private List<File> getDumpList(){
        File file = new File(dumpFolder);
        File [] dumpArray;
        dumpArray =  file.listFiles();
        if(dumpArray==null){
            return null;
        }
        List<File> dumpList = new ArrayList<>();
        for(File dumpFile : dumpArray){
            if(dumpFile.getName().contains(".sql")){
                dumpList.add(dumpFile);
            }
        }
        return dumpList;

    }


}
