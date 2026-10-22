package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleIslamicData
import com.example.model.IslamicCategory
import com.example.model.IslamicPost
import com.example.model.IslamicSubcategory
import com.example.ui.components.IslamicCalendarDialog
import com.example.ui.components.IslamicQuizDialog
import com.example.ui.components.NamesOfAllahDialog
import com.example.ui.components.TasbihDialog
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldAccent
import com.example.ui.theme.IslamicGoldLight

@Composable
fun CategoriesScreen(
  currentLang: String,
  onPostClick: (IslamicPost) -> Unit,
) {
  // Navigation Hierarchy State
  // Level 1: selectedCategory == null
  // Level 2: selectedCategory != null && selectedSubcategory == null
  // Level 3: selectedSubcategory != null
  var selectedCategory by remember { mutableStateOf<IslamicCategory?>(null) }
  var selectedSubcategory by remember { mutableStateOf<IslamicSubcategory?>(null) }
  var parentSubcategory by remember { mutableStateOf<IslamicSubcategory?>(null) }

  var searchQuery by remember { mutableStateOf("") }

  // Quick Action Dialogs
  var showTasbihDialog by remember { mutableStateOf(false) }
  var showNamesOfAllahDialog by remember { mutableStateOf(false) }
  var showIslamicCalendarDialog by remember { mutableStateOf(false) }
  var showIslamicQuizDialog by remember { mutableStateOf(false) }

  // Back handling across hierarchy
  BackHandler(enabled = selectedSubcategory != null || selectedCategory != null) {
    if (selectedSubcategory != null) {
      if (parentSubcategory != null) {
        selectedSubcategory = parentSubcategory
        parentSubcategory = null
      } else {
        selectedSubcategory = null
      }
    } else if (selectedCategory != null) {
      selectedCategory = null
      searchQuery = ""
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(14.dp))

    // BREADCRUMB & NAVIGATION BAR
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (selectedCategory != null) {
          IconButton(
            onClick = {
              if (selectedSubcategory != null) {
                if (parentSubcategory != null) {
                  selectedSubcategory = parentSubcategory
                  parentSubcategory = null
                } else {
                  selectedSubcategory = null
                }
              } else {
                selectedCategory = null
                searchQuery = ""
              }
            },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
          // Breadcrumb Trail
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "বিষয়সমূহ"
                "ar" -> "الأقسام"
                else -> "Topics"
              },
              fontSize = 11.sp,
              color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.clickable(enabled = selectedCategory != null) {
                selectedCategory = null
                selectedSubcategory = null
                parentSubcategory = null
                searchQuery = ""
              }
            )

            if (selectedCategory != null) {
              Text(
                text = " > ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = selectedCategory!!.localizedName(currentLang),
                fontSize = 11.sp,
                color = if (selectedSubcategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selectedSubcategory == null) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = selectedSubcategory != null) {
                  selectedSubcategory = null
                  parentSubcategory = null
                  searchQuery = ""
                }
              )
            }

            if (selectedSubcategory != null) {
              Text(
                text = " > ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = selectedSubcategory!!.localizedName(currentLang),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Main Title
          Text(
            text = when {
              selectedSubcategory != null -> selectedSubcategory!!.localizedName(currentLang)
              selectedCategory != null -> selectedCategory!!.localizedName(currentLang)
              else -> when (currentLang) {
                "bn" -> "ইসলামিক প্রধান বিষয়সমূহ"
                "ar" -> "الأقسام والموضوعات الإسلامية"
                else -> "Main Topic Categories"
              }
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Search TextField
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = {
        Text(
          when {
            selectedSubcategory != null -> when (currentLang) {
              "bn" -> "এই বিষয়ের পোস্ট অনুসন্ধান করুন..."
              else -> "Search posts in this topic..."
            }
            selectedCategory != null -> when (currentLang) {
              "bn" -> "উপ-বিভাগ অনুসন্ধান করুন..."
              else -> "Search subcategories..."
            }
            else -> when (currentLang) {
              "bn" -> "বিষয় বা অধ্যায় অনুসন্ধান করুন..."
              "ar" -> "ابحث في الأقسام..."
              else -> "Search topics or chapters..."
            }
          },
          fontSize = 12.sp
        )
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // DYNAMIC SCREEN DISPLAY (LEVEL 1 / LEVEL 2 / LEVEL 3)
    AnimatedContent(
      targetState = Triple(selectedCategory, selectedSubcategory, parentSubcategory),
      transitionSpec = {
        fadeIn() togetherWith fadeOut()
      },
      label = "category_levels"
    ) { (cat, sub, _) ->
      when {
        // LEVEL 3: Posts belonging strictly to the selected Subcategory
        sub != null -> {
          Level3PostsScreen(
            currentLang = currentLang,
            subcategory = sub,
            searchQuery = searchQuery,
            onPostClick = onPostClick
          )
        }

        // LEVEL 2: Subcategories belonging strictly to the selected Main Category
        cat != null -> {
          Level2SubcategoriesScreen(
            currentLang = currentLang,
            category = cat,
            searchQuery = searchQuery,
            onSubcategoryClick = { clickedSub ->
              // Check if clickedSub has children
              val hasChildren = SampleIslamicData.subcategories.any { it.parentSubcategoryId == clickedSub.id }
              if (hasChildren) {
                parentSubcategory = clickedSub
                selectedSubcategory = clickedSub
              } else {
                selectedSubcategory = clickedSub
              }
              searchQuery = ""
            }
          )
        }

        // LEVEL 1: Main Categories
        else -> {
          Level1MainCategoriesScreen(
            currentLang = currentLang,
            searchQuery = searchQuery,
            onCategoryClick = { clickedCat ->
              selectedCategory = clickedCat
              searchQuery = ""
            },
            onOpenCalendar = { showIslamicCalendarDialog = true },
            onOpenNames = { showNamesOfAllahDialog = true },
            onOpenTasbih = { showTasbihDialog = true },
            onOpenQuiz = { showIslamicQuizDialog = true }
          )
        }
      }
    }
  }

  // Dialog implementations
  if (showTasbihDialog) TasbihDialog(onDismiss = { showTasbihDialog = false })
  if (showNamesOfAllahDialog) NamesOfAllahDialog(onDismiss = { showNamesOfAllahDialog = false })
  if (showIslamicCalendarDialog) IslamicCalendarDialog(currentLang = currentLang, onDismiss = { showIslamicCalendarDialog = false })
  if (showIslamicQuizDialog) IslamicQuizDialog(currentLang = currentLang, onDismiss = { showIslamicQuizDialog = false })
}

// ----------------------------------------------------
// LEVEL 1: Main Categories View
// ----------------------------------------------------
@Composable
private fun Level1MainCategoriesScreen(
  currentLang: String,
  searchQuery: String,
  onCategoryClick: (IslamicCategory) -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenNames: () -> Unit,
  onOpenTasbih: () -> Unit,
  onOpenQuiz: () -> Unit,
) {
  val allCategories = SampleIslamicData.categories
  val filteredCategories = remember(allCategories, searchQuery, currentLang) {
    if (searchQuery.isBlank()) allCategories
    else {
      val q = searchQuery.trim().lowercase()
      allCategories.filter {
        it.localizedName(currentLang).lowercase().contains(q) ||
        it.nameEn.lowercase().contains(q) ||
        it.nameBn.lowercase().contains(q)
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    // Top 4 Islamic Utility Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        QuickToolCard(
          icon = Icons.Default.CalendarMonth,
          title = when (currentLang) { "bn" -> "ক্যালেন্ডার"; else -> "Calendar" },
          bg = MaterialTheme.colorScheme.secondary,
          iconTint = Color.Black,
          modifier = Modifier.weight(1f),
          onClick = onOpenCalendar
        )
        QuickToolCard(
          icon = Icons.Default.Star,
          title = when (currentLang) { "bn" -> "৯৯ নাম"; else -> "99 Names" },
          bg = IslamicEmeraldPrimary,
          iconTint = IslamicGoldAccent,
          modifier = Modifier.weight(1f),
          onClick = onOpenNames
        )
        QuickToolCard(
          icon = Icons.Default.TouchApp,
          title = when (currentLang) { "bn" -> "তাসবীহ"; else -> "Tasbih" },
          bg = MaterialTheme.colorScheme.primary,
          iconTint = Color.White,
          modifier = Modifier.weight(1f),
          onClick = onOpenTasbih
        )
        QuickToolCard(
          icon = Icons.Default.HelpOutline,
          title = when (currentLang) { "bn" -> "কুইজ"; else -> "Quiz" },
          bg = IslamicEmeraldPrimary,
          iconTint = Color.White,
          modifier = Modifier.weight(1f),
          onClick = onOpenQuiz
        )
      }
    }

    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (currentLang) {
            "bn" -> "প্রধান বিষয়সমূহ (${filteredCategories.size})"
            "ar" -> "الأقسام الرئيسية"
            else -> "Main Categories (${filteredCategories.size})"
          },
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Text(
          text = when (currentLang) {
            "bn" -> "ট্যাপ করে উপ-বিভাগ দেখুন"
            "ar" -> "اضغط لعرض الأقسام الفرعية"
            else -> "Tap to view subcategories"
          },
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (filteredCategories.isEmpty()) {
      item {
        EmptyStateCard(
          message = when (currentLang) {
            "bn" -> "কোনো ক্যাটাগরি পাওয়া যায়নি"
            else -> "No categories found"
          }
        )
      }
    } else {
      items(filteredCategories, key = { it.id }) { cat ->
        MainCategoryCard(
          category = cat,
          currentLang = currentLang,
          onClick = { onCategoryClick(cat) }
        )
      }
    }
  }
}

@Composable
private fun MainCategoryCard(
  category: IslamicCategory,
  currentLang: String,
  onClick: () -> Unit
) {
  val iconVector = getCategoryIcon(category.iconName)
  val subsCount = SampleIslamicData.subcategories.count { it.categoryId == category.id }
  val postsCount = SampleIslamicData.posts.count { it.categoryId == category.id }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .background(
            Brush.linearGradient(listOf(IslamicEmeraldPrimary, IslamicEmeraldDark)),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          iconVector,
          contentDescription = null,
          tint = IslamicGoldAccent,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = category.localizedName(currentLang),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (category.nameEn != category.nameBn) {
          Text(
            text = if (currentLang == "bn") category.nameEn else category.nameBn,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "$subsCount টি উপ-বিভাগ"
                else -> "$subsCount Subcategories"
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = when (currentLang) {
                "bn" -> "$postsCount টি পোস্ট"
                else -> "$postsCount Posts"
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Icon(
        Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

// ----------------------------------------------------
// LEVEL 2: Subcategories Screen
// ----------------------------------------------------
@Composable
private fun Level2SubcategoriesScreen(
  currentLang: String,
  category: IslamicCategory,
  searchQuery: String,
  onSubcategoryClick: (IslamicSubcategory) -> Unit
) {
  val allSubs = SampleIslamicData.subcategories.filter { it.categoryId == category.id }
  val filteredSubs = remember(allSubs, searchQuery, currentLang) {
    if (searchQuery.isBlank()) allSubs
    else {
      val q = searchQuery.trim().lowercase()
      allSubs.filter {
        it.localizedName(currentLang).lowercase().contains(q) ||
        it.nameEn.lowercase().contains(q) ||
        it.nameBn.lowercase().contains(q)
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    // Header Info Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Brush.linearGradient(listOf(IslamicEmeraldPrimary, IslamicEmeraldDark)))
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(IslamicGoldAccent, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(getCategoryIcon(category.iconName), contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = category.localizedName(currentLang),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = when (currentLang) {
                "bn" -> "উপ-বিভাগ নির্বাচন করে বিষয়ভিত্তিক পোস্টসমূহ পড়ুন"
                else -> "Select a subcategory to browse posts"
              },
              fontSize = 11.sp,
              color = IslamicGoldLight
            )
          }
        }
      }
    }

    item {
      Text(
        text = when (currentLang) {
          "bn" -> "উপ-বিভাগসমূহ (${filteredSubs.size})"
          "ar" -> "الأقسام الفرعية"
          else -> "Subcategories (${filteredSubs.size})"
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    if (filteredSubs.isEmpty()) {
      item {
        EmptyStateCard(
          message = when (currentLang) {
            "bn" -> "এই ক্যাটাগরিতে কোনো উপ-বিভাগ পাওয়া যায়নি"
            else -> "No subcategories found in this category"
          }
        )
      }
    } else {
      items(filteredSubs, key = { it.id }) { sub ->
        SubcategoryItemCard(
          subcategory = sub,
          currentLang = currentLang,
          onClick = { onSubcategoryClick(sub) }
        )
      }
    }
  }
}

@Composable
private fun SubcategoryItemCard(
  subcategory: IslamicSubcategory,
  currentLang: String,
  onClick: () -> Unit
) {
  val postsInSub = SampleIslamicData.posts.count { it.subcategoryId == subcategory.id || (it.categoryId == subcategory.categoryId && it.subcategoryNameBn == subcategory.nameBn) }
  val iconVector = getSubcategoryIcon(subcategory.iconName)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          iconVector,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = subcategory.localizedName(currentLang),
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (subcategory.nameEn != subcategory.nameBn) {
          Text(
            text = if (currentLang == "bn") subcategory.nameEn else subcategory.nameBn,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
      ) {
        Text(
          text = when (currentLang) {
            "bn" -> "$postsInSub টি পোস্ট"
            else -> "$postsInSub Posts"
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      Icon(
        Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

// ----------------------------------------------------
// LEVEL 3: Posts Screen
// ----------------------------------------------------
@Composable
private fun Level3PostsScreen(
  currentLang: String,
  subcategory: IslamicSubcategory,
  searchQuery: String,
  onPostClick: (IslamicPost) -> Unit
) {
  val allPosts = SampleIslamicData.posts.filter {
    it.subcategoryId == subcategory.id || (it.categoryId == subcategory.categoryId && it.subcategoryNameBn == subcategory.nameBn)
  }

  val filteredPosts = remember(allPosts, searchQuery, currentLang) {
    if (searchQuery.isBlank()) allPosts
    else {
      val q = searchQuery.trim().lowercase()
      allPosts.filter { post ->
        val title = post.localizedTitle(currentLang).lowercase()
        val sub = post.localizedSubtitle(currentLang).lowercase()
        val body = post.localizedBody(currentLang).lowercase()
        title.contains(q) || sub.contains(q) || body.contains(q)
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Default.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = subcategory.localizedName(currentLang),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = when (currentLang) {
                "bn" -> "মোট ${filteredPosts.size} টি পোস্ট প্রদর্শিত হচ্ছে"
                else -> "Showing ${filteredPosts.size} posts"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    if (filteredPosts.isEmpty()) {
      item {
        EmptyStateCard(
          message = when (currentLang) {
            "bn" -> "এই উপ-বিভাগে এখনও কোনো পোস্ট যুক্ত করা হয়নি"
            else -> "No posts found in this subcategory"
          }
        )
      }
    } else {
      items(filteredPosts, key = { it.id }) { post ->
        PostCardItem(
          post = post,
          currentLang = currentLang,
          onClick = { onPostClick(post) }
        )
      }
    }
  }
}

@Composable
private fun PostCardItem(
  post: IslamicPost,
  currentLang: String,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = post.source.ifEmpty { "নূর ডেইলি" },
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "${post.readTimeMinutes} মিনিট",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = post.localizedTitle(currentLang),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      if (post.localizedSubtitle(currentLang).isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = post.localizedSubtitle(currentLang),
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (currentLang) {
            "bn" -> "বিস্তারিত পড়ুন"
            "ar" -> "قراءة المزيد"
            else -> "Read More"
          },
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}

// ----------------------------------------------------
// UI HELPERS & CARDS
// ----------------------------------------------------
@Composable
private fun QuickToolCard(
  icon: ImageVector,
  title: String,
  bg: Color,
  iconTint: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .background(bg, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun EmptyStateCard(message: String) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        Icons.Default.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = Modifier.size(40.dp)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = message,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun getCategoryIcon(iconName: String): ImageVector {
  return when (iconName.lowercase()) {
    "quran" -> Icons.Default.MenuBook
    "prayer" -> Icons.Default.Mosque
    "hadith" -> Icons.Default.Bookmark
    "dua" -> Icons.Default.PanTool
    "moon", "fasting" -> Icons.Default.NightlightRound
    "charity", "zakat" -> Icons.Default.VolunteerActivism
    "calendar" -> Icons.Default.CalendarMonth
    else -> Icons.Default.Folder
  }
}

private fun getSubcategoryIcon(iconName: String): ImageVector {
  return when (iconName.lowercase()) {
    "wb_twilight" -> Icons.Default.WbTwilight
    "wb_sunny" -> Icons.Default.WbSunny
    "brightness_6" -> Icons.Default.Brightness6
    "bedtime" -> Icons.Default.Bedtime
    "nightlight_round" -> Icons.Default.NightlightRound
    "stars" -> Icons.Default.Stars
    "mosque" -> Icons.Default.Mosque
    "access_time" -> Icons.Default.AccessTime
    "favorite" -> Icons.Default.Favorite
    "restaurant" -> Icons.Default.Restaurant
    "nights_stay" -> Icons.Default.NightsStay
    "brightness_high" -> Icons.Default.BrightnessHigh
    "menu_book" -> Icons.Default.MenuBook
    "auto_stories" -> Icons.Default.AutoStories
    "shield" -> Icons.Default.Shield
    "record_voice_over" -> Icons.Default.RecordVoiceOver
    "water_drop" -> Icons.Default.WaterDrop
    "sentiment_satisfied_alt" -> Icons.Default.SentimentSatisfiedAlt
    "calculate" -> Icons.Default.Calculate
    "volunteer_activism" -> Icons.Default.VolunteerActivism
    else -> Icons.Default.FolderOpen
  }
}
