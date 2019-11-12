package es.ua.eps.readwifi;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IPfinder extends AsyncTask<Void,Void,String> {


    @Override
    protected String doInBackground(Void... argumentos) {
        HttpURLConnection httpURLConnection;
        URL url = null;
        try {
            String ip = "http://ipv4bot.whatismyipaddress.com";
            url = new URL(ip);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            //Establecemos una petición GET a la url proporcionada y obtenemos su respuesta
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            int code = httpURLConnection.getResponseCode();
            if(code == HttpURLConnection.HTTP_OK){
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                StringBuffer stringBuffer = new StringBuffer();
                while ((line=reader.readLine())!=null){
                    stringBuffer.append(line);
                }
                return stringBuffer.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
