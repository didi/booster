package com.didiglobal.booster.kotlinx

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.format(format: String) = format(SimpleDateFormat(format, Locale.getDefault()))

fun Date.format(formatter: DateFormat): String = formatter.format(this)
