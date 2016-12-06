package com.temerarious.mccocr13.temerariousocr.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.temerarious.mccocr13.temerariousocr.R;
import com.temerarious.mccocr13.temerariousocr.activities.OCRActivity;

/**
 * Created by fabiano.brito on 06/12/2016.
 */

public class RunLocalOCR extends AsyncTask<String,Void,String> {

    public OCRActivity source = null;
    private Context context;
    private ProgressDialog loading;

    public RunLocalOCR(OCRActivity fl, Context ctx) {
        source = fl;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        String partialOCRResult = "";

        for (int i = 0; i < source.imageStream.size(); i++) {
            partialOCRResult += source.ocrInitializer.runOCR(source.imageStream.get(i));
        }

        return partialOCRResult;
    }

    @Override
    protected void onPreExecute() {
        loading = ProgressDialog.show(context, source.getResources().getString(R.string.running_local_ocr), null, true, true);
    }

    @Override
    protected void onPostExecute(String result) {
        loading.dismiss();
        if(result != null) {
            source.displayTranslatedText(result);
        } else {
            Toast.makeText(context, R.string.local_ocr_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}