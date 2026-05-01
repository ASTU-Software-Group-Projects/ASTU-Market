package com.market.astu.util

data class RunnerTrustBand(
    val label: String,
    val maxOrderValue: Double
)

fun runnerTrustBand(score: Int): RunnerTrustBand {
    return when (score.coerceIn(0, 100)) {
        in 0..19 -> RunnerTrustBand("Bronze", 20.0)
        in 20..49 -> RunnerTrustBand("Silver", 100.0)
        in 50..79 -> RunnerTrustBand("Gold", 500.0)
        else -> RunnerTrustBand("Platinum", 2000.0)
    }
}
