package com.temerarious.mccocr13.temerariousocr;

/**
 * Created by ivan on 21.11.16.
 */

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {

    // declaration of variables for fragment ets
    EditText etLogUsername, etLogPassword;

    // declaration of strings for login stage
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);


        if (fragment == null) {
            fragment = new FacebookFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
        etLogUsername = (EditText) findViewById(R.id.etLogUsername);
        etLogPassword = (EditText) findViewById(R.id.etLogPassword);


        // Login button
        Button btnLogin = (Button) findViewById(R.id.login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etLogUsername.getText().toString();
                password = etLogPassword.getText().toString();


                if (username.equals("test1") && (password.equals("secret1")) || (username.equals("test2") && (password.equals("secret2"))
                        || (username.equals("test3") && (password.equals("secret3"))))) {
                    Intent intent = new Intent(getApplicationContext(), OCRActivity.class);
                    startActivity(intent);
                }

                // send username:password to database and check is it correct
                    /*LoginBW loginBW = new LoginBW(MainActivity.this, getApplicationContext());
                    loginBW.execute(username, password);*/
                else {
                    Toast.makeText(getApplicationContext(), R.string.wrong_pass, Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


    // internet connection state check
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item:
                //Toast.makeText(this, "ADD!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}