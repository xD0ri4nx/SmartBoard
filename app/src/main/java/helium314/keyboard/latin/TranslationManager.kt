package helium314.keyboard.latin

import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * Singleton manager for language detection and translation using ML Kit.
 */
object TranslationManager {
    private const val TAG = "TranslationManager"

    fun translateText(
        input: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (input.isBlank()) {
            onError(IllegalArgumentException("Input is blank"))
            return
        }

        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(input)
            .addOnSuccessListener { langCode ->
                if (langCode == "und") {
                    onError(Exception("Unknown language"))
                    return@addOnSuccessListener
                }
                Log.d(TAG, "detected: $langCode")
                performTranslation(input, langCode, onSuccess, onError)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    private fun performTranslation(
        input: String,
        sourceLang: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        var resolvedLang = TranslateLanguage.fromLanguageTag(sourceLang)
        if (resolvedLang == null && sourceLang.contains("-")) {
            val baseLang = sourceLang.split("-")[0]
            resolvedLang = TranslateLanguage.fromLanguageTag(baseLang)
            if (resolvedLang != null) {
                Log.d(TAG, "Falling back from $sourceLang to $baseLang")
            }
        }

        if (resolvedLang == null) {
            onError(IllegalArgumentException("Unsupported language: $sourceLang"))
            return
        }
        Log.d(TAG, "source language tag: $sourceLang, resolved: $resolvedLang")

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(resolvedLang)
            .setTargetLanguage(TranslateLanguage.ROMANIAN)
            .build()
        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(input)
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "translated length: ${result.length}")
                        onSuccess(result)
                        translator.close()
                    }
                    .addOnFailureListener { e ->
                        onError(e)
                        translator.close()
                    }
            }
            .addOnFailureListener { e ->
                onError(e)
                translator.close()
            }
    }
}
