package com.maiot.smartbeachcustomer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";
    private TextView tvumbrellainfo = null;
    private int uid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String tokenPath = getApplicationContext().getFilesDir().getPath().toString()
                + "/" + getString(R.string.TOKEN_FILENAME);

        File tokenFile = new File(tokenPath);
        try {
            if (tokenFile.exists())
                Utils.LoadToken(tokenFile, getApplicationContext());
            else
                Utils.GenerateToken(tokenFile, getApplicationContext());
        }catch(IOException e)
        {
            Log.e(TAG, "Problema nell'apertura del file");
        }
        Intent intent = getIntent();

        tvumbrellainfo = findViewById(R.id.ombrellainfo);
        if(tokenFile.exists())
        {
            tvumbrellainfo.setText("Informazioni sul tuo ombrellone:");
            //Altre cose
        }else
        {
            tvumbrellainfo.setText("Scannerizza un ombrellone per cominciare.");
        }

    }






}