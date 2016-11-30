package com.temerarious.mccocr13.temerariousocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.Map;

import static java.security.AccessController.getContext;


public class OCRActivity extends AppCompatActivity{

    private OCRInitializer ocrInitializer = new OCRInitializer(this, this);

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Bitmap image;
    private ImageView img;
    String[] type = {"Local", "Remote", "Benchmark"};
    String selectedMode = type[0];
    ImageView imgCamera, imgGalery, profilePicImageView;
    ProgressDialog progressDoalog;

    public ArrayList<String> imageName = new ArrayList<String>();
    public ArrayList<byte[]> imageStream = new ArrayList<byte[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        ocrInitializer.initOCR();

        imgCamera=(ImageView) findViewById(R.id.camera);
        imgGalery=(ImageView) findViewById(R.id.gallery);
        profilePicImageView = (ImageView) findViewById(R.id.profilePicture);
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.user_default);
        profilePicImageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(getApplicationContext(), icon, 200, 200, 200, false, false, false, false));

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

        //init image
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        img=(ImageView) findViewById(R.id.imageView);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select Type");
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMode = type[position];
                Toast.makeText(getBaseContext(), "Selected mode = " + selectedMode, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void select_from_galery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }
    public void select_from_camera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                try {
                    onCaptureImageResult(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void onCaptureImageResult(Intent data) throws IOException {
        /*Uri uri = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
        img.setImageBitmap(bitmap);*/

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        thumbnail = Bitmap.createScaledBitmap(thumbnail, 500, 500, true);
        image = thumbnail;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, stream);

        if (imageStream.size() > 0) {
            imageStream.clear();
            imageName.clear();
        }

        img.setImageBitmap(thumbnail);

        imageStream.add(stream.toByteArray());
        imageName.add(System.currentTimeMillis() + ".jpg");

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
        img.setImageBitmap(bm);

    }

    public void processImage(View view){
        final Handler handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressDoalog.incrementProgressBy(1);
            }
        };
        progressDoalog = new ProgressDialog(OCRActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Its loading....");
        progressDoalog.setTitle("ProgressDialog bar example");
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


        // If mode = Local
        if(selectedMode.equals(type[0])) {
            String ocrResult = ocrInitializer.runOCR(image);
            displayTranslatedText(ocrResult);
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

}