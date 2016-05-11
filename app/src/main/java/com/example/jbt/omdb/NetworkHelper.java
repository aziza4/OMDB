package com.example.jbt.omdb;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHelper {

    private URL url;

    public NetworkHelper(URL url) {
        this.url = url;
    }

    public String GetJsonString()
    {
        String jsonString = "";
        HttpURLConnection con = null;

        try {

            con = (HttpURLConnection)url.openConnection();

            int resCode = con.getResponseCode();

            if (resCode != HttpURLConnection.HTTP_OK)
                return null;

            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while((line = r.readLine()) != null)
                jsonString += line;

        } catch (IOException e) {

            Log.e(MainActivity.LOG_CAT, e.getMessage());

        } finally {

            if (con != null)
                con.disconnect();
        }

        return jsonString;
    }
}
