package com.dindz.android.ui.screens.searchresult

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dindz.android.LocalPlayerServiceBinder
import com.dindz.android.R
import com.dindz.android.preferences.UIStatePreferences
import com.dindz.android.ui.components.LocalMenuState
import com.dindz.android.ui.components.themed.Header
import com.dindz.android.ui.components.themed.NonQueuedMediaItemMenu
import com.dindz.android.ui.components.themed.Scaffold
import com.dindz.android.ui.items.AlbumItem
import com.dindz.android.ui.items.AlbumItemPlaceholder
import com.dindz.android.ui.items.ArtistItem
import com.dindz.android.ui.items.ArtistItemPlaceholder
import com.dindz.android.ui.items.PlaylistItem
import com.dindz.android.ui.items.PlaylistItemPlaceholder
import com.dindz.android.ui.items.SongItem
import com.dindz.android.ui.items.SongItemPlaceholder
import com.dindz.android.ui.items.VideoItem
import com.dindz.android.ui.items.VideoItemPlaceholder
import com.dindz.android.ui.screens.GlobalRoutes
import com.dindz.android.ui.screens.Route
import com.dindz.android.ui.screens.albumRoute
import com.dindz.android.ui.screens.artistRoute
import com.dindz.android.ui.screens.playlistRoute
import com.dindz.android.utils.asMediaItem
import com.dindz.android.utils.forcePlay
import com.dindz.compose.persist.LocalPersistMap
import com.dindz.compose.persist.PersistMapCleanup
import com.dindz.compose.routing.RouteHandler
import com.dindz.core.ui.Dimensions
import com.dindz.providers.innertube.Innertube
import com.dindz.providers.innertube.models.bodies.ContinuationBody
import com.dindz.providers.innertube.models.bodies.SearchBody
import com.dindz.providers.innertube.requests.searchPage
import com.dindz.providers.innertube.utils.from

@OptIn(ExperimentalFoundationApi::class)
@Route
@Composable
fun SearchResultScreen(query: String, onSearchAgain: () -> Unit) {
    val persistMap = LocalPersistMap.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "searchResults/$query/")

    RouteHandler {
        GlobalRoutes()

        Content {
            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = {
                Header(
                    title = query,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            persistMap?.clean("searchResults/$query/")
                            onSearchAgain()
                        }
                    }
                )
            }

            Scaffold(
                key = "searchresult",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = UIStatePreferences.searchResultScreenTabIndex,
                onTabChange = { UIStatePreferences.searchResultScreenTabIndex = it },
                tabColumnContent = {
                    tab(0, R.string.songs, R.drawable.musical_notes)
                    tab(1, R.string.albums, R.drawable.disc)
                    tab(2, R.string.artists, R.drawable.person)
                    tab(3, R.string.videos, R.drawable.film)
                    tab(4, R.string.playlists, R.drawable.playlist)
                }
            ) { tabIndex ->
                saveableStateHolder.SaveableStateProvider(tabIndex) {
                    when (tabIndex) {
                        0 -> ItemsPage(
                            tag = "searchResults/$query/songs",
                            provider = { continuation ->
                                if (continuation == null) Innertube.searchPage(
                                    body = SearchBody(
                                        query = query,
                                        params = Innertube.SearchFilter.Song.value
                                    ),
                                    fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                ) else Innertube.searchPage(
                                    body = ContinuationBody(continuation = continuation),
                                    fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                )
                            },
                            emptyItemsText = stringResource(R.string.no_search_results),
                            header = headerContent,
                            itemContent = { song ->
                                SongItem(
                                    song = song,
                                    thumbnailSize = Dimensions.thumbnails.song,
                                    modifier = Modifier.combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem
                                                )
                                            }
                                        },
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(song.asMediaItem)
                                            binder?.setupRadio(song.info?.endpoint)
                                        }
                                    )
                                )
                            },
                            itemPlaceholderContent = {
                                SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
                            }
                        )

                        1 -> ItemsPage(
                            tag = "searchResults/$query/albums",
                            provider = { continuation ->
                                if (continuation == null) {
                                    Innertube.searchPage(
                                        body = SearchBody(
                                            query = query,
                                            params = Innertube.SearchFilter.Album.value
                                        ),
                                        fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                    )
                                } else {
                                    Innertube.searchPage(
                                        body = ContinuationBody(continuation = continuation),
                                        fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                    )
                                }
                            },
                            emptyItemsText = stringResource(R.string.no_search_results),
                            header = headerContent,
                            itemContent = { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    modifier = Modifier.clickable(onClick = { albumRoute(album.key) })
                                )
                            },
                            itemPlaceholderContent = {
                                AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                            }
                        )

                        2 -> ItemsPage(
                            tag = "searchResults/$query/artists",
                            provider = { continuation ->
                                if (continuation == null) {
                                    Innertube.searchPage(
                                        body = SearchBody(
                                            query = query,
                                            params = Innertube.SearchFilter.Artist.value
                                        ),
                                        fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                    )
                                } else {
                                    Innertube.searchPage(
                                        body = ContinuationBody(continuation = continuation),
                                        fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                    )
                                }
                            },
                            emptyItemsText = stringResource(R.string.no_search_results),
                            header = headerContent,
                            itemContent = { artist ->
                                ArtistItem(
                                    artist = artist,
                                    thumbnailSize = 64.dp,
                                    modifier = Modifier
                                        .clickable(onClick = { artistRoute(artist.key) })
                                )
                            },
                            itemPlaceholderContent = {
                                ArtistItemPlaceholder(thumbnailSize = 64.dp)
                            }
                        )

                        3 -> ItemsPage(
                            tag = "searchResults/$query/videos",
                            provider = { continuation ->
                                if (continuation == null) Innertube.searchPage(
                                    body = SearchBody(
                                        query = query,
                                        params = Innertube.SearchFilter.Video.value
                                    ),
                                    fromMusicShelfRendererContent = Innertube.VideoItem::from
                                ) else Innertube.searchPage(
                                    body = ContinuationBody(continuation = continuation),
                                    fromMusicShelfRendererContent = Innertube.VideoItem::from
                                )
                            },
                            emptyItemsText = stringResource(R.string.no_search_results),
                            header = headerContent,
                            itemContent = { video ->
                                VideoItem(
                                    video = video,
                                    thumbnailWidth = 128.dp,
                                    thumbnailHeight = 72.dp,
                                    modifier = Modifier.combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    mediaItem = video.asMediaItem,
                                                    onDismiss = menuState::hide
                                                )
                                            }
                                        },
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(video.asMediaItem)
                                            binder?.setupRadio(video.info?.endpoint)
                                        }
                                    )
                                )
                            },
                            itemPlaceholderContent = {
                                VideoItemPlaceholder(
                                    thumbnailWidth = 128.dp,
                                    thumbnailHeight = 72.dp
                                )
                            }
                        )

                        4 -> ItemsPage(
                            tag = "searchResults/$query/playlists",
                            provider = { continuation ->
                                if (continuation == null) Innertube.searchPage(
                                    body = SearchBody(
                                        query = query,
                                        params = Innertube.SearchFilter.CommunityPlaylist.value
                                    ),
                                    fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                ) else Innertube.searchPage(
                                    body = ContinuationBody(continuation = continuation),
                                    fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                )
                            },
                            emptyItemsText = stringResource(R.string.no_search_results),
                            header = headerContent,
                            itemContent = { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    thumbnailSize = Dimensions.thumbnails.playlist,
                                    modifier = Modifier.clickable {
                                        playlistRoute(playlist.key, null, null, false)
                                    }
                                )
                            },
                            itemPlaceholderContent = {
                                PlaylistItemPlaceholder(thumbnailSize = Dimensions.thumbnails.playlist)
                            }
                        )
                    }
                }
            }
        }
    }
}
