package com.ideacraftlab.healthbell.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onNextClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeAnimation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val eyeScaleY by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                1f at 0
                1f at 4600
                0.1f at 4800
                1f at 5000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "EyeBlink"
    )

    val primaryOrange = MaterialTheme.colorScheme.primary
    val faceColor = Color(0xFFFFB74D)
    val layer1Color = Color(0xFFFFF1D6).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
    val layer2Color = Color(0xFFFFE4B5).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 0.7f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) 
    ) {
        // Bottom Wave
        val waveColor = MaterialTheme.colorScheme.surface
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
        ) {
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * 0.4f)
                quadraticTo(size.width * 0.5f, 0f, size.width, size.height * 0.4f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color = waveColor)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = "Welcome to\nHealth Bell",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Before you explore the app, we have a few questions that'll help us personalize your experience.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            
            Spacer(modifier = Modifier.weight(1.2f))
            
            // Character Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulse
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .graphicsLayer { alpha = pulseAlpha }
                        .background(layer1Color)
                )
                
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(layer2Color)
                )
                
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(faceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 4.dp.toPx()
                        val darkColor = Color(0xFF4A4A4A)
                        val eyeHeight = 15.dp.toPx() * eyeScaleY
                        
                        // Eyes
                        drawArc(
                            color = darkColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = size.width * 0.28f, 
                                y = size.height * 0.42f + (15.dp.toPx() - eyeHeight) / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(25.dp.toPx(), eyeHeight),
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = darkColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = size.width * 0.58f, 
                                y = size.height * 0.42f + (15.dp.toPx() - eyeHeight) / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(25.dp.toPx(), eyeHeight),
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Smile
                        drawArc(
                            color = darkColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.58f),
                            size = androidx.compose.ui.geometry.Size(45.dp.toPx(), 25.dp.toPx()),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                shape = CircleShape
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
