package com.myserialization.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myserialization.data.Parent;
import com.myserialization.data.TestDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSerializationTest {

    private Parent parent;
    private byte[] input = new byte[0];

    @BeforeEach
    void setUp() {
        parent = TestDataProvider.getTestData();
        try {
            input = JsonSerialization.writeJsonObject(parent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    void readJsonObjectEquals() {
        Parent p2 = (Parent)JsonSerialization.readJsonObject(input,Parent.class);
        assertEquals(p2.getAge(),parent.getAge());
        assertEquals(p2.getName(),parent.getName());
        assertArrayEquals(p2.getChildren().toArray(),parent.getChildren().toArray());
    }

}