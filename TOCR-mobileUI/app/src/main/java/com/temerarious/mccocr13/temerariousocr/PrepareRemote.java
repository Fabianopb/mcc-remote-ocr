package com.temerarious.mccocr13.temerariousocr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

class PrepareRemote extends AsyncTask<String,Void,String> {

    public OCRActivity source = null;
    private Context context;
    //ProgressDialog loading;

    PrepareRemote(OCRActivity fl, Context ctx) {
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

            URL url = new URL(prepare_remote_url);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)url.openConnection();
            httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            httpsURLConnection.setSSLSocketFactory(SecureSocket.getSSLContext(context).getSocketFactory());
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.connect();

            byte[] totalImages = ("images_total=" + images_total).getBytes("UTF-8");
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            outputStream.write(totalImages);
            outputStream.close();

            InputStream inputStream = httpsURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String result = "";
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();

            httpsURLConnection.disconnect();
            return result;


        } catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | KeyStoreException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        //loading = ProgressDialog.show(context, source.getResources().getString(R.string.remote_dialog), null, true, true);
    }

    @Override
    protected void onPostExecute(String result) {
        Log.v("TAG", "result = " + result);
        if(result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                Log.v("TAG", jsonObj.toString());
                Toast.makeText(context, jsonObj.getString("message"), Toast.LENGTH_SHORT).show();
                //loading.dismiss();
            } catch (JSONException e) {
                Log.e("Parsing error", e.toString());
            }
        } else {
            //loading.dismiss();
            Toast.makeText(context, R.string.remote_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}