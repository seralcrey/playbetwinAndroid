package com.example.sergio.playbetwincliente;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class Perfil extends Fragment {


    Principal p;

    public Perfil( Principal p) {
        this.p = p;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_perfil, container, false);


        DownloadTask ressul = new DownloadTask(1);
        String consulta = ("");
        ressul.execute(consulta);

        return v ;
    }




    public class DownloadTask extends AsyncTask<String, Void, String> {

        boolean termino = false;
        String resultado;
        int opcion;

        public DownloadTask(int opcion){
            this.opcion = opcion;
        }


        @Override
        protected String doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread
            try {
                return downloadContent(params[0]);
            } catch (IOException e) {
                return "Unable to retrieve data. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.trim();

            String[] res = result.split("Â¬");


        }

        public String getResultado() {
            return resultado;
        }

        public boolean getTermino(){
            return termino;
        }

        private String downloadContent(String myurl) throws IOException {
            InputStream is = null;
            int length = 500;

            try {
                URL url = new URL("http://192.168.0.201:8080/usuario/consulta.php?consulta="+myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("The response is: " , " "+ response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = convertInputStreamToString(is, length);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String convertInputStreamToString(InputStream stream, int length) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "ISO-8859-1");
            char[] buffer = new char[length];
            reader.read(buffer);
            return new String(buffer);
        }



    }
}
