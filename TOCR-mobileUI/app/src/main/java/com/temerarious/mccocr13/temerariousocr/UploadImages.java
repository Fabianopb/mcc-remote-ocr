package com.temerarious.mccocr13.temerariousocr;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by fabiano.brito on 26/11/2016.
 */

class UploadImages extends AsyncTask<String,Void,String> {

    public OCRActivity source = null;
    private Context context;
    //ProgressDialog loading;

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
        String filename = "test_ocr.jpg";
        Bitmap image = source.image;
        String boundary = "---" + System.currentTimeMillis() + "---";
        String line_feed = "\r\n";
        String charset = "UTF-8";

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
            httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            //httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "*****");
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.connect();

            /*
            DataOutputStream outputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            outputStream.writeBytes("--" + "*****" + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                    "uid"
                    image_key + "\";filename=\"" + image_name + "\"" + "\r\n");
            outputStream.writeBytes("\r\n");
            */

            //byte[] uid_key = ("uid=" + uid).getBytes("UTF-8");
            //byte[] seq_key = ("seq=" + seq).getBytes("UTF-8");
            //byte[] image_key = ("image=" + image).getBytes("UTF-8");
            //OutputStream outputStream = httpsURLConnection.getOutputStream();
            //outputStream.write(uid_key);
            //outputStream.write(seq_key);
            //outputStream.write(image_key);
            //outputStream.write("---".getBytes("UTF-8"));
            //outputStream.close();

            DataOutputStream outputStream = new DataOutputStream(httpsURLConnection.getOutputStream());

            outputStream.writeBytes("--" + boundary + line_feed);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uid\"" + line_feed + line_feed + uid + line_feed);
            outputStream.writeBytes("--" + boundary + line_feed);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"seq\"" + line_feed + line_feed + seq + line_feed);
            outputStream.writeBytes("--" + boundary + line_feed);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"" + line_feed);
            outputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(filename) + line_feed);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + line_feed);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            outputStream.writeBytes(encodedImage);

            outputStream.writeBytes(line_feed + line_feed);

            outputStream.writeBytes("--" + boundary + "--");
            outputStream.flush();
            outputStream.close();

            /*
            FileInputStream inputStream = new FileInputStream(image);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(line_feed);
            writer.flush();*/

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
                Toast.makeText(context, "WORKS!!!!" + jsonObj.toString(), Toast.LENGTH_SHORT).show();
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