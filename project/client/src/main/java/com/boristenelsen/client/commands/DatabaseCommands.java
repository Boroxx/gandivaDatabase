package com.boristenelsen.client.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class DatabaseCommands {

    @ShellMethod("Informationen ueber den Table der Datenbank.")
    public String tableInfo(){
        return "Table information";
    }
}
