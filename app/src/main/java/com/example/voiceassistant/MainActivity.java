package com.example.voiceassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        imButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                if(count==0){
                    imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_24));
                    // start listening
                    speechRecognizer.startListening(speechRecognizerIntent);
                    count = 1;
                    progressBar.setVisibility(View.VISIBLE);
                    tv.setText("Listening");
                    tv1.setVisibility(View.INVISIBLE);
                    progressBar2.setVisibility(View.VISIBLE);
                }

                else{
                    imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    // stop listening
                    speechRecognizer.stopListening();
                    count = 0;
                    progressBar.setVisibility(View.INVISIBLE);
                    tv.setText("Say something");
                    tv1.setVisibility(View.VISIBLE);
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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // A delay given
                        if(count==1 && !tts.isSpeaking()) {
                            speechRecognizer.startListening(speechRecognizerIntent);
                        }
                    }
                }, 3000);
            }

            @Override
            public void onError(int error) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // A delay given
                        if(count==1 && !tts.isSpeaking()) {
                            speechRecognizer.startListening(speechRecognizerIntent);
                        }
                    }
                }, 3000);
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if(data.get(0).equals("stop") || data.get(0).equals("okay stop") || data.get(0).equals("ok stop")){
                    tts.speak("Okay stopping", TextToSpeech.QUEUE_FLUSH, null);
                    imButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    speechRecognizer.stopListening();
                    count = 0;
                    progressBar.setVisibility(View.INVISIBLE);
                    tv.setText("Say something");
                    tv1.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);
                }
                else if(data.get(0) != null){

                    et.setText(data.get(0));

                    getResponse(data.get(0));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // A delay given
                            while(tts.isSpeaking()){
                                int i = 0;
                            }
                            if(count==1 && !tts.isSpeaking()) {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }
                    }, 3000);
                }
                else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // A delay given
                            if (count == 1 && !tts.isSpeaking()) {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }
                    }, 3000);
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

    private void getResponse(String msg){
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
                    System.out.println(response.code());
                    return;
                }
                MsgModel model = response.body();
                tts.speak(model.getCnt(), TextToSpeech.QUEUE_FLUSH, null);
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