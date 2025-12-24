package com.base.utils;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTests {

    static class MyDto {

        int id;
        String name;

        MyDto() {}

        MyDto(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MyDto other)) return false;
            return id == other.id && ((name == null && other.name == null) || (name != null && name.equals(other.name)));
        }
    }

    @Test
    void parseJsonArray_validJson_shouldReturnList() {
        String json = "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"}]";
        List<MyDto> list = JsonUtils.parseJsonArray(json, MyDto[].class);
        assertEquals(2, list.size());
        assertEquals(new MyDto(1, "A"), list.get(0));
        assertEquals(new MyDto(2, "B"), list.get(1));
    }

    @Test
    void parseJsonArray_emptyJson_shouldReturnEmptyList() {
        List<MyDto> list1 = JsonUtils.parseJsonArray("", MyDto[].class);
        List<MyDto> list2 = JsonUtils.parseJsonArray(null, MyDto[].class);
        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
    }

    @Test
    void parseJsonObject_validJson_shouldReturnObject() {
        String json = "{\"id\":5,\"name\":\"Test\"}";
        MyDto obj = JsonUtils.parseJsonObject(json, MyDto.class);
        assertEquals(new MyDto(5, "Test"), obj);
    }

    @Test
    void parseJsonObject_emptyJson_shouldReturnNull() {
        MyDto obj1 = JsonUtils.parseJsonObject("", MyDto.class);
        MyDto obj2 = JsonUtils.parseJsonObject(null, MyDto.class);
        assertNull(obj1);
        assertNull(obj2);
    }

}
