package com.maiot.smartbeachcustomer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

import okhttp3.internal.Util;

public class ReservationPage extends AppCompatActivity {

    private final String TAG = "ReservationPage";

    private int uid, inizioprenotazione = 0;

    private TextView tvtitle,tvprezzo,tvombrellone,tvinizioprenotazione;

    private String RemoteToken = null;

    Button bttok;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_page);
        tvtitle = findViewById(R.id.tvtitolo);
        tvombrellone = findViewById(R.id.tvombrellone);
        tvprezzo = findViewById(R.id.tvprezzo);
        tvinizioprenotazione = findViewById(R.id.tvdatainizioprenotazione);

        bttok = findViewById(R.id.bttok);

        ParseNfcMessage(getIntent());

        String[] t = Utils.GetRemoteTokenAndDate(uid).split("");

        RemoteToken = t[0];
        inizioprenotazione = Integer.parseInt(t[1]);

        if(Utils.isConnectedToThisServer(Utils.ServerUrl, 1000))
        {
            if(!RemoteToken.equals(""))
            {
                if(Utils.IsMyToken(RemoteToken))
                {
                    tvtitle.setText(getString(R.string.TITLE_OCCUPIED));
                    tvombrellone.setText(getString(R.string.OMBRELLONE) + uid);
                    tvinizioprenotazione.setText(getString(R.string.INIZIOPRENOTAZIONE) + inizioprenotazione);

                }
            }
        }
        else
        {
            tvtitle.setText("Non puoi prenotare un ombrellone senza connessione");
            bttok.setText("Ok");
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