package com.temerarious.mccocr13.temerariousocr.helpers;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.temerarious.mccocr13.temerariousocr.activities.DetailsActivity;
import com.temerarious.mccocr13.temerariousocr.activities.OCRActivity;
import com.temerarious.mccocr13.temerariousocr.activities.ResultActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by ivan on 09.12.16.
 */

public class SaveResultHelper {


    private Context context;
    public SaveResultHelper(Context mContext) {
        context = mContext;
    }


    public void saveToText(String text) {
        try {
            File myFile = new File(Environment.getExternalStorageDirectory().getPath(), "OCR_result_"+System.currentTimeMillis() + ".txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(context,
                    "Saved",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
