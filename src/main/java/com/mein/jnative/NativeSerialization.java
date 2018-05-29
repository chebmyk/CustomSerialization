package com.mein.jnative;

import java.io.*;

public class NativeSerialization {

    public static byte[] writeObject(Object obj){
        byte[] result =null;
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream objOutput = new ObjectOutputStream(bos) ){
            objOutput.writeObject(obj);
            objOutput.flush();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Object readObject(byte[] input){
        try(ByteArrayInputStream bis = new ByteArrayInputStream(input) ; ObjectInputStream objInput = new ObjectInputStream(bis)) {
            return objInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
