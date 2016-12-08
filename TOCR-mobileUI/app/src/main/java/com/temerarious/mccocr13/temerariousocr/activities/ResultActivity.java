package com.temerarious.mccocr13.temerariousocr.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.temerarious.mccocr13.temerariousocr.R;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        LinearLayout ll = (LinearLayout) findViewById(R.id.result_linear_layout);

        String ocrResult = getIntent().getStringExtra("ocr-result");

        TextView resultView = new TextView(this);
        resultView.setText(ocrResult);
        ll.addView(resultView);

    }

    public void saveTextAsFile(View view) {
    }
}
