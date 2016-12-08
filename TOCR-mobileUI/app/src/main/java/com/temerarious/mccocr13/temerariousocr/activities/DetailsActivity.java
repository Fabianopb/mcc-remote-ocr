package com.temerarious.mccocr13.temerariousocr.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.temerarious.mccocr13.temerariousocr.R;
import com.temerarious.mccocr13.temerariousocr.helpers.SecureSocket;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class DetailsActivity extends AppCompatActivity {

    private String[] imagesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String ocrText = getIntent().getStringExtra("ocr_text");
        imagesArray = getIntent().getStringArrayExtra("images_array");
        String timestamp = getIntent().getStringExtra("timestamp");

        LinearLayout ll = (LinearLayout) findViewById(R.id.details_linear_layout);

        TextView msg = new TextView(this);
        msg.setPadding(10, 10, 10, 10);
        msg.setText(ocrText);
        ll.addView(msg);

        TextView timestampView = (TextView) findViewById(R.id.creation_time_view);
        timestampView.setText(getString(R.string.created_at) + timestamp);

    }

    public void showSourceImages(View view) {
        for (int i = 0; i < imagesArray.length; i++) {
            if (imagesArray[i].equals("")) {
                Toast.makeText(this, getString(R.string.toast_no_source), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent(getApplicationContext(), ShowImagesActivity.class);
        intent.putExtra("images_array", imagesArray);
        startActivity(intent);
    }

    public void saveTextAsFile(View view) {
    }
}
