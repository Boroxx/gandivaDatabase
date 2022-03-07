package com.boristenelsen.app.api.services;

import com.boristenelsen.app.database.models.Table;
import org.apache.arrow.gandiva.evaluator.Filter;
import org.apache.arrow.gandiva.evaluator.SelectionVectorInt32;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;

public interface IGandivaProvider {
    public Filter lessThan_NumberFilter(Table table, String columnName, int value);
    public Filter greaterThan_NumberFilter(Table table,String columnName,int value);
    public Filter equalsTo_NumberFilter(Table table,String columnName,int value);
    public ArrowRecordBatch createBatch(Table table, String colName);
    public SelectionVectorInt32 createSelectionVector(Table table);
}
