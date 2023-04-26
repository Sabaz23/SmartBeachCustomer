package com.maiot.smartbeachcustomer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    private static String Token = "";
    private static int TokenLength = 30;

    private static final String TAG = "Utils";

    private static String ServerUrl = "http://192.168.1.186/";

    private static String UpdateTokenUrl = "http://192.168.1.186/umbrellaapp/elaborateReservation.php";

    public static boolean IsTokenEmpty(){
        return Token.equals("");
    }
    public static boolean CreateToken(int uid, Context ctx){
        String AlphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder s = new StringBuilder(TokenLength);
        for (int i=0; i<30; i++) {
            int ch = (int)(AlphaNumericStr.length() * Math.random());
            s.append(AlphaNumericStr.charAt(ch));
        }
        Token = s.toString();
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
        //Controlla se riesce a connettersi al server dove si trova il file della mappa
        if(isConnectedToThisServer(ServerUrl,1000)) {
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
        }else {
            //Se non riesce a connettersi, ritorna false (il token non è stato caricato)
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

}

