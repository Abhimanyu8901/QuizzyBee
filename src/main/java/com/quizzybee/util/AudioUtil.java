package com.quizzybee.util;

import java.awt.Toolkit;

public final class AudioUtil {
    private AudioUtil() {
    }

    public static void playCorrectAnswerSound() {
        Toolkit.getDefaultToolkit().beep();
    }
}
