package com.example.duellpcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import com.example.duellpcounter.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class UtilityTool { COIN, DICE, CALC }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DuelLPCounterTheme {
                DuelApp()
            }
        }
    }
}

@Composable
fun DuelApp() {
    var p1Lp by rememberSaveable { mutableIntStateOf(8000) }
    var p2Lp by rememberSaveable { mutableIntStateOf(8000) }
    var p1Name by rememberSaveable { mutableStateOf("Player 1") }
    var p2Name by rememberSaveable { mutableStateOf("Player 2") }
    var activeTool by rememberSaveable { mutableStateOf<UtilityTool?>(null) }
    var calcPreselectedPlayer by rememberSaveable { mutableIntStateOf(1) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showVictoryOverlay by rememberSaveable { mutableStateOf(true) }

    val duelFinished = p1Lp == 0 || p2Lp == 0
    val winnerName = if (p1Lp == 0) p2Name else p1Name

    LaunchedEffect(p1Lp, p2Lp) {
        if (p1Lp > 0 && p2Lp > 0) {
            showVictoryOverlay = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoal)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx()
            val strokeWidth = 1.dp.toPx()
            val gridColor = AncientGold.copy(alpha = 0.04f)
            
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth)
                x += gridStep
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth)
                y += gridStep
            }

            val starRandom = java.util.Random(42)
            repeat(120) {
                val startX = starRandom.nextFloat() * size.width
                val startY = starRandom.nextFloat() * size.height
                val radius = starRandom.nextFloat() * 1.8f
                val starType = starRandom.nextInt(3)
                val starColor = when(starType) {
                    0 -> AncientGold.copy(alpha = 0.12f)
                    1 -> Color(0xFF81D4FA).copy(alpha = 0.12f)
                    else -> Color.White.copy(alpha = 0.15f)
                }
                drawCircle(starColor, radius, Offset(startX, startY))
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).rotate(180f)) {
                PlayerArea(
                    name = p2Name,
                    lp = p2Lp,
                    onLpChange = { p2Lp = it },
                    onCalcClick = { 
                        calcPreselectedPlayer = 2
                        activeTool = UtilityTool.CALC 
                    },
                    onNameChange = { p2Name = it }
                )
            }

            UtilityBar(
                onToolClick = { activeTool = it },
                onResetClick = { showResetConfirm = true }
            )

            Box(modifier = Modifier.weight(1f)) {
                PlayerArea(
                    name = p1Name,
                    lp = p1Lp,
                    onLpChange = { p1Lp = it },
                    onCalcClick = { 
                        calcPreselectedPlayer = 1
                        activeTool = UtilityTool.CALC 
                    },
                    onNameChange = { p1Name = it }
                )
            }
        }

        UtilityOverlay(
            activeTool = activeTool,
            onClose = { activeTool = null },
            p1Lp = p1Lp,
            p2Lp = p2Lp,
            p1Name = p1Name,
            p2Name = p2Name,
            preselectedCalcPlayer = calcPreselectedPlayer,
            onApplyLp = { player, newLp ->
                if (player == 1) p1Lp = newLp else p2Lp = newLp
                activeTool = null
            }
        )

        if (showResetConfirm) {
            var showCustomReset by remember { mutableStateOf(false) }
            var customLpInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { 
                    showResetConfirm = false
                    showCustomReset = false
                },
                confirmButton = {
                    if (showCustomReset) {
                        Button(
                            onClick = {
                                val newLp = customLpInput.toIntOrNull() ?: 8000
                                p1Lp = newLp
                                p2Lp = newLp
                                showResetConfirm = false
                                showCustomReset = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighlightGold)
                        ) {
                            Text("START", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showResetConfirm = false
                        showCustomReset = false
                    }) {
                        Text("CANCEL", color = TextGray, fontFamily = FontFamily.Serif)
                    }
                },
                title = { 
                    Text(
                        if (showCustomReset) "CUSTOM START LP" else "RESET DUEL", 
                        color = Color.White, 
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black
                    ) 
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!showCustomReset) {
                            Text("Select starting Life Points for both players:", color = TextWhite, fontFamily = FontFamily.Serif)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        p1Lp = 8000
                                        p2Lp = 8000
                                        showResetConfirm = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StonePanel),
                                    border = BorderStroke(1.dp, AncientGold),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("8000", color = TextAgedWhite, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        p1Lp = 4000
                                        p2Lp = 4000
                                        showResetConfirm = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StonePanel),
                                    border = BorderStroke(1.dp, AncientGold),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("4000", color = TextAgedWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Button(
                                onClick = { showCustomReset = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, HighlightGold.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("CUSTOM", color = HighlightGold, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            TextField(
                                value = customLpInput,
                                onValueChange = { if (it.all { char -> char.isDigit() }) customLpInput = it },
                                placeholder = { Text("Enter LP...", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif, color = Color.White),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedIndicatorColor = HighlightGold
                                )
                            )
                        }
                    }
                },
                containerColor = StonePanel,
                titleContentColor = Color.White,
                textContentColor = TextWhite
            )
        }

        if (duelFinished && showVictoryOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { /* Block clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(32.dp)
                        .background(StonePanel, RoundedCornerShape(8.dp))
                        .border(2.dp, AncientGold, RoundedCornerShape(8.dp))
                        .padding(32.dp)
                ) {
                    Text(
                        text = "VICTORY",
                        style = MaterialTheme.typography.displayMedium,
                        color = HighlightGold,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = winnerName.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "WINS THE DUEL",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            p1Lp = 8000
                            p2Lp = 8000
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighlightGold),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("NEW DUEL", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { showVictoryOverlay = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", color = TextGray, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerArea(
    name: String,
    lp: Int,
    onLpChange: (Int) -> Unit,
    onCalcClick: () -> Unit,
    onNameChange: (String) -> Unit
) {
    var showSetLpOverlay by remember { mutableStateOf(false) }
    var showRenameOverlay by remember { mutableStateOf(false) }

    var lastLp by remember { mutableIntStateOf(lp) }
    var feedbackValue by remember { mutableIntStateOf(0) }
    var feedbackVisible by remember { mutableStateOf(false) }

    LaunchedEffect(lp) {
        val diff = lp - lastLp
        if (diff != 0) {
            feedbackValue = diff
            feedbackVisible = true
            delay(500.milliseconds)
            feedbackVisible = false
        }
        lastLp = lp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .drawCornerBorders(AncientGold)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { showRenameOverlay = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = HighlightGold,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp
                )
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = HighlightGold.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp).padding(start = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = BiasAlignment(0f, -0.2f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                        LpFeedback(visible = feedbackVisible, value = feedbackValue)
                    }
                    
                    Text(
                        text = lp.toString().padStart(4, '0'),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 2.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = AncientGold.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 12f
                            )
                        ),
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickButton(
                        text = "▼ 1000",
                        color = MinusRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onLpChange((lp - 1000).coerceAtLeast(0)) },
                        isLarge = true
                    )
                    QuickButton(
                        text = "▲ 1000",
                        color = AncientGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onLpChange(lp + 1000) },
                        isLarge = true
                    )
                }
                // Row 2: Smaller increments
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickButton("-500", color = MinusRed, modifier = Modifier.weight(1f), onClick = { onLpChange((lp - 500).coerceAtLeast(0)) })
                    QuickButton("-100", color = MinusRed, modifier = Modifier.weight(1f), onClick = { onLpChange((lp - 100).coerceAtLeast(0)) })
                    QuickButton("-50", color = MinusRed, modifier = Modifier.weight(1f), onClick = { onLpChange((lp - 50).coerceAtLeast(0)) })
                    QuickButton("+50", color = AncientGreen, modifier = Modifier.weight(1f), onClick = { onLpChange(lp + 50) })
                    QuickButton("+100", color = AncientGreen, modifier = Modifier.weight(1f), onClick = { onLpChange(lp + 100) })
                    QuickButton("+500", color = AncientGreen, modifier = Modifier.weight(1f), onClick = { onLpChange(lp + 500) })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickButton(
                        text = "SET LP",
                        color = HighlightGold,
                        modifier = Modifier.weight(1f),
                        onClick = { showSetLpOverlay = true }
                    )
                    QuickButton(
                        text = "CALC",
                        color = HighlightGold,
                        modifier = Modifier.weight(1f),
                        onClick = onCalcClick
                    )
                }
            }
        }

        if (showSetLpOverlay) {
            var inputLp by remember { mutableStateOf("") }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showSetLpOverlay = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(SurfacePanel, RoundedCornerShape(4.dp))
                        .border(1.dp, AncientGold, RoundedCornerShape(4.dp))
                        .padding(24.dp)
                        .clickable(enabled = false) {},
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SET LP FOR ${name.uppercase()}", color = HighlightGold, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = inputLp,
                        onValueChange = { if (it.all { char -> char.isDigit() }) inputLp = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedIndicatorColor = HighlightGold
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSetLpOverlay = false }) {
                            Text("CANCEL", color = TextGray, fontFamily = FontFamily.Serif)
                        }
                        Button(
                            onClick = {
                                if (inputLp.isNotEmpty()) {
                                    onLpChange(inputLp.toInt().coerceAtLeast(0))
                                }
                                showSetLpOverlay = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighlightGold)
                        ) {
                            Text("SET", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    }
                }
            }
        }

        if (showRenameOverlay) {
            var inputName by remember { mutableStateOf(name) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showRenameOverlay = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(SurfacePanel, RoundedCornerShape(4.dp))
                        .border(1.dp, AncientGold, RoundedCornerShape(4.dp))
                        .padding(24.dp)
                        .clickable(enabled = false) {},
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RENAME PLAYER", color = HighlightGold, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedIndicatorColor = HighlightGold
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRenameOverlay = false }) {
                            Text("CANCEL", color = TextGray, fontFamily = FontFamily.Serif)
                        }
                        Button(
                            onClick = {
                                if (inputName.isNotBlank()) {
                                    onNameChange(inputName)
                                }
                                showRenameOverlay = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighlightGold)
                        ) {
                            Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LpFeedback(visible: Boolean, value: Int) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 }
    ) {
        Text(
            text = if (value > 0) "+$value" else value.toString(),
            color = if (value > 0) AncientGreen else ForbiddenRed,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp
        )
    }
}

@Composable
fun QuickButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isLarge: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(2.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(vertical = if (isLarge) 6.dp else 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = if (isLarge) 16.sp else 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                color = color
            )
        }
    }
}

@Composable
fun UtilityBar(
    onToolClick: (UtilityTool) -> Unit,
    onResetClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF151515),
                        DarkCharcoal,
                        Color(0xFF151515)
                    )
                )
            )
            .border(width = 1.dp, color = AncientGold.copy(alpha = 0.2f), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(AncientGold.copy(alpha = 0.4f), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
            drawLine(AncientGold.copy(alpha = 0.4f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                UtilityBarButton(Icons.Rounded.RadioButtonUnchecked, "COIN", onClick = { onToolClick(UtilityTool.COIN) })
                UtilityBarButton(Icons.Rounded.Casino, "DICE", onClick = { onToolClick(UtilityTool.DICE) })
                UtilityBarButton(Icons.Rounded.Calculate, "CALC", onClick = { onToolClick(UtilityTool.CALC) })
            }
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onResetClick() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = ForbiddenRed, modifier = Modifier.size(20.dp))
                Text("RESET", color = ForbiddenRed, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Serif)
            }
        }
    }
}

@Composable
fun UtilityBarButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = HighlightGold, modifier = Modifier.size(24.dp))
        Text(label, color = HighlightGold.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
    }
}

@Composable
fun UtilityOverlay(
    activeTool: UtilityTool?,
    onClose: () -> Unit,
    p1Lp: Int,
    p2Lp: Int,
    p1Name: String,
    p2Name: String,
    preselectedCalcPlayer: Int,
    onApplyLp: (Int, Int) -> Unit
) {
    AnimatedVisibility(
        visible = activeTool != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = true, onClick = onClose),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = StonePanel,
                border = BorderStroke(2.dp, AncientGold)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activeTool) {
                                UtilityTool.COIN -> "COIN TOSS"
                                UtilityTool.DICE -> "DICE ROLL"
                                UtilityTool.CALC -> "CALCULATOR"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = HighlightGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = HighlightGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        when (activeTool) {
                            UtilityTool.COIN -> CoinTossPanel()
                            UtilityTool.DICE -> DiceRollPanel()
                            UtilityTool.CALC -> CalculatorPanel(p1Lp, p2Lp, p1Name, p2Name, preselectedCalcPlayer, onApplyLp)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoinTossPanel() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    var isFlipping by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    rotationY = rotation.value
                }
                .clip(RoundedCornerShape(80.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(HighlightGold, Color(0xFFB8860B))
                    )
                )
                .border(6.dp, AncientGold, RoundedCornerShape(80.dp))
                .padding(8.dp)
                .border(2.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(80.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result ?: "?",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                scope.launch {
                    isFlipping = true
                    result = null
                    rotation.animateTo(
                        targetValue = rotation.value + 1440f,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                    )
                    result = if (Random.nextBoolean()) "HEADS" else "TAILS"
                    isFlipping = false
                }
            },
            enabled = !isFlipping,
            colors = ButtonDefaults.buttonColors(containerColor = HighlightGold),
            modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.5f))
        ) {
            Text("FLIP COIN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif)
        }
    }
}

@Composable
fun DiceRollPanel() {
    var result by rememberSaveable { mutableIntStateOf(0) }
    var isRolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(StonePanel, RoundedCornerShape(12.dp))
                .border(4.dp, AncientGold, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (result == 0) {
                Text("?", color = AncientGold, style = MaterialTheme.typography.displayMedium, fontFamily = FontFamily.Serif)
            } else {
                DiceFace(result)
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                scope.launch {
                    isRolling = true
                    val secureRandom = java.security.SecureRandom()
                    var lastResult = result
                    repeat(12) {
                        var next: Int
                        do {
                            next = secureRandom.nextInt(6) + 1
                        } while (next == lastResult && it < 10) // Try to avoid same number during animation sequence
                        result = next
                        lastResult = next
                        delay((60 + it * 5).milliseconds) // Slightly slow down
                    }
                    result = secureRandom.nextInt(6) + 1
                    isRolling = false
                }
            },
            enabled = !isRolling,
            colors = ButtonDefaults.buttonColors(containerColor = HighlightGold),
            modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.5f))
        ) {
            Text("ROLL DICE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif)
        }
    }
}

@Composable
fun DiceFace(number: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dotRadius = 8.dp.toPx()
        val color = HighlightGold
        
        when (number) {
            1 -> drawCircle(color, dotRadius, center)
            2 -> {
                drawCircle(color, dotRadius, Offset(size.width * 0.25f, size.height * 0.25f))
                drawCircle(color, dotRadius, Offset(size.width * 0.75f, size.height * 0.75f))
            }
            3 -> {
                drawCircle(color, dotRadius, Offset(size.width * 0.2f, size.height * 0.2f))
                drawCircle(color, dotRadius, center)
                drawCircle(color, dotRadius, Offset(size.width * 0.8f, size.height * 0.8f))
            }
            4 -> {
                drawCircle(color, dotRadius, Offset(size.width * 0.25f, size.height * 0.25f))
                drawCircle(color, dotRadius, Offset(size.width * 0.75f, size.height * 0.25f))
                drawCircle(color, dotRadius, Offset(size.width * 0.25f, size.height * 0.75f))
                drawCircle(color, dotRadius, Offset(size.width * 0.75f, size.height * 0.75f))
            }
            5 -> {
                drawCircle(color, dotRadius, Offset(size.width * 0.2f, size.height * 0.2f))
                drawCircle(color, dotRadius, Offset(size.width * 0.8f, size.height * 0.2f))
                drawCircle(color, dotRadius, center)
                drawCircle(color, dotRadius, Offset(size.width * 0.2f, size.height * 0.8f))
                drawCircle(color, dotRadius, Offset(size.width * 0.8f, size.height * 0.8f))
            }
            6 -> {
                drawCircle(color, dotRadius, Offset(size.width * 0.25f, size.height * 0.2f))
                drawCircle(color, dotRadius, Offset(size.width * 0.75f, size.height * 0.2f))
                drawCircle(color, dotRadius, Offset(size.width * 0.25f, size.height * 0.5f))
                drawCircle(color, dotRadius, Offset(size.width * 0.75f, size.height * 0.5f))
                drawCircle(color, dotRadius, Offset(size.width * 0.25f, size.height * 0.8f))
                drawCircle(color, dotRadius, Offset(size.width * 0.75f, size.height * 0.8f))
            }
        }
    }
}

@Composable
fun CalculatorPanel(
    p1Lp: Int,
    p2Lp: Int,
    p1Name: String,
    p2Name: String,
    preselectedPlayerId: Int,
    onApplyLp: (Int, Int) -> Unit
) {
    var selectedPlayer by rememberSaveable { mutableIntStateOf(preselectedPlayerId) }
    var calcInput by rememberSaveable { mutableStateOf("") }
    val currentLp = if (selectedPlayer == 1) p1Lp else p2Lp
    val currentPlayerName = if (selectedPlayer == 1) p1Name else p2Name

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayerChip(
                text = p1Name.uppercase(),
                selected = selectedPlayer == 1,
                onClick = { selectedPlayer = 1 },
                accentColor = Player1Indigo,
                modifier = Modifier.weight(1f)
            )
            PlayerChip(
                text = p2Name.uppercase(),
                selected = selectedPlayer == 2,
                onClick = { selectedPlayer = 2 },
                accentColor = Player2Teal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AncientDeepBlue, RoundedCornerShape(4.dp))
                .border(1.dp, AncientGold, RoundedCornerShape(4.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Text("CURRENT: $currentLp", color = AncientGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = calcInput.ifEmpty { "0" },
                    color = TextAgedWhite,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val buttons = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", "=", "+")
        )

        Column(modifier = Modifier.weight(1f)) {
            buttons.forEach { row ->
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { char ->
                        CalcButton(
                            text = char,
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 4.dp),
                            onClick = {
                                when (char) {
                                    "C" -> calcInput = ""
                                    "=" -> {
                                        val expression = calcInput
                                            .replace("×", "*")
                                            .replace("÷", "/")
                                        calcInput = evaluateSimpleExpression(expression)
                                    }
                                    else -> if (calcInput.length < 9) calcInput += char
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val finalValue = calcInput.toIntOrNull() ?: evaluateSimpleExpression(
                    calcInput.replace("×", "*").replace("÷", "/")
                ).toIntOrNull() ?: currentLp
                onApplyLp(selectedPlayer, finalValue.coerceAtLeast(0))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HighlightGold
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("APPLY TO ${currentPlayerName.uppercase()}", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        }
    }
}

@Composable
fun PlayerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = if (selected) accentColor.copy(alpha = 0.2f) else AncientDeepBlue,
        border = BorderStroke(1.dp, if (selected) accentColor else AncientGold.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) accentColor else TextMuted,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CalcButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    val isOperator = text in listOf("+", "-", "×", "÷", "=", "C")
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = if (isOperator) Color(0xFF101428) else Color(0xFF1A1F3C),
        border = BorderStroke(1.dp, AncientGold.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (text == "C") ForbiddenRed else if (isOperator) HighlightGold else Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

fun evaluateSimpleExpression(expression: String): String {
    try {
        val operators = setOf('+', '-', '*', '/')
        val operatorIndex = expression.indexOfFirst { it in operators }
        if (operatorIndex <= 0) return expression
        
        val operator = expression[operatorIndex]
        val parts = expression.split(operator)
        if (parts.size < 2) return expression
        
        val left = parts[0].toLongOrNull() ?: 0L
        val right = parts[1].toLongOrNull() ?: 0L
        
        val result = when (operator) {
            '+' -> left + right
            '-' -> left - right
            '*' -> left * right
            '/' -> if (right != 0L) left / right else 0L
            else -> left
        }
        return result.toString()
    } catch (_: Exception) {
        return "0"
    }
}

fun Modifier.drawCornerBorders(color: Color): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        val strokeWidth = 3.dp.toPx()
        val thinStroke = 1.dp.toPx()
        val length = 24.dp.toPx()
        val offset = 2.dp.toPx()
        
        drawRect(
            color = color.copy(alpha = 0.1f),
            topLeft = Offset(offset, offset),
            size = Size(size.width - offset * 2, size.height - offset * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = thinStroke)
        )

        drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
        drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth)
        
        drawLine(color, Offset(size.width - length, 0f), Offset(size.width, 0f), strokeWidth)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, length), strokeWidth)
        
        drawLine(color, Offset(0f, size.height - length), Offset(0f, size.height), strokeWidth)
        drawLine(color, Offset(0f, size.height), Offset(length, size.height), strokeWidth)
        
        drawLine(color, Offset(size.width - length, size.height), Offset(size.width, size.height), strokeWidth)
        drawLine(color, Offset(size.width, size.height - length), Offset(size.width, size.height), strokeWidth)
        
        val studSize = 4.dp.toPx()
        val studColor = color.copy(alpha = 0.8f)
        drawCircle(studColor, studSize, Offset(offset, offset))
        drawCircle(studColor, studSize, Offset(size.width - offset, offset))
        drawCircle(studColor, studSize, Offset(offset, size.height - offset))
        drawCircle(studColor, studSize, Offset(size.width - offset, size.height - offset))

        drawLine(color.copy(alpha = 0.2f), Offset(0f, 0f), Offset(size.width, 0f), thinStroke)
        drawLine(color.copy(alpha = 0.2f), Offset(0f, size.height), Offset(size.width, size.height), thinStroke)
        drawLine(color.copy(alpha = 0.2f), Offset(0f, 0f), Offset(0f, size.height), thinStroke)
        drawLine(color.copy(alpha = 0.2f), Offset(size.width, 0f), Offset(size.width, size.height), thinStroke)
    }
)
