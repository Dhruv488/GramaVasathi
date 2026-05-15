package com.dhruv.gramavasathi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to hold step info
data class TrainingStep(val title: String, val description: String, val icon: ImageVector)

@Composable
fun HostTrainingScreen(onExploreClick: () -> Unit = {}) {
    // Refined Warm Color Palette
    val backgroundColor = Color(0xFFF8F6F3) // Warm off-white #F8F6F3
    val cardBackground = Color(0xFFFFFBF7)  // Soft cream #FFFBF7
    val terracotta = Color(0xFFE07A5F)      // Soft terracotta #E07A5F
    val textPrimary = Color(0xFF2E2E2E)     // Dark grey #2E2E2E
    val textSecondary = Color(0xFF7A7A7A)   // Softer grey #7A7A7A
    val peachAccent = Color(0xFFFFE8E0)     // Light peach background for icons #FFE8E0

    val trainingSteps = remember {
        listOf(
            TrainingStep(
                "Clean & fresh bedding",
                "Ensure all linens are washed and beds are made neatly before guests arrive.",
                Icons.Default.Bed
            ),
            TrainingStep(
                "Safe drinking water",
                "Provide fresh filtered or bottled water in a clean container within the guest room.",
                Icons.Default.WaterDrop
            ),
            TrainingStep(
                "Clean & private toilet",
                "Maintain high standards of hygiene and provide basic toiletries for guest comfort.",
                Icons.Default.Wc
            ),
            TrainingStep(
                "A tidy & welcoming room",
                "Remove personal clutter and add a small welcoming touch like a note or flowers.",
                Icons.Default.CleaningServices
            ),
            TrainingStep(
                "Basic first aid kit",
                "Keep essential medical supplies like bandages and antiseptic easily accessible.",
                Icons.Default.MedicalServices
            ),
            TrainingStep(
                "Bright & cozy lighting",
                "Use warm lights or lamps to create a comfortable and safe atmosphere at night.",
                Icons.Default.Lightbulb
            ),
            TrainingStep(
                "Fresh air & ventilation",
                "Ensure good air circulation by opening windows or using fans before guests arrive.",
                Icons.Default.Air
            ),
            TrainingStep(
                "Daily waste management",
                "Empty trash bins daily and follow local disposal rules to keep the room fresh.",
                Icons.Default.Delete
            )
        )
    }

    // Persist current step and checklist states using rememberSaveable
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var checkedStates by rememberSaveable { 
        mutableStateOf(List(trainingSteps.size) { false }) 
    }
    var isSuccessView by rememberSaveable { mutableStateOf(false) }
    
    val checkedCount = checkedStates.count { it }
    val overallProgress = if (trainingSteps.isNotEmpty()) checkedCount.toFloat() / trainingSteps.size else 0f
    val scorePercentage = (overallProgress * 100).toInt()
    val stepProgress = if (trainingSteps.isNotEmpty()) (currentStep + 1).toFloat() / trainingSteps.size else 0f

    val scrollState = rememberScrollState()

    // Automatically transition to success view when 100% is reached
    LaunchedEffect(scorePercentage) {
        if (scorePercentage == 100) {
            isSuccessView = true
        } else {
            isSuccessView = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp)
    ) {
        // Main content area with scrollability
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- Readiness Summary Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(cardBackground, Color(0xFFFDF2ED).copy(alpha = 0.5f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Home Readiness",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "Your journey to becoming a top host",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = textSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$scorePercentage%",
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = terracotta
                            )
                            
                            if (overallProgress >= 0.8f) {
                                Surface(
                                    color = peachAccent,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Ready to Host",
                                        color = terracotta,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = terracotta,
                            trackColor = Color(0xFFEFE6E1),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Step Indicator ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${currentStep + 1} OF ${trainingSteps.size}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 12.sp
                    ),
                    color = textSecondary
                )
                Text(
                    text = "${(stepProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = terracotta
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { stepProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = terracotta,
                trackColor = Color(0xFFE8E2DD),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Current Step Header ---
            Text(
                text = "Prepare your home",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = textPrimary
            )
            Text(
                text = "Follow these steps to ensure a wonderful stay",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- The Interactive Step Card or Success Card ---
            if (!isSuccessView) {
                if (currentStep in trainingSteps.indices) {
                    val step = trainingSteps[currentStep]
                    StepCard(
                        step = step,
                        isChecked = checkedStates.getOrElse(currentStep) { false },
                        onCheckedChange = { isChecked ->
                            val newList = checkedStates.toMutableList()
                            newList[currentStep] = isChecked
                            checkedStates = newList
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        peachAccent = peachAccent,
                        cardColor = cardBackground
                    )
                }
            } else {
                SuccessCard(
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    peachAccent = peachAccent,
                    cardColor = cardBackground
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Navigation Buttons (Fixed at bottom) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSuccessView) {
                // Success State Navigation
                OutlinedButton(
                    onClick = { 
                        isSuccessView = false 
                        currentStep = trainingSteps.size - 1 // Return to last item
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                    border = BorderStroke(1.dp, Color(0xFFDCD4CD))
                ) {
                    Text("Previous", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Button(
                    onClick = { onExploreClick() },
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = terracotta),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Explore Farm Stays",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            } else {
                // Standard Checklist Navigation
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                        border = BorderStroke(1.dp, Color(0xFFDCD4CD))
                    ) {
                        Text("Previous", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = { 
                        if (currentStep < trainingSteps.size - 1) {
                            currentStep++ 
                        } else if (scorePercentage == 100) {
                            isSuccessView = true
                        }
                    },
                    modifier = Modifier.weight(if (currentStep == 0) 1f else 1.5f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = terracotta),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (currentStep == trainingSteps.size - 1) "Complete" else "Next Step",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessCard(
    textPrimary: Color,
    textSecondary: Color,
    peachAccent: Color,
    cardColor: Color
) {
    val terracotta = Color(0xFFE07A5F)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = peachAccent,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = terracotta,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "You're Ready to Host",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textPrimary
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "Your farm stay setup is complete.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp,
                        fontSize = 14.sp
                    ),
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
fun StepCard(
    step: TrainingStep,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    peachAccent: Color,
    cardColor: Color
) {
    val terracotta = Color(0xFFE07A5F)
    val backgroundColor = if (isChecked) Color(0xFFFFF7F2) else cardColor
    val borderColor = if (isChecked) terracotta.copy(alpha = 0.5f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = peachAccent,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = terracotta,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp,
                        fontSize = 13.sp
                    ),
                    color = textSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isChecked) terracotta else Color(0xFFD1C9C2),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
