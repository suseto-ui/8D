package com.example.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.UserExperienceMode
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppLocaleManager
import com.example.ui.speech.SpeechSettingsManager
import com.example.ui.speech.VoiceGender
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SettingsView(
    userExperienceMode: UserExperienceMode,
    onExperienceModeChange: (UserExperienceMode) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by AppLocaleManager.currentLanguage.collectAsStateWithLifecycle()
    val speechRate by SpeechSettingsManager.speechRate.collectAsStateWithLifecycle()
    val speechPitch by SpeechSettingsManager.speechPitch.collectAsStateWithLifecycle()
    val voiceGender by SpeechSettingsManager.voiceGender.collectAsStateWithLifecycle()

    val isEnglish = currentLang == AppLanguage.EN

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OmnisBgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Główny nagłówek / Title
        Surface(
            color = OmnisPanelDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OmnisBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OmnisCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Nastavení",
                        tint = OmnisCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isEnglish) "System Settings" else "Nastavení Systému",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnglish) "Customize voice output, language, and UX parameters" else "Přizpůsobení hlasu, jazyka a parametrů rozhraní",
                        color = OmnisTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Sekce 1: Hlasový výstup (Text-To-Speech)
        Surface(
            color = OmnisPanelDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OmnisBorderDark),
            modifier = Modifier.fillMaxWidth().testTag("speech_settings_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = OmnisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Voice Output (Text-To-Speech)" else "Hlasový Výstup (Předčítání)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = OmnisBorderDark, thickness = 1.dp)

                // Rychlost čtení
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Reading Speed" else "Rychlost čtení",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            color = OmnisCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = String.format("%.1fx", speechRate),
                                color = OmnisCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { SpeechSettingsManager.setSpeechRate(context, it) },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = OmnisCyan,
                            activeTrackColor = OmnisCyan,
                            inactiveTrackColor = OmnisBorderDark
                        ),
                        modifier = Modifier.testTag("speech_rate_slider")
                    )
                }

                // Výška hlasu (Pitch)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Voice Pitch" else "Výška hlasu",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            color = OmnisCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = String.format("%.1fx", speechPitch),
                                color = OmnisCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { SpeechSettingsManager.setSpeechPitch(context, it) },
                        valueRange = 0.5f..1.5f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = OmnisCyan,
                            activeTrackColor = OmnisCyan,
                            inactiveTrackColor = OmnisBorderDark
                        ),
                        modifier = Modifier.testTag("speech_pitch_slider")
                    )
                }

                // Gender hlasu
                Column {
                    Text(
                        text = if (isEnglish) "Voice Gender" else "Pohlaví / Gender hlasu",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceGender.values().forEach { g ->
                            val isSelected = voiceGender == g
                            Button(
                                onClick = { SpeechSettingsManager.setVoiceGender(context, g) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) OmnisCyan else OmnisBgDark,
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) g.labelEn else g.labelCs,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tlačítko pro vyzkoušení hlasu
                Button(
                    onClick = {
                        val sampleText = if (isEnglish) {
                            "This is a preview of the O.M.N.I.S. voice synthesis engine with your selected settings."
                        } else {
                            "Tohle je zkušební přehrávání hlasového výstupu O.M.N.I.S. s vaším nastavením."
                        }
                        onSpeak(sampleText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("test_voice_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OmnisCyan.copy(alpha = 0.2f),
                        contentColor = OmnisCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmnisCyan)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) "Test Selected Voice" else "Vyzkoušet vybraný hlas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Sekce 2: Jazyk aplikace (Localization)
        Surface(
            color = OmnisPanelDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OmnisBorderDark),
            modifier = Modifier.fillMaxWidth().testTag("language_settings_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = OmnisAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Application Language" else "Jazyk Aplikace",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = OmnisBorderDark, thickness = 1.dp)

                Text(
                    text = if (isEnglish) "Select the primary language for all user interfaces and messages:" else "Vyberte primární jazyk pro celé rozhraní a zprávy:",
                    color = OmnisTextMuted,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLang == lang
                        Surface(
                            onClick = { AppLocaleManager.setLanguage(context, lang) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) OmnisAmber.copy(alpha = 0.2f) else OmnisBgDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) OmnisAmber else OmnisBorderDark
                            ),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = lang.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = lang.label,
                                    color = if (isSelected) OmnisAmber else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sekce 3: Uživatelský režim O.M.N.I.S.
        Surface(
            color = OmnisPanelDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OmnisBorderDark),
            modifier = Modifier.fillMaxWidth().testTag("mode_settings_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = OmnisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "User Experience Mode" else "Uživatelský Režim O.M.N.I.S.",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = OmnisBorderDark, thickness = 1.dp)

                UserExperienceMode.values().forEach { mode ->
                    val isSelected = userExperienceMode == mode
                    Surface(
                        onClick = { onExperienceModeChange(mode) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OmnisCyan.copy(alpha = 0.15f) else OmnisBgDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) OmnisCyan else OmnisBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onExperienceModeChange(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = OmnisCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (mode == UserExperienceMode.STANDARD) {
                                        if (isEnglish) "Standard User (Plain language)" else "Běžný uživatel (Srozumitelná čeština)"
                                    } else {
                                        if (isEnglish) "Expert Operator (Strict 8D Tensors & Introspection)" else "Expertní operátor (Striktní 8D tenzory a introspekce)"
                                    },
                                    color = if (isSelected) OmnisCyan else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (mode == UserExperienceMode.STANDARD) {
                                        if (isEnglish) "Friendly explanations without complex matrix math" else "Přirozené lidské odpovedi bez složitých vzorců"
                                    } else {
                                        if (isEnglish) "Full cognitive process logs, 8D vectors, Zero-Fluff" else "Zobrazuje plný myšlenkový postup, 8D matice a verifikace"
                                    },
                                    color = OmnisTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sekce 4: Informace o systému
        Surface(
            color = OmnisPanelDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OmnisBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "O.M.N.I.S. AKGE-8D v4.2",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (isEnglish) "Engine: Agentic Knowledge Graph & Invariant Inference Engine 8D" else "Jádro: Agentní znalostní graf a invariantní inferenční engine 8D",
                    color = OmnisTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
