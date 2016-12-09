package com.temerarious.mccocr13.temerariousocr.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.temerarious.mccocr13.temerariousocr.R;
import com.temerarious.mccocr13.temerariousocr.helpers.SaveResultHelper;
import com.temerarious.mccocr13.temerariousocr.helpers.SecureSocket;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class DetailsActivity extends AppCompatActivity {

    private String server_ip = "";
    private String credentials = "";
    public SaveResultHelper saveResultHelper = new SaveResultHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(DetailsActivity.this);
        server_ip = SP.getString("server_ip", getResources().getString(R.string.server_default_ip));

        credentials = Credentials.basic(OCRActivity.token, "");

        final String ocrText = getIntent().getStringExtra("ocr_text");
        String[] imagesArray = getIntent().getStringArrayExtra("images_array");

        LinearLayout ll = (LinearLayout) findViewById(R.id.details_linear_layout);
        Button btnSave=(Button) findViewById(R.id.button_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResultHelper.saveToText(ocrText);
            }
        });

        TextView msg = new TextView(this);
        msg.setPadding(10, 10, 10, 10);
        msg.setText(ocrText);
        ll.addView(msg);

        for (int i = 0; i < imagesArray.length; i++) {
            ImageView iv = new ImageView(this);
            Bitmap bitmap = getFullImage(imagesArray[i]);
            iv.setImageBitmap(bitmap);
            ll.addView(iv);
        }

    }

    private Bitmap getFullImage(String imageID) {

        String imageUrl = "https://" + server_ip + "/image/" + imageID;
        Bitmap bmp = null;

        try {

            OkHttpClient client = new OkHttpClient()
                    .setSslSocketFactory(SecureSocket.getSSLContext(DetailsActivity.this).getSocketFactory())
                    .setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            Request request = new Request.Builder()
                    .url(imageUrl)
                    .header("Authorization", credentials)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                throw new IOException("Unauthorized");
            }
            bmp = BitmapFactory.decodeStream(response.body().byteStream());


        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return bmp;

    }
}
