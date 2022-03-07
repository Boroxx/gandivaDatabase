package com.boristenelsen.app.database.models;

import com.boristenelsen.app.database.utils.MemoryUtil;
import com.boristenelsen.app.enums.DataType;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public int counter;
    public RootAllocator allocator;
    public VectorSchemaRoot vectorSchemaRoot;
    public List<Field> fieldList;
    List<DataType> schemaDictionary;

    public Table(){

    }
    public Table(Schema schema, List<Field>fieldList) {
        allocator = new RootAllocator(Long.MAX_VALUE);
        vectorSchemaRoot = VectorSchemaRoot.create(schema, allocator);
        counter = 0;
        schemaDictionary = new ArrayList<>();
        this.fieldList= fieldList;
    }


    public int getCounter() {
        return counter;
    }

    public void incrementCounter(){
        this.counter++;
    }

    public Field getFieldByName(String colName){
        for(Field field : this.fieldList){
            if(field.getName().equals(colName)){
                return field;
            }
        }
        return null;
    }
    public String getMemoryadress(String colName){
        for(FieldVector fieldVector : vectorSchemaRoot.getFieldVectors()){
            if(fieldVector.getName().equals(colName)){
                return Long.toHexString(fieldVector.getDataBufferAddress());
            }
        }
        return null;
    }

    public String getTypeByName(String colName){
        for(Field field : this.fieldList){
            if(field.getName().equals(colName)){
                return field.getType().toString();
            }
        }
        return null;
    }

    public int getRowSize(){
        return counter;
    }


}
