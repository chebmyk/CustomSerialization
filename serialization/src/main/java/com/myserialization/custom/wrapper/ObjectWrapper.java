package com.myserialization.custom.wrapper;

public interface ObjectWrapper <T> {

    T readObject();

    ObjectWrapper writeObject(T o);

}
