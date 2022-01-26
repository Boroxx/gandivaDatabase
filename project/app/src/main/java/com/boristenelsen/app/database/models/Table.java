package com.boristenelsen.app.database.models;

import com.boristenelsen.app.enums.DataType;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.types.pojo.Schema;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public int counter;
    public RootAllocator allocator;
    public VectorSchemaRoot vectorSchemaRoot;
    List<DataType> schemaDictionary;

    public Table(Schema schema) {
        allocator = new RootAllocator(Long.MAX_VALUE);
        vectorSchemaRoot = VectorSchemaRoot.create(schema, allocator);
        counter = 0;
        schemaDictionary = new ArrayList<>();
    }


    public int getCounter() {
        return counter;
    }

    public void incrementCounter(){
        this.counter++;
    }
}
