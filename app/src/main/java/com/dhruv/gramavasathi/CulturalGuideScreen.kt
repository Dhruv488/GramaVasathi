package com.dhruv.gramavasathi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruv.gramavasathi.ui.theme.Terracotta

@Composable
fun CulturalGuideScreen() {
    val backgroundColor = Color(0xFFF7F3EE)
    val cardBackgroundColor = Color(0xFFFFFDF9) // Cream card background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp) // Standard margin
    ) {
        // iOS style top padding
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Cultural Guide",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = (-0.5).sp
            ),
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Simple tips to help you respect local traditions and have a meaningful stay.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF666666)
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp)) // More compact spacing to cards

        val guideItems = listOf(
            "How to Greet Locals",
            "What to Wear",
            "Dining Etiquette",
            "Respect Nature & Farm Life",
            "Local Experiences"
        )

        guideItems.forEach { title ->
            GuidePlaceholderCard(title = title, containerColor = cardBackgroundColor)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun GuidePlaceholderCard(title: String, containerColor: Color) {
    val icon = when (title) {
        "How to Greet Locals" -> Icons.Default.EmojiPeople
        "What to Wear" -> Icons.Default.Checkroom
        "Dining Etiquette" -> Icons.Default.Restaurant
        "Respect Nature & Farm Life" -> Icons.Default.Eco
        "Local Experiences" -> Icons.Default.Groups
        else -> Icons.Default.Info
    }
    
    val iconBgColor = Terracotta.copy(alpha = 0.12f) // Soft tinted background (muted terracotta)

    val tips = when (title) {
        "How to Greet Locals" -> listOf(
            "Greet with a smile and be friendly.",
            "Use \"Namaste\" as a polite greeting.",
            "Respect elders and address them politely.",
            "Remove footwear before entering homes."
        )
        "What to Wear" -> listOf(
            "Wear comfortable, modest clothing.",
            "Avoid flashy or revealing outfits.",
            "Carry a light jacket or shawl.",
            "Wear footwear suitable for walking."
        )
        "Dining Etiquette" -> listOf(
            "Homemade food is a part of our hospitality.",
            "Wash hands before meals.",
            "Some families eat sitting on the floor.",
            "Don't waste food. Take what you need."
        )
        "Respect Nature & Farm Life" -> listOf(
            "Do not litter. Keep the surroundings clean.",
            "Avoid damaging crops, plants or trees.",
            "Ask before entering farm areas.",
            "Keep noise low, especially at night."
        )
        "Local Experiences" -> listOf(
            "Participate in farm activities if invited.",
            "Try local food and learn family recipes.",
            "Learn about local traditions and stories.",
            "Support local and handmade products."
        )
        else -> emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        shadowElevation = 0.5.dp,
        tonalElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Balanced internal padding
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular pastel icon container
            Box(
                modifier = Modifier
                    .size(52.dp) // Balanced size for compactness
                    .background(iconBgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Terracotta, // Consistent accent for the glyph itself
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp)) // Tightened spacing

            // Thin vertical divider
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.width(14.dp)) // Tightened spacing

            // RIGHT content column - Maximized width
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = Color(0xFF1F1F1F)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 0.5.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF333333),
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF333333),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
