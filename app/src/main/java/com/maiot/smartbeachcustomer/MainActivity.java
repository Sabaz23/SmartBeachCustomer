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

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";
    private TextView tvumbrellainfo = null;
    private int uid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String tokenPath = getApplicationContext().getFilesDir().getPath().toString()
                + "/" + getString(R.string.TOKEN_FILENAME);
        tvumbrellainfo = findViewById(R.id.ombrellainfo);

        File tokenFile = new File(tokenPath);
        if(tokenFile.exists())
        {
            tvumbrellainfo.setText("Informazioni sul tuo ombrellone:");
        }else
        {
            tvumbrellainfo.setText("Scannerizza un ombrellone per cominciare.");
        }

        Log.i(TAG, "Intent action: " + intent.getAction());

        if(intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            ParseNfcMessage(intent);
            if(Utils.CreateToken(uid,getApplicationContext()))
            {
                //Riempire schermata con info su ombrellone
            }
        }
    }




    void ParseNfcMessage(Intent intent)
    {
        Parcelable[] NdefMessageArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndefMessage = (NdefMessage) NdefMessageArray[0];
        String msg = new String(ndefMessage.getRecords()[0].getPayload());
        uid = Integer.parseInt(msg);
        Toast.makeText(getApplicationContext(),Integer.toString(uid),Toast.LENGTH_LONG).show();
    }

}