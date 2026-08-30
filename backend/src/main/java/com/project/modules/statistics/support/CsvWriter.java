package com.project.modules.statistics.support;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class CsvWriter {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvWriter() {
    }

    public static byte[] write(List<String> headers, List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append(headers.stream().map(CsvWriter::escape).reduce((left, right) -> left + "," + right).orElse(""))
                .append("\r\n");
        for (Map<String, Object> row : rows) {
            for (int index = 0; index < headers.size(); index++) {
                if (index > 0) {
                    csv.append(',');
                }
                Object value = row.get(headers.get(index));
                csv.append(escape(value == null ? "" : String.valueOf(value)));
            }
            csv.append("\r\n");
        }
        byte[] content = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + content.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(content, 0, result, UTF8_BOM.length, content.length);
        return result;
    }

    private static String escape(String rawValue) {
        String value = preventFormulaInjection(rawValue);
        boolean quote = value.contains(",") || value.contains("\"") || value.contains("\r") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }

    private static String preventFormulaInjection(String value) {
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }
}
