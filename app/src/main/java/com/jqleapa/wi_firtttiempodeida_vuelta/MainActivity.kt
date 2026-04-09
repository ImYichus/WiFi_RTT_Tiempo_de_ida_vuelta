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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.jqleapa.wi_firtttiempodeida_vuelta.ui.theme.WiFiRTTTiempoDeIdavueltaTheme

class MainActivity : ComponentActivity() {

    private var estadoHardware = mutableStateOf("Verificando soporte Wi-Fi RTT...")
    private var botonHabilitado = mutableStateOf(false)
    private var resultadosTexto = mutableStateOf("Los resultados aparecerán aquí.")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val todosOtorgados = permissions.entries.all { it.value }
        if (todosOtorgados) {
            iniciarEscaneoSimulado()
        } else {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            resultadosTexto.value = "Error: Se requieren permisos para la demostración."
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
                        resultadosTexto = resultadosTexto.value,
                        onEscanearClick = { verificarPermisosYEjecutar() }
                    )
                }
            }
        }
    }

    private fun verificarSoporteHardware() {
        val tieneHardware = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)

        if (!tieneHardware) {
            estadoHardware.value = "Modo Simulación: Hardware RTT no detectado. Usando datos de prueba."
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
        resultadosTexto.value = "Buscando Puntos de Acceso (Routers) RTT cercanos..."
        botonHabilitado.value = false

        Handler(Looper.getMainLooper()).postDelayed({
            val sb = StringBuilder()
            sb.append("Se encontraron 3 Puntos de Acceso compatibles.\nIniciando medición de distancia (802.11mc)...\n\n")

            sb.append("MAC: 00:14:22:01:23:45\n")
            sb.append("Distancia: 1.45 m\n")
            sb.append("Margen de error: ±0.12 m\n\n")

            sb.append("MAC: 0A:11:22:33:44:55\n")
            sb.append("Distancia: 4.80 m\n")
            sb.append("Margen de error: ±0.30 m\n\n")

            sb.append("MAC: BC:AE:C5:11:00:99\n")
            sb.append("Distancia: 12.10 m\n")
            sb.append("Margen de error: ±0.85 m\n\n")

            sb.append("Medición completada exitosamente.")
            resultadosTexto.value = sb.toString()
            botonHabilitado.value = true
        }, 2000)
    }
}

@Composable
fun PantallaRtt(
    estadoHardware: String,
    botonHabilitado: Boolean,
    resultadosTexto: String,
    onEscanearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp)
    ) {
        Text(
            text = estadoHardware,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = onEscanearClick,
            enabled = botonHabilitado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escanear y Medir Distancia RTT")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = resultadosTexto,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}