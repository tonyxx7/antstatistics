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

import org.apache.commons.lang.StringUtils;

public class AsciiTable {

	private String[] headers;
	private List<String[]> rows = new ArrayList<String[]>();
	private String title;
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

	private final static String VERTICAL_TABLE_BORDER = "-";
	private final static String HORIZONTAL_TABLE_BORDER = "|";
	private final static Object TABLE_CONNECTOR = "+";
	private final static int HORIZONTAL_CELLPADDING = 1;
	private final static String NEWLINE = System.getProperty("line.separator");

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
		result.append(StringUtils.center(text, totalTableWidth));
		result.append(HORIZONTAL_TABLE_BORDER);
		result.append(NEWLINE);
	}

	private void createRow(Map<Integer, Integer> maxColumnWidths, String[] row, StringBuffer result) {

		result.append(HORIZONTAL_TABLE_BORDER);
		for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
			String cellText = row[columnIndex];

			result.append(StringUtils.repeat(" ", HORIZONTAL_CELLPADDING));
			result.append(StringUtils.center(cellText, maxColumnWidths.get(columnIndex)));
			result.append(StringUtils.repeat(" ", HORIZONTAL_CELLPADDING));

			if (columnIndex < headers.length - 1) {
				result.append(HORIZONTAL_TABLE_BORDER);
			}

		}
		result.append(HORIZONTAL_TABLE_BORDER);
		result.append(NEWLINE);
	}

	private void createHorizontalLine(int totalTableWidth, StringBuffer result) {
		result.append(TABLE_CONNECTOR);
		result.append(StringUtils.repeat(VERTICAL_TABLE_BORDER, totalTableWidth));
		result.append(TABLE_CONNECTOR);
		result.append(NEWLINE);
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public String getFooter() {
		return footer;
	}
}
