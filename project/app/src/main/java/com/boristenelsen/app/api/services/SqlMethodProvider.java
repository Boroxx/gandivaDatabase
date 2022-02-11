package com.boristenelsen.app.api.services;

import com.boristenelsen.app.api.model.IndexResponse;
import com.boristenelsen.app.database.models.Table;
import org.springframework.stereotype.Service;

@Service
public class SqlMethodProvider {


    //Deckt ein SELECT * FROM Table ab
    public IndexResponse selectEverything(Table table){
        return null;
    }
    //Deckt ein SELECT * FROM Table WHERE ab. -> GandivaService
    public IndexResponse selectCondtion(){
        return null;
    }

    //Deckt ein SELECT Spalte FROM Table ab
    public IndexResponse selectCol(){
        return null;
    }
}
