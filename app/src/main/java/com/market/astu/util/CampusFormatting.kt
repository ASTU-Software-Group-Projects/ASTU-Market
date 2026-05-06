package com.market.astu.util

import java.text.DecimalFormat

private val campusFormat = DecimalFormat("#,##0.##")

fun formatCampus(amount: Double): String = "${campusFormat.format(amount)} ETB"
