package com.example.jbt.omdb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class NetworkHelper {

    private URL mUrl;

    public NetworkHelper(URL url)
    {
        this.mUrl = url;
    }

    public NetworkHelper(String urlString)
    {
        try {

            this.mUrl = new URL(urlString);

        } catch (MalformedURLException e) {

            Log.e(MainActivity.LOG_CAT, e.getMessage());
        }
    }

    public String GetJsonString()
    {
        String jsonString = "";
        HttpURLConnection con = null;

        try {

            con = (HttpURLConnection) mUrl.openConnection();

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


    public Bitmap GetImage()
    {
        InputStream stream = null;

        try {

            stream = (InputStream) mUrl.getContent();

            if (stream == null)
                return null;

        } catch (IOException e) {
            Log.e(MainActivity.LOG_CAT, e.getMessage());
        }

        return BitmapFactory.decodeStream(stream);
    }
}
