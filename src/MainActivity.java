package com.example.albe.js;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    WebView mWebViewView;
    private int CAMPIONAMENTO = 10000;
    private float accX = 0, accY = 0, accZ = 0;
    private float giroX = 0, giroY = 0, giroZ = 0;
    private float gravX = 0, gravY = 0, gravZ = 0;
    private long inizio = 0;
    private int accC=0, gravC=0, giroC=0;
    private float misure[][];
    private int misC=0;
    public SensorManager sensorManager;
    private Sensor accelerometer, giroscopio, gravità;
    private final float NOISE = (float) 2.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebViewView = (WebView)findViewById(R.id.webView); //Aggancio WebView
        mWebViewView.getSettings().setJavaScriptEnabled(true);  //Abilito il Javascritp
        mWebViewView.addJavascriptInterface(new WebAppMiddleware(this), "Android"); //Abilito la classe che mi permette di far comunicare la pagina web con l'app

        mWebViewView.loadUrl("http://mastercasteggio.altervista.org/webapp2.html");    //Dico dove andare a prendere la pagina

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);  // senza la gravità
     //   sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL); // parte il rilevamento subito?

        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
     //   sensorManager.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_NORMAL);

        gravità = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
     //   sensorManager.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // per ora inutile
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long tempo = event.timestamp;
        if (inizio == 0) inizio = tempo;        // la prima volta devo inizializzare la partenza del tempo

        if (tempo - inizio > CAMPIONAMENTO) {   // sono finiti 10 ms

            inizio = inizio + CAMPIONAMENTO;    // il tempo va avanti per step fissi di 10 ms non dell'utimo timestamp

            misure[misC][1]=accX/accC;          // salvo dati ma anche contatore per valutazione media
            misure[misC][2]=accY/accC;
            misure[misC][3]=accZ/accC;

            misure[misC][4]=giroX/giroC;
            misure[misC][5]=giroY/giroC;
            misure[misC][6]=giroZ/giroC;

            misure[misC][7]=gravX/gravC;
            misure[misC][8]=gravY/gravC;
            misure[misC][9]=gravZ/gravC;

            misure[misC][10]=accC;
            misure[misC][11]=giroC;
            misure[misC][12]=gravC;

            misC++;

            accX=0;
            accY=0;
            accZ=0;

            giroX=0;
            giroY=0;
            giroZ=0;

            gravX=0;
            gravY=0;
            gravZ=0;

            accC = 0;
            giroC = 0;
            gravC = 0;
        }

        Sensor sensor = event.sensor;       // sia che sono finiti 10ms che no, la lettura attuale va salvata.

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accX = accX + x;
                accY = accY + y;
                accZ = accZ + z;
                accC++;
            } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                giroX = giroX + x;
                giroY = giroY + y;
                giroZ = giroZ + z;
                giroC++;
            } else {
                gravX = gravX + x;
                gravY = gravY + y;
                gravZ = gravZ + z;
                gravC++;

            }
    }

    //Classe che serve racchide i metodi che possono essere chiamati dal codice javascript nella pagina
    public class WebAppMiddleware {

        Context c;  //Mi serve per usare i Toast

        WebAppMiddleware(Context c) {
            this.c = c; //Aggancio il Context dell'app

        }

        @JavascriptInterface
        public void showToast(String message){
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show(); //MOstro il Toast
        }

        // restituisce l'accelerazione sull'asse x
        @JavascriptInterface
        public float getAccX(){
            return accX;
        }

        // restituisce un array[3] di float con i valori dell'accelerazione x,y,z
        @JavascriptInterface
        public float[] getData() {
            float[]mis;
            int i,k;
            mis[0]=misC;
            for(k=0;k<12;k++){
                for (i=0;i<(k+1)*misC;i++){
                    mis[k*i+1]=misure[i][k];
                }
            }

            return mis;  // JS non ha gli array bidimensionali !!!!
        }

        @JavascriptInterface
        public void VIA() {   // per esperimenti lego a un pulsante VIA.
            sensorManager.registerListener(c, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(c, giroscopio, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(c, gravità, SensorManager.SENSOR_DELAY_NORMAL);
            misC=0;  // sovrascrivo le vecchie.
        }

        @JavascriptInterface
        public void STOP() {    // per esperimenti lego a un pulsante STOP.
            sensorManager.unregisterListener(this);
        }
    }
}
