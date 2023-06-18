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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    private static String Token = "";
    private static int TokenLength = 30;

    private static final String TAG = "Utils";

    public static String ServerUrl = "http://localhost/";

    public static final int Timeout = 5000;

    private static String UpdateTokenUrl = ServerUrl + "umbrellapp/elaborateReservation.php";

    private static String GetTokenUrl = ServerUrl + "umbrellapp/getToken.php";

    private static String GetMyUmbrellasUrl = ServerUrl + "umbrellapp/getMyUmbrellas.php";

    private static final float PrezzoAlMinuto = 0.07f;

    public static float getPrezzoAlMinuto() {return PrezzoAlMinuto;}

    public static float getPrezzoDaPagare(Calendar sd)
    {
        Date sdDate = sd.getTime();
        Date fdDate = Calendar.getInstance().getTime();
        long diffInSecs = (fdDate.getTime()-sdDate.getTime())/1000;
        long diff = TimeUnit.MINUTES.convert(diffInSecs,TimeUnit.SECONDS);
        float prezzo = diff * PrezzoAlMinuto;
        BigDecimal bd = new BigDecimal(Double.toString(prezzo));
        bd = bd.setScale(2, RoundingMode.HALF_DOWN);
        return bd.floatValue();
    }

    public static boolean IsMyToken(String tk) { return Token.equals(tk); }

    public static String GetRemoteTokenAndDate(int uid)
    {
        final OkHttpClient client = new OkHttpClient();

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

    public static boolean AssignToken(int uid, String token, String inizioprenotazione, Context ctx){
        if(UpdateToken(uid, token, inizioprenotazione))
        {
            Log.i(TAG, "Update Token andato a buon fine");
            return true;
        }else{
            Log.i(TAG, "Update Token fallito!");
            return false;
        }

    }

    private static boolean UpdateToken(int uid, String token, String inizioprenotazione)
    {
            final OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("uid", Integer.toString(uid))
                    .add("token", token)
                    .add("inizioprenotazione", inizioprenotazione)
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

    public static String[] GetMyUmbrellas()
    {
        final OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("token", Token)
                .build();

        Request request = new Request.Builder()
                .url(GetMyUmbrellasUrl)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string().split(";");
        } catch (IOException e) {
            return new String[]{};
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

    public static String getToken() {
        return Token;
    }

    public static void CreateOrLoadToken(File tokenFile, Context ctx)
    {
        try {
            if (tokenFile.exists())
                Utils.LoadToken(tokenFile, ctx);
            else
                Utils.GenerateToken(tokenFile, ctx);
        }catch(IOException e)
        {
            Log.e(TAG, "Problema nell'apertura del file");
        }
    }

}



