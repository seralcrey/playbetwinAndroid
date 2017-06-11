package com.example.sergio.playbetwincliente;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Login extends Activity {


    TextView usuario,pass, error;
    String direccion = "http://192.168.1.3:8080";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usuario = (TextView) findViewById(R.id.textUsuario);
        pass = (TextView) findViewById(R.id.textPass);
        error = (TextView) findViewById(R.id.textErrror);

    }

    public void abrir(View v){
        //Intent i = new Intent(this, Principal.class);
        //startActivity(i);
        DownloadTask ressul = new DownloadTask(this);
        String consulta = ("select nick ,password, id from usuarios where nick = \""+ usuario.getText() + "\" and activado=true").replace(" ", "%20");
        ressul.execute(consulta);
    }


    public void abrirRegistro(View view){
        Intent i = new Intent(this, RegistroActivity.class);
        startActivity(i);
    }

    public void abrirPrincipal(String nick){
        Intent i = new Intent(this, Principal.class);
        i.putExtra("nick", usuario.getText().toString());
        startActivity(i);

        SharedPreferences prefs =
                getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nick", usuario.getText().toString());
        editor.commit();
    }

    public void crearID(int id){
        SharedPreferences prefs =
                getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("id", id);
        editor.commit();
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        boolean termino = false;
        String resultado;
        Login login;

        public DownloadTask(Login login){
            this.login = login;
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
            if (result.equals("")) {
                error.setVisibility(View.VISIBLE);
            } else {

                String[] res = result.split("Â¬");
                String passSQL = res[1].trim().replace(" ","");
                Log.e("+"+pass.getText().toString()+"+","+"+ passSQL+"+");
                if(pass.getText().toString().equals(passSQL)){
                    crearID(Integer.parseInt(res[2].trim().replace(" ","")));
                    login.abrirPrincipal(res[0].trim().replace(" ",""));
                } else {
                    error.setVisibility(View.VISIBLE);
                }
            }




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
                URL url = new URL(direccion + "/usuario/consulta.php?consulta="+myurl);
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
