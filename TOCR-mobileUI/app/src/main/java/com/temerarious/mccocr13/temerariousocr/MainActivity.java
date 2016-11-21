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

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // declaration of variables for fragment ets
    EditText etLogUsername, etLogPassword;

    // declaration of strings for login stage
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}