package com.temerarious.mccocr13.temerariousocr;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        String amountOfRecords = "10";

        FetchRecords fetchRecords = new FetchRecords(RecordsActivity.this, RecordsActivity.this);
        fetchRecords.execute(amountOfRecords);
    }

    public void createRecordsList(JSONArray recordsJSONArray) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final ArrayList<String> recordsList = new ArrayList<String>();
        final ArrayList<ArrayList<String>> imagesListsList = new ArrayList<ArrayList<String>>();
        final ArrayList<ArrayList<String>> thumbsListsList = new ArrayList<ArrayList<String>>();

        int totalRecords = recordsJSONArray.length();
        for (int i = 0; i < totalRecords; i++) {

            try {
                JSONObject record = recordsJSONArray.getJSONObject(i);
                String creationTime = record.getString("creation_time");
                String ocr_text = record.getString("ocr_text");
                recordsList.add(ocr_text);

                JSONArray imagesJSONArray = record.getJSONArray("image_fs_ids");
                ArrayList<String> imagesList = new ArrayList<String>();
                ArrayList<String> thumbsList = new ArrayList<String>();

                int totalImages = imagesJSONArray.length();
                for (int j = 0; j < totalImages; j++) {
                    JSONObject imageIDs = imagesJSONArray.getJSONObject(j);
                    String imageID = imageIDs.getString("image_fs_id");
                    imagesList.add(imageID);
                    String thumbID = imageIDs.getString("thumbnail_fs_id");
                    thumbsList.add(thumbID);
                }
                imagesListsList.add(imagesList);
                thumbsListsList.add(thumbsList);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(RecordsActivity.this);
        String server_ip = SP.getString("server_ip", getResources().getString(R.string.server_default_ip));

        ListView list = (ListView) findViewById(R.id.records_listview);
        RecordsListAdapter adapter = new RecordsListAdapter(this, recordsList, thumbsListsList, server_ip);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // start a new activity with the full text and the full size images
                // pass the imagesList in the position "position" (imagesListList.get(position)) into the activity

            }
        });
    }
}
