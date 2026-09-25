package com.ideacraftlab.healthbell.ui.dashboard

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import javax.annotation.concurrent.Immutable

@Immutable
data class ShopItemData(val title: String, val price: String, val productId: String)

@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel()
) {
    val isReady by viewModel.isBillingReady.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    val activity = LocalActivity.current
    
    val bgColor = Color(0xFFFDF9F3)
    val primaryOrange = MaterialTheme.colorScheme.primary

    // Stable lambda for clicks
    val onBuyClicked = remember(viewModel, activity) {
        { productId: String ->
            activity?.let { viewModel.buyCoins(it, productId) }
            Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (!isReady) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryOrange)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 32.dp, bottom = 24.dp)
            ) {
                // Title
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HeaderSection()
                }

                // Balance Card (Observes its own state)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val userCoins by viewModel.userCoins.collectAsState()
                    TotalCoinsCard(userCoins)
                }

                // Spacing
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Shop Items
                items(
                    items = shopItems,
                    key = { it.productId }
                ) { item ->
                    CoinShopCard(
                        item = item,
                        onClick = onBuyClicked,
                        primaryColor = primaryOrange
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Coin Shop",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF2D2D2D)
        )
        Text(
            text = "Premium store for health journey",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TotalCoinsCard(coins: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D4F44)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Balance",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "$coins Coins",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
            }
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🪙", fontSize = 32.sp)
                }
            }
        }
    }
}

@Composable
fun CoinShopCard(
    item: ShopItemData, 
    onClick: (String) -> Unit, 
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.productId) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🪙", fontSize = 28.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2D2D2D),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
