package com.myserialization.custom.wrapper;

public class WrapperFactory {
    public static Object getWrapper(Object o){
        if ( o instanceof Integer) {
            return new IntegerWrapper().writeObject(o);
        } else {
            if(ObjectWrapper.class.isAssignableFrom(o.getClass())){
                return ((ObjectWrapper)o).writeObject(o);
            } else {
                return o;
            }
        }
    }
}
