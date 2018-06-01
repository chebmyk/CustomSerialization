package com.mein.custom;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MetaDataLoader {
    private static Map<Class, Map<String, Field>> cashClassFields = new ConcurrentHashMap<>();

    public static byte[] serialize(Object o) {
        ByteArray root_clazz = new ByteArray(1000);
        root_clazz.addAll(getClassMetaData(o));
        root_clazz.add(BinaryMarker.MYPROTOCOL.code);
        return root_clazz.getByteArray();
    }

    public static Object deSerialized(byte[] data) {
        // String example of serialization protocol
        //metadata = "☼20▬com.mein.data.Parent▬3▬age▬3▬int▬30▬4▬name▬16▬java.lang.String▬ParentName1▬8▬children▬13▬java.util.Set▬☼19▬com.mein.data.Child▬3▬age▬3▬int▬8▬4▬name▬16▬java.lang.String▬ChildName▬☼☼19▬com.mein.data.Child▬3▬age▬3▬int▬9▬4▬name▬16▬java.lang.String▬ChildName2▬☼☼"
        Object object = null;
        try {
            if (data[data.length - 1] != BinaryMarker.MYPROTOCOL.code) {
                throw new UnsupportedClassVersionError("Usupported format for Object");
            }
            ByteArrayReader reader = new ByteArrayReader(data);
            while (reader.hasNext() && reader.current() != BinaryMarker.MYPROTOCOL.code) {
                object = readClass(reader);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return object;
    }

    private static byte[] getClassMetaData(Object o) {
        ByteArray clazz = new ByteArray(500);
        clazz.add(BinaryMarker.CLASS_START.code);
        try {
            if (o != null) {
                addDescription(clazz, o.getClass().getName());
                Collection<Field> fields = cashClassFields.computeIfAbsent(o.getClass(), MetaDataLoader::getFields).values(); //o.getClass().getDeclaredFields();
                clazz.addAll(ByteArrayUtils.toByta(fields.size()));            //fields count
                for (Field f : fields) {
                    f.setAccessible(true);
                    clazz.add(BinaryMarker.FIELD_START.code);
                    clazz.addAll(getFieldDescription(f));
                    clazz.addAll(objectMetadata(f.get(o), f.getType()));
                    clazz.add(BinaryMarker.FIELD_END.code);
                }
            } else {
                clazz.add(BinaryMarker.TYPE_NULL.code);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Class metadata:" + e);
        }
        clazz.add(BinaryMarker.CLASS_END.code);
        return clazz.getByteArray();
    }

    private static byte[] objectMetadata(Object obj, Class cl) {
        ByteArray clazz = new ByteArray(500);
        if (obj != null) {
            if (cl.isPrimitive() || obj instanceof String) {  ///////////PrimitiveType/////////////
                clazz.add(BinaryMarker.TYPE_PRIMITIVE.code);
                clazz.addAll(getPrimitiveFieldValue(cl, obj));
            } else if (obj.getClass().isEnum()) {
                clazz.add(BinaryMarker.TYPE_ENUM.code);
                clazz.addAll(getPrimitiveFieldValue(String.class, obj.toString()));
            } else if (obj.getClass().isArray()) {            //////////Arrays/////////////////////
                clazz.add(BinaryMarker.TYPE_ARRAY.code);
                int length = Array.getLength(obj);
                clazz.addAll(ByteArrayUtils.toByta(length));
                while (length > 0) {
                    clazz.addAll(objectMetadata(Array.get(obj, length - 1), obj.getClass().getComponentType()));
                    length--;
                }
            } else if (obj instanceof Collection) {          //////Collections////////////////
                clazz.add(BinaryMarker.TYPE_ARRAY.code);
                clazz.addAll(ByteArrayUtils.toByta(((Collection) obj).size()));
                ((Collection) obj).stream().forEach(i -> clazz.addAll(objectMetadata(i, i==null? null:i.getClass())));  //MetaDataLoader.getClassMetaData(i)
            } else {
                clazz.add(BinaryMarker.TYPE_CLASS.code);
                clazz.addAll(ByteArrayUtils.toByta(1));
                clazz.addAll(MetaDataLoader.getClassMetaData(obj));
            }
        } else {
            clazz.add(BinaryMarker.TYPE_CLASS.code);
            clazz.addAll(ByteArrayUtils.toByta(1));
            clazz.addAll(MetaDataLoader.getClassMetaData(null));
        }
        return clazz.getByteArray();
    }

    private static void addDescription(ByteArray ba, String name) {
        byte[] val = ByteArrayUtils.toByta(name);
        ba.addAll(ByteArrayUtils.toByta(val.length));
        ba.addAll(val);
    }

    private static byte[] getFieldDescription(Field f) {
        ByteArray ba = new ByteArray();
        addDescription(ba, f.getName());
        //addDescription(ba, f.getType().getTypeName());
        return ba.getByteArray();
    }

    private static Object readClass(ByteArrayReader reader) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        Object root_object = null;
        if (reader.next() != BinaryMarker.CLASS_START.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        if (reader.current() != BinaryMarker.TYPE_NULL.code) {
            int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
            String clazz_name = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
            Class classDefinition = Class.forName(clazz_name);
            root_object = classDefinition.newInstance();
            int filds_count = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
            //read fields info
            while (filds_count > 0) {
                populateClassField(reader, root_object);
                filds_count--;
            }
        } else {
            if (reader.next() != BinaryMarker.TYPE_NULL.code) {
                throw new UnsupportedClassVersionError("Usupported format for Object");
            }
        }
        if (reader.next() != BinaryMarker.CLASS_END.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        return root_object;
    }

    private static void populateClassField(ByteArrayReader reader, Object root_object) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException {
        if (reader.next() != BinaryMarker.FIELD_START.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        String field_name = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
/*      length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        String field_type = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));*/
        Field field = cashClassFields.computeIfAbsent(root_object.getClass(), MetaDataLoader::getFields).get(field_name);//root_object.getClass().getDeclaredField(field_name);
        field.setAccessible(true);
        //todo read value
        if(field.getType().isPrimitive() || field.getType() == String.class){
            reader.next();
            int len_val = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
            setPrimitiveValue(reader, field, root_object, len_val);
        } else {
            field.set(root_object,readObjectValue(reader, field.get(root_object) == null? field.getType():field.get(root_object).getClass())); // todo check reflex proxy
        }
        if (reader.next() != BinaryMarker.FIELD_END.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
    }

    private static Object readObjectValue(ByteArrayReader reader, Class target_class) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        byte type_marker = reader.next();
        int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        Object value = null;
        if (type_marker == BinaryMarker.TYPE_ENUM.code) {
            value = Enum.valueOf(target_class.asSubclass(Enum.class), ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length)));
        } else if (type_marker == BinaryMarker.TYPE_ARRAY.code) {
            Class classDefinition = Class.forName(target_class.getName());
            if (Collection.class.isAssignableFrom(classDefinition)) {
                value = classDefinition.newInstance();
                int i = 0;
                while (i < length) {
                    ((Collection)value).add(readObjectValue(reader, value.getClass()));  //readObjectValue(reader, value.getClass()) //((Collection)value).add(readClass(reader));
                    i++;
                }
            } else {
                value = Array.newInstance(classDefinition.getComponentType(), length);
                while (length>0) {
                    if (classDefinition.getComponentType().isPrimitive()) {
                        type_marker = reader.next();
                        int val_length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
                        if (int.class == classDefinition.getComponentType()) {
                            Array.setInt(value, length-1,  ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(val_length)));
                        } else if (boolean.class == classDefinition.getComponentType()) {
                            Array.setBoolean(value, length-1,  ByteArrayUtils.toBoolean(reader.getSubArrayFromCurrent(val_length)));
                        } else if (byte.class == classDefinition.getComponentType()) {
                            Array.setByte(value, length-1,  ByteArrayUtils.toByte(reader.getSubArrayFromCurrent(val_length)));
                        } else if (char.class == classDefinition.getComponentType()) {
                            Array.setChar(value, length-1,  ByteArrayUtils.toChar(reader.getSubArrayFromCurrent(val_length)));
                        } else if (double.class == classDefinition.getComponentType()) {
                            Array.setDouble(value, length-1,  ByteArrayUtils.toDouble(reader.getSubArrayFromCurrent(val_length)));
                        } else if (float.class == classDefinition.getComponentType()) {
                            Array.setFloat(value, length-1,  ByteArrayUtils.toFloat(reader.getSubArrayFromCurrent(val_length)));
                        } else if (long.class == classDefinition.getComponentType()) {
                            Array.setLong(value, length-1,  ByteArrayUtils.toLong(reader.getSubArrayFromCurrent(val_length)));
                        } else if (short.class == classDefinition.getComponentType()) {
                            Array.setShort(value, length-1,  ByteArrayUtils.toShort(reader.getSubArrayFromCurrent(val_length)));
                        } else if (String.class == classDefinition.getComponentType()) {    //todo check String arrays
                            Array.set(value, length-1,  ByteArrayUtils.toString(reader.getSubArrayFromCurrent(val_length)));
                        } else {
                            throw new UnsupportedOperationException("type [" + classDefinition.getComponentType() + "] is not supported");
                        }
                    } else {
                       Array.set(value, length-1, readObjectValue(reader, value.getClass()));
                    }
                    length--;
                }
            }
        } else if (type_marker == BinaryMarker.TYPE_CLASS.code) {
            value = readClass(reader);
        }
        return value;
    }

    private static void setPrimitiveValue(ByteArrayReader reader, Field field, Object o, int length) throws IllegalAccessException {
        if (int.class == field.getType()) {
            field.setInt(o, ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(length)));
        } else if (boolean.class == field.getType()) {
            field.setBoolean(o, ByteArrayUtils.toBoolean(reader.getSubArrayFromCurrent(length)));
        } else if (byte.class == field.getType()) {
            field.setByte(o, ByteArrayUtils.toByte(reader.getSubArrayFromCurrent(length)));
        } else if (char.class == field.getType()) {
            field.setChar(o, ByteArrayUtils.toChar(reader.getSubArrayFromCurrent(length)));
        } else if (double.class == field.getType()) {
            field.setDouble(o, ByteArrayUtils.toDouble(reader.getSubArrayFromCurrent(length)));
        } else if (float.class == field.getType()) {
            field.setFloat(o, ByteArrayUtils.toFloat(reader.getSubArrayFromCurrent(length)));
        } else if (long.class == field.getType()) {
            field.setLong(o, ByteArrayUtils.toLong(reader.getSubArrayFromCurrent(length)));
        } else if (short.class == field.getType()) {
            field.setShort(o, ByteArrayUtils.toShort(reader.getSubArrayFromCurrent(length)));
        } else if (String.class == field.getType()) {
            field.set(o, ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length)));
        } else {
            throw new UnsupportedOperationException("type [" + field.getType() + "] is not supported");
        }
    }

    private static byte[] getPrimitiveFieldValue(Class clazz, Object o) {
        ByteArray ba = new ByteArray();
        byte[] result;
        if (clazz == int.class) {
            result = ByteArrayUtils.toByta((int) o);
        } else if (clazz == boolean.class) {
            result = ByteArrayUtils.toByta((boolean) o);
        } else if (clazz == byte.class) {
            result = ByteArrayUtils.toByta((byte) o);
        } else if (clazz == char.class) {
            result = ByteArrayUtils.toByta((char) o);
        } else if (clazz == double.class) {
            result = ByteArrayUtils.toByta((double) o);
        } else if (clazz == float.class) {
            result = ByteArrayUtils.toByta((float) o);
        } else if (clazz == long.class) {
            result = ByteArrayUtils.toByta((long) o);
        } else if (clazz == short.class) {
            result = ByteArrayUtils.toByta((short) o);
        } else if (clazz == String.class) {
            result = ByteArrayUtils.toByta(o.toString());
        } else {
            throw new UnsupportedOperationException("type [" + clazz.getName() + "] is not supported");
        }
        ba.addAll(ByteArrayUtils.toByta(result.length));
        ba.addAll(result);
        return ba.getByteArray();
    }

    private static Map<String, Field> getFields(Class clazz) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : Arrays.stream(clazz.getDeclaredFields()).filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())).collect(Collectors.toCollection(ArrayList::new))) {
            map.put(field.getName(), field);
        }
        return Collections.unmodifiableMap(map);
    }
}
