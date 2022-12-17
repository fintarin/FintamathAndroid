package com.fintamath.calculator;

import android.app.Activity;
import android.widget.TextView;

import com.fintamath.textview.MathEditText;

public class CalculatorProcessor {

    private final Activity activity;
    private final MathEditText inText;
    private final TextView outText;

    private final Calculator calculator;

    private Thread calcThread;

    public CalculatorProcessor(Activity activity, MathEditText inText, TextView outText) {
        this.activity = activity;
        this.inText = inText;
        this.outText = outText;

        this.calculator = new Calculator();
    }

    public void calculate() {
        if (calcThread != null && calcThread.isAlive()) {
            calcThread.interrupt();
        }

        String text = inText.getText();
        if (text.isEmpty()) {
            outText.setText("");
            return;
        }

        calcThread = new Thread(() -> {
            activity.runOnUiThread(() -> {
                outText.setText(". . .");
            });

            String res = calculator.calculate(text);

            if (Thread.currentThread() == calcThread) {
                activity.runOnUiThread(() -> {
                    outText.setText(res);
                });
            }
        });

        calcThread.start();
    }
}
