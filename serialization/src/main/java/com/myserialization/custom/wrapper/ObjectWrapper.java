package com.myserialization.custom.wrapper;

public interface ObjectWrapper <E> {

    Object readObject();

    ObjectWrapper writeObject(Object o);

}
