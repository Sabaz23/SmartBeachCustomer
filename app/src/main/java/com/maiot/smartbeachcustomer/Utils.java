package com.maiot.smartbeachcustomer;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    private static String Token = "";
    private static int TokenLength = 30;

    private static final String TAG = "Utils";

    public static String ServerUrl = "http://192.168.1.186/";

    private static String UpdateTokenUrl = "http://192.168.1.186/umbrellaapp/elaborateReservation.php";

    private static String GetTokenUrl = "http://192.168.1.186/umbrellaapp/getToken.php";

    private static final float PrezzoAlMinuto = 0.07f;

    public float getPrezzoAlMinuto() {return PrezzoAlMinuto;}

    public float getPrezzoDaPagare(Calendar sd)
    {
        //Da fare calcolo
        return 0;
    }

    public static boolean IsMyToken(String tk) { return Token.equals(tk); }

    public static String GetRemoteTokenAndDate(int uid)
    {
        final OkHttpClient client = new OkHttpClient();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        RequestBody formBody = new FormBody.Builder()
                .add("uid", Integer.toString(uid))
                .build();

        Request request = new Request.Builder()
                .url(GetTokenUrl)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG,"Errore nella connessione per il token remoto " + e.getMessage());
            return "";
        }
    }

    public static void GenerateToken(File save, Context appContext) throws IOException {
        String AlphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder s = new StringBuilder(TokenLength);
        for (int i=0; i<30; i++) {
            int ch = (int)(AlphaNumericStr.length() * Math.random());
            s.append(AlphaNumericStr.charAt(ch));
        }
        Token = s.toString();

        FileInputStream fis = null;

        try {
            fis = appContext.openFileInput(save.getName());
        } catch (IOException e) {
            Log.e(TAG,"Errore nella apertura del file " + e.getMessage());
        }


        FileOutputStream fos = appContext.openFileOutput(save.getName(), Context.MODE_PRIVATE);
        fos.write(Token.getBytes(StandardCharsets.UTF_8));


    }

    public static boolean AssignToken(int uid, Context ctx){
        if(UpdateToken(uid))
        {
            Toast.makeText(ctx,"Ombrellone prenotato!", Toast.LENGTH_LONG).show();
            return true;
        }else{
            Toast.makeText(ctx, "Errore nella prenotazione.", Toast.LENGTH_SHORT).show();
            Token = "";
            return false;
        }

    }

    private static boolean UpdateToken(int uid)
    {
            final OkHttpClient client = new OkHttpClient();

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            RequestBody formBody = new FormBody.Builder()
                    .add("uid", Integer.toString(uid))
                    .add("token", Token)
                    .add("inizioprenotazione", timestamp.toString())
                    .build();

            Request request = new Request.Builder()
                    .url(UpdateTokenUrl)
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return true;
            } catch (IOException e) {
                return false;
            }
    }

    public static boolean isConnectedToThisServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            Log.e(TAG,"Errore nella connessione : " + e);
            return false;
        }


    }

    public static void LoadToken(File load, Context appContext) throws IOException {
        FileInputStream fis = null;
        try {
            fis = appContext.openFileInput(load.getName());
        } catch (IOException e) {
            Log.e(TAG,"Errore nella apertura del file " + e.getMessage());
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            Token = reader.readLine();
        }

    }
}

