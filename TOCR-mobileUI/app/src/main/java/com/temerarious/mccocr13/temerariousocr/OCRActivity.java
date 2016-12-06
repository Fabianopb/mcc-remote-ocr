package com.temerarious.mccocr13.temerariousocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;

import static android.R.attr.width;
import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;
import static com.temerarious.mccocr13.temerariousocr.R.attr.height;
import static java.security.AccessController.getContext;


public class OCRActivity extends AppCompatActivity {

    private OCRInitializer ocrInitializer = new OCRInitializer(this, this);

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Bitmap image;
    private TextView imgSelectorStatus;
    String[] type = {"Local", "Remote", "Benchmark"};
    String selectedMode = type[0];
    ImageView imgCamera, imgGalery;
    ProgressDialog progressDoalog;
    Button button_save;
    Uri imageUri;
    int i;

    public ArrayList<String> imageName = new ArrayList<String>();
    public ArrayList<byte[]> imageStream = new ArrayList<byte[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        ocrInitializer.initOCR();

        imgCamera = (ImageView) findViewById(R.id.camera);
        imgGalery = (ImageView) findViewById(R.id.gallery);


        imgCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                select_from_camera();
            }
        });
        imgGalery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                select_from_galery();
            }
        });

        imgSelectorStatus = (TextView) findViewById(R.id.img_selector_status);
        imgSelectorStatus.setText(getString(R.string.status_no_image));



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select Type");
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMode = type[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });



    }

    public void select_from_galery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    public void select_from_camera() {
        String filename = "OCR.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUEST_CAMERA)
                try {
                    onCaptureImageResult(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void onCaptureImageResult(Uri uri) throws IOException {
        InputStream is = null;
        String text = "";
        try {
            is = this.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            image=bitmap;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            imageStream.add(stream.toByteArray());
            imageName.add(System.currentTimeMillis() + ".jpg");

            /*if (imageStream.size() > 0) {
                imageStream.clear();
                imageName.clear();
            }*/
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        imgSelectorStatus.setText(getString(R.string.status_img_camera));

        /*Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        //thumbnail = Bitmap.createScaledBitmap(thumbnail, 500, 500, true);
        image = thumbnail;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, stream);*/





        File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            file.createNewFile();
            fo = new FileOutputStream(file);
            fo.write(imageStream.get(0));
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        imgSelectorStatus.setText(getString(R.string.status_img_gallery_1) + " 1 " + getString(R.string.status_img_gallery_2));

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);

        if (imageStream.size() > 0) {
            imageStream.clear();
            imageName.clear();
        }

        imageStream.add(stream.toByteArray());
        imageName.add(System.currentTimeMillis() + ".jpg");

        image = bm;

    }

    public void previewImage(View view) {

        if (image == null) {
            Toast.makeText(this, getString(R.string.toast_no_images), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
            intent.putExtra("number of images", imageStream.size());
            for (int i = 0; i < imageStream.size(); i++) {
                intent.putExtra("byte-array-" + String.valueOf(i), imageStream.get(i));
            }
            startActivity(intent);
        }
    }

    public void processImage(View view) {

        // If mode = Local
        if (selectedMode.equals(type[0])) {
            final String ocrResult = ocrInitializer.runOCR(image);
            displayTranslatedText(ocrResult);
            button_save = (Button) findViewById(R.id.button_save);
            button_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveToText(ocrResult);
                }
            });
            final Handler handle = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    progressDoalog.incrementProgressBy(1);
                }
            };
            progressDoalog = new ProgressDialog(OCRActivity.this);
            progressDoalog.setMax(10);
            progressDoalog.setMessage("OCR processing");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDoalog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (progressDoalog.getProgress() <= progressDoalog
                                .getMax()) {
                            Thread.sleep(200);
                            handle.sendMessage(handle.obtainMessage());
                            if (progressDoalog.getProgress() == progressDoalog
                                    .getMax()) {
                                progressDoalog.dismiss();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        // If mode = Remote
        else if (selectedMode.equals(type[1])) {
            String images_total = "1";
            PrepareRemote prepareRemote = new PrepareRemote(OCRActivity.this, OCRActivity.this);
            prepareRemote.execute(images_total);
        }
        // If mode = Benchmark
        else if (selectedMode.equals(type[2])) {

        }

    }

    public void openRecords(View view) {
        Intent intent = new Intent(getApplicationContext(), RecordsActivity.class);
        startActivity(intent);
    }

    public void displayTranslatedText(String result) {
        TextView ocrTextView = (TextView) findViewById(R.id.OCRTextView);
        ocrTextView.setText(result);
    }


    public void saveToText(String text) {
        try {
            //File myFile = new File("/sdcard/OCRfiles/"+System.currentTimeMillis()+".txt");
            File myFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getBaseContext(),
                    "Saved",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}