package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleIslamicData
import com.example.model.IslamicPost
import com.example.ui.components.IslamicCalendarDialog
import com.example.ui.components.IslamicQuizDialog
import com.example.ui.components.NamesOfAllahDialog
import com.example.ui.components.TasbihDialog
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldAccent

@Composable
fun CategoriesScreen(
  currentLang: String,
  onPostClick: (IslamicPost) -> Unit,
) {
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  var showTasbihDialog by remember { mutableStateOf(false) }
  var showNamesOfAllahDialog by remember { mutableStateOf(false) }
  var showIslamicCalendarDialog by remember { mutableStateOf(false) }
  var showIslamicQuizDialog by remember { mutableStateOf(false) }

  val filteredPosts = remember(selectedCategoryId, searchQuery, currentLang, SampleIslamicData.posts) {
    SampleIslamicData.posts.filter { post ->
      val matchesCat = selectedCategoryId == null || post.categoryId == selectedCategoryId
      val title = post.localizedTitle(currentLang).lowercase()
      val sub = post.localizedSubtitle(currentLang).lowercase()
      val q = searchQuery.trim().lowercase()
      val matchesSearch = q.isEmpty() || title.contains(q) || sub.contains(q)
      matchesCat && matchesSearch
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = when (currentLang) {
        "bn" -> "বিষয়ভিত্তিক ক্যাটাগরি ও ইসলামিক টুলস"
        "ar" -> "الأقسام والموضوعات الإسلامية"
        else -> "Topic Categories & Islamic Tools"
      },
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Search Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = {
        Text(
          when (currentLang) {
            "bn" -> "ইসলামিক আলোচনা বা বিষয় অনুসন্ধান করুন..."
            "ar" -> "ابحث في المحتوى الإسلامي..."
            else -> "Search Islamic topics..."
          },
          fontSize = 13.sp
        )
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Three Primary Category Cards: Islamic Calendar, 99 Names, Digital Tasbih
    // Four Primary Category Cards: Islamic Calendar, 99 Names, Digital Tasbih, Islamic Quiz
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // 1. Islamic Calendar Category
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .weight(1f)
          .clickable { showIslamicCalendarDialog = true }
      ) {
        Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = when (currentLang) { "bn" -> "ক্যালেন্ডার"; else -> "Calendar" },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // 2. 99 Names Category
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .weight(1f)
          .clickable { showNamesOfAllahDialog = true }
      ) {
        Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(IslamicEmeraldPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = IslamicGoldAccent, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = when (currentLang) { "bn" -> "৯৯ নাম"; else -> "99 Names" },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // 3. Digital Tasbih Category
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .weight(1f)
          .clickable { showTasbihDialog = true }
      ) {
        Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = when (currentLang) { "bn" -> "তাসবীহ"; else -> "Tasbih" },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // 4. Islamic Quiz Category
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IslamicEmeraldPrimary.copy(alpha = 0.5f)),
        modifier = Modifier
          .weight(1f)
          .clickable { showIslamicQuizDialog = true }
      ) {
        Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(IslamicEmeraldPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = when (currentLang) { "bn" -> "কুইজ"; else -> "Quiz" },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Horizontal Category Selector Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(vertical = 4.dp)
    ) {
      item {
        FilterChip(
          selected = selectedCategoryId == null,
          onClick = { selectedCategoryId = null },
          label = {
            Text(
              when (currentLang) {
                "bn" -> "সব বিষয়"
                "ar" -> "الكل"
                else -> "All Topics"
              },
              fontSize = 12.sp
            )
          },
          shape = RoundedCornerShape(10.dp)
        )
      }

      items(SampleIslamicData.categories) { cat ->
        FilterChip(
          selected = selectedCategoryId == cat.id,
          onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
          label = { Text(cat.localizedName(currentLang), fontSize = 12.sp) },
          shape = RoundedCornerShape(10.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Posts in Selected Category
    if (filteredPosts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = when (currentLang) {
            "bn" -> "এই ক্যাটাগরিতে কোনো পোস্ট পাওয়া যায়নি"
            "ar" -> "لا توجد مقالات في هذا القسم"
            else -> "No posts found in this category"
          },
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(filteredPosts) { post ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onPostClick(post) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Folder,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = post.localizedTitle(currentLang),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = post.localizedSubtitle(currentLang),
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }

  // Active Dialogs
  if (showTasbihDialog) {
    TasbihDialog(onDismiss = { showTasbihDialog = false })
  }

  if (showNamesOfAllahDialog) {
    NamesOfAllahDialog(onDismiss = { showNamesOfAllahDialog = false })
  }

  if (showIslamicCalendarDialog) {
    IslamicCalendarDialog(currentLang = currentLang, onDismiss = { showIslamicCalendarDialog = false })
  }

  if (showIslamicQuizDialog) {
    IslamicQuizDialog(currentLang = currentLang, onDismiss = { showIslamicQuizDialog = false })
  }
}
