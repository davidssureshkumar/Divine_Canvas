package com.divinecanvas.ui.editor

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divinecanvas.R
import com.divinecanvas.ui.canvas.ImageExporter
import com.divinecanvas.ui.canvas.VerseCanvas
import com.divinecanvas.ui.editor.components.CustomizationPanel
import com.divinecanvas.ui.editor.components.VerseSelectionPanel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditorScreen(
    onOpenSettings: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current

    // Dedicated off-screen render node captured at a fixed 1080x1920 (9:16) so
    // exports are always full resolution regardless of the on-screen preview size.
    val exportLayer = rememberGraphicsLayer()
    val exportWidth = with(density) { 1080.toDp() }
    val exportHeight = with(density) { 1920.toDp() }

    // Surface one-off messages from the ViewModel.
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    // Storage permission only matters on API <= 28 for gallery saves.
    val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    val storagePermission =
        rememberPermissionState(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

    suspend fun renderBitmap() = exportLayer.toImageBitmap().asAndroidBitmap()

    fun saveToGallery() {
        scope.launch {
            val saved = ImageExporter.saveToGallery(context, renderBitmap())
            snackbarHostState.showSnackbar(
                context.getString(
                    if (saved != null) R.string.export_saved else R.string.export_save_failed
                )
            )
        }
    }

    fun shareToWhatsApp() {
        scope.launch {
            val uri = ImageExporter.cacheForShare(context, renderBitmap())
            val ok = ImageExporter.shareToWhatsApp(context, uri)
            if (!ok) snackbarHostState.showSnackbar(context.getString(R.string.export_share_failed))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Off-screen full-resolution (1080x1920) render source. It is drawn first
        // and then fully covered by the opaque Scaffold, so it is never visible —
        // it exists only to be captured into [exportLayer] at print resolution.
        VerseCanvas(
            state = state.canvas,
            modifier =
                Modifier.requiredSize(exportWidth, exportHeight).drawWithContent {
                    exportLayer.record { this@drawWithContent.drawContent() }
                    drawContent()
                },
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.screen_editor_title)) },
                    actions = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // --- Live 9:16 preview ---
                VerseCanvas(
                    state = state.canvas,
                    modifier =
                        Modifier.fillMaxWidth(0.6f)
                            .align(Alignment.CenterHorizontally)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp)),
                )

                // --- Export actions ---
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { shareToWhatsApp() },
                        enabled = state.canvas.verse != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Text("  " + stringResource(R.string.action_share_whatsapp))
                    }
                    OutlinedButton(
                        onClick = {
                            if (
                                needsLegacyPermission && !storagePermission.status.isGrantedSafe()
                            ) {
                                storagePermission.launchPermissionRequest()
                            } else {
                                saveToGallery()
                            }
                        },
                        enabled = state.canvas.verse != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Text("  " + stringResource(R.string.action_save_gallery))
                    }
                }

                // --- Verse selection ---
                VerseSelectionPanel(
                    state = state,
                    onModeChange = viewModel::onModeChange,
                    onBookSelected = viewModel::onBookSelected,
                    onChapterSelected = viewModel::onChapterSelected,
                    onVerseSelected = viewModel::onVerseSelected,
                    onTranslationSelected = viewModel::onTranslationSelected,
                    onThemeSelected = viewModel::onThemeSelected,
                    onRandomTheme = viewModel::onRandomThemeVerse,
                    onLoadVerse = viewModel::onLoadVerse,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (state.isLoadingVerse) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }

                // --- Customization ---
                CustomizationPanel(
                    state = state,
                    gradients = remember { com.divinecanvas.domain.model.DefaultBackgrounds.all() },
                    onSelectGradient = viewModel::onSelectGradient,
                    onSelectPhoto = viewModel::onSelectPhoto,
                    onPhotoQueryChange = viewModel::onPhotoQueryChange,
                    onSearchPhotos = viewModel::onSearchPhotos,
                    onFontChange = viewModel::onFontChange,
                    onFontSizeChange = viewModel::onFontSizeChange,
                    onLineHeightChange = viewModel::onLineHeightChange,
                    onLetterSpacingChange = viewModel::onLetterSpacingChange,
                    onAlignChange = viewModel::onAlignChange,
                    onTextColorChange = viewModel::onTextColorChange,
                    onToggleShadow = viewModel::onToggleShadow,
                    onOverlayChange = viewModel::onOverlayChange,
                    onBannerTextChange = viewModel::onBannerTextChange,
                    onBannerPositionChange = viewModel::onBannerPositionChange,
                    onBannerColorChange = viewModel::onBannerColorChange,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun com.google.accompanist.permissions.PermissionStatus.isGrantedSafe(): Boolean =
    this is com.google.accompanist.permissions.PermissionStatus.Granted
