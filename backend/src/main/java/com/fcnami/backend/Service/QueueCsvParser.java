package com.fcnami.backend.Service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
class QueueCsvParser {

    List<Map<String, String>> parse(String csv) {
        List<List<String>> table = parseTable(csv == null ? "" : csv);
        trimBlankEdges(table);
        if (table.isEmpty() || table.getFirst().stream().allMatch(String::isBlank)) {
            return List.of();
        }

        List<String> headers = table.getFirst().stream()
                .map(this::normalizeHeader)
                .toList();
        List<Map<String, String>> rows = new ArrayList<>();

        for (int i = 1; i < table.size(); i++) {
            rows.add(mapRow(headers, table.get(i)));
        }
        return rows;
    }

    private List<List<String>> parseTable(String csv) {
        List<List<String>> table = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < csv.length(); i++) {
            char current = csv.charAt(i);
            if (quoted) {
                if (current == '"' && isEscapedQuote(csv, i)) {
                    cell.append('"');
                    i++;
                } else if (current == '"') {
                    quoted = false;
                } else {
                    cell.append(current);
                }
                continue;
            }

            if (current == '"') {
                quoted = true;
            } else if (current == ',') {
                addCell(row, cell);
            } else if (current == '\n') {
                addCell(row, cell);
                table.add(row);
                row = new ArrayList<>();
            } else if (current != '\r') {
                cell.append(current);
            }
        }

        addCell(row, cell);
        table.add(row);
        return table;
    }

    private void trimBlankEdges(List<List<String>> table) {
        while (!table.isEmpty() && isBlankRow(table.getFirst())) {
            table.removeFirst();
        }
        while (!table.isEmpty() && isBlankRow(table.getLast())) {
            table.removeLast();
        }
    }

    private boolean isBlankRow(List<String> row) {
        return row.stream().allMatch(String::isBlank);
    }

    private boolean isEscapedQuote(String csv, int index) {
        return index + 1 < csv.length() && csv.charAt(index + 1) == '"';
    }

    private void addCell(List<String> row, StringBuilder cell) {
        row.add(cell.toString().trim());
        cell.setLength(0);
    }

    private Map<String, String> mapRow(List<String> headers, List<String> values) {
        Map<String, String> mapped = new LinkedHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String header = i < headers.size() && hasText(headers.get(i)) ? headers.get(i) : "column_" + (i + 1);
            mapped.put(header, values.get(i));
        }
        return mapped;
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
