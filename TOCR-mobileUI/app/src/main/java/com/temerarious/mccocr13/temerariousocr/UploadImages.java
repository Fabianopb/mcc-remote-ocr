package com.temerarious.mccocr13.temerariousocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by fabiano.brito on 26/11/2016.
 */

class UploadImages extends AsyncTask<String,Void,String> {

    public OCRActivity source = null;
    private Context context;
    ProgressDialog loading;

    UploadImages(OCRActivity fl, Context ctx) {
        source = fl;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String server_ip = SP.getString("server_ip", context.getResources().getString(R.string.server_default_ip));

        String prepare_remote_url = "https://" + server_ip + "/upload/";
        String uid = params[0];
        String seq = params[1];

        try {

            OkHttpClient client = new OkHttpClient()
                    .setSslSocketFactory(SecureSocket.getSSLContext(context).getSocketFactory())
                    .setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uid", uid)
                    .addFormDataPart("seq", seq)
                    .addFormDataPart("image", "filename.png", RequestBody.create(MediaType.parse("image/jpg"), source.byteArray))
                    .build();

            Request request = new Request.Builder()
                    .url(prepare_remote_url)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();

        } catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | KeyStoreException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        loading = ProgressDialog.show(context, source.getResources().getString(R.string.remote_dialog), null, true, true);
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null) {
            try {

                JSONObject jsonObj = new JSONObject(result);

                String message = jsonObj.getString("message");
                String uid = jsonObj.getString("uid");
                String next_seq = jsonObj.getString("next_seq");
                String ocr_result = jsonObj.getString("ocr_result");

                Toast.makeText(context, result, Toast.LENGTH_LONG).show();

                source.displayTranslatedText(ocr_result);

                loading.dismiss();

            } catch (JSONException e) {
                Log.e("Parsing error", e.toString());
            }
        } else {
            loading.dismiss();
            Toast.makeText(context, R.string.remote_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}