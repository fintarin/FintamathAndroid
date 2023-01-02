package com.fintamath.calculator;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.fintamath.textview.MathAlternativesTextView;
import com.fintamath.textview.MathEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalculatorProcessor {

    private final Activity activity;
    private final MathEditText inTextView;
    private final MathAlternativesTextView outTextView;

    private final Calculator calculator;

    private Thread calcThread;

    public CalculatorProcessor(Activity activity, MathEditText inTextView, MathAlternativesTextView outTextView) {
        this.activity = activity;
        this.inTextView = inTextView;
        this.outTextView = outTextView;
        this.calculator = new Calculator();
    }

    public void calculate() {
        if (calcThread != null && calcThread.isAlive()) {
            calcThread.interrupt();
        }

        String text = inTextView.getText();
        if (text.isEmpty()) {
            outTextView.setTexts(null);
            return;
        }

        calcThread = new Thread(() -> {
            activity.runOnUiThread(() -> {
                outTextView.setTexts(List.of(". . ."));
            });

            String resData = calculator.calculate(text);

            if (Thread.currentThread() == calcThread) {
                activity.runOnUiThread(() -> {
                    List<String> results = List.of(resData.split("\n"));
                    outTextView.setTexts(results);
                });
            }
        });

        calcThread.start();
    }
}
