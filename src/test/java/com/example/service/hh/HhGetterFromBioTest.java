package com.example.service.hh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class HhGetterFromBioTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void convertToRubNullValue() {
        assertNull(HhGetterFromBio.convertToRub(null, "USD"));
    }

    @Test
    void convertToRubNullCurrency() {
        assertEquals(100, HhGetterFromBio.convertToRub(100, null));
    }

    @Test
    void getTextWithDefaultValue() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("field1", "value1");
        node.put("field2", 123);

        assertEquals("value1", HhGetterFromBio.getText(node, "field1"));
        assertEquals("123", HhGetterFromBio.getText(node, "field2"));
        assertEquals("", HhGetterFromBio.getText(node, "field3"));
    }

    @Test
    void getTextWithCustomDefaultValue() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("field1", "value1");

        assertEquals("value1", HhGetterFromBio.getText(node, "field1", "default"));
        assertEquals("default", HhGetterFromBio.getText(node, "field2", "default"));
    }

    @Test
    void getText_WithNullNode() {
        assertEquals("", HhGetterFromBio.getText(null, "field1"));
        assertEquals("default", HhGetterFromBio.getText(null, "field1", "default"));
    }

    @Test
    void parseSalaryField_ValidNumber() {
        ObjectNode salNode = objectMapper.createObjectNode();
        salNode.put("from", 1000);
        salNode.put("to", 2000);

        assertEquals(1000, HhGetterFromBio.parseSalaryField(salNode, "from"));
        assertEquals(2000, HhGetterFromBio.parseSalaryField(salNode, "to"));
    }

    @Test
    void parseSalaryFieldInvalidField() {
        ObjectNode salNode = objectMapper.createObjectNode();
        salNode.put("from", 1000);

        assertNull(HhGetterFromBio.parseSalaryField(salNode, "nonexistent"));
    }

    @Test
    void parseSalaryFieldNonNumberValue() {
        ObjectNode salNode = objectMapper.createObjectNode();
        salNode.put("from", "not a number");

        assertNull(HhGetterFromBio.parseSalaryField(salNode, "from"));
    }

    @Test
    void parseSalaryFieldNonObjectNode() {
        JsonNode node = objectMapper.createArrayNode();

        assertNull(HhGetterFromBio.parseSalaryField(node, "from"));
    }

    @Test
    void parseSalaryFieldNullNode() {
        assertNull(HhGetterFromBio.parseSalaryField(null, "from"));
    }
}