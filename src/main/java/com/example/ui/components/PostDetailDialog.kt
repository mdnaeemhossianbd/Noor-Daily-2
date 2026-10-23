package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.IslamicPost
import com.example.ui.theme.IslamicGoldAccent

@Composable
fun PostDetailDialog(
  post: IslamicPost,
  currentLang: String,
  isBookmarked: Boolean,
  onToggleBookmark: () -> Unit,
  onDismiss: () -> Unit,
) {
  var fontSize by remember { mutableFloatStateOf(16f) }
  val clipboardManager = LocalClipboardManager.current
  var showCopiedSnackbar by remember { mutableStateOf(false) }

  val title = post.localizedTitle(currentLang)
  val subtitle = post.localizedSubtitle(currentLang)
  val body = post.localizedBody(currentLang)
  val isAr = currentLang == "ar"

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 28.dp),
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        
        // Top Action Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Decrease Font Size
            IconButton(
              onClick = { if (fontSize > 13f) fontSize -= 1.5f },
              enabled = fontSize > 13f
            ) {
              Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Increase Font Size
            IconButton(
              onClick = { if (fontSize < 24f) fontSize += 1.5f },
              enabled = fontSize < 24f
            ) {
              Text("A+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Copy Text
            IconButton(onClick = {
              clipboardManager.setText(AnnotatedString("$title\n\n$body\n\n- ${post.source}"))
              showCopiedSnackbar = true
            }) {
              Icon(Icons.Default.Share, contentDescription = "Copy text")
            }

            // Bookmark Toggle
            IconButton(onClick = onToggleBookmark) {
              Icon(
                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = if (isBookmarked) IslamicGoldAccent else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Article Content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
        ) {
          // Category Tag
          AssistChip(
            onClick = {},
            label = {
              Text(
                post.localizedCategory(currentLang),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
            },
            colors = AssistChipDefaults.assistChipColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = null
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Title
          Text(
            text = title,
            fontSize = (fontSize + 6).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (fontSize + 12).sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (isAr) TextAlign.End else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
          )

          if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = subtitle,
              fontSize = fontSize.sp,
              lineHeight = (fontSize + 6).sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = if (isAr) TextAlign.End else TextAlign.Start,
              modifier = Modifier.fillMaxWidth()
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
          Spacer(modifier = Modifier.height(16.dp))

          // Body text
          Text(
            text = body,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.6f).sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (isAr) TextAlign.End else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Source Footer
          Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Source: ${post.source}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${post.readTimeMinutes} min read",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(40.dp))
        }

        // Notification if copied
        if (showCopiedSnackbar) {
          Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
              TextButton(onClick = { showCopiedSnackbar = false }) {
                Text("OK", color = Color.White)
              }
            }
          ) {
            Text("Content copied to clipboard!")
          }
        }
      }
    }
  }
}
