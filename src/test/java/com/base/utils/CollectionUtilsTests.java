package com.base.utils;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CollectionUtilsTests {

    @Test
    void findDuplicates_withDuplicates_shouldReturnSet() {
        List<String> input = List.of("A", "B", "A", "C", "B", "D");
        Set<String> duplicates = CollectionUtils.findDuplicates(input);
        assertEquals(Set.of("A", "B"), duplicates);
    }

    @Test
    void findDuplicates_noDuplicates_shouldReturnEmptySet() {
        List<Integer> input = List.of(1, 2, 3, 4);
        Set<Integer> duplicates = CollectionUtils.findDuplicates(input);
        assertTrue(duplicates.isEmpty());
    }

    @Test
    void findDuplicates_emptyList_shouldReturnEmptySet() {
        List<String> input = Collections.emptyList();
        Set<String> duplicates = CollectionUtils.findDuplicates(input);
        assertTrue(duplicates.isEmpty());
    }

    @Test
    void findDuplicates_nullInput_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> CollectionUtils.findDuplicates(null));
    }

    static class A {
        private final String code;
        private final String name;

        A(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    void toMapByCode_basicTest() {
        List<A> list = List.of(
                new A("A", "Alpha"),
                new A("b", "Bravo"),
                new A("A", "Another Alpha"),
                new A(null, "NullCode")
        );

        Map<String, A> map = CollectionUtils.toMapByCode(list, A::getCode);

        assertEquals(2, map.size());
        assertTrue(map.containsKey("A"));
        assertTrue(map.containsKey("B"));
        assertEquals("Alpha", map.get("A").getName());
    }

    @Test
    void toMapByCode_nullList_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> CollectionUtils.toMapByCode(null, A::getCode));
    }

    @Test
    void toMapByCode_nullKeyExtractor_shouldThrow() {
        List<A> list = List.of(new A("X", "Name"));
        assertThrows(NullPointerException.class,
                () -> CollectionUtils.toMapByCode(list, null));
    }
}
