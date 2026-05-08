package com.market.astu.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.market.astu.data.model.ThemeMode

@Composable
fun CommerceTopBar(
    title: String = "",
    themeMode: ThemeMode,
    notificationCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    showSearch: Boolean = true,
    searchPlaceholder: String = "Search products, sellers, pickup points",
    avatarText: String = "A",
    balanceLabel: String? = null,        // kept for compatibility but no longer shown
    subtitle: String = "",
    onOpenHome: (() -> Unit)? = null,    // click on left home icon
    onOpenProfile: (() -> Unit)? = null  // click on right avatar
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    var isSearchExpanded by rememberSaveable(showSearch) { mutableStateOf(searchQuery.isNotBlank()) }
    val searchVisible = showSearch && (isSearchExpanded || searchQuery.isNotBlank())

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top row: left home icon / title, right actions + profile avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ----- LEFT: home icon (app) + optional title -----
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home / app icon
                    if (onOpenHome != null) {
                        IconButton(onClick = onOpenHome, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // fallback: plain icon without click
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                // ----- RIGHT: search, notification, theme, profile -----
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search toggle
                    if (showSearch && !searchVisible) {
                        TopBarActionButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Open search", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Notification
                    if (notificationCount > 0) {
                        BadgedBox(badge = { Badge { Text("$notificationCount") } }) {
                            TopBarActionButton(onClick = {}) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        TopBarActionButton(onClick = {}) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Theme toggle
                    TopBarActionButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Profile avatar (rightmost)
                    if (onOpenProfile != null) {
                        IconButton(onClick = onOpenProfile, modifier = Modifier.size(32.dp)) {
                            Surface(
                                modifier = Modifier.size(30.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = avatarText,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        // non‑clickable fallback
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = avatarText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Expandable search field (unchanged)
            AnimatedVisibility(
                visible = searchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text(text = searchPlaceholder, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                            IconButton(onClick = {
                                onSearchQueryChange("")
                                isSearchExpanded = false
                                keyboardController?.hide()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

@Composable
private fun TopBarActionButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
            content()
        }
    }
}