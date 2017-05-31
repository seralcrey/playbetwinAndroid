package com.example.sergio.playbetwincliente;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class RegistroActivity extends Activity {


    int opcion = 0;
    EditText textNombre ;
    EditText textEmail ;
    EditText textNick ;
    EditText pass1 ;
    EditText pass2 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        textNombre = (EditText) findViewById(R.id.textNombre);
        textEmail = (EditText) findViewById(R.id.textEmail);
        textNick = (EditText) findViewById(R.id.textNick);
        pass1 = (EditText) findViewById(R.id.passContra);
        pass2 = (EditText) findViewById(R.id.passConfirmar);
    }


    public void abrirLogin(){
        Intent i = new Intent(this, Login.class);
        startActivity(i);

    }

    public void registrarUsuario(View v){
        new Email("sergio.alcantara.1992@gmail.com","prueba", "pre");

        String emailRegexp = "[^@]+@[^@]+\\.[a-zA-Z]{2,}";
        System.out.println(Pattern.matches(emailRegexp, "a@b.com"));

        String passRegexp = "[a-zA-Z0-9-]{4,}";


        if (textNombre.getText().toString().length() != 0 && textEmail.getText().toString().length() != 0 && textNick.getText().toString().length() != 0 && pass1.getText().toString().length() != 0 &&
                pass2.getText().toString().length() != 0 ){
            if (pass2.getText().toString().equals(pass1.getText().toString()) && Pattern.matches(passRegexp, pass2.getText().toString())){
                if(Pattern.matches(emailRegexp, textEmail.getText().toString())){
                    if(Pattern.matches(passRegexp, textNick.getText().toString()))
                    {
                        opcion=1;
                        DownloadTask ressul = new DownloadTask();
                        String consulta = ("select nick from usuarios where nick = '"+ textNick.getText().toString() + "' ").replace(" ", "%20");
                        ressul.execute(consulta);
                    } else {
                        Toast toast1 =
                                Toast.makeText(getApplicationContext(),
                                        "El nombre de usuario no es valido", Toast.LENGTH_LONG);
                        toast1.show();
                    }

                } else {
                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    "El Email no es correcto", Toast.LENGTH_LONG);
                    toast1.show();
                }
            } else {
                Toast toast1 =
                        Toast.makeText(getApplicationContext(),
                                "Error en la contraseña, no es idéntica o no es correcta. Solo se permite número, letras y – superior o más de 4 caracteres", Toast.LENGTH_LONG);
                toast1.show();
            }

        } else {
            Toast toast1 =
                    Toast.makeText(getApplicationContext(),
                            "Hay campos que no han sido rellanados", Toast.LENGTH_LONG);

            toast1.show();


        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        boolean termino = false;

        public DownloadTask(){

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


            if(opcion==1){
                if(result.toString().trim().length()==0){
                    opcion=2;
                    DownloadTask ressul = new DownloadTask();
                    String consulta = ("INSERT INTO usuarios (nick, nombre, password, email , coins, rol_id, activado)"+
                    "VALUES ('"+ textNick.getText().toString() +"', '"+ textNombre.getText().toString() +"', '"+ pass1.getText().toString() +"', '"+ textEmail.getText().toString() +"' , 4000, 2, 1);").replace(" ", "%20");
                    ressul.execute(consulta);
                }
                else {
                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    "El nick ya esta en uso", Toast.LENGTH_LONG);
                    toast1.show();
                }
            }

            if(opcion==2){
                abrirLogin();
                Toast toast1 =
                        Toast.makeText(getApplicationContext(),
                                "Se ha registrado, ya puedes apostar en play bet win", Toast.LENGTH_LONG);
                toast1.show();


            }



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
