package com.example.service.hh;

import com.example.config.Constants;
import com.fasterxml.jackson.databind.JsonNode;

public class HhGetterFromBio {
    public static Integer convertToRub(Integer value, String currency) {
        if (value == null || currency == null) return value;
        int rate = Constants.CURRENCY_TO_RUB.getOrDefault(currency, 1);
        return value * rate;
    }

    public static String getText(JsonNode node, String fieldName) {
        return getText(node, fieldName, "");
    }

    public static String getText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null) return defaultValue;
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asText(defaultValue) : defaultValue;
    }

    public static Integer parseSalaryField(JsonNode salNode, String field) {
        if (salNode == null || !salNode.isObject()) return null;
        JsonNode valueNode = salNode.get(field);
        if (valueNode != null && valueNode.isNumber()) {
            return valueNode.asInt();
        }
        return null;
    }
}