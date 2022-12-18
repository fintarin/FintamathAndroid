package com.fintamath.calculator;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.fintamath.textview.MathEditText;

public class CalculatorProcessor {

    private final Activity activity;
    private final MathEditText inText;
    private final TextView outText;
    private final TextView outTextFull;
    private final View alternativeFormTitle;

    private final Calculator calculator;

    private Thread calcThread;

    public CalculatorProcessor(Activity activity, MathEditText inText, TextView outText, TextView outFullText, View alternativeFormTitle) {
        this.activity = activity;
        this.inText = inText;
        this.outText = outText;
        this.outTextFull = outFullText;
        this.alternativeFormTitle = alternativeFormTitle;
        this.calculator = new Calculator();
    }

    public void calculate() {
        if (calcThread != null && calcThread.isAlive()) {
            calcThread.interrupt();
        }

        String text = inText.getText();
        if (text.isEmpty()) {
            outText.setText("");
            outTextFull.setText("");
            alternativeFormTitle.setVisibility(View.GONE);
            return;
        }

        calcThread = new Thread(() -> {
            activity.runOnUiThread(() -> {
                outText.setText(". . .");
                outTextFull.setText("");
                alternativeFormTitle.setVisibility(View.GONE);
            });

            String resData = calculator.calculate(text);
            int i = resData.indexOf('\n');

            if (Thread.currentThread() == calcThread) {
                activity.runOnUiThread(() -> {
                    if (i != -1) {
                        String res = resData.substring(0, resData.indexOf('\n'));
                        String resFull = resData.substring(resData.indexOf('\n') + 1);

                        outText.setText(res);

                        if (!resFull.isEmpty() && !resFull.equals(res)) {
                            alternativeFormTitle.setVisibility(View.VISIBLE);
                            outTextFull.setText(resFull);
                        }
                    } else {
                        outText.setText(resData);
                    }
                });
            }
        });

        calcThread.start();
    }
}
