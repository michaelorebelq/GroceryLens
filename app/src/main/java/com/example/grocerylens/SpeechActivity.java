

package com.example.grocerylens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

// this activity enables users to speak ingredients which are then sent to
// a deployed FoodBERT API to extract food items
// speech recognition, text-to-speech, and lottie loading animations are all integrated


public class SpeechActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {


    private SpeechRecognizer speechRecognizer; // speech recogniser instance
    private RequestQueue requestQueue; // volley queue for API calls
    private TextToSpeech tts; // text-to-speech engine
    private LottieAnimationView micAnimation; // mic animation when speaking
    private LottieAnimationView loadingAnimation; // loading spinner animation
    private View loadingOverlay; // loading screen overlay
    private TextView recordInstructionText;
    private static final int REQUEST_MICROPHONE = 1;
    private static final String TAG = "SpeechActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        // initialise views

        micAnimation = findViewById(R.id.mic_animation);
        loadingAnimation = findViewById(R.id.loading_animation);
        loadingOverlay = findViewById(R.id.loading_overlay);
        requestQueue = Volley.newRequestQueue(this);
        recordInstructionText = findViewById(R.id.record_instruction);

        requestMicrophonePermission(); // ask for mic access

        micAnimation.setOnClickListener(v -> startSpeechRecognition());

        tts = new TextToSpeech(this, this); // start TTS engine
    }

    // asks user for mic permission if not already granted
    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        }
    }

    // handles text to speech engine
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS Language not supported");
            }
        } else {
            Log.e(TAG, "TTS Initialization failed");
        }
    }

    // plays TTS audio
    private void speakOut(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // adding loading animation for google cloud cold starts
    private void showOnlyLoading() {

        micAnimation.setVisibility(View.GONE);
        recordInstructionText.setVisibility(View.GONE); // hide instructions when loading

        loadingOverlay.setVisibility(View.VISIBLE);
        loadingOverlay.setAlpha(0f);
        loadingOverlay.animate().alpha(1f).setDuration(300).start();

        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.setAlpha(0f);
        loadingAnimation.animate().alpha(1f).setDuration(300).start();
        loadingAnimation.playAnimation();
    }

    private void hideLoadingAndRestoreUI() {
        loadingAnimation.pauseAnimation();
        loadingAnimation.setVisibility(View.GONE);
        loadingOverlay.setVisibility(View.GONE);

        recordInstructionText.setVisibility(View.VISIBLE); // show instructions again

        micAnimation.setVisibility(View.VISIBLE);
    }

    // handles speech recogniser logic
    private void startSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech2Recipe is not available on this device!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                speakOut("What will you cook with");
                micAnimation.playAnimation();
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onEndOfSpeech() {
                micAnimation.pauseAnimation();
            }
            @Override public void onError(int error) {
                micAnimation.pauseAnimation();
                String errorMessage = getErrorMessage(error);
                Log.e(TAG, "Error: " + errorMessage);
                speakOut(errorMessage);
                Toast.makeText(SpeechActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
            @Override public void onPartialResults(Bundle partialResults) {}

            // handles final speech recognition result
            @Override
            public void onResults(Bundle results) {
                micAnimation.pauseAnimation();
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    Log.d(TAG, "Recognized Text: " + spokenText);
                    sendToFoodBERT(spokenText);
                } else {
                    speakOut("No speech detected. Try again.");
                }
            }
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    // maps speech errors to user-friendly messages
    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client-side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Permission denied";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Speech recognizer is busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Speech timeout";
            default: return "Unknown speech recognition error";
        }
    }

    // sends recognised text to deployed food NER API (FoodBERT)
    private void sendToFoodBERT(String text) {
        // set up api endpoint for deployed FoodBERT model
        String apiUrl = "https://grocerylensner-server-85280077454.europe-west2.run.app/extract_food";

        // build the json request payload with input text
        JSONObject postData = new JSONObject();
        try {
            postData.put("text", text);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        showOnlyLoading(); // show lottie loading UI
        long startApiRequestTime = SystemClock.elapsedRealtime(); // start timing

        // make a POST request to the API
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, apiUrl, postData,
                response -> {
                    hideLoadingAndRestoreUI(); // hide loading once response returns

                    // calculate and log time taken for response
                    long endApiResponseTime = SystemClock.elapsedRealtime();
                    long duration = endApiResponseTime - startApiRequestTime;
                    Log.d("ApiTimeTest", "NLP API response time is " + duration + " ms");

                    try {
                        // extract the "food_items" array from response
                        JSONArray foodItemsArray = response.getJSONArray("food_items");
                        ArrayList<String> detectedIngredients = new ArrayList<>();
                        for (int i = 0; i < foodItemsArray.length(); i++) {
                            detectedIngredients.add(foodItemsArray.getString(i));
                        }

                        // speak out result to user
                        String ingredientString = String.join(", ", detectedIngredients);
                        speakOut("Detected ingredients: " + ingredientString);

                        // send to next screen with recognised items
                        Intent intent = new Intent(SpeechActivity.this, SelectionActivity.class);
                        intent.putStringArrayListExtra("detectedIngredients", detectedIngredients);
                        intent.putExtra("fromDetection", true); // [added] flag to indicate it came from detection
                        startActivity(intent);

                    } catch (Exception e) {
                        Log.e("API Response Error", "Parsing error: " + e.getMessage());
                        Toast.makeText(SpeechActivity.this, "Error processing API response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoadingAndRestoreUI();
                    Log.e("API Error", "Request failed: " + error.toString());
                    Toast.makeText(SpeechActivity.this, "API Request Failed", Toast.LENGTH_SHORT).show();
                }
        );

        // set retry and timeout policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // add request to queue
        requestQueue.add(request);
    }

    // cleanup resources
    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

