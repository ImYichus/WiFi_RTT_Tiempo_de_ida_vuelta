package com.jqleapa.wi_firtttiempodeida_vuelta

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.jqleapa.wi_firtttiempodeida_vuelta.ui.theme.WiFiRTTTiempoDeIdavueltaTheme

// Modelo de datos para los resultados
data class DispositivoRtt(
    val mac: String,
    val distancia: String,
    val margenError: String
)

class MainActivity : ComponentActivity() {

    private var estadoHardware = mutableStateOf("Verificando soporte Wi-Fi RTT...")
    private var botonHabilitado = mutableStateOf(false)
    private var resultadosRtt = mutableStateListOf<DispositivoRtt>()
    private var estaEscaneando = mutableStateOf(false)
    private var mensajeEstado = mutableStateOf("Listo para iniciar medición")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val todosOtorgados = permissions.entries.all { it.value }
        if (todosOtorgados) {
            iniciarEscaneoSimulado()
        } else {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            mensajeEstado.value = "Error: Se requieren permisos."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verificarSoporteHardware()

        setContent {
            WiFiRTTTiempoDeIdavueltaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaRtt(
                        estadoHardware = estadoHardware.value,
                        botonHabilitado = botonHabilitado.value,
                        resultados = resultadosRtt,
                        estaEscaneando = estaEscaneando.value,
                        mensajeEstado = mensajeEstado.value,
                        onEscanearClick = { verificarPermisosYEjecutar() }
                    )
                }
            }
        }
    }

    private fun verificarSoporteHardware() {
        val tieneHardware = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
        if (!tieneHardware) {
            estadoHardware.value = "Modo Simulación: Hardware RTT no detectado."
            botonHabilitado.value = true
        } else {
            estadoHardware.value = "Dispositivo compatible con Wi-Fi RTT. Listo."
            botonHabilitado.value = true
        }
    }

    private fun verificarPermisosYEjecutar() {
        val permisos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        val permisosFaltantes = permisos.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permisosFaltantes.isNotEmpty()) {
            requestPermissionLauncher.launch(permisosFaltantes.toTypedArray())
        } else {
            iniciarEscaneoSimulado()
        }
    }

    private fun iniciarEscaneoSimulado() {
        estaEscaneando.value = true
        mensajeEstado.value = "Buscando Puntos de Acceso RTT..."
        resultadosRtt.clear()

        Handler(Looper.getMainLooper()).postDelayed({
            resultadosRtt.add(DispositivoRtt("00:14:22:01:23:45", "1.45 m", "±0.12 m"))
            resultadosRtt.add(DispositivoRtt("0A:11:22:33:44:55", "4.80 m", "±0.30 m"))
            resultadosRtt.add(DispositivoRtt("BC:AE:C5:11:00:99", "12.10 m", "±0.85 m"))

            estaEscaneando.value = false
            mensajeEstado.value = "Medición completada exitosamente"
            botonHabilitado.value = true
        }, 2000)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PantallaRtt(
    estadoHardware: String,
    botonHabilitado: Boolean,
    resultados: List<DispositivoRtt>,
    estaEscaneando: Boolean,
    mensajeEstado: String,
    onEscanearClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WiFi RTT Explorer", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card de Estado del Hardware
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Estado del Sistema",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = estadoHardware,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Efecto de Radar y Botón Principal
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) {
                if (estaEscaneando) {
                    RadarAnimation()
                }

                // Animación de pulso cuando está listo
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (botonHabilitado && !estaEscaneando) 1.05f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                val gradientBrush = Brush.horizontalGradient(
                    colors = if (estaEscaneando) {
                        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
                    } else {
                        listOf(MaterialTheme.colorScheme.primary, Color(0xFF6200EE))
                    }
                )

                Button(
                    onClick = onEscanearClick,
                    enabled = botonHabilitado && !estaEscaneando,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .scale(pulseScale)
                        .graphicsLayer {
                            shadowElevation = 8.dp.toPx()
                            shape = RoundedCornerShape(20.dp)
                            clip = true
                        },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = estaEscaneando,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + slideInVertically { it } with
                                        fadeOut(animationSpec = tween(300)) + slideOutVertically { -it }
                            },
                            label = "buttonContent"
                        ) { escaneando ->
                            if (escaneando) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Midiendo...", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "INICIAR ESCANEO RTT",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = mensajeEstado, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de Resultados
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Resultados Cercanos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                if (resultados.isNotEmpty()) {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
                        Text(
                            text = "${resultados.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (resultados.isEmpty() && !estaEscaneando) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Wifi, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Text("No hay datos recientes", color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                resultados.forEach { dispositivo ->
                    CardResultado(dispositivo)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RadarCircle(pulse1)
        RadarCircle(pulse2)
    }
}

@Composable
fun RadarCircle(progress: Float) {
    Box(
        modifier = Modifier
            .size((70 + (200 * progress)).dp)
            .graphicsLayer {
                alpha = (1f - progress).coerceIn(0f, 1f)
                scaleX = 0.5f + (0.5f * progress)
                scaleY = 0.5f + (0.5f * progress)
            }
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = CircleShape
            )
    )
}

@Composable
fun CardResultado(dispositivo: DispositivoRtt) {
    // Cálculo visual de proximidad
    val distanciaFloat = dispositivo.distancia.split(" ")[0].toFloatOrNull() ?: 0f
    val progreso = (1f - (distanciaFloat / 20f)).coerceIn(0f, 1f)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Wifi, null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.inversePrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = dispositivo.mac, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "Error: ${dispositivo.margenError}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("DISTANCIA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = dispositivo.distancia,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    modifier = Modifier.size(40.dp),
                    tint = if (distanciaFloat < 5f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progreso,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (distanciaFloat < 5f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}