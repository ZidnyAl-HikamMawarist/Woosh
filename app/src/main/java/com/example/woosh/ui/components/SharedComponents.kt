package com.example.woosh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.WooshRed
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary
import com.example.woosh.ui.theme.TextSecondary

@Composable
fun SearchFieldClickable(label: String, value: String, icon: ImageVector, onClick: () -> Unit) { 
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { 
        Box(modifier = Modifier.size(36.dp).background(WooshRed.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) { 
            Icon(icon, null, tint = WooshRed, modifier = Modifier.size(18.dp)) 
        }; Spacer(modifier = Modifier.width(16.dp)); 
        Column { 
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary); 
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary) 
        } 
    } 
}

@Composable
fun HeaderSection(navController: NavHostController, unreadCount: Int = 0) { 
    Row(verticalAlignment = Alignment.CenterVertically) { 
        Column(modifier = Modifier.weight(1f)) { 
            Text("Mau ke mana", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = (-1).sp); 
            Text("hari ini?", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = (-1).sp) 
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(onClick = { navController.navigate("information") }, shape = CircleShape, color = SurfaceWhite, shadowElevation = 4.dp) { 
                Icon(Icons.Default.Info, null, tint = TextPrimary, modifier = Modifier.padding(12.dp).size(24.dp)) 
            }
            Box {
                Surface(onClick = { navController.navigate("notification") }, shape = CircleShape, color = SurfaceWhite, shadowElevation = 4.dp) { 
                    Icon(Icons.Default.Notifications, null, tint = TextPrimary, modifier = Modifier.padding(12.dp).size(24.dp)) 
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(12.dp)
                            .background(WooshRed, CircleShape)
                    )
                }
            }
            Surface(onClick = { navController.navigate("profile") }, shape = CircleShape, color = SurfaceWhite, shadowElevation = 4.dp) { 
                Icon(Icons.Default.Person, null, tint = TextPrimary, modifier = Modifier.padding(12.dp).size(24.dp)) 
            } 
        }
    } 
}

@Composable
fun SectionTitle(title: String) { 
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp)) 
}

@Composable
fun FrequentWhoosherCard(points: Long = 0, onClick: () -> Unit = {}) {
    val formattedPoints = java.text.NumberFormat.getIntegerInstance().format(points) + " pts"
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(WooshRed, Color(0xFFFF6B6B))))) {
            Box(modifier = Modifier.size(150.dp).offset(x = 200.dp, y = (-50).dp).background(Color.White.copy(0.1f), CircleShape))
            
            Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Frequent Whoosher", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Loyalty Program", color = Color.White.copy(0.7f), fontSize = 10.sp)
                    }
                    Icon(Icons.Default.Train, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Poin Kamu", color = Color.White.copy(0.7f), fontSize = 10.sp)
                        Text(formattedPoints, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(12.dp)) {
                        Text("Gold Member", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OfferBanner() { 
    Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp)).background(WooshRed)) { 
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.4f), Color.Transparent)))); 
        Column(modifier = Modifier.padding(24.dp).align(Alignment.CenterStart)) { 
            Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(8.dp)) { 
                Text("DISKON 20%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) 
            }; 
            Spacer(Modifier.height(8.dp)); 
            Text("Liburan hemat\nke mana saja!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) 
        } 
    } 
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) { 
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = WooshRed) 
        Spacer(modifier = Modifier.width(16.dp)) 
        Column { 
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold) 
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary) 
        } 
    } 
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuButton(icon: ImageVector, title: String, onClick: () -> Unit = {}) { 
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), 
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite), 
        shape = RoundedCornerShape(16.dp), 
        onClick = onClick
    ) { 
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
            Icon(icon, null, tint = WooshRed) 
            Spacer(modifier = Modifier.width(16.dp)) 
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = TextPrimary) 
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSecondary) 
        } 
    } 
}

@Composable
fun TicketInfo(label: String, value: String, valueColor: Color) { 
    Column { 
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary) 
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor) 
    } 
}

@Composable
fun CityInfo(code: String, name: String, align: Alignment.Horizontal) { 
    Column(horizontalAlignment = align) { 
        Text(code, fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Text(name, fontSize = 12.sp, color = TextSecondary) 
    } 
}

@Composable
fun ContactItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = WooshRed)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = TextPrimary)
    }
}


