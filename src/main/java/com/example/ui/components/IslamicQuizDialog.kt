package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldAccent
import com.example.ui.theme.IslamicGoldLight

data class LocalQuizItem(
  val id: String,
  val questionBn: String,
  val questionEn: String,
  val optionsBn: List<String>,
  val optionsEn: List<String>,
  val correctOptionIndex: Int,
  val explanationBn: String,
  val explanationEn: String,
  val hadithReference: String,
  val category: String,
)

val defaultQuizList = listOf(
  LocalQuizItem(
    id = "q1",
    questionBn = "পবিত্র কুরআনের কোন সূরাকে 'কুরআনের হৃৎপিণ্ড' বা 'দিল' বলা হয়?",
    questionEn = "Which Surah in the Holy Quran is known as the 'Heart of the Quran'?",
    optionsBn = listOf("সূরা আল-বাকারা", "সূরা ইয়াসীন", "সূরা আর-রহমান", "সূরা আল-মুলক"),
    optionsEn = listOf("Surah Al-Baqarah", "Surah Ya-Sin", "Surah Ar-Rahman", "Surah Al-Mulk"),
    correctOptionIndex = 1,
    explanationBn = "রাসূলুল্লাহ (সা.) বলেছেন: 'নিশ্চয় প্রত্যেক বস্তুর একটি হৃৎপিণ্ড আছে, আর কুরআনের হৃৎপিণ্ড হলো সূরা ইয়াসীন।' (সুনানে তিরমিযী: ২৮৮৭)",
    explanationEn = "Prophet Muhammad (PBUH) said: 'Indeed, everything has a heart, and the heart of the Quran is Surah Ya-Sin.' (Sunan al-Tirmidhi 2887)",
    hadithReference = "সুনানে তিরমিযী: ২৮৮৭",
    category = "Quran"
  ),
  LocalQuizItem(
    id = "q2",
    questionBn = "ইসলামে প্রতিদিন কয় ওয়াক্ত ফরজ নামাজ নির্ধারিত?",
    questionEn = "How many obligatory (Fard) daily prayers are ordained in Islam?",
    optionsBn = listOf("৩ ওয়াক্ত", "৪ ওয়াক্ত", "৫ ওয়াক্ত", "৭ ওয়াক্ত"),
    optionsEn = listOf("3 times", "4 times", "5 times", "7 times"),
    correctOptionIndex = 2,
    explanationBn = "মিরাজের রজনীতে মহান আল্লাহ উম্মতে মুহাম্মাদীর উপর প্রতিদিন ৫ ওয়াক্ত নামাজ (ফজর, যোহর, আসর, মাগরিব ও এশা) ফরজ করেছেন।",
    explanationEn = "During the journey of Miraj, Allah obligated 5 daily prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) upon the Muslim Ummah.",
    hadithReference = "সহীহ বুখারী: ৩৪৯",
    category = "Salah"
  ),
  LocalQuizItem(
    id = "q3",
    questionBn = "হেরা গুহায় মহানবী (সা.)-এর প্রতি নাজিলকৃত সর্বপ্রথম শব্দটি কী ছিল?",
    questionEn = "What was the very first word revealed to Prophet Muhammad (PBUH) in Cave Hira?",
    optionsBn = listOf("ক্বুল (বলুন)", "ইক্বরা (পড়ুন)", "আল-হামদ (প্রশংসা)", "বিসমিল্লাহ"),
    optionsEn = listOf("Qul (Say)", "Iqra (Read/Recite)", "Al-Hamd (Praise)", "Bismillah"),
    correctOptionIndex = 1,
    explanationBn = "হযরত জিবরাঈল (আ.)-এর মাধ্যমে সূরা আলাকের প্রথম ৫টি আয়াত নাজিল হয় এবং এর প্রথম শব্দ ছিল 'ইক্বরা' (পড়ুন আপনার রবের নামে যিনি সৃষ্টি করেছেন)।",
    explanationEn = "The first revealed word was 'Iqra' (Read! In the Name of your Lord Who created) in Surah Al-Alaq, brought by Angel Jibreel (AS).",
    hadithReference = "সূরা আল-আলাক: আয়াত ১",
    category = "Seerah"
  ),
  LocalQuizItem(
    id = "q4",
    questionBn = "কোন আরবি হিজরি মাসে মুসলমানদের জন্য ফরজ রোজা পালন করা হয়?",
    questionEn = "In which Islamic month is the obligatory fasting (Sawm) observed?",
    optionsBn = listOf("মুহররম", "রজব", "শা'বান", "রমজান"),
    optionsEn = listOf("Muharram", "Rajab", "Sha'ban", "Ramadan"),
    correctOptionIndex = 3,
    explanationBn = "সূরা আল-বাকারার ১৮৫ নম্বর আয়াতে বলা হয়েছে: 'রমজান মাসই হলো সেই মাস যাতে মানুষের হেদায়াতস্বরূপ কুরআন নাজিল হয়েছে।'",
    explanationEn = "Allah says in Surah Al-Baqarah (2:185): 'The month of Ramadan in which was revealed the Quran, guidance for people.'",
    hadithReference = "সূরা আল-বাকারা: ১৮৫",
    category = "Ramadan"
  ),
  LocalQuizItem(
    id = "q5",
    questionBn = "ইসলামের প্রথম স্তম্ভ বা মূল ভিত্তি কোনটি?",
    questionEn = "What is the very first pillar of Islam?",
    optionsBn = listOf("কালিমা শাহাদাত (ঈমান)", "সালাত (নামাজ)", "যাকাত", "হজ্ব"),
    optionsEn = listOf("Kalima Shahadah (Faith)", "Salah (Prayer)", "Zakat", "Hajj"),
    correctOptionIndex = 0,
    explanationBn = "রাসূলুল্লাহ (সা.) বলেছেন: 'ইসলামের ভিত্তি পাঁচটি স্তম্ভের ওপর স্থাপিত—তার প্রথমটি হলো এ সাক্ষ্য দেওয়া যে আল্লাহ ছাড়া কোনো ইলাহ নেই এবং মুহাম্মাদ (সা.) তাঁর বান্দা ও রাসূল।' (সহীহ বুখারী: ৮)",
    explanationEn = "The Prophet (PBUH) said: 'Islam is built on five pillars: to testify that there is no god but Allah and Muhammad is His Messenger...' (Bukhari: 8)",
    hadithReference = "সহীহ বুখারী: ৮",
    category = "General"
  )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicQuizDialog(
  currentLang: String,
  onDismiss: () -> Unit,
) {
  var currentIndex by remember { mutableIntStateOf(0) }
  var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
  var hasSubmittedAnswer by remember { mutableStateOf(false) }
  var score by remember { mutableIntStateOf(0) }
  var isQuizCompleted by remember { mutableStateOf(false) }

  val questions = defaultQuizList
  val currentQuestion = questions.getOrNull(currentIndex) ?: questions[0]

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.88f)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top Header
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(0.dp, Color.Transparent)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(IslamicEmeraldPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.HelpOutline,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = when (currentLang) {
                    "bn" -> "ইসলামিক কুইজ"
                    "ar" -> "المسابقة الإسلامية"
                    else -> "Islamic Quiz"
                  },
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = when (currentLang) {
                    "bn" -> "সঠিক উত্তরের মাধ্যমে জ্ঞান বৃদ্ধি করুন"
                    "ar" -> "اختبر معلوماتك الإسلامية"
                    else -> "Test & strengthen your Islamic knowledge"
                  },
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }

        // Quiz Content
        if (isQuizCompleted) {
          // Completed Screen
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp)
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(90.dp)
                .background(IslamicEmeraldPrimary.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = IslamicGoldAccent,
                modifier = Modifier.size(52.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = when (currentLang) {
                "bn" -> "মাশাআল্লাহ! কুইজ সমাপ্ত"
                "ar" -> "ما شاء الله! اكتملت المسابقة"
                else -> "Masha'Allah! Quiz Completed"
              },
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = when (currentLang) {
                "bn" -> "আপনার অর্জিত স্কোর: $score / ${questions.size}"
                "ar" -> "درجتك: $score / ${questions.size}"
                else -> "Your Score: $score / ${questions.size}"
              },
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            val feedbackText = when {
              score == questions.size -> when (currentLang) {
                "bn" -> "অসাধারণ! আপনার ইসলামিক জ্ঞান অত্যন্ত প্রশংসনীয়।"
                else -> "Outstanding! You have exceptional Islamic knowledge."
              }
              score >= 3 -> when (currentLang) {
                "bn" -> "খুব ভালো! নিয়মিত চর্চা অব্যাহত রাখুন।"
                else -> "Very good! Keep learning and revising."
              }
              else -> when (currentLang) {
                "bn" -> "আলহামদুলিল্লাহ! প্রতিনিয়ত কুরআন ও হাদিস পাঠ করুন।"
                else -> "Alhamdulillah! Keep reading Quran and authentic Hadiths."
              }
            }

            Text(
              text = feedbackText,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Button(
                onClick = {
                  currentIndex = 0
                  selectedOptionIndex = null
                  hasSubmittedAnswer = false
                  score = 0
                  isQuizCompleted = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (currentLang) { "bn" -> "পুনরায় খেলুন"; else -> "Play Again" },
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(when (currentLang) { "bn" -> "বন্ধ করুন"; else -> "Close" })
              }
            }
          }
        } else {
          // Question Flow Screen
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              // Progress Bar & Counter
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = "${currentQuestion.category} • " + when (currentLang) {
                      "bn" -> "প্রশ্ন ${currentIndex + 1} / ${questions.size}"
                      else -> "Question ${currentIndex + 1} of ${questions.size}"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Text(
                  text = when (currentLang) {
                    "bn" -> "স্কোর: $score"
                    else -> "Score: $score"
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Question Text
              Text(
                text = when (currentLang) {
                  "bn" -> currentQuestion.questionBn
                  else -> currentQuestion.questionEn
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
              )

              Spacer(modifier = Modifier.height(16.dp))

              // 4 Options Grid/Column
              val options = when (currentLang) {
                "bn" -> currentQuestion.optionsBn
                else -> currentQuestion.optionsEn
              }

              options.forEachIndexed { optIndex, optionText ->
                val letter = ('A' + optIndex).toString()
                val isSelected = selectedOptionIndex == optIndex
                val isCorrectAnswer = optIndex == currentQuestion.correctOptionIndex

                val (containerColor, borderColor, textColor) = when {
                  hasSubmittedAnswer && isCorrectAnswer -> Triple(
                    IslamicEmeraldPrimary.copy(alpha = 0.2f),
                    IslamicEmeraldPrimary,
                    IslamicEmeraldPrimary
                  )
                  hasSubmittedAnswer && isSelected && !isCorrectAnswer -> Triple(
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.error
                  )
                  isSelected -> Triple(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary
                  )
                  else -> Triple(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.onSurface
                  )
                }

                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = containerColor),
                  border = BorderStroke(1.dp, borderColor),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable(enabled = !hasSubmittedAnswer) {
                      selectedOptionIndex = optIndex
                    }
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(28.dp)
                          .background(
                            if (isSelected || (hasSubmittedAnswer && isCorrectAnswer)) IslamicEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                          ),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = letter,
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isSelected || (hasSubmittedAnswer && isCorrectAnswer)) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(
                        text = optionText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected || (hasSubmittedAnswer && isCorrectAnswer)) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                      )
                    }

                    if (hasSubmittedAnswer) {
                      if (isCorrectAnswer) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = IslamicEmeraldPrimary, modifier = Modifier.size(20.dp))
                      } else if (isSelected) {
                        Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                      }
                    }
                  }
                }
              }

              // Answer Explanation Box (Revealed after submission)
              AnimatedVisibility(visible = hasSubmittedAnswer) {
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                  border = BorderStroke(1.dp, IslamicEmeraldPrimary.copy(alpha = 0.3f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = IslamicEmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = when (currentLang) {
                          "bn" -> "সঠিক উত্তরের রেফারেন্স ও তাৎপর্য:"
                          else -> "Authentic Reference & Explanation:"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = when (currentLang) {
                        "bn" -> currentQuestion.explanationBn
                        else -> currentQuestion.explanationEn
                      },
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurface,
                      lineHeight = 18.sp
                    )
                    if (currentQuestion.hadithReference.isNotEmpty()) {
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = "সূত্র: ${currentQuestion.hadithReference}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = IslamicGoldLight
                      )
                    }
                  }
                }
              }
            }

            // Bottom Action Button
            Spacer(modifier = Modifier.height(20.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              if (!hasSubmittedAnswer) {
                Button(
                  onClick = {
                    if (selectedOptionIndex != null) {
                      hasSubmittedAnswer = true
                      if (selectedOptionIndex == currentQuestion.correctOptionIndex) {
                        score++
                      }
                    }
                  },
                  enabled = selectedOptionIndex != null,
                  colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = when (currentLang) { "bn" -> "উত্তর যাচাই করুন"; else -> "Check Answer" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              } else {
                Button(
                  onClick = {
                    if (currentIndex < questions.size - 1) {
                      currentIndex++
                      selectedOptionIndex = null
                      hasSubmittedAnswer = false
                    } else {
                      isQuizCompleted = true
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = if (currentIndex < questions.size - 1) {
                      when (currentLang) { "bn" -> "পরবর্তী প্রশ্ন"; else -> "Next Question" }
                    } else {
                      when (currentLang) { "bn" -> "ফলাফল দেখুন"; else -> "View Results" }
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}
