package com.mein.custom;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        ByteArray clazz = new ByteArray(1000);
        clazz.add(BinaryMarker.CLASS_START.code);
        try {
            if (o != null) {
                addDescription(clazz, o.getClass().getName());
                Collection<Field> fields = cashClassFields.computeIfAbsent(o.getClass(), MetaDataLoader::getFields).values(); //o.getClass().getDeclaredFields();
                clazz.addAll(ByteArrayUtils.toByta(fields.size()));            //fields count
                for (Field f : fields) {
                    f.setAccessible(true);
                    clazz.add(BinaryMarker.FIELD_START.code);
                    if (f.getType().isPrimitive()) {  //////////////////////////////Pimitive type///////////////
                        clazz.addAll(getFieldDescription(f, BinaryMarker.TYPE_PRIMITIVE));
                        clazz.addAll(getPrimitiveFieldValue(f.getType(), f.get(o)));
                    } else if (f.getType().isEnum()) {
                        clazz.addAll(getFieldDescription(f, BinaryMarker.TYPE_ENUM));
                        clazz.addAll(getPrimitiveFieldValue(String.class, f.get(o).toString()));
                    } else if (f.getType().isArray()) {                  ////////////Arrays/////////////////////
                        clazz.addAll(getFieldDescription(f, BinaryMarker.TYPE_ARRAY));
                        int length = Array.getLength(f.get(o));
                        clazz.addAll(ByteArrayUtils.toByta(length));
                        while (length > 0) {
                            if (f.get(o).getClass().getComponentType().isPrimitive()){
                                clazz.addAll(getPrimitiveFieldValue(f.get(o).getClass().getComponentType(), Array.get(f.get(o), length - 1)));
                            } else {
                                clazz.addAll(MetaDataLoader.getClassMetaData(Array.get(f.get(o), length - 1))); //todo
                            }
                            length--;
                        }
                    } else if (f.get(o) instanceof Collection) {               //////Collections/////////////////
                        clazz.addAll(getFieldDescription(f, BinaryMarker.TYPE_ARRAY));
                        clazz.addAll(ByteArrayUtils.toByta(((Collection) f.get(o)).size()));
                        ((Collection) f.get(o)).stream().forEach(i -> clazz.addAll(MetaDataLoader.getClassMetaData(i)));
                    } else if (f.get(o) instanceof String) {                   //////String/////////////////////
                        clazz.addAll(getFieldDescription(f, BinaryMarker.TYPE_PRIMITIVE));
                        clazz.addAll(getPrimitiveFieldValue(f.getType(), f.get(o)));
                    } else {
                        clazz.addAll(getFieldDescription(f, BinaryMarker.TYPE_CLASS));
                        clazz.addAll(ByteArrayUtils.toByta(1));
                        clazz.addAll(MetaDataLoader.getClassMetaData(f.get(o)));
                    }
                    clazz.add(BinaryMarker.FIELD_END.code);
                }
            } else {
                clazz.add(BinaryMarker.TYPE_NULL.code);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        clazz.add(BinaryMarker.CLASS_END.code);
        return clazz.getByteArray();
    }

    private static void addDescription(ByteArray ba, String name) {
        byte[] val = ByteArrayUtils.toByta(name);
        ba.addAll(ByteArrayUtils.toByta(val.length));
        ba.addAll(val);
    }

    private static byte[] getFieldDescription(Field f, BinaryMarker marker) {
        ByteArray ba = new ByteArray();
        addDescription(ba, f.getName());
        //addDescription(ba, f.getType().getTypeName());
        ba.add(marker.code);
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
/*        length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        String field_type = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));*/
        byte type_marker = reader.next();
        length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));

        Field field = cashClassFields.computeIfAbsent(root_object.getClass(), MetaDataLoader::getFields).get(field_name);//root_object.getClass().getDeclaredField(field_name);
        field.setAccessible(true);

        if (type_marker == BinaryMarker.TYPE_ARRAY.code) {
            Class classDefinition = Class.forName(field.get(root_object).getClass().getName());
            Object value = null;
            if (!field.getType().isArray()) {
                value = classDefinition.newInstance();
            } else {
                value = Array.newInstance(classDefinition.getComponentType(), length);
            }
            int i = 0;
            while (i < length) {
                Object o = readClass(reader);
                if (value instanceof Collection) {
                    ((Collection) value).add(o);
                } else {
                    Array.set(value, i, o);
                }
                i++;
            }
            field.set(root_object, value);

        } else if (type_marker == BinaryMarker.TYPE_PRIMITIVE.code) {
            setPrimitiveValue(reader, field, root_object, length);
        } else if (type_marker == BinaryMarker.TYPE_ENUM.code) {
            field.set(root_object, Enum.valueOf(field.getType().asSubclass(Enum.class) , ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length))));
        } if (type_marker == BinaryMarker.TYPE_CLASS.code) {
            field.set(root_object, readClass(reader));
        }

        if (reader.next() != BinaryMarker.FIELD_END.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
    }

    private static void setPrimitiveValue(ByteArrayReader reader, Field field, Object o, int length) throws IllegalAccessException {
        if (int.class == field.getType()) {
            field.set(o, ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(length)));
        } else if (boolean.class == field.getType()) {
            field.set(o, ByteArrayUtils.toBoolean(reader.getSubArrayFromCurrent(length)));
        } else if (byte.class == field.getType()) {
            field.set(o, ByteArrayUtils.toByte(reader.getSubArrayFromCurrent(length)));
        } else if (char.class == field.getType()) {
            field.set(o, ByteArrayUtils.toChar(reader.getSubArrayFromCurrent(length)));
        } else if (double.class == field.getType()) {
            field.set(o, ByteArrayUtils.toDouble(reader.getSubArrayFromCurrent(length)));
        } else if (float.class == field.getType()) {
            field.set(o, ByteArrayUtils.toFloat(reader.getSubArrayFromCurrent(length)));
        } else if (long.class == field.getType()) {
            field.set(o, ByteArrayUtils.toLong(reader.getSubArrayFromCurrent(length)));
        } else if (short.class == field.getType()) {
            field.set(o, ByteArrayUtils.toShort(reader.getSubArrayFromCurrent(length)));
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
