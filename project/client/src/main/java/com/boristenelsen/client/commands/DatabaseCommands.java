package com.boristenelsen.client.commands;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;

@ShellComponent
public class DatabaseCommands {

    @Value("${db.port}")
    public String port;
    @Value("${db.host}")
    public String host;


    public boolean StatementBlocker = true;
    @ShellMethod("Informationen ueber den Table der Datenbank.")
    public String tableInfo(){
        String reqURL = "http://"+host+":"+port;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(reqURL+"/information");
        String response;
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String jsonString = EntityUtils.toString(httpResponse.getEntity());
            return jsonString;
        } catch (IOException e) {
            response= e.toString();
        }
        return response;
    }

    @ShellMethod("Initialisieren der Datenbank")
    public String initDatabase(){
        String reqURL = "http://"+host+":"+port;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(reqURL+"/initDatabase");
        String response;
        try {
           HttpResponse httpResponse = httpClient.execute(httpGet);
           response = httpResponse.toString();
           this.StatementBlocker = false;
        } catch (IOException e) {
           response= e.toString();
        }
        return response;
    }

    @ShellMethod("Liste aller auf dem Server liegenden Dumps.")
    public String listDumps(){
        System.out.println("Host: " + host + " port: " + port);
        return "Table information";
    }

    @ShellMethod("Sende SQL Statement")
    public String send(@ShellOption String statement){
        String reqURL = "http://"+host+":"+port;
        if(this.StatementBlocker){
            return "Datenbank muss zuerst initiliasiert werden.";
        }
        HttpClient httpClient = HttpClientBuilder.create().build();

        JSONObject json = new JSONObject();
        json.put("statement",statement);

        String response;
        try {

            HttpPost httpPost = new HttpPost(reqURL+"/statement");
            System.out.println(json.toString());
            StringEntity params = new StringEntity(json.toString());
            httpPost.setHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json");
            httpPost.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(httpPost);

            String jsonString = EntityUtils.toString(httpResponse.getEntity());

            return jsonString;

        } catch (IOException e) {
            response= e.toString();
        }
        return response;
    }
}
