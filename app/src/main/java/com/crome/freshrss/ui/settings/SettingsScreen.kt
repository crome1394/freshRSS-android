package com.crome.freshrss.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.crome.freshrss.ui.theme.AppPalette
import com.crome.freshrss.ui.theme.scheme
import com.crome.freshrss.ui.theme.AppThemeMode

/** Settings tabs, swiped left and right inside the sheet. */
private enum class SettingsTab(val label: String) {
    SERVER("Server"),
    READING("Reading"),
    LOOK("Look"),
    TAILSCALE("Tailscale"),
}

/**
 * Settings as a bottom sheet over the list. Drag the handle to resize it
 * (half ↔ full height) or down to close. Tabs swipe sideways. Theme and colors
 * apply at once; the rest waits for Save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    vm: SettingsViewModel,
    onDismiss: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val pager = rememberPagerState { SettingsTab.entries.size }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .imePadding(),
        ) {
            // Title row keeps Test and Save in reach at half height.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { vm.testConnection() }, enabled = !state.testing) {
                    Text(if (state.testing) "Testing…" else "Test")
                }
                Spacer(Modifier.width(4.dp))
                Button(onClick = { vm.save() }) {
                    Text(if (state.saved) "Saved" else "Save")
                }
            }
            state.testResult?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = testResultColor(it),
                    maxLines = 3,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
                )
            }
            // Scrolls when the tabs don't fit (large font sizes, narrow phones).
            PrimaryScrollableTabRow(
                selectedTabIndex = pager.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 12.dp,
            ) {
                SettingsTab.entries.forEachIndexed { i, tab ->
                    Tab(
                        selected = pager.currentPage == i,
                        onClick = { scope.launch { pager.animateScrollToPage(i) } },
                        text = { Text(tab.label, maxLines = 1) },
                    )
                }
            }
            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    when (SettingsTab.entries[page]) {
                        SettingsTab.SERVER -> ServerSettings(state, vm)
                        SettingsTab.READING -> ReadingSettings(state, vm)
                        SettingsTab.LOOK -> LookSettings(state, vm)
                        SettingsTab.TAILSCALE -> TailscaleSettings(state, vm)
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** First run: just the server fields, full screen, until a server is saved. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit,
    /** First launch with no server URL — show setup intro and primary “Save & continue”. */
    firstRun: Boolean = false,
    onSetupComplete: () -> Unit = {},
) {
    val state by vm.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (firstRun) "Set up FreshRSS" else "Settings") },
                navigationIcon = {
                    if (!firstRun) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Welcome", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter your self-hosted FreshRSS server. " +
                    "URLs default to HTTPS. Enable “Allow insecure HTTP” only for trusted LAN/VPN.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            ServerSettings(state, vm)
            Spacer(Modifier.height(16.dp))
            SaveBar(
                state = state,
                vm = vm,
                saveLabel = "Save & continue",
                onSaved = if (firstRun) onSetupComplete else null,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ServerSettings(state: SettingsUiState, vm: SettingsViewModel) {
    OutlinedTextField(
        value = state.baseUrl,
        onValueChange = vm::updateBaseUrl,
        label = { Text("Server URL") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("https://freshrss.example.com") },
        supportingText = { Text("A bare host gets https:// automatically.") },
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.user,
        onValueChange = vm::updateUser,
        label = { Text("User") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.apiPassword,
        onValueChange = vm::updatePassword,
        label = { Text("API password") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        supportingText = { Text("FreshRSS → Profile → API password (not your login password).") },
    )
    Spacer(Modifier.height(16.dp))
    LayoutSwitchRow(
        title = "Allow insecure HTTP",
        subtitle = "Permit http:// server URLs (LAN / Tailscale only). " +
            "Credentials should travel over HTTPS when possible.",
        checked = state.allowCleartextHttp,
        onCheckedChange = vm::updateAllowCleartextHttp,
    )
}

@Composable
private fun ReadingSettings(state: SettingsUiState, vm: SettingsViewModel) {
    SectionTitle("Download")
    OutlinedTextField(
        value = state.historyDays.toString(),
        onValueChange = vm::updateHistoryDays,
        label = { Text("History (days)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            Text("Articles from the last N days (default 30). 0 = use the limits below.")
        },
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.itemLimit.toString(),
        onValueChange = vm::updateItemLimit,
        label = { Text("Max articles (Unread / Starred)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = state.historyDays == 0,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.perFeedLimit.toString(),
        onValueChange = vm::updatePerFeed,
        label = { Text("Per feed (All / Read)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = state.historyDays == 0,
    )
    Spacer(Modifier.height(20.dp))
    SectionTitle("Filters")
    LayoutSwitchRow(
        title = "Open Filters on start",
        subtitle = "Show the date and download filters when the app opens.",
        checked = state.expandFiltersOnStart,
        onCheckedChange = vm::updateExpandFiltersOnStart,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LookSettings(state: SettingsUiState, vm: SettingsViewModel) {
    SectionTitle("Theme")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (mode in AppThemeMode.entries) {
            FilterChip(
                selected = state.themeMode == mode,
                onClick = { vm.updateThemeMode(mode) },
                label = { Text(mode.label) },
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    SectionTitle("Colors")
    Text(
        "Each has a light and a dark version. Wallpaper uses your phone's colors (Android 12+).",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (p in AppPalette.entries) {
            if (p == AppPalette.DYNAMIC && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) continue
            FilterChip(
                selected = state.palette == p,
                onClick = { vm.updatePalette(p) },
                label = { Text(p.label) },
                leadingIcon = { PaletteSwatch(p) },
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    SectionTitle("Layout")
    LayoutSwitchRow(
        title = "Scope chips at bottom",
        subtitle = "Unread / All / Read / Starred / Video / Sound sit just above the footer.",
        checked = state.scopeChipsAtBottom,
        onCheckedChange = vm::updateScopeChipsAtBottom,
    )
    Spacer(Modifier.height(12.dp))
    LayoutSwitchRow(
        title = "Filters at bottom",
        subtitle = "The date and download filters open above the footer.",
        checked = state.filtersAtBottom,
        onCheckedChange = vm::updateFiltersAtBottom,
    )
}

@Composable
private fun TailscaleSettings(state: SettingsUiState, vm: SettingsViewModel) {
    LayoutSwitchRow(
        title = "Show Tailscale button",
        subtitle = "Key in the footer: tap to connect or disconnect, long-press to open Tailscale.",
        checked = state.showTailscaleButton,
        onCheckedChange = vm::updateShowTailscaleButton,
    )
    Spacer(Modifier.height(16.dp))
    LayoutSwitchRow(
        title = "Use Tailscale on mobile data",
        subtitle = "Off Wi-Fi, connect Tailscale to refresh or mark articles, then disconnect " +
            "right after, or when you leave the app. Leaves a connection you made yourself alone.",
        checked = state.autoTailscale,
        onCheckedChange = vm::updateAutoTailscale,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        "Tailscale must be installed and logged in, with its VPN prompt accepted once.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Test connection and Save, with the test result above them. */
@Composable
private fun SaveBar(
    state: SettingsUiState,
    vm: SettingsViewModel,
    modifier: Modifier = Modifier,
    saveLabel: String? = null,
    onSaved: (() -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        state.testResult?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = testResultColor(it),
                modifier = Modifier.padding(bottom = 8.dp),
                maxLines = 3,
            )
        }
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { vm.testConnection() },
                enabled = !state.testing,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (state.testing) "Testing…" else "Test", maxLines = 1)
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { vm.save(onSuccess = onSaved) },
                modifier = Modifier.weight(1f),
            ) {
                Text(saveLabel ?: if (state.saved) "Saved" else "Save", maxLines = 1)
            }
        }
    }
}

@Composable
private fun LayoutSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Two dots, light and dark, each the palette's accent on its background. */
@Composable
private fun PaletteSwatch(palette: AppPalette) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (dark in listOf(false, true)) {
            val scheme = if (palette == AppPalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                palette.scheme(dark)
            }
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(scheme.background)
                    .border(1.dp, scheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                )
            }
        }
    }
}

/** Connection test result: bright green when it worked, bright red when it didn't. */
private fun testResultColor(result: String): Color = when {
    result.startsWith("OK") -> Color(0xFF22C55E)
    result.startsWith("Connecting") -> Color.Unspecified // still running: normal text
    else -> Color(0xFFFF3B3B)
}
