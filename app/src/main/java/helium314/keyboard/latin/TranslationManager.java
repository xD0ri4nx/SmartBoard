package helium314.keyboard.latin;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

/**
 * Singleton class to manage offline translation using Google ML Kit.
 */
public class TranslationManager {
    private static final String TAG = "MLKit_Sanity";
    private static TranslationManager instance;
    private Translator translator;
    public boolean isReady = false;

    public interface OnTranslationResult {
        void onResult(String translatedText);
    }

    private TranslationManager() {}

    public static synchronized TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }
        return instance;
    }

    /**
     * Initializes the translator (EN -> RO) and downloads the model if needed over Wi-Fi.
     */
    public void init() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.ROMANIAN)
                .build();
        translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        isReady = true;
                        Log.d(TAG, "Model downloaded successfully or already present.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isReady = false;
                        Log.e(TAG, "Model download failed: " + e.getMessage());
                    }
                });
    }

    /**
     * Translates the given text. Returns the original text if the translator is not ready.
     */
    public void translateText(final String text, final OnTranslationResult listener) {
        if (text == null || text.isEmpty()) {
            if (listener != null) listener.onResult("");
            return;
        }

        if (translator == null || !isReady) {
            Log.w(TAG, "Translator not ready. isReady=" + isReady);
            if (listener != null) listener.onResult(text);
            return;
        }

        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translatedText) {
                        if (listener != null) listener.onResult(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Translation failed: " + e.getMessage());
                        if (listener != null) listener.onResult(text);
                    }
                });
    }
}
