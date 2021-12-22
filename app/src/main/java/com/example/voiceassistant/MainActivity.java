package com.example.voiceassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.ThreeBounce;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;
    ImageButton imButton;
    EditText et;
    TextView tv, tv1, tv2;
    WifiManager wifiManager;


    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imButton = findViewById(R.id.im_button);
        et = findViewById(R.id.editTextTextPersonName);
        tv = findViewById(R.id.textView);
        tv1 = findViewById(R.id.textView2);
        tv2 = findViewById(R.id.textView3);

//        tts.setPitch();
//        tts.setSpeechRate();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("tts", "Language not supported");
                    }else{
                        Log.e("tts", "Initialization failed");
                    }
                }
            }
        });

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        Sprite doubleBounce = new DoubleBounce();
        progressBar.setIndeterminateDrawable(doubleBounce);

        ProgressBar progressBar2 = (ProgressBar)findViewById(R.id.spin_kit2);
        Sprite threeBounce = new ThreeBounce();
        progressBar2.setIndeterminateDrawable(threeBounce);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        setProgressbar(progressBar);

        imButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                if(count==0){
                    imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_24));
                    // start listening
                    if(tts.isSpeaking()){
                        tts.stop();
                    }
                    speechRecognizer.startListening(speechRecognizerIntent);
                    count = 1;
                    tv.setText("Listening");
                    progressBar2.setVisibility(View.VISIBLE);
                }

                else{
                    imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    // stop listening
                    speechRecognizer.stopListening();
                    count = 0;
                    progressBar.setVisibility(View.INVISIBLE);
                    tv.setText("Say something");
                    progressBar2.setVisibility(View.INVISIBLE);
                }
            }
        });
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onError(int error) {
                progressBar2.setVisibility(View.INVISIBLE);
                imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                count = 0;
                tv.setText("Say something");
                tv1.setVisibility(View.VISIBLE);
            }

            @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                progressBar2.setVisibility(View.INVISIBLE);
                imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                count = 0;
                tv.setText("Say something");

                if(data.get(0).equals("stop") || data.get(0).equals("okay stop") || data.get(0).equals("ok stop") || data.get(0).equals("nothing")){
                    tts.speak("Okay stopping. You can talk to me anytime you want.", TextToSpeech.QUEUE_FLUSH, null);
                    imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    speechRecognizer.stopListening();
                    count = 0;
                    progressBar.setVisibility(View.INVISIBLE);
                }

                else if(data.get(0).toLowerCase().contains("open facebook")){
                    et.setText(data.get(0));
                    tts.speak("Opening Facebook.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening Facebook");
                    openFacebook();
                }

                else if(data.get(0).toLowerCase().contains("open whatsapp")){
                    et.setText(data.get(0));
                    tts.speak("Opening WhatsApp.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening WhatsApp");
                    openWhatsapp();
                }

                else if(data.get(0).toLowerCase().contains("open gmail")){
                    et.setText(data.get(0));
                    tts.speak("Opening Gmail.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening Gmail");
                    openGmail();
                }

                else if(data.get(0).toLowerCase().contains("open pubg") || data.get(0).toLowerCase().contains("open bgm")){
                    et.setText(data.get(0));
                    tts.speak("Opening B G M I.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening BGMI");
                    openBGMI();
                }

                else if(data.get(0).toLowerCase().contains("open youtube")){
                    et.setText(data.get(0));
                    tts.speak("Opening YouTube.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening YouTube");
                    openYouTube();
                }

                else if(data.get(0).toLowerCase().contains("open camera") || data.get(0).toLowerCase().contains("open the camera")){
                    et.setText(data.get(0));
                    tts.speak("Opening Camera.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening Camera");
                    openCamera();
                }

                else if(data.get(0).toLowerCase().contains("open chrome") || data.get(0).toLowerCase().contains("open google chrome")){
                    et.setText(data.get(0));
                    tts.speak("Opening Chrome.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Opening Chrome");
                    openChrome();
                }

                else if(data.get(0).contains("created you") || data.get(0).contains("by whom were you created") || data.get(0).contains("who gave its name") || data.get(0).contains("made you") || data.get(0).contains("by whom were you made")){
                    et.setText(data.get(0));
                    tts.speak("I was created by my friend Aritra. He is a student of N I T Durgapur.", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("I was created by my friend Aritra. He is a student of NIT Durgapur.");
                }

                else if(data.get(0).contains("your name") || data.get(0).equals("name") || data.get(0).equals("what is the name")){
                    et.setText(data.get(0));
                    tts.speak("My name is Mila. What is your name?", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("My name is Mila. What is your name?");
                }

                else if(data.get(0).toLowerCase().contains("open wi-fi") || data.get(0).toLowerCase().contains("open the wi-fi") || data.get(0).toLowerCase().contains("on wi-fi") || data.get(0).toLowerCase().contains("wi-fi on") || data.get(0).toLowerCase().contains("on the wi-fi")){
                    et.setText(data.get(0));
                    openWifi();
                    tts.speak("Okay opening WiFi", TextToSpeech.QUEUE_FLUSH, null);
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Okay opening WiFi");
                }

                else if(data.get(0).toLowerCase().contains("close wi-fi") || data.get(0).toLowerCase().contains("close the wi-fi") || data.get(0).toLowerCase().contains("off wi-fi") || data.get(0).toLowerCase().contains("wi-fi off") || data.get(0).toLowerCase().contains("off the wi-fi")){
                    et.setText(data.get(0));
                    tts.speak("Okay closing WiFi", TextToSpeech.QUEUE_FLUSH, null);
                    closeWifi();
                    progressBar.setVisibility(View.VISIBLE);
                    tv2.setText("Okay closing WiFi");
                }

                else if(data.get(0) != null){
                    et.setText(data.get(0));
                    getResponse(data.get(0), progressBar);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }

    public void openFacebook(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

    public void openWhatsapp(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

    public void openWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        Toast.makeText(this, "WiFi On", Toast.LENGTH_SHORT).show();
    }

    public void closeWifi(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

    }

    public void openGmail(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

    public void openBGMI(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.pubg.imobile");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

    public void openYouTube(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCamera(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.camera");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

    public void openChrome(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
        if(launchIntent != null){
            startActivity(launchIntent);
        }else{
            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
        }
    }

//    public void openYouTube(){
//        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
//        if(launchIntent != null){
//            startActivity(launchIntent);
//        }else{
//            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void openYouTube(){
//        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
//        if(launchIntent != null){
//            startActivity(launchIntent);
//        }else{
//            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void openYouTube(){
//        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
//        if(launchIntent != null){
//            startActivity(launchIntent);
//        }else{
//            Toast.makeText(this, "There is no package", Toast.LENGTH_SHORT).show();
//        }
//    }

    public void setProgressbar(ProgressBar progressbar){
        final Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(!tts.isSpeaking()){
                    progressbar.setVisibility(View.INVISIBLE);
                }
                h.postDelayed(this, 1000);
            }
        };
        h.postDelayed(r, 1000);

    }

    private void getResponse(String msg, ProgressBar progressBar){
        String url = "http://api.brainshop.ai/get?bid=162170&key=LhZkiqFY8KwyAk8N&uid=[uid]&msg="+msg;
        String BASE_URL = "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if(!response.isSuccessful()){
                    return;
                }
                MsgModel model = response.body();
                tts.speak(model.getCnt(), TextToSpeech.QUEUE_FLUSH, null);
                progressBar.setVisibility(View.VISIBLE);
                tv2.setText(model.getCnt());
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}