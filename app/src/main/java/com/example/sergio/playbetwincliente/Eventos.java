package com.example.sergio.playbetwincliente;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
public class Eventos extends Fragment {



    Button bFutbol, bBaloncesto,bTenis,botros,bSport, bCopiar;
    private ViewGroup layout;
    private ScrollView scrollView;
    LayoutInflater inflater;


    public Eventos() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflaterr, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflaterr.inflate(R.layout.fragment_eventos, container, false);
        /*bFutbol = (Button) v.findViewById(R.id.tbFutbol);
        bBaloncesto = (Button) v.findViewById(R.id.btBaloncesto);
        bTenis = (Button) v.findViewById(R.id.btTenis);
        botros = (Button) v.findViewById(R.id.btOtros);
        bSport = (Button) v.findViewById(R.id.btSport);
        bBaloncesto.setWidth(300);
        bTenis.setWidth(container.getWidth()*100/78);s

        */



        layout = (ViewGroup) v.findViewById(R.id.content);
        scrollView = (ScrollView) v.findViewById(R.id.scrollView);
        inflater = LayoutInflater.from(v.getContext());






        DownloadTask ressul = new DownloadTask();
        String consulta = ("select participante_casa , participante_visitante , fecha_hora, competicion , deporte  from v_eventos_participantes  where fecha_hora > now() order by fecha_hora\n".replace(" ", "%20"));
        ressul.execute(consulta);

        return v;
    }

    public void abrirOtro(View v){

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        boolean termino = false;
        String resultado;

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
            try {
                if (res.length > 1) {
                    for (int i = 0; i < res.length; i = i + 5) {

                        int id = R.layout.botonevento;

                        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(id, null, false);

                        Button textView = (Button) relativeLayout.findViewById(R.id.btParaFutbol);

                        switch (res[i + 4].trim()) {
                            case "Baloncesto":
                                textView.setBackgroundColor(Color.rgb(255, 215, 186));
                                break;
                            case "Tenis":
                                textView.setBackgroundColor(Color.rgb(251, 255, 186));
                                break;
                            case "E-sport":
                                textView.setBackgroundColor(Color.rgb(236, 186, 255));
                                break;
                            case "Mas deportes":
                                textView.setBackgroundColor(Color.rgb(186, 255, 251));
                                break;

                        }


                        //Aqui poner hora
                        textView.setText(res[i] + " vs " + res[i + 1] + " \n" + res[i + 2]);


                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        params.topMargin = 15;
                        relativeLayout.setPadding(5, 3, 5, 3);
                        relativeLayout.setLayoutParams(params);

                        layout.addView(relativeLayout);


                        scrollView.post(new Runnable() {
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });


                    }
                }
            } catch (Exception e){
                Log.e("Error", e.toString());
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
            int length = 10000;

            try {
                URL url = new URL("http://192.168.0.201:8080/usuario/consulta.php?consulta="+myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(30000 /* milliseconds */);
                conn.setConnectTimeout(35000 /* milliseconds */);
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
