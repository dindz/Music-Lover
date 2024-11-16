package com.dindz.android.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.dindz.android.R
import com.dindz.android.ui.components.themed.Scaffold
import com.dindz.android.ui.screens.GlobalRoutes
import com.dindz.android.ui.screens.Route
import com.dindz.android.ui.screens.albumRoute
import com.dindz.compose.persist.PersistMapCleanup
import com.dindz.compose.routing.RouteHandler

@Route
@Composable
fun MoreAlbumsScreen() {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "more_albums/")

    RouteHandler {
        GlobalRoutes()

        Content {
            Scaffold(
                key = "morealbums",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChange = { },
                tabColumnContent = {
                    tab(0, R.string.albums, R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> MoreAlbumsList(
                            onAlbumClick = { albumRoute(it) }
                        )
                    }
                }
            }
        }
    }
}
