package com.quizzybee.util;

import com.quizzybee.model.Question;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcelQuestionImporter {
    private final DataFormatter formatter = new DataFormatter();

    public List<Question> readQuestions(File file) throws IOException {
        if (file.getName().toLowerCase(Locale.ENGLISH).endsWith(".csv")) {
            return readQuestionsFromCsv(file);
        }

        List<Question> questions = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return questions;
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> headerMap = buildHeaderMap(headerRow);
            validateHeaders(headerMap);

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmpty(row)) {
                    continue;
                }
                questions.add(buildQuestion(row, headerMap, rowIndex + 1));
            }
        }
        return questions;
    }

    private List<Question> readQuestionsFromCsv(File file) throws IOException {
        List<Question> questions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return questions;
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> headerMap = buildHeaderMap(headers);
            validateHeaders(headerMap);

            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] values = parseCsvLine(line);
                questions.add(buildQuestion(values, headerMap, rowNumber));
            }
        }
        return questions;
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> headers = new HashMap<>();
        for (Cell cell : headerRow) {
            headers.put(normalize(formatter.formatCellValue(cell)), cell.getColumnIndex());
        }
        return headers;
    }

    private Map<String, Integer> buildHeaderMap(String[] headerValues) {
        Map<String, Integer> headers = new HashMap<>();
        for (int index = 0; index < headerValues.length; index++) {
            headers.put(normalize(headerValues[index]), index);
        }
        return headers;
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        List<String> requiredHeaders = List.of(
                "question_text", "option_a", "option_b", "option_c", "option_d",
                "correct_answer", "category", "difficulty_level"
        );
        for (String header : requiredHeaders) {
            if (!headerMap.containsKey(header)) {
                throw new IllegalArgumentException("Missing required Excel column: " + header);
            }
        }
    }

    private Question buildQuestion(Row row, Map<String, Integer> headerMap, int excelRowNumber) {
        Question question = new Question();
        question.setQuestionText(read(row, headerMap, "question_text"));
        question.setOptionA(read(row, headerMap, "option_a"));
        question.setOptionB(read(row, headerMap, "option_b"));
        question.setOptionC(read(row, headerMap, "option_c"));
        question.setOptionD(read(row, headerMap, "option_d"));
        question.setCorrectAnswer(read(row, headerMap, "correct_answer"));
        question.setCategory(read(row, headerMap, "category"));
        question.setDifficultyLevel(read(row, headerMap, "difficulty_level"));

        if (question.getQuestionText().isBlank() || question.getOptionA().isBlank() || question.getOptionB().isBlank()
                || question.getOptionC().isBlank() || question.getOptionD().isBlank()
                || question.getCorrectAnswer().isBlank() || question.getCategory().isBlank() || question.getDifficultyLevel().isBlank()) {
            throw new IllegalArgumentException("Incomplete question data at Excel row " + excelRowNumber);
        }

        question.setCorrectAnswer(resolveCorrectAnswer(question.getCorrectAnswer(), question, excelRowNumber));
        return question;
    }

    private Question buildQuestion(String[] rowValues, Map<String, Integer> headerMap, int rowNumber) {
        Question question = new Question();
        question.setQuestionText(read(rowValues, headerMap, "question_text"));
        question.setOptionA(read(rowValues, headerMap, "option_a"));
        question.setOptionB(read(rowValues, headerMap, "option_b"));
        question.setOptionC(read(rowValues, headerMap, "option_c"));
        question.setOptionD(read(rowValues, headerMap, "option_d"));
        question.setCorrectAnswer(read(rowValues, headerMap, "correct_answer"));
        question.setCategory(read(rowValues, headerMap, "category"));
        question.setDifficultyLevel(read(rowValues, headerMap, "difficulty_level"));

        if (question.getQuestionText().isBlank() || question.getOptionA().isBlank() || question.getOptionB().isBlank()
                || question.getOptionC().isBlank() || question.getOptionD().isBlank()
                || question.getCorrectAnswer().isBlank() || question.getCategory().isBlank() || question.getDifficultyLevel().isBlank()) {
            throw new IllegalArgumentException("Incomplete question data at row " + rowNumber);
        }

        question.setCorrectAnswer(resolveCorrectAnswer(question.getCorrectAnswer(), question, rowNumber));
        return question;
    }

    private String resolveCorrectAnswer(String value, Question question, int excelRowNumber) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "option_a", "a" -> question.getOptionA();
            case "option_b", "b" -> question.getOptionB();
            case "option_c", "c" -> question.getOptionC();
            case "option_d", "d" -> question.getOptionD();
            default -> {
                List<String> valid = List.of(question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD());
                if (!valid.contains(value)) {
                    throw new IllegalArgumentException("Correct answer does not match any option at Excel row " + excelRowNumber);
                }
                yield value;
            }
        };
    }

    private String read(Row row, Map<String, Integer> headerMap, String header) {
        Integer columnIndex = headerMap.get(header);
        if (columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private String read(String[] rowValues, Map<String, Integer> headerMap, String header) {
        Integer columnIndex = headerMap.get(header);
        if (columnIndex == null || columnIndex >= rowValues.length) {
            return "";
        }
        return rowValues[columnIndex].trim();
    }

    private boolean isEmpty(Row row) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH).replace(' ', '_');
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }
}
