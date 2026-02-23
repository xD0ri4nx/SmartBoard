package helium314.keyboard.latin

import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * Manager simplu pentru identificarea limbii și traducere offline.
 */
object TranslationManager {
    private const val TAG = "MLKit_Sanity"

    fun translateText(
        input: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (input.isBlank()) return

        // 1. Identificăm limba textului introdus
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(input)
            .addOnSuccessListener { langCode ->
                if (langCode == "und") {
                    onError(Exception("Limbă necunoscută"))
                } else {
                    Log.d(TAG, "Limbă detectată: $langCode")
                    performTranslation(input, langCode, onSuccess, onError)
                }
            }
            .addOnFailureListener { onError(it) }
    }

    private fun performTranslation(
        input: String,
        sourceLangCode: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sourceLang = TranslateLanguage.fromLanguageTag(sourceLangCode)
        if (sourceLang == null) {
            onError(Exception("Limbă nesuportată: $sourceLangCode"))
            return
        }

        // 2. Configurăm translatorul din limba detectată -> Română
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(TranslateLanguage.ROMANIAN)
            .build()
        val translator = Translation.getClient(options)

        // 3. Descărcăm modelul (dacă nu există) și traducem
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(input)
                    .addOnSuccessListener { result ->
                        onSuccess(result)
                        translator.close()
                    }
                    .addOnFailureListener {
                        onError(it)
                        translator.close()
                    }
            }
            .addOnFailureListener {
                onError(it)
                translator.close()
            }
    }
}
