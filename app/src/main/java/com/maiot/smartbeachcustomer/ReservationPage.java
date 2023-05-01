package com.maiot.smartbeachcustomer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.internal.Util;

public class ReservationPage extends AppCompatActivity {

    private final String TAG = "ReservationPage";

    private int uid = 0;

    private Calendar inizioprenotazione = Calendar.getInstance();

    private TextView tvtitle, tvprezzo, tvombrellone, tvinizioprenotazione;

    private String RemoteToken = null;
    private final SimpleDateFormat sdfDisplay = new SimpleDateFormat("HH:mm:ss", Locale.ITALIAN);
    Button bttok = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_page);
        tvtitle = findViewById(R.id.tvtitolo);
        tvombrellone = findViewById(R.id.tvombrellone);
        tvprezzo = findViewById(R.id.tvprezzo);
        tvinizioprenotazione = findViewById(R.id.tvdatainizioprenotazione);


        bttok = findViewById(R.id.bttok);

        bttok.setOnClickListener(this::ButtonBehaviour);

        ParseNfcMessage(getIntent());

        Thread SetViewsText = new Thread(() -> {
            if (Utils.isConnectedToThisServer(Utils.ServerUrl, Utils.Timeout)) {
                SetRemoteTokenAndDate();
                runOnUiThread(this::SetViewsTextMethod);
            } else {
                runOnUiThread(this::SetViewsWrong);
            }
        });

        SetViewsText.start();


    }

    void ButtonBehaviour(View v)
    {
        Button b = (Button) v;
        String free = getString(R.string.BUTTON_FREE);
        String wrong = getString(R.string.BUTTON_WRONG);
        String occupied = getString(R.string.BUTTON_OCCUPIED);
        if(b.getText().toString().equals(free))
        {
            Thread thr = new Thread(() -> {
                if(Utils.isConnectedToThisServer(Utils.ServerUrl,Utils.Timeout))
                {
                    Log.i(TAG, "inizio pren " + inizioprenotazione.getTime().toString());
                    if(Utils.AssignToken(uid,
                            Utils.getToken(),
                            sdfDisplay.format(inizioprenotazione.getTime()),
                            getApplicationContext()))
                        runOnUiThread(() -> Toast.makeText(this,"Ombrellone prenotato!",Toast.LENGTH_LONG).show());
                    else
                        runOnUiThread(() -> Toast.makeText(this,"Problema nella prenotazione.",Toast.LENGTH_LONG).show());
                    startActivity(new Intent(ReservationPage.this,MainActivity.class));
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(this,"Problema di connessione.",Toast.LENGTH_LONG).show());
                }
            });
            thr.start();
        }else if(b.getText().toString().equals(wrong)) finish();
        else if(b.getText().toString().equals(occupied))
        {
            Thread thr = new Thread(() -> {
                if(Utils.isConnectedToThisServer(Utils.ServerUrl,Utils.Timeout))
                {
                    if(Utils.AssignToken(uid, "null", "null", getApplicationContext()))
                        runOnUiThread(() -> Toast.makeText(this,"Ombrellone liberato!",Toast.LENGTH_LONG).show());
                    else
                        runOnUiThread(() -> Toast.makeText(this,"Problema nella liberazione.",Toast.LENGTH_LONG).show());
                    startActivity(new Intent(ReservationPage.this,MainActivity.class));
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(this,"Problema di connessione.",Toast.LENGTH_LONG).show());
                }
            });
            thr.start();
        }
    }

    void ParseNfcMessage(Intent intent) {
        Parcelable[] NdefMessageArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndefMessage = (NdefMessage) NdefMessageArray[0];
        String msg = new String(ndefMessage.getRecords()[0].getPayload());
        uid = Integer.parseInt(msg);
    }

    void SetRemoteTokenAndDate() {
        String[] t = Utils.GetRemoteTokenAndDate(uid).split(" ");

        if(t.length == 0){
            RemoteToken = "";
            inizioprenotazione = Calendar.getInstance();
            return;
        }

        RemoteToken = t[0];

        try {
            if (t[1] != null){
                Log.i(TAG,"T1 " + t[1]);
                inizioprenotazione.setTime(sdfDisplay.parse(t[1]));
                //Questo lo facciamo perchè si suppone che tutti gli ombrelloni vengano prenotati
                //e liberati lo stesso giorno.
                inizioprenotazione.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                inizioprenotazione.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
                inizioprenotazione.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            }

            else
                inizioprenotazione = Calendar.getInstance();
        } catch (ParseException e) {
            Log.e(TAG, "Non sono riuscito a parsare la data di inizio.");
        }
    }

    @SuppressLint("SetTextI18n")
    void SetViewsTextMethod() {

        Log.i(TAG,"Token del telefono: " + Utils.getToken());
        Log.i(TAG,"Token remoto dell'ombrellone: " + RemoteToken);

        //Se il token dell'ombrellone che ho scannerizzato NON è vuoto (l'ombrellone è occupato)
        if (!RemoteToken.equals("")) {
            //Se il token dell'ombrellone scannerizzato è il mio
            if (Utils.IsMyToken(RemoteToken)) {
                tvtitle.setText(getString(R.string.TITLE_OCCUPIED));
                tvtitle.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
                tvombrellone.setText(getString(R.string.OMBRELLONE) + " " + uid);
                Date tmp = inizioprenotazione.getTime();
                tvinizioprenotazione.setText(getString(R.string.INIZIOPRENOTAZIONE) + " " + sdfDisplay.format(tmp));
                tvprezzo.setText(getString(R.string.PREZZODAPAGARE) + " " + Utils.getPrezzoDaPagare(inizioprenotazione) + "€");
                bttok.setText(getString(R.string.BUTTON_OCCUPIED));
            } else //Se il token dell'ombrellone scannerizzato NON è il mio
            {
                tvtitle.setText(getString(R.string.TITLE_WRONG));
                tvtitle.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
                bttok.setText(getString(R.string.BUTTON_WRONG));
            }
        } else //Se il token è vuoto allora l'ombrellone è libero
        {
            tvtitle.setText(getString(R.string.TITLE_FREE));
            tvtitle.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
            tvombrellone.setText(getString(R.string.OMBRELLONE) + " " + uid);
            Date tmp = inizioprenotazione.getTime();
            tvinizioprenotazione.setText(getString(R.string.INIZIOPRENOTAZIONE) + " " + sdfDisplay.format(tmp));
            tvprezzo.setText(getString(R.string.PREZZOALMINUTO) + " " + Utils.getPrezzoAlMinuto() + "€");
            bttok.setText(getString(R.string.BUTTON_FREE));
        }
    }

    void SetViewsWrong()
    {
        tvtitle.setText(getString(R.string.TITLE_NOCONN));
        tvtitle.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
        bttok.setText(getString(R.string.BUTTON_WRONG));
    }

}