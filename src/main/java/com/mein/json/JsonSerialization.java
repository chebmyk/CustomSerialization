package com.mein.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonSerialization {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static String filename = "jsonmapper_output.json";

    public static void writeJsonObject(Object obj){
        try {
            objectMapper.writeValue(new File(filename), obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Object readJsonObject(Class objClass){
        try {
            return objectMapper.readValue(new File(filename), objClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
