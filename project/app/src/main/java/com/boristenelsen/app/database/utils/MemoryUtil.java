package com.boristenelsen.app.database.utils;

import com.google.common.primitives.Longs;
import org.apache.arrow.gandiva.evaluator.SelectionVector;


public class MemoryUtil {

    public static void printLongAsAdress(Long address){
        System.out.println(Long.toHexString(address));

    }

    public static int[] selectionVectorToArray(SelectionVector vector) {
        int[] actual = new int[vector.getRecordCount()];
        for (int i = 0; i < vector.getRecordCount(); ++i ) {
            actual[i] = vector.getIndex(i);
        }
        return actual;
    }
}
