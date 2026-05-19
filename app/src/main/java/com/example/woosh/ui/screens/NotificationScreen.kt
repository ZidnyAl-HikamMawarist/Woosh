package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.model.Notification
import com.example.woosh.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifikasi", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { 
                            notifications.forEach { viewModel.deleteNotification(it.id) }
                        }) {
                            Icon(Icons.Default.DeleteSweep, "Hapus Semua", tint = WooshRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WooshRed)
            }
        } else if (notifications.isEmpty()) {
            EmptyNotifications()
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification, onClick = {
                        if (!notification.isRead) {
                            viewModel.markAsRead(notification.id)
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val icon = when (notification.type) {
        "SUCCESS" -> Icons.Default.CheckCircle
        "ALERT" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }
    val iconColor = when (notification.type) {
        "SUCCESS" -> Color(0xFF4CAF50)
        "ALERT" -> Color(0xFFF44336)
        else -> Color(0xFF2196F3)
    }

    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = sdf.format(notification.timestamp.toDate())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) SurfaceWhite else Color.White,
        shadowElevation = if (notification.isRead) 1.dp else 4.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    if (!notification.isRead) {
                        Box(Modifier.size(8.dp).background(Color.Red, CircleShape))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.body, fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(dateString, fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun EmptyNotifications() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = WooshRed.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text("Tidak ada notifikasi baru", color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}
