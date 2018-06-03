package com.myserialization.custom;

import com.myserialization.data.Parent;
import com.myserialization.data.TestDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetaDataLoaderTest {

    private Parent parent;
    private byte[] input = new byte[0];

    @BeforeEach
    void setUp() {
        parent = TestDataProvider.getTestData();
        input = MetaDataLoader.serialize(parent);
    }

    @Test
    void testSerialize() {
        System.out.println(Arrays.toString(input));
    }

    @Test
    void testDeSerialized() {
        Parent p2 = (Parent) MetaDataLoader.deSerialized(MetaDataLoader.serialize(parent));
        assertEquals(p2.getAge(), parent.getAge());
        assertEquals(p2.getName(), parent.getName());
        assertArrayEquals(p2.getFriends(), parent.getFriends());
        assertArrayEquals(p2.getChildren().toArray(), parent.getChildren().toArray());
    }
}