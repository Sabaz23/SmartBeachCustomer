package com.maiot.smartbeachcustomer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ReservationPage extends AppCompatActivity {

    private final String TAG = "ReservationPage";

    private TextView tvtitle,tvprezzo,tvombrellone,tvinizioprenotazione;

    Button bttok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_page);

        tvtitle = findViewById(R.id.tvtitolo);
        tvombrellone = findViewById(R.id.tvombrellone);
        tvprezzo = findViewById(R.id.tvprezzo);
        tvinizioprenotazione = findViewById(R.id.tvdatainizioprenotazione);

        bttok = findViewById(R.id.bttok);

        if(Utils.IsTokenEmpty())
        {
            tvtitle.setText(getString(R.string.TITLE_FREE));
        }
    }
}