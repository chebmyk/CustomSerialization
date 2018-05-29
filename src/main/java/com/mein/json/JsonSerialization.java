package com.mein.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonSerialization {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static String filename = "jsonmapper_output.json";

    public static byte[] writeJsonObject(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(obj);
    }


    public static Object readJsonObject(byte[] input ,Class objClass){
        try {
            return objectMapper.readValue(input, objClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
