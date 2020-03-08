package com.didiglobal.booster.kotlinx

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.format(format: String) = SimpleDateFormat(format, Locale.getDefault()).format(this)
