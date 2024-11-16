package com.dindz.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.dindz.android.Database
import com.dindz.android.LocalPlayerServiceBinder
import com.dindz.android.R
import com.dindz.android.handleUrl
import com.dindz.android.models.Mood
import com.dindz.android.models.SearchQuery
import com.dindz.android.preferences.DataPreferences
import com.dindz.android.query
import com.dindz.android.ui.screens.album.AlbumScreen
import com.dindz.android.ui.screens.artist.ArtistScreen
import com.dindz.android.ui.screens.pipedplaylist.PipedPlaylistScreen
import com.dindz.android.ui.screens.playlist.PlaylistScreen
import com.dindz.android.ui.screens.search.SearchScreen
import com.dindz.android.ui.screens.searchresult.SearchResultScreen
import com.dindz.android.ui.screens.settings.LogsScreen
import com.dindz.android.ui.screens.settings.SettingsScreen
import com.dindz.android.utils.toast
import com.dindz.compose.routing.Route0
import com.dindz.compose.routing.Route1
import com.dindz.compose.routing.Route3
import com.dindz.compose.routing.Route4
import com.dindz.compose.routing.RouteHandlerScope
import com.dindz.core.data.enums.BuiltInPlaylist
import io.ktor.http.Url
import java.util.UUID

/**
 * Marker class for linters that a composable is a route and should not be handled like a regular
 * composable, but rather as an entrypoint.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Route

val albumRoute = Route1<String>("albumRoute")
val artistRoute = Route1<String>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val localPlaylistRoute = Route1<Long>("localPlaylistRoute")
val logsRoute = Route0("logsRoute")
val pipedPlaylistRoute = Route3<String, String, String>("pipedPlaylistRoute")
val playlistRoute = Route4<String, String?, Int?, Boolean>("playlistRoute")
val moodRoute = Route1<Mood>("moodRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")

@Composable
fun RouteHandlerScope.GlobalRoutes() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current

    albumRoute { browseId ->
        AlbumScreen(browseId = browseId)
    }

    artistRoute { browseId ->
        ArtistScreen(browseId = browseId)
    }

    logsRoute {
        LogsScreen()
    }

    pipedPlaylistRoute { apiBaseUrl, sessionToken, playlistId ->
        PipedPlaylistScreen(
            apiBaseUrl = runCatching { Url(apiBaseUrl) }.getOrNull()
                ?: error("Invalid apiBaseUrl: $apiBaseUrl is not a valid Url"),
            sessionToken = sessionToken,
            playlistId = runCatching {
                UUID.fromString(playlistId)
            }.getOrNull() ?: error("Invalid playlistId: $playlistId is not a valid UUID")
        )
    }

    playlistRoute { browseId, params, maxDepth, shouldDedup ->
        PlaylistScreen(
            browseId = browseId,
            params = params,
            maxDepth = maxDepth,
            shouldDedup = shouldDedup
        )
    }

    settingsRoute {
        SettingsScreen()
    }

    searchRoute { initialTextInput ->
        SearchScreen(
            initialTextInput = initialTextInput,
            onSearch = { query ->
                searchResultRoute(query)

                if (!DataPreferences.pauseSearchHistory) query {
                    Database.insert(SearchQuery(query = query))
                }
            },
            onViewPlaylist = { url ->
                with(context) {
                    runCatching {
                        handleUrl(url.toUri(), binder)
                    }.onFailure {
                        toast(getString(R.string.error_url, url))
                    }
                }
            }
        )
    }

    searchResultRoute { query ->
        SearchResultScreen(
            query = query,
            onSearchAgain = { searchRoute(query) }
        )
    }
}
