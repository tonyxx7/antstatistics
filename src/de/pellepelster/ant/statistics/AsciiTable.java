/*
 * AsciiTable.java created on 28.07.2010
 *
 * Copyright (c) 2008 Lufthansa Systems
 * All rights reserved.
 *
 * This program and the accompanying materials are proprietary information
 * of Lufthansa Systems.
 * Use is subject to license terms.
 */
package de.pellepelster.ant.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsciiTable {

    private final String[] headers;
    private final List<String[]> rows = new ArrayList<String[]>();
    private final String title;
    private String footer;

    public AsciiTable(String title, String[] headers) {
        this.title = title;
        this.headers = headers;
    }

    public void addRow(String... row) {
        rows.add(row);
    }

    private Map<Integer, Integer> getMaxColumnWidths() {
        List<String[]> data = new ArrayList<String[]>(rows);
        data.add(headers);

        Map<Integer, Integer> result = new HashMap<Integer, Integer>();

        for (String[] row : data) {
            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                if (!result.containsKey(columnIndex)) {
                    result.put(columnIndex, 0);
                }

                if (row[columnIndex].length() > result.get(columnIndex)) {
                    result.put(columnIndex, row[columnIndex].length());
                }
            }
        }

        return result;
    }

    private static final String VERTICAL_TABLE_BORDER = "-";
    private static final String HORIZONTAL_TABLE_BORDER = "|";
    private static final Object TABLE_CONNECTOR = "+";
    private static final int HORIZONTAL_CELLPADDING = 1;
    private static final String NEWLINE = System.getProperty("line.separator");

    /** {@inheritDoc} */
    @Override
    public String toString() {
        Map<Integer, Integer> maxColumnWidths = getMaxColumnWidths();

        int totalTableWidth = 0;
        for (Integer columnWidth : maxColumnWidths.values()) {
            totalTableWidth += columnWidth;
            totalTableWidth += HORIZONTAL_CELLPADDING * 2;
        }

        totalTableWidth += maxColumnWidths.size() - HORIZONTAL_TABLE_BORDER.length();

        StringBuffer result = new StringBuffer();

        createHorizontalLine(totalTableWidth, result);

        createCenteredLine(totalTableWidth, title, result);

        createHorizontalLine(totalTableWidth, result);

        createRow(maxColumnWidths, headers, result);
        createHorizontalLine(totalTableWidth, result);

        for (String[] row : rows) {
            createRow(maxColumnWidths, row, result);
        }
        createHorizontalLine(totalTableWidth, result);

        // footer
        if (footer != null) {
            createCenteredLine(totalTableWidth, footer, result);
            createHorizontalLine(totalTableWidth, result);
        }

        return result.toString();
    }

    private void createCenteredLine(int totalTableWidth, String text, StringBuffer result) {
        result.append(HORIZONTAL_TABLE_BORDER);
        result.append(center(text, totalTableWidth));
        result.append(HORIZONTAL_TABLE_BORDER);
        result.append(NEWLINE);
    }

    private void createRow(Map<Integer, Integer> maxColumnWidths, String[] row, StringBuffer result) {
        result.append(HORIZONTAL_TABLE_BORDER);
        for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
            String cellText = row[columnIndex];

            result.append(repeat(" ", HORIZONTAL_CELLPADDING));
            result.append(center(cellText, maxColumnWidths.get(columnIndex)));
            result.append(repeat(" ", HORIZONTAL_CELLPADDING));

            if (columnIndex < headers.length - 1) {
                result.append(HORIZONTAL_TABLE_BORDER);
            }

        }
        result.append(HORIZONTAL_TABLE_BORDER);
        result.append(NEWLINE);
    }

    private void createHorizontalLine(int totalTableWidth, StringBuffer result) {
        result.append(TABLE_CONNECTOR);
        result.append(repeat(VERTICAL_TABLE_BORDER, totalTableWidth));
        result.append(TABLE_CONNECTOR);
        result.append(NEWLINE);
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getFooter() {
        return footer;
    }

    private String repeat(String s, int n) {
        return String.format("%0" + n + "d", 0).replace("0", s);
    }

    private String center(final String str, final int size) {
        return center(str, size, ' ');
    }

    private String center(String str, final int size, final char padChar) {
        if (str == null || size <= 0) {
            return str;
        }
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        str = pad(str, strLen + pads / 2, padChar, false);
        str = pad(str, size, padChar, true);
        return str;
    }

    private String pad(final String str, final int size, final char padChar, final boolean b) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return b ? str.concat(repeat(String.valueOf(padChar), pads))
                : repeat(String.valueOf(padChar), pads).concat(str);
    }

}
