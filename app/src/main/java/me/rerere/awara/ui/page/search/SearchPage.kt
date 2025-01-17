package me.rerere.awara.ui.page.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.rerere.awara.ui.LocalRouterProvider
import me.rerere.awara.ui.component.common.BackButton
import me.rerere.awara.ui.component.common.Button
import me.rerere.awara.ui.hooks.rememberFullScreenState

@Composable
fun SearchPage() {
    val router = LocalRouterProvider.current
    val fullScreenState = rememberFullScreenState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Search")
                },
                navigationIcon = {
                    BackButton()
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            Button(
                onClick = {
                    if (fullScreenState.isFullScreen) {
                        fullScreenState.exitFullScreen()
                    } else {
                        fullScreenState.enterFullScreen()
                    }
                }
            ) {
                Text("F: ${fullScreenState.isFullScreen}")
            }
        }
    }
}
