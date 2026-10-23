package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.example.util.LocationAndNotificationHelper
import kotlin.math.*

private const val KAABA_LATITUDE = 21.4225
private const val KAABA_LONGITUDE = 39.8262

@Composable
fun QiblaCompassScreen(currentLang: String) {
  val context = LocalContext.current
  var location by remember { mutableStateOf(LocationAndNotificationHelper.getCurrentDeviceLocation(context)) }
  LaunchedEffect(Unit) {
    LocationAndNotificationHelper.requestFreshDeviceLocation(context) { fresh -> location = fresh }
  }
  QiblaCompassPanel(currentLang, location.latitude, location.longitude, compact = false, locationLabel = location.addressName)
}

@Composable
fun QiblaCompassPanel(
  currentLang: String,
  latitude: Double,
  longitude: Double,
  compact: Boolean = false,
  locationLabel: String? = null
) {
  val context = LocalContext.current
  var azimuth by remember { mutableFloatStateOf(0f) }
  var hasCompassSensor by remember { mutableStateOf(false) }

  DisposableEffect(context) {
    val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    hasCompassSensor = sensor != null
    val rotation = FloatArray(9)
    val orientation = FloatArray(3)
    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
          SensorManager.getRotationMatrixFromVector(rotation, event.values)
          SensorManager.getOrientation(rotation, orientation)
          azimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat() % 360f) + 360f) % 360f
        }
      }
      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }
    if (sensor != null) manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    onDispose { manager.unregisterListener(listener) }
  }

  val qiblaBearing = remember(latitude, longitude) { calculateQiblaBearing(latitude, longitude) }
  val relativeBearing = (qiblaBearing - azimuth + 360f) % 360f
  val compassSize = if (compact) 148.dp else 240.dp

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.extraLarge
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(if (compact) 14.dp else 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp)
    ) {
      Text(
        text = when (currentLang) { "bn" -> "কিবলা কম্পাস"; "ar" -> "بوصلة القبلة"; else -> "Qibla Compass" },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      Box(
        modifier = Modifier.size(compassSize).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val radius = size.minDimension * 0.40f
          drawCircle(color = Color(0xFF15966A), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
          for (degree in 0 until 360 step 30) {
            val angle = Math.toRadians(degree.toDouble() - 90.0)
            val inner = radius * if (degree % 90 == 0) 0.82f else 0.90f
            drawLine(
              color = Color(0xFF8A9691),
              start = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner),
              end = Offset(center.x + cos(angle).toFloat() * radius, center.y + sin(angle).toFloat() * radius),
              strokeWidth = if (degree % 90 == 0) 2.dp.toPx() else 1.dp.toPx()
            )
          }
        }
        Text(
          text = "N",
          modifier = Modifier.align(Alignment.TopCenter).padding(top = 13.dp).rotate(-azimuth),
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold,
          fontSize = if (compact) 12.sp else 15.sp
        )
        Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = "Qibla direction",
          modifier = Modifier.size(if (compact) 58.dp else 92.dp).rotate(relativeBearing),
          tint = Color(0xFF15966A)
        )
        Surface(
          modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = if (compact) 12.dp else 18.dp),
          color = MaterialTheme.colorScheme.primary,
          shape = CircleShape
        ) {
          Text("🕋", modifier = Modifier.padding(if (compact) 4.dp else 7.dp), fontSize = if (compact) 15.sp else 20.sp)
        }
      }

      Text(
        text = when (currentLang) {
          "bn" -> "কিবলার দিক: " + qiblaBearing.roundToInt() + "° উত্তর থেকে"
          "ar" -> "اتجاه القبلة: " + qiblaBearing.roundToInt() + "° من الشمال"
          else -> "Qibla bearing: " + qiblaBearing.roundToInt() + "° from north"
        },
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
      )
      if (!hasCompassSensor) {
        Text(
          text = when (currentLang) {
            "bn" -> "এই ফোনে কম্পাস সেন্সর পাওয়া যায়নি। দিক নির্দেশনা নির্ভুল নাও হতে পারে।"
            "ar" -> "لم يتم العثور على مستشعر البوصلة؛ قد لا يكون الاتجاه دقيقًا."
            else -> "No compass sensor found. Live direction may not be accurate on this device."
          },
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodySmall
        )
      } else {
        Text(
          text = when (currentLang) {
            "bn" -> "ফোন সমতল রাখুন এবং কম্পাস ঠিক করতে ৮ আকৃতিতে নাড়ান।"
            "ar" -> "أبقِ الهاتف مستويًا وحرّكه على شكل رقم ٨ لمعايرة البوصلة."
            else -> "Keep the phone level. Move it in a figure-eight to calibrate the compass."
          },
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodySmall
        )
      }
      if (!locationLabel.isNullOrBlank()) {
        Text(locationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

private fun calculateQiblaBearing(latitude: Double, longitude: Double): Float {
  val lat1 = Math.toRadians(latitude)
  val lat2 = Math.toRadians(KAABA_LATITUDE)
  val deltaLon = Math.toRadians(KAABA_LONGITUDE - longitude)
  val y = sin(deltaLon)
  val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(deltaLon)
  return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}
