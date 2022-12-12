package com.fintamath;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class CalculatorProcessor {

    private final Activity activity;
    private final EditText inText;
    private final TextView outText;

    private final Calculator calculator;

    private Thread calcThread;

    CalculatorProcessor(Activity activity, EditText inText, TextView outText) {
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
            String res = calculator.solve(inText.getText().toString());

            activity.runOnUiThread(() -> {
                outText.setText(res);
            });
        });

        calcThread.start();
    }
}
