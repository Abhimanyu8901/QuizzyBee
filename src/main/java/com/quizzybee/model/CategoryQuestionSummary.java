package com.quizzybee.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CategoryQuestionSummary {
    private final int serialNo;
    private final String categoryName;
    private final int easyCount;
    private final int mediumCount;
    private final int hardCount;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public CategoryQuestionSummary(int serialNo, String categoryName, int easyCount, int mediumCount, int hardCount) {
        this.serialNo = serialNo;
        this.categoryName = categoryName;
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getEasyCount() {
        return easyCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public int getHardCount() {
        return hardCount;
    }

    public int getTotalCount() {
        return easyCount + mediumCount + hardCount;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
