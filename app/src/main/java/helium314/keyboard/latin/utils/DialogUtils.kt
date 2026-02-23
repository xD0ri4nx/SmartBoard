package helium314.keyboard.latin.utils

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import helium314.keyboard.latin.R
import helium314.keyboard.latin.TranslationManager

// todo: ideally the custom InputMethodPicker would be removed / replaced with compose dialog, then this can be removed
fun getPlatformDialogThemeContext(context: Context): Context {
    // Because {@link AlertDialog.Builder.create()} doesn't honor the specified theme with
    // createThemeContextWrapper=false, the result dialog box has unneeded paddings around it.
    return ContextThemeWrapper(context, R.style.platformActivityTheme)
}

/**
 * Shows a dialog with the translated text and an option to replace the current input.
 */
fun showTranslateDialog(context: Context, view: View, translatedText: String, onConfirm: () -> Unit) {
    val dialog = AlertDialog.Builder(getPlatformDialogThemeContext(context))
        .setTitle("Translation")
        .setMessage(translatedText)
        .setPositiveButton("Use Translation") { _, _ -> onConfirm() }
        .setNegativeButton("Cancel", null)
        .create()

    setupDialogWindow(dialog, view)
    dialog.show()
}

/**
 * Shows a dialog to select the target language.
 */
fun showLanguageSelectionDialog(context: Context, view: View, onLanguageSelected: (String, String) -> Unit) {
    val languages = TranslationManager.getSupportedLanguages()
    val names = languages.map { it.second }.toTypedArray()
    val currentLang = TranslationManager.getTargetLanguage(context)
    val currentIndex = languages.indexOfFirst { it.first == currentLang }.coerceAtLeast(0)

    val dialog = AlertDialog.Builder(getPlatformDialogThemeContext(context))
        .setTitle("Choose Target Language")
        .setSingleChoiceItems(names, currentIndex) { d, which ->
            onLanguageSelected(languages[which].first, languages[which].second)
            d.dismiss()
        }
        .setNegativeButton("Cancel", null)
        .create()

    setupDialogWindow(dialog, view)
    dialog.show()
}

private fun setupDialogWindow(dialog: AlertDialog, view: View) {
    dialog.window?.let { window ->
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG)
        val lp = window.attributes
        lp.token = view.windowToken
        window.attributes = lp
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }
}
