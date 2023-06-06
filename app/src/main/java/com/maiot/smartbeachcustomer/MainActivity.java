package com.maiot.smartbeachcustomer;

import androidx.appcompat.app.AppCompatActivity;

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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";
    private TextView tvumbrellainfo, tvumbrellanum, tviniziopren, tvprezzodapagare;
    private Button bttnext;
    private int uid = 0;


    private int CurrentDisplaying = 0;

    private final SimpleDateFormat sdfDisplay = new SimpleDateFormat("HH:mm:ss", Locale.ITALIAN);

    String[] MyUmbrellas = null;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private Calendar StartCalendar = Calendar.getInstance();

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String tokenPath = getApplicationContext().getFilesDir().getPath().toString()
                + "/" + getString(R.string.TOKEN_FILENAME);

        File tokenFile = new File(tokenPath);

        Utils.CreateOrLoadToken(tokenFile,getApplicationContext());

        tvumbrellainfo = findViewById(R.id.ombrellainfo);
        tvumbrellanum = findViewById(R.id.tvumbrellaNum);
        tviniziopren = findViewById(R.id.tvinizioPren);
        tvprezzodapagare = findViewById(R.id.tvprezzoDaPagare);
        bttnext = findViewById(R.id.bttnext);
        bttnext.setOnClickListener(bttnextlistener);



        Thread thr = new Thread(() -> {
            if(Utils.isConnectedToThisServer(Utils.ServerUrl,Utils.Timeout)) {
                MyUmbrellas = Utils.GetMyUmbrellas();
                if(MyUmbrellas.length != 1){
                    runOnUiThread(() -> {
                        try {
                            SetViews();
                        } catch (ParseException e) {
                            Log.e(TAG,"Problema nel parsing della data: " + e.getMessage());
                        }
                    });
                }else
                    runOnUiThread(() -> {
                        tvumbrellainfo.setText(R.string.TITLE_HOME_FREE);
                        bttnext.setEnabled(false);
                        bttnext.setVisibility(View.GONE);
                    });
            }else
            {
                runOnUiThread(() -> Toast.makeText(this,"Problema di connessione", Toast.LENGTH_LONG).show());
            }
        });
        thr.start();
    }

    @SuppressLint("SetTextI18n")
    void SetViews() throws ParseException {
        String[] currentUmbrellaDisplaying = MyUmbrellas[CurrentDisplaying].split(" ");
        tvumbrellainfo.setText("ℹ️ "+getString(R.string.TITLE_HOME_OCCUPIED) +" ℹ️ ");
        tvumbrellanum.setText("\uD83C\uDFD6️ "+ getString(R.string.OMBRELLONE) + " " + currentUmbrellaDisplaying[0]);
        tviniziopren.setText("\uD83D\uDD51 " + getString(R.string.INIZIOPRENOTAZIONE) + " " + currentUmbrellaDisplaying[1]);
        StartCalendar.setTime(sdfDisplay.parse(currentUmbrellaDisplaying[1]));
        StartCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        StartCalendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        StartCalendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(updatePrezzoRunnable,0,1, TimeUnit.MINUTES);
        if(MyUmbrellas.length == 2)
        {
            bttnext.setVisibility(View.GONE);
            bttnext.setEnabled(false);
        }
        else
        {
            bttnext.setVisibility(View.VISIBLE);
            bttnext.setEnabled(true);
        }
    }

    private final View.OnClickListener bttnextlistener = view -> {
        executor.shutdownNow();
        if(CurrentDisplaying < MyUmbrellas.length-2) //-2 perchè uno è vuoto (l'ultimo dopo ;) e l'altro è indice 0
            CurrentDisplaying++;
        else
            CurrentDisplaying = 0;
        try {
            SetViews();
        } catch (ParseException e) {
            Log.e(TAG,"Prolema nel parsing della data " + e.getMessage());
        }
    };

    @SuppressLint("SetTextI18n")
    Runnable updatePrezzoRunnable = () -> {
            runOnUiThread(() -> tvprezzodapagare.setText("\uD83D\uDCB0 " + getString(R.string.PREZZODAPAGARE) + " " + Utils.getPrezzoDaPagare(StartCalendar) + "€"));
            Log.i(TAG, "Aggiorno il prezzo da pagare....");
    };

}