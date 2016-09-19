package com.aleks.dronecommandcenter.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.aleks.dronecommandcenter.ARDrone;
import com.aleks.dronecommandcenter.DroneCommandCenterApp;
import com.aleks.dronecommandcenter.R;

public class VoiceActivity extends AppCompatActivity implements RecognitionListener {

    private ARDrone drone;

    private TextView returnedText;
    private ToggleButton toggleButton;
    private ToggleButton tglPractice;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";

    List<String> oldWords;
    int lastCommandIndex;
    private final long DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        tglPractice = (ToggleButton) findViewById(R.id.tglPractice);

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        // 30 sec min wait before stopping recog
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, Long.valueOf(30000));
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });

        DroneCommandCenterApp app = (DroneCommandCenterApp) getApplicationContext();
        drone = app.getARDrone();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        //Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
        returnedText.setText("");
        lastCommandIndex = -1;
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        //Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle partials) {
        //Log.i(LOG_TAG, "onPartialResults");
        ArrayList<String> newWords = partials.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String words = newWords.get(0);

        int newIndexTakeoff = words.lastIndexOf("take off");
        if(newIndexTakeoff > lastCommandIndex) {
            lastCommandIndex = newIndexTakeoff;
            if(tglPractice.isChecked()) {
                System.out.println("take off");
                drone.takeOff();
                drone.doFor(DELAY, this);
            }
        }
        words = words.replace("take off", "<font color=#00ff00>take off</font> ");

        int newIndexLand = words.lastIndexOf("take off");
        if(newIndexLand > lastCommandIndex) {
            lastCommandIndex = newIndexLand;
            if(tglPractice.isChecked()) {
                System.out.println("land");
                drone.land();
                drone.doFor(DELAY, this);
            }
        }
        words = words.replace("land", "<font color=#00ff00>land</font> ");

        int newIndexForward = words.lastIndexOf("forward");
        if(newIndexForward > lastCommandIndex) {
            lastCommandIndex = newIndexForward;
            System.out.println("forward");
            if(tglPractice.isChecked()) {
                drone.forward();
                drone.doFor(DELAY, this);
            }
        }
        words = words.replace("forward", "<font color=#00ff00>forward</font> ");

        int newIndexBackward = words.lastIndexOf("backward");
        if(newIndexBackward > lastCommandIndex) {
            lastCommandIndex = newIndexBackward;
            if(tglPractice.isChecked()) {
                System.out.println("backward");
                drone.backward();
                drone.doFor(DELAY, this);
            }
        }
        words = words.replace("backward", "<font color=#00ff00>backward</font> ");

        int newIndexLeft = words.lastIndexOf("left");
        if(newIndexLeft > lastCommandIndex) {
            lastCommandIndex = newIndexLeft;
            if(tglPractice.isChecked()) {
                System.out.println("left");
                drone.goLeft();
                drone.doFor(DELAY, this);
            }
        }
        words = words.replace("left", "<font color=#00ff00>left</font> ");

        int newIndexRight = words.lastIndexOf("right");
        if(newIndexRight > lastCommandIndex) {
            lastCommandIndex = newIndexRight;
            if(tglPractice.isChecked()) {
                System.out.println("right");
                drone.goRight();
                drone.doFor(DELAY, this);
            }
        }
        words = words.replace("right", "<font color=#00ff00>right</font> ");

        returnedText.setText(Html.fromHtml(words));
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        //Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        /*
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        returnedText.setText(text);
        */
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public void sendEmergency(View v) {
        drone.reset();
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

}