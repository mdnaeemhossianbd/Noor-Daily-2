package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SampleIslamicData
import com.example.model.CityLocation
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldAccent
import com.example.util.PrayerTimeCalculator

@Composable
fun CitySelectionDialog(
  currentLang: String,
  selectedCity: CityLocation?,
  isUsingGps: Boolean,
  onSelectGps: () -> Unit,
  onSelectCity: (CityLocation) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val allCities = SampleIslamicData.cities

  val filteredCities = remember(searchQuery, currentLang) {
    if (searchQuery.isBlank()) allCities
    else {
      val q = searchQuery.trim().lowercase()
      allCities.filter {
        it.nameBn.lowercase().contains(q) ||
        it.nameEn.lowercase().contains(q) ||
        it.country.lowercase().contains(q) ||
        it.countryBn.lowercase().contains(q)
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.85f),
      shape = RoundedCornerShape(22.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .background(IslamicEmeraldPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Public, contentDescription = null, tint = IslamicGoldAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = when (currentLang) {
                  "bn" -> "দেশ ও শহর নির্বাচন"
                  "ar" -> "اختر الدولة والمدينة"
                  else -> "Select Country & City"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = when (currentLang) {
                  "bn" -> "যেকোনো শহরের নামাজের সময় ও ঘড়ির সময় দেখুন"
                  else -> "View live prayer times & clock for any city"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier.fillMaxWidth(),
          placeholder = {
            Text(
              when (currentLang) {
                "bn" -> "শহর বা দেশের নাম লিখুন (যেমন: ঢাকা, মক্কা, দুবাই)..."
                else -> "Search city or country (e.g. Dhaka, Makkah)..."
              },
              fontSize = 12.sp
            )
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // List of Cities
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Option 1: Auto GPS
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isUsingGps) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
              ),
              border = BorderStroke(
                1.dp,
                if (isUsingGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectGps()
                  onDismiss()
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = when (currentLang) {
                      "bn" -> "ডিভাইসের বর্তমান জিপিএস অবস্থান"
                      "ar" -> "موقع الجهاز الحالي عبر GPS"
                      else -> "Current Device GPS Location"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = when (currentLang) {
                      "bn" -> "আপনার বর্তমান অবস্থানের উপর ভিত্তি করে স্বয়ংক্রিয় সময়"
                      else -> "Calculates automatically based on physical coordinates"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                if (isUsingGps) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }

          item {
            Text(
              text = when (currentLang) {
                "bn" -> "উপলব্ধ শহরসমূহ (${filteredCities.size})"
                else -> "Available Cities (${filteredCities.size})"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
          }

          items(filteredCities, key = { it.nameEn }) { city ->
            val isSelected = !isUsingGps && selectedCity?.nameEn == city.nameEn
            val cityLiveClock = remember(city.timeZoneId) {
              PrayerTimeCalculator.getCityLiveTime(city.timeZoneId, currentLang)
            }

            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
              ),
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectCity(city)
                  onDismiss()
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(
                      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (currentLang == "bn") city.nameBn else city.nameEn,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${if (currentLang == "bn") "স্থানীয় সময়: " else "Time: "}$cityLiveClock",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }

                if (isSelected) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }
        }
      }
    }
  }
}
