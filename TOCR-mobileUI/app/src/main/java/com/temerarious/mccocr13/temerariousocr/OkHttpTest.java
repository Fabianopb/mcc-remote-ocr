package com.temerarious.mccocr13.temerariousocr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


/**
 * Created by fabiano.brito on 29/11/2016.
 */

class OkHttpTest extends AsyncTask<String,Void,String> {

    public OCRActivity source = null;
    private Context context;
    //ProgressDialog loading;

    OkHttpTest(OCRActivity fl, Context ctx) {
        source = fl;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String server_ip = SP.getString("server_ip", context.getResources().getString(R.string.server_default_ip));

        String prepare_remote_url = "https://" + server_ip + "/ocr/";
        String images_total = params[0];

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
                    .addPart(
                            Headers.of("Content-Disposition", "form-data; name=\"images_total\""),
                            RequestBody.create(null, images_total))
                    /*.addPart(
                            Headers.of("Content-Disposition", "form-data; name=\"image\""),
                            RequestBody.create(MEDIA_TYPE_PNG, new File("website/static/logo-square.png")))*/
                    .build();

            Request request = new Request.Builder()
                    //.header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .url(prepare_remote_url)
                    .post(requestBody)
                    .build();

            /*Request request = new Request.Builder()
                    .url(prepare_remote_url)
                    .build();*/

            Response response = client.newCall(request).execute();
            return response.body().string();

        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onPostExecute(String result) {
        if(result != null) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "ok http went bad", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}