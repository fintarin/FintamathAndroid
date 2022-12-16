package com.fintamath;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.fintamath.mathtextview.MathTextView;

public class CalculatorProcessor {

    private final Activity activity;
    private final MathTextView inText;
    private final TextView outText;

    private final Calculator calculator;

    private Thread calcThread;

    CalculatorProcessor(Activity activity, MathTextView inText, TextView outText) {
        this.activity = activity;
        this.inText = inText;
        this.outText = outText;

        this.calculator = new Calculator();
    }

    void calculate() {
        if (calcThread != null && calcThread.isAlive()) {
            calcThread.interrupt();
        }

        if ("".equals(inText.getText().toString())) {
            outText.setText("");
            return;
        }

        calcThread = new Thread(() -> {
            activity.runOnUiThread(() -> {
                outText.setText(". . .");
            });

            String res = calculator.solve(inText.getText().toString());

            if (Thread.currentThread() == calcThread) {
                activity.runOnUiThread(() -> {
                    outText.setText(res);
                });
            }
        });

        calcThread.start();
    }
}
