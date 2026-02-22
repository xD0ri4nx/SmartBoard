package helium314.keyboard.latin

import android.util.Log
import android.view.inputmethod.ExtractedTextRequest

/**
 * Extension function for LatinIME to read the full text from the input field.
 */
fun LatinIME.getCurrentFullText(): String {
    val ic = currentInputConnection
    if (ic == null) {
        Log.d("GetText", "IC is null")
        return ""
    }

    val etr = ExtractedTextRequest().apply {
        hintMaxChars = Int.MAX_VALUE
        flags = 0
    }

    val et = ic.getExtractedText(etr, 0)
    val result = et?.text?.toString() ?: ""

    Log.d("GetText", "length: " + result.length)
    return result
}
