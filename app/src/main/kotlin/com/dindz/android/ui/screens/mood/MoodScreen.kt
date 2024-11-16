package com.dindz.android.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.dindz.android.R
import com.dindz.android.models.Mood
import com.dindz.android.ui.components.themed.Scaffold
import com.dindz.android.ui.screens.GlobalRoutes
import com.dindz.android.ui.screens.Route
import com.dindz.compose.persist.PersistMapCleanup
import com.dindz.compose.routing.RouteHandler

@Route
@Composable
fun MoodScreen(mood: Mood) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "playlist/mood/")

    RouteHandler {
        GlobalRoutes()

        Content {
            Scaffold(
                key = "mood",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChange = { },
                tabColumnContent = {
                    tab(0, R.string.mood, R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> MoodList(mood = mood)
                    }
                }
            }
        }
    }
}
