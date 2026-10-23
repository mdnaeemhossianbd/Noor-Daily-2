package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.LocationAndNotificationHelper
import kotlin.math.*

private const val KAABA_LATITUDE = 21.4225
private const val KAABA_LONGITUDE = 39.8262

@Composable
fun QiblaCompassScreen(currentLang: String) {
  val context = LocalContext.current
  var location by remember { mutableStateOf(LocationAndNotificationHelper.getCurrentDeviceLocation(context)) }

  LaunchedEffect(Unit) {
    LocationAndNotificationHelper.requestFreshDeviceLocation(context) { fresh ->
      location = fresh
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    QiblaCompassPanel(
      currentLang = currentLang,
      latitude = location.latitude,
      longitude = location.longitude,
      compact = false,
      locationLabel = location.addressName
    )
  }
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

  // Dual Sensor Support: Accelerometer + Magnetometer as well as Rotation Vector
  DisposableEffect(context) {
    val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val accelSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    hasCompassSensor = rotationSensor != null || (accelSensor != null && magnetSensor != null)

    val rotationMatrix = FloatArray(9)
    val inclinationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)
    val gravity = FloatArray(3)
    val geomagnetic = FloatArray(3)
    var hasGravity = false
    var hasGeomagnetic = false

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
          Sensor.TYPE_ROTATION_VECTOR -> {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val degree = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat() % 360f) + 360f) % 360f
            azimuth = degree
          }
          Sensor.TYPE_ACCELEROMETER -> {
            // Apply low-pass filter
            val alpha = 0.8f
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            hasGravity = true
          }
          Sensor.TYPE_MAGNETIC_FIELD -> {
            val alpha = 0.8f
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
            hasGeomagnetic = true
          }
        }

        if (rotationSensor == null && hasGravity && hasGeomagnetic) {
          val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)
          if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val degree = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat() % 360f) + 360f) % 360f
            azimuth = degree
          }
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    if (rotationSensor != null) {
      manager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
    } else {
      if (accelSensor != null) manager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
      if (magnetSensor != null) manager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
    }

    onDispose {
      manager.unregisterListener(listener)
    }
  }

  val qiblaBearing = remember(latitude, longitude) { calculateQiblaBearing(latitude, longitude) }
  val distanceKm = remember(latitude, longitude) { calculateDistanceToKaabaKm(latitude, longitude) }
  
  // Calculate relative angle: needle rotation
  val relativeBearing = (qiblaBearing - azimuth + 360f) % 360f
  val isFacingKaaba = abs(relativeBearing) < 5f || abs(relativeBearing - 360f) < 5f

  // Smooth needle animation
  val animatedAzimuth by animateFloatAsState(
    targetValue = azimuth,
    animationSpec = spring(stiffness = Spring.StiffnessLow),
    label = "azimuth_anim"
  )
  val animatedRelativeBearing by animateFloatAsState(
    targetValue = relativeBearing,
    animationSpec = spring(stiffness = Spring.StiffnessLow),
    label = "qibla_bearing_anim"
  )

  val dialBorderColor by animateColorAsState(
    targetValue = if (isFacingKaaba) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    label = "dial_border_color"
  )

  val compassSize = if (compact) 160.dp else 260.dp

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(24.dp),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, dialBorderColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(if (compact) 14.dp else 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp)
    ) {
      // Header Title & Facing Kaaba Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Explore, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
          }
          Column {
            Text(
              text = when (currentLang) {
                "bn" -> "কিবলা কম্পাস"
                "ar" -> "بوصلة القبلة"
                else -> "Qibla Direction"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = when (currentLang) {
                "bn" -> "মক্কা মুকাররমার দিক"
                "ar" -> "مكة المكرمة"
                else -> "Makkah al-Mukarramah"
              },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (isFacingKaaba) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF10B981),
            shadowElevation = 4.dp
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "ক্বিবলামুখী"
                  "ar" -> "مواجه للقبلة"
                  else -> "Facing Kaaba"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        } else {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Text(
              text = "${qiblaBearing.roundToInt()}°",
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // Interactive Compass Dial with 360 Tick Marks and Kaaba Pointer
      Box(
        modifier = Modifier
          .size(compassSize)
          .background(
            brush = Brush.radialGradient(
              listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
              )
            ),
            shape = CircleShape
          )
          .border(
            width = if (isFacingKaaba) 3.dp else 1.dp,
            color = if (isFacingKaaba) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant,
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        // Static Compass Graduations (Ticks)
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val radius = size.minDimension * 0.44f

          // Outer circle
          drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.25f),
            radius = radius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
          )

          // 360 degree ticks every 15 degrees
          for (degree in 0 until 360 step 15) {
            val isMajor = degree % 90 == 0
            val isMedium = degree % 30 == 0
            val angle = Math.toRadians(degree.toDouble() - 90.0)
            val tickLength = if (isMajor) 14.dp.toPx() else if (isMedium) 9.dp.toPx() else 5.dp.toPx()
            val inner = radius - tickLength

            drawLine(
              color = if (isMajor) Color(0xFF10B981) else Color(0xFF6B7280).copy(alpha = 0.5f),
              start = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner),
              end = Offset(center.x + cos(angle).toFloat() * radius, center.y + sin(angle).toFloat() * radius),
              strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
            )
          }
        }

        // Rotating Dial Container (Rotates based on device heading)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .rotate(-animatedAzimuth),
          contentAlignment = Alignment.Center
        ) {
          // North indicator
          Text(
            text = "N",
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 14.dp),
            color = Color(0xFFEF4444), // Red for North
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (compact) 13.sp else 16.sp
          )

          // East
          Text(
            text = "E",
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .padding(end = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 13.sp
          )

          // South
          Text(
            text = "S",
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 13.sp
          )

          // West
          Text(
            text = "W",
            modifier = Modifier
              .align(Alignment.CenterStart)
              .padding(start = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 13.sp
          )
        }

        // Qibla Indicator (Points directly towards Kaaba relative to device)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .rotate(animatedRelativeBearing),
          contentAlignment = Alignment.Center
        ) {
          // Needle pointer
          Icon(
            imageVector = Icons.Filled.Navigation,
            contentDescription = "Qibla pointer",
            modifier = Modifier
              .size(if (compact) 64.dp else 98.dp)
              .offset(y = if (compact) (-14).dp else (-22).dp),
            tint = if (isFacingKaaba) Color(0xFF10B981) else Color(0xFFF59E0B)
          )

          // Kaaba Icon at the tip of the needle
          Surface(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = if (compact) 24.dp else 36.dp),
            color = if (isFacingKaaba) Color(0xFF10B981) else Color(0xFF1F2937),
            shape = CircleShape,
            shadowElevation = 6.dp
          ) {
            Text(
              text = "🕋",
              modifier = Modifier.padding(if (compact) 4.dp else 6.dp),
              fontSize = if (compact) 14.sp else 18.sp
            )
          }
        }

        // Center Pivot Pin
        Box(
          modifier = Modifier
            .size(if (compact) 16.dp else 22.dp)
            .background(Color(0xFF111827), CircleShape)
            .border(2.dp, Color(0xFF10B981), CircleShape)
        )
      }

      // Direction & Bearing Details Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Qibla Bearing Card
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "কিবলার দিক"
                "ar" -> "اتجاه القبلة"
                else -> "Qibla Bearing"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${qiblaBearing.roundToInt()}° N",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Distance to Makkah Card
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "মক্কার দূরত্ব"
                "ar" -> "المسافة إلى مكة"
                else -> "Distance to Kaaba"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = when (currentLang) {
                "bn" -> "${formatBengaliNumber(distanceKm)} কিমি"
                "ar" -> "$distanceKm كم"
                else -> "$distanceKm km"
              },
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // Sensor Status & Calibration Advice
      if (!hasCompassSensor) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Text(
              text = when (currentLang) {
                "bn" -> "ম্যাগনেটোমিটার বা কম্পাস সেন্সর পাওয়া যায়নি। কিবলার দিক কোণ (${qiblaBearing.roundToInt()}°) অনুসারে উত্তর থেকে চিহ্নিত করুন।"
                "ar" -> "لم يتم العثور على مستشعر البوصلة. استخدم الزاوية ${qiblaBearing.roundToInt()}° من الشمال."
                else -> "No compass hardware sensor found. Use the bearing angle ${qiblaBearing.roundToInt()}° from North."
              },
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = when (currentLang) {
              "bn" -> "ফোনটি সমতল রাখুন এবং নিখুঁত ফলাফলের জন্য ৮ (Eight) আকারে নাড়ান।"
              "ar" -> "أبقِ الهاتف مستوياً وحركه على شكل رقم ٨ للمعايرة."
              else -> "Hold the phone flat and move in a figure-8 motion for calibration."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }

      // Location tag
      if (!locationLabel.isNullOrBlank()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
          Text(
            text = locationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
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

private fun calculateDistanceToKaabaKm(latitude: Double, longitude: Double): Int {
  val earthRadiusKm = 6371.0
  val dLat = Math.toRadians(KAABA_LATITUDE - latitude)
  val dLon = Math.toRadians(KAABA_LONGITUDE - longitude)
  val a = sin(dLat / 2) * sin(dLat / 2) +
          cos(Math.toRadians(latitude)) * cos(Math.toRadians(KAABA_LATITUDE)) *
          sin(dLon / 2) * sin(dLon / 2)
  val c = 2 * atan2(sqrt(a), sqrt(1 - a))
  return (earthRadiusKm * c).roundToInt()
}

private fun formatBengaliNumber(num: Int): String {
  val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
  val formattedStr = java.text.NumberFormat.getNumberInstance().format(num)
  val sb = StringBuilder()
  for (ch in formattedStr) {
    if (ch in '0'..'9') {
      sb.append(bengaliDigits[ch - '0'])
    } else {
      sb.append(ch)
    }
  }
  return sb.toString()
}
