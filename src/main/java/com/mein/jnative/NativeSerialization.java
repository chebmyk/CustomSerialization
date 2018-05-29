package com.mein.jnative;

import java.io.*;

public class NativeSerialization {

    static final String filename = "native_output.txt";

    public static void writeObject(Object obj){
        try(ObjectOutputStream objOutput = new ObjectOutputStream(new FileOutputStream(filename)) ){
            objOutput.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readObject(){
        try(ObjectInputStream objInput = new ObjectInputStream(new FileInputStream(filename))) {
            return objInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
