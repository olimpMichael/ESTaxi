package ru.michael.estaxi;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michael on 20.06.2017.
 */

class DownloadTask extends AsyncTask<String, Void, ArrayList<HashMap<String, Double>> > {
    boolean flagNoConnect = false;

    @Override
    protected ArrayList<HashMap<String, Double>> doInBackground(String... path) {

        //################## Download File #####################
        try {
            URL url = new URL(path[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            //create file
            File cacheDir = MainActivity.context.getCacheDir();
            File file = new File(cacheDir.getPath() + "/" + "esTaxi.txt");

            if (file.exists()){
                Log.d(MainActivity.LOG_TAG, "file.exists()");
                file.delete();
            }
            file.createNewFile();
            Log.d(MainActivity.LOG_TAG, "file.createNewFile()");


            InputStream is = conection.getInputStream();
            BufferedInputStream inputStream = new BufferedInputStream(is, 1024 * 5);

            FileOutputStream outputStream = new FileOutputStream(file);

            byte data[] = new byte[5 * 1024];
            int count;
            while ((count = inputStream.read(data)) != -1) {
                Log.d(MainActivity.LOG_TAG, "count" + count);
                outputStream.write(data, 0, count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            flagNoConnect = true;
            Log.e("Error: ", e.getMessage());
            return null;
        }

        //################## Read File #####################
        ArrayList<HashMap<String, Double>> coordsList = new ArrayList<HashMap<String, Double>>();
        HashMap<String, Double> hashMap;
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray jsonArray = obj.getJSONArray("coords");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json_inside = jsonArray.getJSONObject(i);
                double lat_value = Double.parseDouble(json_inside.getString("la"));
                double lon_value = Double.parseDouble(json_inside.getString("lo"));

                hashMap = new HashMap<String, Double>();
                hashMap.put("lat", lat_value);
                hashMap.put("lon", lon_value);
                coordsList.add(hashMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(MainActivity.LOG_TAG, "coordsList" + coordsList);
        return coordsList;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, Double>> content) {
        // if 'No Connection to Server'
        if (flagNoConnect) {
            Toast.makeText(MainActivity.context, "No connection to the server", Toast.LENGTH_LONG).show();
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            File cacheDir = MainActivity.context.getCacheDir();
            File file = new File(cacheDir.getPath() + "/" + "esTaxi.txt");
            FileInputStream inputStream = new FileInputStream(file);

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);

            Log.d(MainActivity.LOG_TAG, "buffer = " + buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
