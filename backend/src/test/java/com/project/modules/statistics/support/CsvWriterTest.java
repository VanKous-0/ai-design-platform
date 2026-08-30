package com.project.modules.statistics.support;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvWriterTest {

    @Test
    void writesUtf8BomEscapesValuesAndPreventsFormulaInjection() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", "张三,测试");
        row.put("value", "=1+1");

        byte[] result = CsvWriter.write(List.of("name", "value"), List.of(row));

        assertEquals((byte) 0xEF, result[0]);
        assertEquals((byte) 0xBB, result[1]);
        assertEquals((byte) 0xBF, result[2]);
        String content = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"张三,测试\""));
        assertTrue(content.contains("'=1+1"));
    }
}
