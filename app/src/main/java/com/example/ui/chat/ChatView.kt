package com.example.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.action.ActionExecutionResult
import com.example.action.ActionPayload
import com.example.auth.UserRole
import com.example.data.MemoryFragment
import com.example.data.OmnisRecord
import com.example.data.to8DVector
import com.example.ui.ActionDrivenInteractivePanel
import com.example.ui.OmnisViewModel
import com.example.ui.UserExperienceMode
import com.example.ui.beginner.BeginnerChatHeader
import com.example.ui.beginner.BeginnerGuideDialog
import com.example.ui.theme.OmnisBgDark
import com.example.ui.theme.OmnisBorderDark
import com.example.ui.theme.OmnisCyan
import com.example.ui.theme.OmnisPanelDark

/**
 * Hlavní konverzační a analytické rozhraní O.M.N.I.S. (ChatView).
 * Zahrnuje proudové zprávy, 8D syntézu, sémantickou bránu, hlasový vstup (Speech-to-Text) a interaktivní režim pro začátečníky.
 */
@Composable
fun ChatView(
    records: List<OmnisRecord>,
    isLoading: Boolean,
    isOcrLoading: Boolean,
    streamState: OmnisViewModel.StreamState?,
    inputQuery: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    onQuickQuery: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onExportPdf: (OmnisRecord) -> Unit,
    onImageSelected: (Uri) -> Unit = {},
    onDocumentSelected: (Uri, String, String) -> Unit = { _, _, _ -> },
    scrollToId: Long? = null,
    onScrollComplete: () -> Unit = {},
    selectedDomains: Set<String> = emptySet(),
    onDomainClick: (String) -> Unit = {},
    onDomainLongClick: (String, OmnisRecord) -> Unit = { _, _ -> },
    onAuthorizeRecord: (OmnisRecord) -> Unit = {},
    onClearDomainSelection: () -> Unit = {},
    onMultiDomainSynthesis: (String) -> Unit = {},
    onExecuteActionPayload: (ActionPayload) -> Unit = {},
    lastActionResult: ActionExecutionResult? = null,
    isActionExecuting: Boolean = false,
    userRole: UserRole = UserRole.STANDARD_USER,
    activeMemoryFragments: List<MemoryFragment> = emptyList(),
    isPromptGuideEnabled: Boolean = false,
    onDisablePromptGuide: () -> Unit = {},
    isCircuitBreakerTripped: Boolean = false,
    onToggleCircuitBreaker: () -> Unit = {},
    isPromptGatewayEnabled: Boolean = false,
    onTogglePromptGateway: () -> Unit = {},
    currentDomain: String = "All",
    onDomainChange: (String) -> Unit = {},
    userExperienceMode: UserExperienceMode = UserExperienceMode.STANDARD,
    onToggleUserMode: () -> Unit = {},
    onExecuteOverride: ((Long) -> Unit)? = null,
    systemStressSigma: Float = 0.18f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var showBeginnerGuideDialog by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    // Launcher pro systémové rozpoznání řeči (fallback přes RecognizerIntent)
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val updatedQuery = if (inputQuery.isBlank()) spokenText else "$inputQuery $spokenText"
                onQueryChange(updatedQuery)
            }
        }
    }

    val speechManager = remember(context) {
        SpeechToTextManager(
            context = context,
            onResult = { text ->
                val updatedQuery = if (inputQuery.isBlank()) text else "$inputQuery $text"
                onQueryChange(updatedQuery)
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            },
            onListeningStateChange = { listening ->
                isListening = listening
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stopListening()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (speechManager.isAvailable()) {
                speechManager.startListening()
            } else {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "cs-CZ")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Mluvte do mikrofonu (O.M.N.I.S.)...")
                }
                try {
                    isListening = true
                    speechIntentLauncher.launch(intent)
                } catch (e: Exception) {
                    isListening = false
                    Toast.makeText(context, "Hlasový vstup není na tomto zařízení k dispozici.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Pro hlasový vstup je vyžadováno oprávnění k mikrofonu.", Toast.LENGTH_SHORT).show()
        }
    }

    val handleMicClick = {
        if (isListening) {
            speechManager.stopListening()
            isListening = false
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                if (speechManager.isAvailable()) {
                    speechManager.startListening()
                } else {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "cs-CZ")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Mluvte do mikrofonu (O.M.N.I.S.)...")
                    }
                    try {
                        isListening = true
                        speechIntentLauncher.launch(intent)
                    } catch (e: Exception) {
                        isListening = false
                        Toast.makeText(context, "Hlasový vstup není na tomto zařízení k dispozici.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Přiložení souboru
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Snímek z fotoaparátu
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Snímek zachycen.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showBeginnerGuideDialog) {
        BeginnerGuideDialog(
            onDismiss = { showBeginnerGuideDialog = false }
        )
    }

    // Řazení zpráv pro reverseLayout: v obráceném rozvržení je index 0 dole (nejnovější zpráva u vstupního pole),
    // takže zprávy řadíme sestupně podle času. Tím se historie vykreslí v přirozeném sledu shora dolů (nejstarší nahoře -> nejnovější dole).
    val displayRecords = remember(records) { records.sortedByDescending { it.timestamp } }

    // Automatický scroll na požadované ID nebo nejnovější zprávu
    LaunchedEffect(scrollToId) {
        if (scrollToId != null) {
            val index = displayRecords.indexOfFirst { it.id == scrollToId }
            if (index >= 0) {
                listState.animateScrollToItem(if (streamState != null && streamState.text.isNotBlank()) index + 1 else index)
                onScrollComplete()
            }
        }
    }

    LaunchedEffect(records.size, streamState?.text?.length) {
        if ((records.isNotEmpty() || (streamState != null && streamState.text.isNotBlank())) && scrollToId == null) {
            listState.animateScrollToItem(0)
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    val isImeVisible = WindowInsets.isImeVisible

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OmnisBgDark)
    ) {
        // Stavový informační pruh Gemini kaskády (skryje se při psaní na klávesnici)
        AnimatedVisibility(
            visible = !isImeVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ChatStatusBanner(
                userRole = userRole,
                userExperienceMode = userExperienceMode
            )
        }

        // Začátečnické záhlaví s nápovědou (zobrazuje se v režimu STANDARD, skryje se při otevřené klávesnici)
        AnimatedVisibility(
            visible = userExperienceMode == UserExperienceMode.STANDARD && !isImeVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            BeginnerChatHeader(
                onQuickPromptSelected = onQuickQuery,
                onOpenGuide = { showBeginnerGuideDialog = true },
                onSwitchToExpert = onToggleUserMode,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Rychlý rozbalovací 8D Oktagon Dashboard pro přehled o celkovém stavu
        var isTopOctagonExpanded by remember { mutableStateOf(false) }
        val latestAssistantRecord = remember(records) { records.firstOrNull { it.role == "assistant" } }
        if (latestAssistantRecord != null && !isImeVisible) {
            Surface(
                onClick = { isTopOctagonExpanded = !isTopOctagonExpanded },
                color = OmnisPanelDark,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OmnisBorderDark),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = OmnisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "8D OKTAGON STAV (${(latestAssistantRecord.compositeScore * 100).toInt()}%)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = if (isTopOctagonExpanded) "Skrýt ▲" else "Rozbalit ▼",
                        color = OmnisCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            AnimatedVisibility(visible = isTopOctagonExpanded) {
                com.example.ui.octagon.AdaptiveOctagonDashboard(
                    values = latestAssistantRecord.to8DVector(),
                    systemStressSigma = systemStressSigma,
                    isCompact = true,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Seznam zpráv
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Streaming stav (připojen na samý konec konverzace u vstupního pole - index 0)
                if (streamState != null && streamState.text.isNotBlank()) {
                    item(key = "streaming_response") {
                        ChatMessageItem(
                            record = OmnisRecord(
                                id = -1,
                                role = "assistant",
                                content = streamState.text,
                                cognitiveProcess = streamState.stage,
                                domain = currentDomain,
                                isGlitchTriggered = streamState.stage == "anomaly",
                                topologicalDiagnostic = streamState.status,
                                timestamp = System.currentTimeMillis()
                            ),
                            onQuickQuery = onQuickQuery,
                            onSpeak = onSpeak,
                            onExportPdf = onExportPdf,
                            selectedDomains = selectedDomains,
                            onDomainClick = onDomainClick,
                            onDomainLongClick = onDomainLongClick,
                            onAuthorize = {},
                            userExperienceMode = userExperienceMode,
                            onExecuteOverride = onExecuteOverride,
                            userRole = userRole,
                            systemStressSigma = systemStressSigma
                        )
                    }
                }

                items(
                    items = displayRecords,
                    key = { it.id }
                ) { record ->
                    ChatMessageItem(
                        record = record,
                        onQuickQuery = onQuickQuery,
                        onSpeak = onSpeak,
                        onExportPdf = onExportPdf,
                        selectedDomains = selectedDomains,
                        onDomainClick = onDomainClick,
                        onDomainLongClick = onDomainLongClick,
                        onAuthorize = { onAuthorizeRecord(record) },
                        userExperienceMode = userExperienceMode,
                        onExecuteOverride = onExecuteOverride,
                        userRole = userRole,
                        systemStressSigma = systemStressSigma
                    )
                }
            }
        }

        // Vstupní lišta zpráv
        ChatInputBar(
            inputQuery = inputQuery,
            onQueryChange = onQueryChange,
            onSend = {
                if (isListening) {
                    speechManager.stopListening()
                    isListening = false
                }
                onSend()
            },
            onAttachFile = { documentPickerLauncher.launch("*/*") },
            onCameraClick = { cameraLauncher.launch(null) },
            onMicClick = handleMicClick,
            isLoading = isLoading,
            isOcrLoading = isOcrLoading,
            isListening = isListening,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isImeVisible) Modifier.navigationBarsPadding() else Modifier)
        )
    }
}
