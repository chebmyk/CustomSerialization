package com.myserialization.custom;

import com.myserialization.custom.wrapper.WrapperFactory;
import com.myserialization.custom.wrapper.ObjectWrapper;

import java.lang.reflect.*;
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
        if (data[data.length - 1] != BinaryMarker.MYPROTOCOL.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        ByteArrayReader reader = new ByteArrayReader(data);
        while (reader.hasNext() && reader.current() != BinaryMarker.MYPROTOCOL.code) {
            object = readClass(reader);
        }
        return object;
    }

    private static byte[] getClassMetaData(Object o) {
        ByteArray clazz = new ByteArray(500);
        clazz.add(BinaryMarker.CLASS_START.code);
        try {
            if (o != null) {
                o = WrapperFactory.getWrapper(o);
                addDescription(clazz, o.getClass().getName());
                Collection<Field> fields = cashClassFields.computeIfAbsent(o.getClass(), MetaDataLoader::getFields).values();
                clazz.addAll(ByteArrayUtils.toByta(fields.size()));   //fields count
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
            } else if (obj.getClass().isArray()) {        //////////Arrays/////////////////////
                clazz.add(BinaryMarker.TYPE_ARRAY.code);
                int length = Array.getLength(obj);
                clazz.addAll(ByteArrayUtils.toByta(length));
                while (length > 0) {
                    clazz.addAll(objectMetadata(Array.get(obj, length - 1), obj.getClass().getComponentType()));
                    length--;
                }
            } else if (obj instanceof Collection) {     //////Collections////////
                clazz.add(BinaryMarker.TYPE_COLLECTION.code);
                clazz.addAll(ByteArrayUtils.toByta(((Collection) obj).size()));
                addDescription(clazz, obj.getClass().getName());
                ((Collection) obj).stream().forEach(i -> clazz.addAll(objectMetadata(i, i == null ? null : i.getClass())));
            } else if (obj instanceof Map) {          //////Map////////////////
                clazz.add(BinaryMarker.TYPE_COLLECTION.code);
                clazz.addAll(ByteArrayUtils.toByta(((Map) obj).size()));
                addDescription(clazz, obj.getClass().getName());
                ((Map) obj).forEach((key, value) -> {
                    clazz.addAll(objectMetadata(key, key == null ? null : key.getClass()));
                    clazz.addAll(objectMetadata(value, value == null ? null : value.getClass()));
                });
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

    private static String getDescription(ByteArrayReader reader) {
        int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        return ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
    }

    private static byte[] getFieldDescription(Field f) {
        ByteArray ba = new ByteArray();
        addDescription(ba, f.getName());
        return ba.getByteArray();
    }

    private static Object readClass(ByteArrayReader reader) {
        Object root_object = null;
        if (reader.next() != BinaryMarker.CLASS_START.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
        if (reader.current() != BinaryMarker.TYPE_NULL.code) {
            int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
            String clazz_name = ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length));
            try {
                Class classDefinition = Class.forName(clazz_name);
                root_object = classDefinition.newInstance();
                int filds_count = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
                while (filds_count > 0) {
                    populateClassField(reader, root_object);
                    filds_count--;
                }
                if (ObjectWrapper.class.isAssignableFrom(root_object.getClass())) {
                    root_object = ((ObjectWrapper) root_object).readObject();
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchFieldException e){
                throw new RuntimeException("Error while creating object " + clazz_name,e);
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
        Field field = cashClassFields.computeIfAbsent(root_object.getClass(), MetaDataLoader::getFields).get(field_name);
        field.setAccessible(true);
        if (field.getType().isPrimitive() || field.getType() == String.class) {
            reader.next();
            int len_val = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
            setPrimitiveValue(reader, field, root_object, len_val);
        } else if ((field.getGenericType() instanceof ParameterizedType)) {
            field.set(root_object, readObjectValue(reader, field.getType(), ((ParameterizedType) field.getGenericType()).getActualTypeArguments()));
        } else {
            field.set(root_object, readObjectValue(reader, field.getType()));
        }
        if (reader.next() != BinaryMarker.FIELD_END.code) {
            throw new UnsupportedClassVersionError("Usupported format for Object");
        }
    }

    private static Object readObjectValue(ByteArrayReader reader, Class target_class, Type[]... type) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        byte type_marker = reader.next();
        int length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
        Object value = null;
        if (type_marker == BinaryMarker.TYPE_ENUM.code) {
            value = Enum.valueOf(target_class.asSubclass(Enum.class), ByteArrayUtils.toString(reader.getSubArrayFromCurrent(length)));
        } else if (type_marker == BinaryMarker.TYPE_ARRAY.code) {
            Class classDefinition = Class.forName(target_class.getName());
            value = Array.newInstance(classDefinition.getComponentType(), length);
            while (length > 0) {
                if (classDefinition.getComponentType().isPrimitive() || classDefinition.getComponentType() == String.class) {
                    type_marker = reader.next();
                    int val_length = ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(4));
                    if (int.class == classDefinition.getComponentType()) {
                        Array.setInt(value, length - 1, ByteArrayUtils.toInt(reader.getSubArrayFromCurrent(val_length)));
                    } else if (boolean.class == classDefinition.getComponentType()) {
                        Array.setBoolean(value, length - 1, ByteArrayUtils.toBoolean(reader.getSubArrayFromCurrent(val_length)));
                    } else if (byte.class == classDefinition.getComponentType()) {
                        Array.setByte(value, length - 1, ByteArrayUtils.toByte(reader.getSubArrayFromCurrent(val_length)));
                    } else if (char.class == classDefinition.getComponentType()) {
                        Array.setChar(value, length - 1, ByteArrayUtils.toChar(reader.getSubArrayFromCurrent(val_length)));
                    } else if (double.class == classDefinition.getComponentType()) {
                        Array.setDouble(value, length - 1, ByteArrayUtils.toDouble(reader.getSubArrayFromCurrent(val_length)));
                    } else if (float.class == classDefinition.getComponentType()) {
                        Array.setFloat(value, length - 1, ByteArrayUtils.toFloat(reader.getSubArrayFromCurrent(val_length)));
                    } else if (long.class == classDefinition.getComponentType()) {
                        Array.setLong(value, length - 1, ByteArrayUtils.toLong(reader.getSubArrayFromCurrent(val_length)));
                    } else if (short.class == classDefinition.getComponentType()) {
                        Array.setShort(value, length - 1, ByteArrayUtils.toShort(reader.getSubArrayFromCurrent(val_length)));
                    } else if (String.class == classDefinition.getComponentType()) {    //todo check String arrays
                        Array.set(value, length - 1, ByteArrayUtils.toString(reader.getSubArrayFromCurrent(val_length)));
                    } else {
                        throw new UnsupportedOperationException("type [" + classDefinition.getComponentType() + "] is not supported");
                    }
                } else {
                    Array.set(value, length - 1, readObjectValue(reader, value.getClass().getComponentType()));
                }
                length--;
            }
        } else if (type_marker == BinaryMarker.TYPE_COLLECTION.code) {
            String impl_class = getDescription(reader);
            Class classDefinition = Class.forName(impl_class);
            value = classDefinition.newInstance();

            int i = 0;
            if (value instanceof Collection) {
                while (i < length) {
                    ((Collection) value).add(readObjectValue(reader, Class.forName(type[0][0].getTypeName())));
                    i++;
                }
            } else {
                while (i < length) {
                    Object k = readObjectValue(reader, Class.forName(type[0][0].getTypeName()));
                    Object v = readObjectValue(reader, Class.forName(type[0][1].getTypeName()));
                    ((Map) value).put(k, v);
                    i++;
                }
            }
        } else if (type_marker == BinaryMarker.TYPE_CLASS.code) {
            value = readClass(reader);
        }
        return value;
    }

    private static void setPrimitiveValue(ByteArrayReader reader, Field field, Object o, int length) {
        try {
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't set primitive field " + field.getType() +" ", e);
        }
    }

    private static byte[] getPrimitiveFieldValue(Class clazz, Object o) {
        ByteArray ba = new ByteArray(12);
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
