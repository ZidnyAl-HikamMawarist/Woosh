package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary
import com.example.woosh.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyScreen(navController: NavHostController) {
    val pointHistory = listOf(
        PointHistory("Perjalanan JKT-BDG", "01 Apr 2026", "+250 pts"),
        PointHistory("Promo Ramadan", "25 Mar 2026", "+500 pts"),
        PointHistory("Perjalanan BDG-JKT", "15 Mar 2026", "+250 pts")
    )
    
    var loyaltyPoints by remember { mutableLongStateOf(0L) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    loyaltyPoints = snapshot?.getLong("loyaltyPoints") ?: 0L
                }
        }
    }

    val formattedPoints = java.text.NumberFormat.getIntegerInstance().format(loyaltyPoints) + " pts"
    val progress = (loyaltyPoints.toFloat() / 3000f).coerceIn(0f, 1f)
    
    val rewards = listOf(
        Reward("Diskon Tiket 50%", "1.500 pts", Icons.Default.CardGiftcard),
        Reward("Free Lounge Access", "800 pts", Icons.Default.Stars),
        Reward("Snack Box Gratis", "300 pts", Icons.Default.Train)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Frequent Whoosher", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ElegantDark)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {
            item {
                Spacer(Modifier.height(16.dp))
                // Point Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(ElegantDark, PrimaryGold)))) {
                        Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Poin Loyalitas", color = Color.White.copy(0.7f), fontSize = 12.sp)
                                    Text(formattedPoints, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                }
                                Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(12.dp)) {
                                    Text("Gold Member", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.White.copy(0.2f), CircleShape),
                                color = Color.White,
                                trackColor = Color.Transparent
                            )
                            Text("3.000 pts untuk mencapai Platinum", color = Color.White.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text("Tukar Hadiah", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
            }

            items(rewards) { reward ->
                RewardItem(reward)
                Spacer(Modifier.height(12.dp))
            }

            item {
                Spacer(Modifier.height(32.dp))
                Text("Riwayat Poin", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
            }

            items(pointHistory) { history ->
                HistoryItem(history)
                Divider(color = Color.LightGray.copy(0.2f), modifier = Modifier.padding(vertical = 12.dp))
            }
            
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

data class PointHistory(val title: String, val date: String, val amount: String)
data class Reward(val title: String, val cost: String, val icon: ImageVector)

@Composable
fun RewardItem(reward: Reward) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(ElegantDark.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(reward.icon, null, tint = ElegantDark, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(reward.cost, color = ElegantDark, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = ElegantDark, contentColor = PrimaryGold), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                Text("Tukar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryItem(history: PointHistory) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(OffWhite, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(history.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(history.date, color = Color.Gray, fontSize = 12.sp)
        }
        Text(history.amount, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

