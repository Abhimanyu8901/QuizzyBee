package com.quizzybee.model;

public class ImportResult {
    private int processedRows;
    private int insertedCount;
    private int updatedCount;
    private int skippedCount;

    public int getProcessedRows() {
        return processedRows;
    }

    public void incrementProcessedRows() {
        processedRows++;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public void incrementInsertedCount() {
        insertedCount++;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void incrementUpdatedCount() {
        updatedCount++;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void incrementSkippedCount() {
        skippedCount++;
    }
}
