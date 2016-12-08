package com.temerarious.mccocr13.temerariousocr.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.temerarious.mccocr13.temerariousocr.R;

public class BenchmarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benchmark);

        LinearLayout ll = (LinearLayout) findViewById(R.id.benchmark_linear_layout);

        String localResults = getResources().getString(R.string.local_header);
        for (int i = 0; i < OCRActivity.imageStream.size(); i++) {
            localResults += "\n  - image #" + String.valueOf(i + 1) + ": " + OCRActivity.benchmarkResults.getLocalElapsedTime(i) + " seconds";
        }
        localResults += "\n  TOTAL time of " + OCRActivity.benchmarkResults.getLocalTotal() + " seconds.";
        localResults += "\n  AVERAGE time of " + OCRActivity.benchmarkResults.getLocalAverage() + " seconds.";
        localResults += "\n  STANDARD DEVIATION of " + OCRActivity.benchmarkResults.getLocalDeviation() + " seconds.";

        TextView localResultView = new TextView(this);
        localResultView.setText(localResults);
        ll.addView(localResultView);

    }
}
