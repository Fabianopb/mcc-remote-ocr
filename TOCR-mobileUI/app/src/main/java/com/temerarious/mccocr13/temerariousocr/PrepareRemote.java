package com.temerarious.mccocr13.temerariousocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PrepareRemote extends AsyncTask<String,Void,String> {

    public OCRActivity source = null;
    Context context;
    //ProgressDialog loading;

    public PrepareRemote(OCRActivity fl, Context ctx) {
        source = fl;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        //SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        //String server_ip = SP.getString("server_ip", context.getResources().getString(R.string.server_default_ip));

        String server_ip = "104.199.92.144";
        String prepare_remote_url = "http://" + server_ip + "/ocr/";
        String images_total = params[0];

        try {

            URL url = new URL(prepare_remote_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            //String credentials = token + ":" + blank;
            //String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");
            //httpURLConnection.setRequestProperty("Authorization", "Basic " + credBase64);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();

            // setting instance name in the body of the request
            byte[] totalImages = ("images_total=" + images_total).getBytes("UTF-8");
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(totalImages);
            outputStream.close();

            // reading answer from server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String result = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();
            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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