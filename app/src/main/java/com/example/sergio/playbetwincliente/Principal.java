package com.example.sergio.playbetwincliente;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;



public class Principal extends ActionBarActivity {

    /*
     DECLARACIONES
     */
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence activityTitle;
    private CharSequence itemTitle;
    private String[] tagTitles;
    private String nick;
    private int posicion;
    private int opcion;
    private int idUsuario;
    private ArrayList<Integer> listaBotones;
    private ArrayList<Integer> listaImagenes;
    private ArrayList<Integer> listaIdPartidos;
    private int coinsActual =0;
    //Publi
    private String url;
    private int idPubli;
    private int coinsPubli;
    private boolean cogerPublicdad=false;
    private boolean darDineroPublicidad = false;

    //Paginacion
    private int pagPerfil = 7;
    public void masPaginacion(View view){
        pagPerfil+=7;
        opcion=2;
        DownloadTask dw = new DownloadTask();
        String con = ("select count(*) from (SELECT count(*)  FROM v_apuestas where id_usuario=" + idUsuario + " group by id_evento order by fecha) f;" ).replace(" ", "%20");

        dw.execute(con);
    }

    public void menosPaginacion(View view){
        pagPerfil-=7;
        opcion=2;
        DownloadTask dw = new DownloadTask();
        String con = ("select count(*) from (SELECT count(*)  FROM v_apuestas where id_usuario=" + idUsuario + " group by id_evento order by fecha) f;" ).replace(" ", "%20");

        dw.execute(con);
    }


    //Evento

    int idEvento;
    char pronostico = 'a';
    char pronosticoActual ='a';
    String fechaEvento;
    SimpleDateFormat actual = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    SimpleDateFormat hourdateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");


    public void abrirNavegador(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
        DownloadTask ressul = new DownloadTask();
        String consulta = ("insert into usuarios_publicidad (id_usuario, id_publicidad, fecha)  value ("+ idUsuario +","+ idPubli+",now())").replace(" ", "%20");
        opcion=2;
        ressul.execute(consulta);

    }

    boolean apuesta = false;

    public void apostar(View view){
        TextView textApostar = (TextView) findViewById(R.id.textCoinsIn);
        if(coinsActual>=Integer.parseInt(textApostar.getText().toString().trim())){
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");

                Date fecha = sdf.parse(fechaEvento.replace("a las " , ""));
                Date fechaActual = new Date();

                if(fechaActual.getTime() < fecha.getTime()){
                    EditText c = (EditText) findViewById(R.id.textCoinsIn);
                    if (coinsActual>=Integer.parseInt(c.getText().toString().trim())) {
                        Button b1 = (Button) findViewById(R.id.apusta1);
                        Button b2 = (Button) findViewById(R.id.apuesta2);
                        Button bx = (Button) findViewById(R.id.apuestaX);



                        DownloadTask ressul = new DownloadTask();
                        String consulta = ("http://192.168.0.201:8080/usuario/apostar.php?pronostico=" + pronosticoActual + "&id_usuario=" + idUsuario + "&id_evento=" + idEvento + "&coins=" + c.getText().toString().trim()).replace(" ", "%20");
                        TextView coinsEve = (TextView) findViewById(R.id.coinsEvento);
                        coinsActual = coinsActual - Integer.parseInt(c.getText().toString().trim());
                        coinsEve.setText("Aun dispone de " + coinsActual + " coins para apostar");
                        apuesta = true;
                        posicion = 10;
                        opcion = 2;
                        ressul.execute(consulta);
                    } else {
                        Toast toast1 =
                                Toast.makeText(getApplicationContext(),
                                        "No tienes suficientes coins",
                                        Toast.LENGTH_LONG);

                        toast1.show();
                    }
                } else {
                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    "No puedes apostar en un evento que ya ha comenzado",
                                    Toast.LENGTH_LONG);

                    toast1.show();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            Toast toast1 =
                    Toast.makeText(getApplicationContext(),
                            "No tiene suficientes coins",
                            Toast.LENGTH_LONG);

            toast1.show();
        }
    }

    //PAGOS PAYPAL
//Paypal intent request code to track onActivityResult method
    public static final int PAYPAL_REQUEST_CODE = 123;


    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    String precio;
    String id_producto;

    private ArrayList<ArrayList> productos;
    public void comprar(View v){

        precio = "";
        String nombre = "";

        for (int i=0;i<listaBotones.size();i++){
            if(listaBotones.get(i)== v.getId()){
                id_producto = productos.get(i).get(0).toString();
                precio = productos.get(i).get(2).toString();
                nombre = productos.get(i).get(1).toString();
            }
        }

        if(precio != "") {
            //Getting the amount from editText



            //Creating a paypalpayment
            PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(precio)), "EUR", nombre,
                    PayPalPayment.PAYMENT_INTENT_SALE);

            //Creating Paypal Payment activity intent
            Intent intent = new Intent(this, PaymentActivity.class);

            //putting the paypal configuration to the intent
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

            //Puting paypal payment to the intent
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

            //Starting the intent activity for result
            //the request code will be used on the method onActivityResult
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        }
    }

    private boolean comprar = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try{
                    String referencia_paypal = confirm.toJSONObject().getJSONObject("response").get("id").toString();
                    String estado_paypal = confirm.toJSONObject().getJSONObject("response").get("state").toString();
                    String precio_total = precio;
                    String id_producto = this.id_producto;
                    String coinsPro = "";


                    for (int i=0;i<productos.size();i++){
                        if(productos.get(i).get(0) == id_producto){
                            coinsPro = productos.get(i).get(3).toString();
                        }
                    }

                    if (coinsPro.length() >0){
                        DownloadTask ressul = new DownloadTask();
                        String consulta = ("http://192.168.0.201:8080/usuario/comprar.php?referencia_paypal="+referencia_paypal+"&estado_paypal="+estado_paypal+"&precio_total="+precio_total+"&id_producto="+id_producto+"&id_usuario="+idUsuario+"&coins="+coinsPro ).replace(" ", "%20");
                        ressul.execute(consulta);
                        opcion=30;
                        comprar=true;
                    }

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }


                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        nick = getIntent().getStringExtra("nick");
        SharedPreferences userDetails = this.getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        idUsuario = userDetails.getInt("id", 0);

        itemTitle = activityTitle = getTitle();
        tagTitles = getResources().getStringArray(R.array.Tags);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Setear una sombra sobre el contenido principal cuando el drawer se despliegue
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //Crear elementos de la lista
        ArrayList<DrawerItem> items = new ArrayList<DrawerItem>();
        items.add(new DrawerItem(nick, R.drawable.ic_conectado));
        items.add(new DrawerItem(tagTitles[1], R.mipmap.ic_perfil));
        items.add(new DrawerItem("   " + tagTitles[2], R.drawable.ic_eventos));
        items.add(new DrawerItem(tagTitles[3], R.mipmap.ic_tienda));
        items.add(new DrawerItem(tagTitles[4], R.mipmap.ic_premios));
        items.add(new DrawerItem(tagTitles[5], R.mipmap.ic_salir));



        // Relacionar el adaptador y la escucha de la lista del drawer
        drawerList.setAdapter(new DrawerListAdapter(this, items));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Habilitar el icono de la app por si hay algún estilo que lo deshabilitó
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Crear ActionBarDrawerToggle para la apertura y cierre
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawe,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(itemTitle);

                /*Usa este método si vas a modificar la action bar
                con cada fragmento
                 */
                //invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(activityTitle);

                /*Usa este método si vas a modificar la action bar
                con cada fragmento
                 */
                //invalidateOptionsMenu();
            }
        };
        //Seteamos la escucha
        drawerLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }
    }


    public void marcarSelecionado(View v){
        if (pronostico == 'a'){
            Button b1= (Button) findViewById(R.id.apusta1);
            if (R.id.apusta1==v.getId()){
                b1.setBackgroundColor(Color.rgb(180,224,243));
                pronosticoActual = '1';
            } else {
                b1.setBackgroundColor(Color.rgb(33,164,216));
            }

            Button bx= (Button) findViewById(R.id.apuestaX);
            if (R.id.apuestaX==v.getId()){
                bx.setBackgroundColor(Color.rgb(180,224,243));
                pronosticoActual = 'X';
            } else {
                bx.setBackgroundColor(Color.rgb(33,164,216));
            }

            Button b2= (Button) findViewById(R.id.apuesta2);
            if (R.id.apuesta2==v.getId()){
                b2.setBackgroundColor(Color.rgb(180,224,243));
                pronosticoActual = '2';
            } else {
                b2.setBackgroundColor(Color.rgb(33,164,216));
            }
        } else {
            Toast toast1 =
                    Toast.makeText(getApplicationContext(),
                            "Ya tienes una apuesta activa",
                            Toast.LENGTH_LONG);

            toast1.show();
        }

    }


    public void consePremios(View view){
        enviarCorreos( );
    }

    //Enviar correos

    public void enviarCorreos(){

        new Email("sergio.alcantara.1992@gmail.com","asdas","asdasdas");
    }

    public void abrirEven(View v){
        for (int i=0;i<listaIdPartidos.size();i++){
            if (v.getId()==listaBotones.get(i)){
                EventoFragment fragment1 = new EventoFragment();
                Bundle args = new Bundle();
                FragmentManager fragmentManager = getSupportFragmentManager();
                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, 0);
                //fragment.setArguments(args);

                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment1).commit();

                SharedPreferences prefs =
                        getSharedPreferences("MisPreferencias",MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("idEvento", listaIdPartidos.get(i));
                editor.commit();


                DownloadTask ressul = new DownloadTask();
                String consulta = ("SELECT id, participante_casa , participante_visitante , competicion , apuesta_1, apuesta_2 , fecha_formu , apuesta_X   FROM v_eventos_participantes where id="+ listaIdPartidos.get(i) +";").replace(" ", "%20");
                posicion = 10;
                opcion=0;
                ressul.execute(consulta);


                drawerList.setItemChecked(0, true);
                setTitle(tagTitles[0]);
                drawerLayout.closeDrawer(drawerList);
            }
        }
    }

    public void cambiarC(View v){
        TextView pass1 = (TextView) findViewById(R.id.pass1);
        TextView pass2 = (TextView) findViewById(R.id.pass2);

        if(pass1.getText().toString().equals(pass2.getText().toString()) && pass1.getText().length()>5){
            DownloadTask ressul = new DownloadTask();
            String consulta = ("update usuarios set password='"+ pass1.getText().toString().trim() + "' where id="+idUsuario ).replace(" ", "%20");
            opcion = 1;
            posicion=0;
            ressul.execute(consulta);

        } else {
            TextView coinsEve   = (TextView) findViewById(R.id.textApostado);
            Toast toast1 =
                    Toast.makeText(getApplicationContext(),
                            coinsEve.getText() + ". Solo puedes aumentar la apuesta",
                            Toast.LENGTH_LONG);

            toast1.show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            // Toma los eventos de selección del toggle aquí
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* La escucha del ListView en el Drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }



    private void selectItem(int position) {
        // Reemplazar el contenido del layout principal por un fragment
        posicion = position;
        Bundle args = new Bundle();
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position){
            case 0:
                Perfil fragment1 = new Perfil(this);

                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, position);
                //fragment.setArguments(args);


                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment1).commit();
                //Carga nombre
                opcion=0;
                DownloadTask ressul = new DownloadTask();
                String consulta = ("select nick ,nombre, coins from usuarios where id="+ idUsuario ).replace(" ", "%20");
                ressul.execute(consulta);
                //Carga eventos apostados
                listaBotones = new ArrayList<>();
                listaBotones.add(R.id.ultimaApuesta1);
                listaBotones.add( R.id.ultimaApuesta2);
                listaBotones.add(R.id.ultimaApuesta3);
                listaBotones.add(R.id.ultimaApuesta4);
                listaBotones.add(R.id.ultimaApuesta5);
                listaBotones.add(R.id.ultimaApuesta6);
                listaBotones.add(R.id.ultimaApuesta7);



                break;
            case 1:
                Inicio fragment2 = new Inicio();
                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, position);
                //fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment2).commit();
                //Cargar datos usuario
                opcion=0;
                DownloadTask ressul1 = new DownloadTask();
                String consulta1 = ("select nick , coins from usuarios where id="+ idUsuario ).replace(" ", "%20");
                ressul1.execute(consulta1);
                break;
            case 2:
                Eventos fragment3 = new Eventos();
                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, position);
                //fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment3).commit();
                break;
            case 3:
                Tienda fragment4 = new Tienda();
                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, position);
                //fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment4).commit();

                listaBotones = new ArrayList<>();
                listaBotones.add(R.id.comprar1);
                listaBotones.add(R.id.comprar2);
                listaBotones.add(R.id.comprar3);
                listaBotones.add(R.id.comprar4);
                listaBotones.add(R.id.comprar5);
                listaBotones.add(R.id.comprar6);
                listaBotones.add(R.id.comprar7);

                listaImagenes = new ArrayList<>();
                listaImagenes.add(R.id.imageTienda1);
                listaImagenes.add(R.id.imageTienda2);
                listaImagenes.add(R.id.imageTienda3);
                listaImagenes.add(R.id.imageTienda4);
                listaImagenes.add(R.id.imageTienda5);
                listaImagenes.add(R.id.imageTienda6);
                listaImagenes.add(R.id.imageTienda7);

                Intent intent = new Intent(this, PayPalService.class);

                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

                startService(intent);
                opcion =0;

                DownloadTask ressul3 = new DownloadTask();
                String consulta3 = ("SELECT count(*) FROM productos where activado=true" ).replace(" ", "%20");
                ressul3.execute(consulta3);

                break;
            case 4:
                Premios fragment5 = new Premios();
                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, position);
                //fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment5).commit();

                listaBotones = new ArrayList<>();
                listaBotones.add(R.id.btPremio1);
                listaBotones.add(R.id.btPremio2);
                listaBotones.add(R.id.btPremio3);
                listaBotones.add(R.id.btPremio4);
                listaBotones.add(R.id.btPremio5);
                listaBotones.add(R.id.btPremio6);
                listaBotones.add(R.id.btPremio7);
                listaBotones.add(R.id.btPremio8);
                listaBotones.add(R.id.btPremio9);
                listaBotones.add(R.id.btPremio10);

                listaImagenes = new ArrayList<>();
                listaImagenes.add(R.id.imgPremio1);
                listaImagenes.add(R.id.imgPremio2);
                listaImagenes.add(R.id.imgPremio3);
                listaImagenes.add(R.id.imgPremio4);
                listaImagenes.add(R.id.imgPremio5);
                listaImagenes.add(R.id.imgPremio6);
                listaImagenes.add(R.id.imgPremio7);
                listaImagenes.add(R.id.imgPremio8);
                listaImagenes.add(R.id.imgPremio9);
                listaImagenes.add(R.id.imgPremio10);

                opcion =0;

                DownloadTask ressul4 = new DownloadTask();
                String consulta4 = ("SELECT count(*) FROM premios where activado=true" ).replace(" ", "%20");
                ressul4.execute(consulta4);
                break;
            case 5:
                this.finish();
        }


        // Se actualiza el item seleccionado y el título, después de cerrar el drawer
        drawerList.setItemChecked(position, true);
        setTitle(tagTitles[position]);
        drawerLayout.closeDrawer(drawerList);
    }

    public void ponerImagePubli(){
        ImageView image = (ImageView) findViewById(R.id.imagenPublicidad);
        Glide.with(this).
                load("http://192.168.0.201:8080/publicidad/"+idPubli+".jpg").
                placeholder(R.drawable.ic_conectado).
                into(image);
    }

    public void abrirImagen(ImageView im, int id){

        Glide.with(this).
                load("http://192.168.0.201:8080/tienda/"+id+".jpg").
                placeholder(R.drawable.ic_conectado).
                into(im);
    }

    public void abrirPremios(ImageView im, int id){

        Glide.with(this).
                load("http://192.168.0.201:8080/premios/"+id+".png").
                placeholder(R.drawable.ic_conectado).
                into(im);
    }

    public void abrirEvento(View v){
        Button b = null;
        /*switch(v.getId()){
            case R.id.btParaFutbol:
                b = (Button) v.findViewById(R.id.btParaFutbol);
                break;

        }

        Toast toast1 = Toast.makeText(getApplicationContext(),
                b.getText() , Toast.LENGTH_SHORT);
        toast1.show();*/
    }

    /* Método auxiliar para setear el titulo de la action bar */
    @Override
    public void setTitle(CharSequence title) {
        itemTitle = title;
        getSupportActionBar().setTitle(itemTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sincronizar el estado del drawer
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Cambiar las configuraciones del drawer si hubo modificaciones
        drawerToggle.onConfigurationChanged(newConfig);
    }


    private boolean ocultar = false;
    public void cambiarContraseña(View v){
        TextView txt1 = (TextView) findViewById(R.id.textConfirmar);
        TextView txt2 = (TextView) findViewById(R.id.texContraseña);
        EditText pass1 = (EditText) findViewById(R.id.pass1);
        EditText pass2 = (EditText) findViewById(R.id.pass2);
        Button cambiarConfr = (Button) findViewById(R.id.btCambiarContr);
        Button cambiarContra = (Button) findViewById(R.id.btCambiarContraseña);
        Button cancelar = (Button) findViewById(R.id.btCancelarContraseña);
        if(ocultar){
            txt1.setVisibility(View.GONE);
            txt2.setVisibility(View.GONE);
            pass1.setVisibility(View.GONE);
            pass2.setVisibility(View.GONE);
            cambiarConfr.setVisibility(View.GONE);
            cambiarContra.setVisibility(View.VISIBLE);
            cancelar.setVisibility(View.GONE);
            ocultar=false;
        } else{
            txt1.setVisibility(View.VISIBLE);
            txt2.setVisibility(View.VISIBLE);
            pass1.setVisibility(View.VISIBLE);
            pass2.setVisibility(View.VISIBLE);
            cambiarConfr.setVisibility(View.VISIBLE);
            cambiarContra.setVisibility(View.GONE);
            cancelar.setVisibility(View.VISIBLE);
            ocultar=true;
        }
    }



    public class DownloadTask extends AsyncTask<String, Void, String> {

        boolean termino = false;
        String resultado;
        String url="";


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

            if(posicion ==0) {
                switch (opcion) {
                    //Carga datos de usuario
                    case 0:
                        TextView nick = (TextView) findViewById(R.id.textNick);
                        TextView nombre = (TextView) findViewById(R.id.textNombre);
                        TextView coins = (TextView) findViewById(R.id.textCoins);
                        nick.setText(res[0].trim());
                        nombre.setText(res[1].trim());
                        coins.setText(res[2].trim() + " coins");
                        coinsActual = Integer.parseInt(res[2].trim());
                        opcion = 2;
                        DownloadTask dw = new DownloadTask();
                        String con = ("select count(*) from (SELECT count(*)  FROM v_apuestas where id_usuario=" + idUsuario + " group by id_evento order by fecha) f;").replace(" ", "%20");

                        dw.execute(con);
                        break;
                    //cambia la contraseña
                    case 1:
                        TextView txt1 = (TextView) findViewById(R.id.textConfirmar);
                        TextView txt2 = (TextView) findViewById(R.id.texContraseña);
                        EditText pass1 = (EditText) findViewById(R.id.pass1);
                        EditText pass2 = (EditText) findViewById(R.id.pass2);
                        Button cambiarConfr = (Button) findViewById(R.id.btCambiarContr);
                        Button cambiarContra = (Button) findViewById(R.id.btCambiarContraseña);
                        Button cancelar = (Button) findViewById(R.id.btCancelarContraseña);
                        txt1.setVisibility(View.GONE);
                        txt2.setVisibility(View.GONE);
                        pass1.setVisibility(View.GONE);
                        pass2.setVisibility(View.GONE);
                        pass1.setText("");
                        pass2.setText("");
                        cambiarConfr.setVisibility(View.GONE);
                        cambiarContra.setVisibility(View.VISIBLE);
                        cancelar.setVisibility(View.GONE);
                        ocultar = false;
                        break;
                    // muestra cuantas apuesta tiene un usaurio
                    case 2:
                        int total = (Integer.parseInt(res[0].trim())-1) - (pagPerfil-7);
                        Button bmenos = (Button) findViewById(R.id.btMenosPerfil);
                        Button bmas = (Button) findViewById(R.id.btMasPerfil);
                        if (total <= 7) {
                            for (int i = total; i < listaBotones.size(); i++) {
                                Button n = (Button) findViewById(listaBotones.get(i));
                                n.setVisibility(View.GONE);
                            }

                            bmas.setVisibility(View.GONE);

                        } else {
                            for (int i = 0; i < listaBotones.size(); i++) {
                                Button n = (Button) findViewById(listaBotones.get(i));
                                n.setVisibility(View.VISIBLE);
                            }


                            bmas.setVisibility(View.VISIBLE);
                        }
                        if (pagPerfil > 8)
                            bmenos.setVisibility(View.VISIBLE);
                        else
                            bmenos.setVisibility(View.GONE);
                        opcion = 3;
                        DownloadTask dw2 = new DownloadTask();
                        String con2 = ("SELECT id_evento, participante_casa , participante_visitante , fecha_hora , deporte FROM v_apuestas where id_usuario=" + idUsuario + " group by id_evento order by id DESC Limit " + (pagPerfil - 7) + " , " + (pagPerfil + 1) + ";").replace(" ", "%20");

                        dw2.execute(con2);
                        break;
                    case 3:
                        listaIdPartidos = new ArrayList<>();
                        int index = 0;
                        for (int i = 0; i <= res.length - 5; i = i + 5) {
                            listaIdPartidos.add(Integer.parseInt(res[i].toString().trim()));
                            Button n = (Button) findViewById(listaBotones.get(index++));

                            if (n.getVisibility() == View.VISIBLE) {
                                n.setText(res[i + 1] + " vs " + res[i + 2] + " \n " + res[i + 3]);
                                switch (res[i + 4].trim()) {
                                    case "Fútbol":
                                        n.setBackgroundColor(Color.rgb(186, 255, 186));
                                        break;
                                    case "Baloncesto":
                                        n.setBackgroundColor(Color.rgb(255, 215, 186));
                                        break;
                                    case "Tenis":
                                        n.setBackgroundColor(Color.rgb(251, 255, 186));
                                        break;
                                    case "E-sport":
                                        n.setBackgroundColor(Color.rgb(236, 186, 255));
                                        break;
                                    case "Mas deportes":
                                        n.setBackgroundColor(Color.rgb(186, 255, 251));
                                        break;

                                }
                            }
                        }
                        break;
                }
            }
            else if (posicion == 1) {
                switch (opcion) {
                    case 0:
                        TextView nick = (TextView) findViewById(R.id.nickInicio);
                        TextView coins = (TextView) findViewById(R.id.coinsInicio);

                        nick.setText(res[0]);
                        coins.setText(res[1] + " coins");
                        coinsActual = Integer.parseInt(res[1].trim());
                        opcion=1;
                        cogerPublicdad=true;
                        DownloadTask dw2 = new DownloadTask();
                        String con2 = "";
                        dw2.execute(con2);

                        break;
                    case 1:
                        if (res.length>2) {
                            Button btoPu = (Button) findViewById(R.id.BotonPubli);
                            coinsPubli = Integer.parseInt(res[3].trim());
                            url = res[2].trim();
                            idPubli = Integer.parseInt(res[0].trim());
                            btoPu.setText("Gana " + res[3].trim() + " coins");

                            ponerImagePubli();
                        } else {
                            Button btoPu = (Button) findViewById(R.id.BotonPubli);
                            ImageView img = (ImageView) findViewById(R.id.imagenPublicidad);
                            btoPu.setVisibility(View.GONE);
                            img.setVisibility(View.GONE);
                        }

                        DownloadTask dw23 = new DownloadTask();
                        opcion=3;
                        dw23.execute("select count(*) as total, id_evento,participante_casa,participante_visitante,deporte,fecha_hora  from v_apuestas group by id_evento order by total desc limit 5");
                        break;
                    case 2:
                        Button btoPu = (Button) findViewById(R.id.BotonPubli);
                        ImageView img = (ImageView) findViewById(R.id.imagenPublicidad);
                        btoPu.setVisibility(View.GONE);
                        img.setVisibility(View.GONE);
                        DownloadTask dw = new DownloadTask();
                        opcion=99;
                        dw.execute("");
                        break;
                    case 3:
                        Button inipar1 = (Button) findViewById(R.id.inipart1);
                        Button inipar2 = (Button) findViewById(R.id.inipart2);
                        Button inipar3 = (Button) findViewById(R.id.inipart3);
                        Button inipar4 = (Button) findViewById(R.id.inipart4);
                        Button inipar5 = (Button) findViewById(R.id.inipart5);


                }
            } else if (posicion == 10) {
                TextView casa = (TextView) findViewById(R.id.EquipoCasa);
                TextView visi = (TextView) findViewById(R.id.EquipoVIsitante);
                Button b1= (Button) findViewById(R.id.apusta1);
                Button b2= (Button) findViewById(R.id.apuesta2);
                Button bx= (Button) findViewById(R.id.apuestaX);
                switch (opcion){
                    case 0:


                        casa.setText(res[1].trim());
                        visi.setText(res[2].trim());
                        idEvento = Integer.parseInt(res[0].trim());

                        b1.setText(1 + " (" + res[4].trim() + ")");


                        b2.setText(2 + " (" + res[5].trim() + ")");

                        fechaEvento = res[6].trim();
                        if(res.length==8){
                            bx.setVisibility(View.VISIBLE);
                            bx.setText("X (" + res[7].trim() + ")");
                        } else {
                            bx.setVisibility(View.GONE);
                        }

                        DownloadTask dw2 = new DownloadTask();
                        String con2 = ("SELECT sum(coins), pronostico  FROM v_apuestas where id_evento=" +idEvento + " and id_usuario=" + idUsuario + " group by id_Evento;").replace(" ", "%20");
                        opcion=1;
                        dw2.execute(con2);
                        break;
                    case 1:

                    if (res.length>=2){
                        int coin = Integer.parseInt(res[0].trim());
                        if (coin > 0) {
                            if (res[1].trim().equals("1")){
                                b1.setBackgroundColor(Color.rgb(180,224,243));
                            } else if (res[1].trim().equals("2")){
                                b2.setBackgroundColor(Color.rgb(180,224,243));
                            } else if (res[1].trim().equals("X")){
                                bx.setBackgroundColor(Color.rgb(180,224,243));
                            }

                            pronostico = res[1].trim().charAt(0);
                            pronosticoActual =  res[1].trim().charAt(0);
                        }

                        TextView texto = (TextView) findViewById(R.id.textApostado);
                        texto.setText("Has apostado a " + pronostico + " y un total de " + coin + " coins.");
                        TextView coinsEve   = (TextView) findViewById(R.id.coinsEvento);
                        coinsEve.setText("Aun dispone de "+coinsActual+" coins para apostar");
                    }
                        break;
                    case 2:
                        DownloadTask ressul = new DownloadTask();
                        String consulta = ("SELECT id, participante_casa , participante_visitante , competicion , apuesta_1, apuesta_2 , fecha_formu , apuesta_X   FROM v_eventos_participantes where id="+ idEvento +";").replace(" ", "%20");
                        posicion = 10;
                        opcion=0;
                        ressul.execute(consulta);
                        break;
                }

            } else if (posicion==3) {
                switch (opcion) {
                    case 0:


                        int n = Integer.parseInt(res[0].trim());

                        for (int i = n; i < listaBotones.size(); i++) {
                            ImageView im = (ImageView) findViewById(listaImagenes.get(i));
                            im.setVisibility(View.GONE);
                            Button bu = (Button) findViewById(listaBotones.get(i));
                            bu.setVisibility(View.GONE);
                        }

                        DownloadTask dw2 = new DownloadTask();
                        String con2 = ("SELECT id, nombre, precio, coins FROM productos where stock>0 and activado=true;").replace(" ", "%20");
                        opcion = 1;
                        dw2.execute(con2);

                        break;

                    case 1:
                        if (res.length > 3) {
                            int index = 0;
                            productos = new ArrayList<>();
                            for (int i = 0; i < res.length; i = i + 4) {
                                ArrayList<String> pro = new ArrayList<>();
                                pro.add(res[0 + i].trim());
                                pro.add(res[1 + i].trim());
                                pro.add(res[2 + i].trim());
                                pro.add(res[3 + i].trim());

                                ImageView im = (ImageView) findViewById(listaImagenes.get(index));
                                Button bu = (Button) findViewById(listaBotones.get(index));
                                abrirImagen(im, Integer.parseInt(res[0 + i].trim()));
                                bu.setText("Compra este producto y consigue " + res[i + 3] + " coins por tan solo " + res[i + 2] + "€");
                                index++;
                                productos.add(pro);
                            }
                        }
                }
            } else if (posicion==4) {
                switch (opcion) {
                    case 0:


                        int n = Integer.parseInt(res[0].trim());

                        for (int i = n; i < listaBotones.size(); i++) {
                            ImageView im = (ImageView) findViewById(listaImagenes.get(i));
                            im.setVisibility(View.GONE);
                            Button bu = (Button) findViewById(listaBotones.get(i));
                            bu.setVisibility(View.GONE);
                        }

                        DownloadTask dw2 = new DownloadTask();
                        String con2 = ("SELECT id, nombre, coins FROM premios where activado=true;").replace(" ", "%20");
                        opcion = 1;
                        dw2.execute(con2);

                        break;

                    case 1:
                        if (res.length > 3) {
                            int index = 0;
                            productos = new ArrayList<>();
                            for (int i = 0; i < res.length; i = i + 3) {
                                ArrayList<String> pro = new ArrayList<>();
                                pro.add(res[0 + i].trim());
                                pro.add(res[1 + i].trim());
                                pro.add(res[2 + i].trim());

                                ImageView im = (ImageView) findViewById(listaImagenes.get(index));
                                Button bu = (Button) findViewById(listaBotones.get(index));
                                abrirPremios(im, Integer.parseInt(res[0 + i].trim()));
                                bu.setText("Consigue este premio por  " + res[i + 2] + " coins ");
                                index++;
                                productos.add(pro);
                            }
                        }
                        break;
                }


            }



        }



        private String downloadContent(String myurl) throws IOException {
            InputStream is = null;
            int length = 500;
            this.url = myurl;
            try {
                String ur = "http://192.168.0.201:8080/usuario/consulta.php?consulta="+myurl;
                if(cogerPublicdad){
                    ur = "http://192.168.0.201:8080/usuario/cogerPublicidad.php?id="+idUsuario;
                    cogerPublicdad = false;
                }

                if(darDineroPublicidad){
                    ur = "http://192.168.0.201:8080/usuario/dineroPublicidad.php?coins="+coinsPubli+"&id="+idUsuario;
                    darDineroPublicidad = false;
                }

                if (comprar){
                    ur = myurl;
                    comprar=false;
                }

                if (apuesta){
                    ur = myurl;
                    apuesta=false;
                }

                URL url = new URL(ur);
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
                this.resultado = contentAsString;
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