package com.crome.freshrss.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crome.freshrss.data.prefs.SettingsRepository
import com.crome.freshrss.data.remote.FreshRssClient
import com.crome.freshrss.ui.home.DUAL_PANE_MIN_WIDTH_DP
import com.crome.freshrss.ui.home.HomeScreen
import com.crome.freshrss.ui.home.HomeViewModel
import com.crome.freshrss.ui.settings.SettingsScreen
import com.crome.freshrss.ui.settings.SettingsSheet
import androidx.compose.runtime.saveable.rememberSaveable
import com.crome.freshrss.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.first

object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val ARTICLE = "article/{id}"
    fun article(id: String) = "article/$id"
}

@Composable
fun FreshRssNav(
    client: FreshRssClient,
    settings: SettingsRepository,
) {
    val nav = rememberNavController()
    val app = LocalContext.current.applicationContext as Application
    val homeVm: HomeViewModel = viewModel(
        factory = remember(client, settings, app) {
            HomeViewModel.factory(app, client, settings)
        },
    )
    val settingsVm: SettingsViewModel = viewModel(
        factory = remember(settings, client) {
            SettingsViewModel.factory(settings, client)
        },
    )

    val widthDp = LocalConfiguration.current.screenWidthDp
    val dualPane = widthDp >= DUAL_PANE_MIN_WIDTH_DP

    // Resolve first-run before composing NavHost so startDestination is correct.
    var bootstrapped by remember { mutableStateOf(false) }
    var needsSetup by remember { mutableStateOf(false) }
    LaunchedEffect(settings) {
        val cfg = settings.config.first()
        needsSetup = !cfg.hasBaseUrl
        bootstrapped = true
    }

    if (!bootstrapped) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Settings open as a bottom sheet over whatever screen is showing.
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    if (settingsOpen) {
        SettingsSheet(vm = settingsVm, onDismiss = { settingsOpen = false })
    }

    NavHost(
        navController = nav,
        startDestination = if (needsSetup) Routes.SETUP else Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                vm = homeVm,
                onOpenSettings = { settingsOpen = true },
                onOpenArticle = { id ->
                    // Phone / narrow: full-screen article route
                    nav.navigate(Routes.article(java.net.URLEncoder.encode(id, "UTF-8")))
                },
                dualPane = dualPane,
            )
        }
        composable(Routes.SETUP) {
            SettingsScreen(
                vm = settingsVm,
                firstRun = true,
                onBack = { /* no back until configured */ },
                onSetupComplete = {
                    needsSetup = false
                    homeVm.refresh()
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                },
            )
        }
        // Kept for phone / portrait; dual-pane never navigates here
        composable(
            route = Routes.ARTICLE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val raw = entry.arguments?.getString("id").orEmpty()
            val id = java.net.URLDecoder.decode(raw, "UTF-8")
            com.crome.freshrss.ui.article.ArticleScreen(
                articleId = id,
                vm = homeVm,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
