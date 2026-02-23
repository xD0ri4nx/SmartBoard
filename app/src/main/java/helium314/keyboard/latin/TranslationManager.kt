package helium314.keyboard.latin

import android.content.Context
import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * Manager pentru identificarea limbii și traducere offline cu suport pentru limbă țintă dinamică.
 */
object TranslationManager {
    private const val TAG = "MLKit_Sanity"
    private const val PREFS_NAME = "translation_settings"
    private const val KEY_TARGET_LANG = "target_lang_code"

    /**
     * Returnează limbile principale suportate pentru meniul de selecție.
     * Pereche: (Cod ML Kit, Nume afișabil)
     */
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            TranslateLanguage.ROMANIAN to "Română",
            TranslateLanguage.ENGLISH to "English",
            TranslateLanguage.GERMAN to "Deutsch",
            TranslateLanguage.FRENCH to "Français",
            TranslateLanguage.SPANISH to "Español",
            TranslateLanguage.ITALIAN to "Italiano",
            TranslateLanguage.HUNGARIAN to "Magyar",
            TranslateLanguage.RUSSIAN to "Русский"
        )
    }

    fun setTargetLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TARGET_LANG, langCode)
            .apply()
        Log.d(TAG, "Limbă țintă salvată: $langCode")
    }

    fun getTargetLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TARGET_LANG, TranslateLanguage.ROMANIAN) ?: TranslateLanguage.ROMANIAN
    }

    /**
     * Traduce textul detectând automat limba sursă și folosind limba țintă salvată.
     */
    fun translateText(
        context: Context,
        input: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (input.isBlank()) return

        val targetLangCode = getTargetLanguage(context)

        // 1. Identificăm limba textului introdus
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(input)
            .addOnSuccessListener { langCode ->
                if (langCode == "und") {
                    onError(Exception("Limbă sursă necunoscută"))
                } else {
                    Log.d(TAG, "Detectat: $langCode -> Țintă: $targetLangCode")
                    performTranslation(input, langCode, targetLangCode, onSuccess, onError)
                }
            }
            .addOnFailureListener { onError(it) }
    }

    private fun performTranslation(
        input: String,
        sourceLangCode: String,
        targetLangCode: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // 2. Configurăm translatorul dinamic
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        val translator = Translation.getClient(options)

        // 3. Descărcăm modelul (dacă e necesar) și traducem
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(input)
                    .addOnSuccessListener { result ->
                        onSuccess(result)
                        translator.close() // Curățenie succes
                    }
                    .addOnFailureListener {
                        onError(it)
                        translator.close() // Curățenie eroare traducere
                    }
            }
            .addOnFailureListener {
                onError(it)
                translator.close() // Curățenie eroare model
            }
    }
}
