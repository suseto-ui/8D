package com.example.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

/**
 * Správce hlasového výstupu (Text-To-Speech) pro O.M.N.I.S.
 * Zajišťuje automatické očištění Markdown syntaxe (#, **, *, _, `, křížky, hvězdičky)
 * před předčítáním výstupů uživateli.
 */
class SpeechController(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    fun initialize() {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("cs-CZ"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "Český jazyk pro TTS není dostupný.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Inicializace TTS selhala.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Očištění Markdownu a speciálních značek před předčítáním (TTS).
     * Odstraňuje nadpisy (#), tučné písmo (**), kurzívu (*), odrážky, kódové bloky, křížky, hvězdičky atd.,
     * tak aby syntéza řeči nepředčítala "hvězdička hvězdička nadpis křížek křížek".
     */
    fun cleanTextForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        var cleaned = rawText

        // 1. Odstranění kódových bloků (```code```) -> ponecháme text uvnitř
        cleaned = cleaned.replace(Regex("```[a-zA-Z]*\\n?([\\s\\S]*?)```")) { match ->
            match.groupValues.getOrNull(1)?.trim() ?: ""
        }

        // 2. Odstranění inline kódu (`code`)
        cleaned = cleaned.replace(Regex("`([^`]+)`"), "$1")

        // 3. Odkazy: [text](url) -> pouze "text"
        cleaned = cleaned.replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")

        // 4. Obrázky: ![alt](url) -> ""
        cleaned = cleaned.replace(Regex("!\\[([^\\]]*)\\]\\([^\\)]+\\)"), "")

        // 5. Nadpisy (#, ##, ###, atd.) na začátku řádků
        cleaned = cleaned.replace(Regex("(?m)^#{1,6}\\s+"), "")

        // 6. Tučný text a kurzíva (**text**, *text*, __text__, _text_)
        cleaned = cleaned.replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
        cleaned = cleaned.replace(Regex("\\*([^*]+)\\*"), "$1")
        cleaned = cleaned.replace(Regex("__([^_]+)__"), "$1")
        cleaned = cleaned.replace(Regex("_([^_]+)_"), "$1")

        // 7. Odrážky seznamu (* , - , + )
        cleaned = cleaned.replace(Regex("(?m)^\\s*[*+\\-]\\s+"), "")

        // 8. Citace (> )
        cleaned = cleaned.replace(Regex("(?m)^\\s*>\\s*"), "")

        // 9. Horizontální oddělovače (---, ***, ___)
        cleaned = cleaned.replace(Regex("(?m)^[-*_]{3,}\\s*$"), "")

        // 10. Odstranění zbylých markdown značek a symbolů (*, #, _, `, ~, |)
        cleaned = cleaned.replace(Regex("[*#_`~|]"), "")

        // 11. Plynulejší čtení ampersandu a zkratek
        cleaned = cleaned.replace(" & ", " a ")

        // 12. Normalizace prázdných řádků na větnou pauzu (tečku) a ořez mezer
        cleaned = cleaned.replace(Regex("\\n{2,}"), ". ")
        cleaned = cleaned.replace(Regex("\\n"), " ")
        cleaned = cleaned.replace(Regex("\\s{2,}"), " ").trim()

        return cleaned
    }

    fun speak(text: String) {
        val cleanText = cleanTextForSpeech(text)
        if (cleanText.isBlank()) return

        stop()

        // Aplikovat uživatelské nastavení rychlosti, výšky a hlasu
        try {
            val rate = com.example.ui.speech.SpeechSettingsManager.speechRate.value
            val pitch = com.example.ui.speech.SpeechSettingsManager.speechPitch.value
            val gender = com.example.ui.speech.SpeechSettingsManager.voiceGender.value

            tts?.setSpeechRate(rate)
            tts?.setPitch(pitch)

            if (gender != com.example.ui.speech.VoiceGender.AUTO && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val availableVoices = tts?.voices ?: emptySet()
                val targetNameKeywords = if (gender == com.example.ui.speech.VoiceGender.MALE) listOf("male", "man", "cz-m", "cs-cz-language", "m01", "m02") else listOf("female", "woman", "cz-f", "cs-cz-language", "f01", "f02")
                val matchedVoice = availableVoices.firstOrNull { voice ->
                    val nameLower = voice.name.lowercase()
                    targetNameKeywords.any { kw -> nameLower.contains(kw) }
                }
                if (matchedVoice != null) {
                    tts?.voice = matchedVoice
                }
            }
        } catch (_: Exception) {
            // Bezpečná záloha při nekompatibilitě TTS engine
        }

        if (cleanText.length >= TextToSpeech.getMaxSpeechInputLength()) {
            val chunks = cleanText.chunked(TextToSpeech.getMaxSpeechInputLength() - 1)
            chunks.forEachIndexed { index, chunk ->
                tts?.speak(
                    chunk,
                    if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    null,
                    null
                )
            }
        } else {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
            // Safe cleanup ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
            // Safe cleanup ignore
        } finally {
            tts = null
        }
    }
}
