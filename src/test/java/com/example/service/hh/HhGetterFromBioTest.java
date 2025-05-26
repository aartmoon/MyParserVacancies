package com.example.service.hh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class HhGetterFromBioTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void convertToRub_NullValue() {
        assertNull(HhGetterFromBio.convertToRub(null, "USD"));
    }

    @Test
    void convertToRub_NullCurrency() {
        assertEquals(100, HhGetterFromBio.convertToRub(100, null));
    }

    @Test
    void getText_WithDefaultValue() {
        // Arrange
        ObjectNode node = objectMapper.createObjectNode();
        node.put("field1", "value1");
        node.put("field2", 123);

        // Act & Assert
        assertEquals("value1", HhGetterFromBio.getText(node, "field1"));
        assertEquals("123", HhGetterFromBio.getText(node, "field2"));
        assertEquals("", HhGetterFromBio.getText(node, "field3"));
    }

    @Test
    void getText_WithCustomDefaultValue() {
        // Arrange
        ObjectNode node = objectMapper.createObjectNode();
        node.put("field1", "value1");

        // Act & Assert
        assertEquals("value1", HhGetterFromBio.getText(node, "field1", "default"));
        assertEquals("default", HhGetterFromBio.getText(node, "field2", "default"));
    }

    @Test
    void getText_WithNullNode() {
        // Act & Assert
        assertEquals("", HhGetterFromBio.getText(null, "field1"));
        assertEquals("default", HhGetterFromBio.getText(null, "field1", "default"));
    }

    @Test
    void parseSalaryField_ValidNumber() {
        // Arrange
        ObjectNode salNode = objectMapper.createObjectNode();
        salNode.put("from", 1000);
        salNode.put("to", 2000);

        // Act & Assert
        assertEquals(1000, HhGetterFromBio.parseSalaryField(salNode, "from"));
        assertEquals(2000, HhGetterFromBio.parseSalaryField(salNode, "to"));
    }

    @Test
    void parseSalaryField_InvalidField() {
        // Arrange
        ObjectNode salNode = objectMapper.createObjectNode();
        salNode.put("from", 1000);

        // Act & Assert
        assertNull(HhGetterFromBio.parseSalaryField(salNode, "nonexistent"));
    }

    @Test
    void parseSalaryField_NonNumberValue() {
        // Arrange
        ObjectNode salNode = objectMapper.createObjectNode();
        salNode.put("from", "not a number");

        // Act & Assert
        assertNull(HhGetterFromBio.parseSalaryField(salNode, "from"));
    }

    @Test
    void parseSalaryField_NonObjectNode() {
        // Arrange
        JsonNode node = objectMapper.createArrayNode();

        // Act & Assert
        assertNull(HhGetterFromBio.parseSalaryField(node, "from"));
    }

    @Test
    void parseSalaryField_NullNode() {
        // Act & Assert
        assertNull(HhGetterFromBio.parseSalaryField(null, "from"));
    }
}