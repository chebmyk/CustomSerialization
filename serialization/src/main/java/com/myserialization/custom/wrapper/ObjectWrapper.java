package com.myserialization.custom.wrapper;

public interface ObjectWrapper {

    Object readObject();

    ObjectWrapper writeObject(Object o);

}
