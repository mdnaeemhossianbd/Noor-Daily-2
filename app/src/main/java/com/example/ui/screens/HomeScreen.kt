
          Spacer(modifier = Modifier.height(6.dp))

          // 2. মাঝে: হিজরি ক্যালেন্ডার (Hijri Calendar Bar)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (currentLang) {
                    "bn" -> "হিজরি ক্যালেন্ডার:"
                    "ar" -> "التقويم الهجري:"
                    else -> "Hijri Calendar:"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Text(
                text = hijriFormatted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }

          if (currentLang == "bn") {
            Spacer(modifier = Modifier.height(6.dp))
            // ৩. বাংলা ক্যালেন্ডার (বঙ্গাব্দ)
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
              border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "বাংলা ক্যালেন্ডার (বঙ্গাব্দ):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                  )
                }
                Text(
                  text = banglaSolarDate,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    // SECTION 1.5: Prominent Sehri & Iftar Time Card with Live Countdowns
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Sehri Column
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                tint = IslamicEmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "সেহরির শেষ সময়"
                  "ar" -> "نهاية السحور"
                  else -> "Sehri Ends"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicEmeraldPrimary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = prayerCalculation.sehriEndTime,
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
              Text(
                text = when (currentLang) {
                  "bn" -> "বাকি: $sehriRemainingFormatted"
                  "ar" -> "متبقي: $sehriRemainingFormatted"
                  else -> "Remaining: $sehriRemainingFormatted"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.height(52.dp)
          )

          // Iftar Column
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = IslamicGoldAccent,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when (currentLang) {
                  "bn" -> "ইফতারের সময়"
                  "ar" -> "وقت الإفطار"
                  else -> "Iftar Time"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGoldAccent
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = prayerCalculation.iftarTime,
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = IslamicGoldAccent.copy(alpha = 0.2f)
            ) {
              Text(
                text = when (currentLang) {
                  "bn" -> "বাকি: $iftarRemainingFormatted"
