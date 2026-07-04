package com.quizzybee.util;

import com.quizzybee.model.Question;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelQuestionExporter {

    public void exportQuestions(File file, List<Question> questions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "question_id",
                    "question_text",
                    "option_a",
                    "option_b",
                    "option_c",
                    "option_d",
                    "correct_answer",
                    "category",
                    "difficulty_level"
            };

            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(headers[index]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Question question : questions) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(question.getQuestionId());
                row.createCell(1).setCellValue(question.getQuestionText());
                row.createCell(2).setCellValue(question.getOptionA());
                row.createCell(3).setCellValue(question.getOptionB());
                row.createCell(4).setCellValue(question.getOptionC());
                row.createCell(5).setCellValue(question.getOptionD());
                row.createCell(6).setCellValue(question.getCorrectAnswer());
                row.createCell(7).setCellValue(question.getCategory());
                row.createCell(8).setCellValue(question.getDifficultyLevel());
            }

            for (int index = 0; index < headers.length; index++) {
                sheet.autoSizeColumn(index);
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }
}
