package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleIslamicData
import com.example.model.IslamicPost
import com.example.ui.theme.IslamicGoldAccent

@Composable
fun SavedScreen(
  currentLang: String,
  bookmarkedIds: Set<String>,
  onPostClick: (IslamicPost) -> Unit,
  onRemoveBookmark: (String) -> Unit,
) {
  val savedPosts = SampleIslamicData.posts.filter { bookmarkedIds.contains(it.id) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = when (currentLang) {
        "bn" -> "সংরক্ষিত পোস্টসমূহ"
        "ar" -> "المقالات المحفوظة"
        else -> "Saved Articles"
      },
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (savedPosts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(
            Icons.Default.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = when (currentLang) {
              "bn" -> "আপনার কোনো সংরক্ষিত পোস্ট নেই"
              "ar" -> "لم تقم بحفظ أي مقال بعد"
              else -> "You have no saved articles yet"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = when (currentLang) {
              "bn" -> "যেকোনো পোস্ট পড়ার সময় বুকমার্ক আইকনে ট্যাপ করে তা অফলাইনে পড়ার জন্য সংরক্ষণ করুন।"
              "ar" -> "انقر فوق علامة الحفظ عند قراءة أي مقال لحفظه وقراءته لاحقاً."
              else -> "Tap the bookmark icon while reading any article to save it for offline reading."
            },
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(savedPosts) { post ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onPostClick(post) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = post.localizedCategory(currentLang),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = post.localizedTitle(currentLang),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = post.localizedSubtitle(currentLang),
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              IconButton(onClick = { onRemoveBookmark(post.id) }) {
                Icon(
                  Icons.Default.Bookmark,
                  contentDescription = "Remove Bookmark",
                  tint = IslamicGoldAccent
                )
              }
            }
          }
        }
      }
    }
  }
}
