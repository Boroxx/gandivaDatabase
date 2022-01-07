<div align="center">
  <img src="https://coconucos.cs.hhu.de/lehre/bigdata/resources/img/hhu-logo.svg" width=300>

  [![Download](https://img.shields.io/static/v1?label=&message=pdf&color=EE3F24&style=for-the-badge&logo=adobe-acrobat-reader&logoColor=FFFFFF)](/../-/jobs/artifacts/master/file/document/thesis.pdf?job=latex)
</div>

# :notebook: &nbsp; Aufgabenbeschreibung

## Anlegen und filtern von Daten in einer Datenbankanwendung mithilfe von Arrow/Gandiva

In der Bachelorarbeit soll eine In-Memory Datenbankanwendung basierend auf Arrow-Buffern implementiert werden. 
Hierzu bildet die Library “Arrow” von Apache die Grundbasis der Anwendung. Arrow definiert ein Sprache zur Prozessierung und effizientem Arbeiten in einem Spalten-Memory-Format. Gefiltert werden die Daten mithilfe von Gandiva, durch zugeordnete Expressions.

### Client:
Grundsätzlich soll auch ein Client in Form einer Konsolenanwendung implementiert werden, welcher an die Datenbankanwendung ein SQL-Statement per Rest-API senden kann.
Die Rest-API soll mit Spring Boot und die Clientanwendung mithilfe von Spring Shell umgesetzt werden.

### Datenbankanwendung:
Für die Bachelor-Arbeit sehe ich folgende Funktionen in der Datenbank notwendig:
Importieren eines Datensatzes (File) 
Abfrage von Daten mithilfe von SELECT-Statement 
Abfrage von Daten mithilfe von Filtern (WHERE)
Einfügen von Daten mithilfe von INSERT-Statement
Einfügen von Daten mithilfe von Filtern

### Workflow: 
Nachdem ein SQL-Statement vom Client an die Datenbankanwendung geschickt wird, soll dieses in der Anwendung durch einen SQL-Parser prozessiert werden und je nach Anfrageart reagiert werden. Hier sind viele Möglichkeiten offen, da mithilfe von Gandiva, die verschiedenen Arrow-Buffer, welche in der Datenbankanwendung liegen, gefiltert werden können. Ebenso lassen sich verschiedene Funktionen auf die Daten anwenden.

Als Antwort an den Client, sollen  die Virtuelle Memory-Adresse und die verschiedenen Offsets für die angefragten Daten zurückgeliefert werden. Ebenfalls sollte der Datentyp zurückgegeben werden.


### Zukünftige Möglichkeit der Anwendung für das Institut:
Durch die Rückgabe des Datentyps, der Memory-Adressen und der Indices, kann der Client per RDMA auf die Daten in der Datenbank zugreifen und so performanter an die Daten gelangen. 

