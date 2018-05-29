package com.mein.custom;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class MetaDataLoader {

    public static byte[] serialize(Object o){
        ByteArray root_clazz = new ByteArray();
        root_clazz.addAll(getClassMetaData(o));
        root_clazz.add(BinaryMarker.MYPROTOCOL.code);
        return root_clazz.getByteArray();
    }

    private static byte[] getClassMetaData(Object o){
        ByteArray clazz = new ByteArray(1000);
        clazz.add(BinaryMarker.CLASS_START.code);
        Field[] fields = new Field[0];
        try {
            if(o != null) {
                clazz.addAll(getDescription(o.getClass().getName()));
                if(o.getClass().isPrimitive() || o instanceof String){  ///todo
                    byte[] field_value = getFieldValue(o.getClass(), o);
                    clazz.addAll(ByteArrayUtils.toByta(field_value.length));
                    clazz.addAll(field_value);
                }else {
                    fields = o.getClass().getDeclaredFields();
                    clazz.addAll(ByteArrayUtils.toByta(fields.length));            //fields count
                }
            } else {
                clazz.add(BinaryMarker.TYPE_NULL.code);
            }
            for (Field f : fields) {

                f.setAccessible(true);
                clazz.add(BinaryMarker.FIELD_START.code);
                if (f.getType().isPrimitive()) {  ///////////////////////////////Pimitive type///////////////
                    clazz.addAll(getDescription(f.getName()));
                    clazz.addAll(getDescription(f.getType().getTypeName()));
                    clazz.add(BinaryMarker.TYPE_PRIMITIVE.code);
                    byte[] field_value = getFieldValue(f.getType(), f.get(o));
                    clazz.addAll(ByteArrayUtils.toByta(field_value.length));
                    clazz.addAll(field_value);
                } else if (f.getType().isArray()) {                  ////////////Arrays/////////////////////
                    clazz.addAll(getDescription(f.getName()));
                    clazz.addAll(getDescription(f.getType().getName()));
                    clazz.add(BinaryMarker.TYPE_ARRAY.code);
                    int length = Array.getLength(f.get(o));
                    clazz.addAll(ByteArrayUtils.toByta(length));
                    while(length>0){
                        clazz.addAll(MetaDataLoader.getClassMetaData(Array.get(f.get(o),length-1))); //todo
                        length--;
                    }
                } else if (f.get(o) instanceof Collection) {               //////Collections/////////////////
                    clazz.addAll(getDescription(f.getName()));
                    clazz.addAll(getDescription(f.getType().getTypeName()));
                    clazz.add(BinaryMarker.TYPE_ARRAY.code);
                    clazz.addAll(ByteArrayUtils.toByta(((Collection) f.get(o)).size()));
                    ((Collection) f.get(o)).stream().forEach(i -> clazz.addAll(MetaDataLoader.getClassMetaData(i)));
                } else if (f.get(o) instanceof String) {                   //////String/////////////////////
                    clazz.addAll(getDescription(f.getName()));
                    clazz.addAll(getDescription(f.getType().getTypeName()));
                    clazz.add(BinaryMarker.TYPE_PRIMITIVE.code);
                    byte[] value = getFieldValue(f.getType(), f.get(o));
                    clazz.addAll(ByteArrayUtils.toByta(value.length));
                    clazz.addAll(ByteArrayUtils.toByta(value));
                }
                else {
                    clazz.addAll(getDescription(f.getName()));
                    clazz.addAll(getDescription(f.getType().getTypeName()));
                    clazz.add(BinaryMarker.TYPE_CLASS.code);
                    clazz.addAll(ByteArrayUtils.toByta(1));
                    clazz.addAll(MetaDataLoader.getClassMetaData(f.get(o)));
                }
                clazz.add(BinaryMarker.FIELD_END.code);

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        clazz.add(BinaryMarker.CLASS_END.code);
        return clazz.getByteArray();
    }

    public static Object deSerialized(byte[] data) {
        //String meta = "☼20▬com.mein.data.Parent▬3▬age▬3▬int▬30▬4▬name▬16▬java.lang.String▬ParentName1▬8▬children▬13▬interface java.util.Set▬☼19▬com.mein.data.Child▬3▬age▬3▬int▬8▬4▬name▬16▬java.lang.String▬ChildName▬☼☼19▬com.mein.data.Child▬3▬age▬3▬int▬9▬4▬name▬16▬java.lang.String▬ChildName2▬☼☼"
        Object object = null;
        try {
            if(data[data.length-1] != BinaryMarker.MYPROTOCOL.code){
                throw new UnsupportedClassVersionError("Usupported format for Object");
            }
            ByteArrayReader reader = new ByteArrayReader(data);
            while (reader.hasNext() && reader.current()!= BinaryMarker.MYPROTOCOL.code) {
                object = readClass(reader);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return object;
    }

    private static byte[] getFieldValue(Class clazz, Object o) throws IllegalAccessException {
        byte[] result;
        if (clazz == int.class) {
            result = ByteArrayUtils.toByta((int)o);
        } else if (clazz== boolean.class){
            result =  ByteArrayUtils.toByta((boolean)o);
        } else if (clazz == byte.class){
            result =  ByteArrayUtils.toByta((byte)o);
        } else if (clazz == char.class){
            result =  ByteArrayUtils.toByta((char)o);
        } else if (clazz == double.class){
            result =  ByteArrayUtils.toByta((double)o);
        } else if (clazz == float.class){
            result =  ByteArrayUtils.toByta((float)o);
        } else if (clazz == long.class){
            result =  ByteArrayUtils.toByta((long)o);
        } else if (clazz == short.class){
            result =  ByteArrayUtils.toByta((short)o);
        } else if (clazz == String.class){
            result =  ByteArrayUtils.toByta(o.toString());
        } else {
            throw new UnsupportedOperationException("Field type [" + clazz.getName() + "] is not supported");
        }
        return result;
    }

    private static byte[] getDescription(String name) {
        ByteArray ba = new ByteArray();
        byte[] val = ByteArrayUtils.toByta(name);
        ba.addAll(ByteArrayUtils.toByta(val.length));
        ba.addAll(val);
        return ba.getByteArray();
    }


    private static Object readClass(ByteArrayReader reader) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        Object root_object = null;

        if(reader.next() != BinaryMarker.CLASS_START.code){
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }

        if(reader.current()!=BinaryMarker.TYPE_NULL.code) {
            int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
            String clazz_name = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));

            Class classDefinition = Class.forName(clazz_name);
            root_object = classDefinition.newInstance();

            if(root_object.getClass().isPrimitive() || root_object instanceof String){
                length =  ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
                root_object = readPrimitiveValue(reader,root_object.getClass().getName(),length);
            } else {
                int filds_count = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
                //read fields info
                while (filds_count > 0) {
                    populateClassField(reader, root_object);
                    filds_count--;
                }
            }
        } else {
            if(reader.next() != BinaryMarker.TYPE_NULL.code){
                throw new UnsupportedClassVersionError("Usupported format for Object");
            }
        }

        if(reader.next() != BinaryMarker.CLASS_END.code){
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        return root_object;
    }

    private static void populateClassField (ByteArrayReader reader, Object root_object) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException {
        if(reader.next() != BinaryMarker.FIELD_START.code){
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        int length =  ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        String field_name = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
        length =  ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        String field_type = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
        byte type_marker = reader.next();
        length =  ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));

        Field field = root_object.getClass().getDeclaredField(field_name);
        field.setAccessible(true);

        Object value = null;
        if(type_marker == BinaryMarker.TYPE_ARRAY.code) {
            Class classDefinition = Class.forName(field.get(root_object).getClass().getName());
            if(!field_type.contains("[")) {
                value = classDefinition.newInstance();
            } else {
                value = Array.newInstance(classDefinition.getComponentType(), length);
            }
            int i =0;
            while (i<length){
                Object o = readClass(reader);
                if(value instanceof Collection){
                    ((Collection)value).add(o);
                }
                else {Array.set(value, i, o);}
                i++;
            }
        } else if(type_marker == BinaryMarker.TYPE_PRIMITIVE.code){
            value = readPrimitiveValue(reader,field_type,length);
        }
        if(type_marker ==  BinaryMarker.TYPE_CLASS.code){
            value = readClass(reader);
        }

        field.set(root_object,value); //Setup value to field;

        if(reader.next() != BinaryMarker.FIELD_END.code){
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
    }

    private static Object readPrimitiveValue (ByteArrayReader reader,String field_type,int length){
        Object value = null;

        if ("int".equals(field_type)) {
            value = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(length));
        } else if ("boolean".equals(field_type)){
            value = ByteArrayUtils.toBoolean(reader.getSubArrayFromCurrent(length));
        } else if ("byte".equals(field_type)){
            value = ByteArrayUtils.toByte(reader.getSubArrayFromCurrent(length));
        } else if ("char".equals(field_type)){
            value = ByteArrayUtils.toChar(reader.getSubArrayFromCurrent(length));
        } else if ("double".equals(field_type)){
            value = ByteArrayUtils.toDouble(reader.getSubArrayFromCurrent(length));
        } else if ("float".equals(field_type)){
            value = ByteArrayUtils.toFloat(reader.getSubArrayFromCurrent(length));
        } else if ("long".equals(field_type)){
            value = ByteArrayUtils.toLong(reader.getSubArrayFromCurrent(length));
        } else if ("short".equals(field_type)){
            value = ByteArrayUtils.toShort(reader.getSubArrayFromCurrent(length));
        } else if ("java.lang.String".equals(field_type)){
            value = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
        } else {
            throw new UnsupportedOperationException("Field type [" + field_type + "] is not supported");
        }
        return value;
    }
}
