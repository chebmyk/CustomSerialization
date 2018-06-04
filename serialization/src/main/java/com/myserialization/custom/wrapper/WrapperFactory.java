package com.myserialization.custom.wrapper;

import java.time.LocalDate;

public class WrapperFactory {
    public static Object getWrapper(Object o) {
        if (o instanceof Integer) {
            return new IntegerWrapper().writeObject(o);
        } else if (o instanceof Byte) {
            return new ByteWrapper().writeObject(o);
        } else if (o instanceof Short) {
            return new ShortWrapper().writeObject(o);
        } else if (o instanceof Long) {
            return new LongWrapper().writeObject(o);
        } else if (o instanceof Double) {
            return new DoubleWrapper().writeObject(o);
        } else if (o instanceof Character) {
            return new CharWrapper().writeObject(o);
        } else if (o instanceof Float) {
            return new FloatWrapper().writeObject(o);
        } else if (o instanceof Boolean) {
            return new BooleanWrapper().writeObject(o);
        } else if (o instanceof LocalDate) {
            return new LocalDateWrapper().writeObject(o);
        } else {
            if (ObjectWrapper.class.isAssignableFrom(o.getClass())) {
                return ((ObjectWrapper) o).writeObject(o);
            } else {
                return o;
            }
        }
    }
}
