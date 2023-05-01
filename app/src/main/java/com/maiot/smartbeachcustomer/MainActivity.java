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

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";
    private TextView tvumbrellainfo, tvumbrellanum, tviniziopren, tvprezzodapagare;
    private Button bttnext;
    private int uid = 0;

    private int CurrentDisplaying = 0;

    private final SimpleDateFormat sdfDisplay = new SimpleDateFormat("HH:mm:ss", Locale.ITALIAN);

    String[] MyUmbrellas = null;

    private Thread updatePrezzoThread;

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
        try {
            if (tokenFile.exists())
                Utils.LoadToken(tokenFile, getApplicationContext());
            else
                Utils.GenerateToken(tokenFile, getApplicationContext());
        }catch(IOException e)
        {
            Log.e(TAG, "Problema nell'apertura del file");
        }

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
        tvumbrellainfo.setText(getString(R.string.TITLE_HOME_OCCUPIED));
        tvumbrellanum.setText(getString(R.string.OMBRELLONE) + currentUmbrellaDisplaying[0]);
        tviniziopren.setText(getString(R.string.INIZIOPRENOTAZIONE) + currentUmbrellaDisplaying[1]);
        StartCalendar.setTime(sdfDisplay.parse(currentUmbrellaDisplaying[1]));
        StartCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        StartCalendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        StartCalendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        updatePrezzoThread = new Thread(() -> {
            while(!updatePrezzoThread.isInterrupted()) {
                try {
                runOnUiThread(() -> tvprezzodapagare.setText(getString(R.string.PREZZODAPAGARE) + Utils.getPrezzoDaPagare(StartCalendar) + "€"));
                Log.i(TAG, "Aggiorno il prezzo da pagare....");
                Thread.sleep(60*1000);
                } catch (InterruptedException e) {
                }
            }
        });
        updatePrezzoThread.start();
        if(MyUmbrellas.length == 2) bttnext.setEnabled(false);
        else bttnext.setEnabled(true);
    }

    private View.OnClickListener bttnextlistener = view -> {
        updatePrezzoThread.interrupt();
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

}