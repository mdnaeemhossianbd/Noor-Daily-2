package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AllahName
import com.example.data.NamesOfAllahData
import com.example.util.AppStrings

private fun banglaPronunciation(transliteration: String): String {
  val lower = transliteration.lowercase()
  val prefix = when {
    lower.startsWith("ar-") -> "আর-" to "ar-"
    lower.startsWith("as-") -> "আস-" to "as-"
    lower.startsWith("ash-") -> "আশ-" to "ash-"
    lower.startsWith("at-") -> "আত-" to "at-"
    lower.startsWith("az-") -> "আয-" to "az-"
    lower.startsWith("ad-") -> "আদ-" to "ad-"
    lower.startsWith("an-") -> "আন-" to "an-"
    lower.startsWith("al-") -> "আল-" to "al-"
    lower.startsWith("ab-") -> "আব-" to "ab-"
    lower.startsWith("am-") -> "আম-" to "am-"
    else -> "" to ""
  }
  var value = if (prefix.second.isNotEmpty()) prefix.first + lower.removePrefix(prefix.second) else lower
  val replacements = listOf(
    "kh" to "খ", "gh" to "গ", "th" to "স", "dh" to "য", "sh" to "শ",
    "aa" to "া", "ee" to "ী", "oo" to "ূ", "a" to "া", "e" to "ে",
    "i" to "ি", "o" to "ো", "u" to "ু", "q" to "ক", "k" to "ক",
    "g" to "গ", "h" to "হ", "j" to "জ", "m" to "ম", "n" to "ন",
    "r" to "র", "l" to "ল", "b" to "ব", "t" to "ত", "d" to "দ",
    "s" to "স", "f" to "ফ", "w" to "ও", "y" to "য়", "z" to "য",
    "p" to "প", "c" to "ক", "'" to "", "-" to "-"
  )
  replacements.forEach { (from, to) -> value = value.replace(from, to) }
  return value.replace(Regex("াা+"), "া").replace(Regex("েে+"), "ে")
}

@Composable
fun NamesOfAllahDialog(
  currentLang: String,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedNameDetail by remember { mutableStateOf<AllahName?>(null) }

  val filteredNames = remember(searchQuery) {
    if (searchQuery.trim().isEmpty()) {
      NamesOfAllahData.names
    } else {
      val q = searchQuery.trim().lowercase()
      NamesOfAllahData.names.filter {
        it.arabic.contains(q) ||
        it.transliteration.lowercase().contains(q) ||
        it.meaningBn.contains(q) ||
        it.meaningEn.lowercase().contains(q) ||
        it.number.toString() == q
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.85f))
        .padding(12.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.92f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          // Top Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = AppStrings.text(currentLang, "names.title"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = when (currentLang) {
                  "bn" -> "আসমাউল হুসনা (الأَسْمَاءُ الْحُسْنَىٰ)"
                  "ar" -> "الأَسْمَاءُ الْحُسْنَىٰ"
                  else -> "Asma ul Husna (الأَسْمَاءُ الْحُسْنَىٰ)"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = AppStrings.text(currentLang, "common.close"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(AppStrings.text(currentLang, "names.search"), fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Grid of Names
          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(filteredNames) { name ->
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedNameDetail = name }
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = AppStrings.digits(currentLang, name.number),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                    Text(
                      text = name.transliteration,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Text(
                    text = name.arabic,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                  )

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = if (currentLang == "bn") banglaPronunciation(name.transliteration) else name.transliteration,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                  )
                  Text(
                    text = if (currentLang == "bn") name.meaningBn else name.meaningEn,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                  )
                }
              }
            }
          }
        }
      }
    }

    // Detail Dialog for clicked Name
    selectedNameDetail?.let { detail ->
      AlertDialog(
        onDismissRequest = { selectedNameDetail = null },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${detail.arabic} • ${detail.transliteration}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "${AppStrings.text(currentLang, "names.serial")}: ${AppStrings.digits(currentLang, detail.number)}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            if (currentLang == "bn") {
              Text(
                text = "বাংলা উচ্চারণ: ${banglaPronunciation(detail.transliteration)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = if (currentLang == "bn") "${AppStrings.text(currentLang, "names.meaning_bn")}: ${detail.meaningBn}" else "${AppStrings.text(currentLang, "names.meaning_en")}: ${detail.meaningEn}",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "English Meaning: ${detail.meaningEn}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(
              text = AppStrings.text(currentLang, "names.benefit") + ":",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = if (currentLang == "bn") detail.benefitBn else AppStrings.text(currentLang, "names.benefit_bangla_only"),
              fontSize = 13.sp,
              lineHeight = 18.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        confirmButton = {
          Button(
            onClick = { selectedNameDetail = null },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Text(AppStrings.text(currentLang, "common.close"))
          }
        }
      )
    }
  }
}
