package com.maxciv.scigraph.local

import kotlin.random.Random

/**
 * @author maxim.oleynik
 * @since 16.04.2020
 */
class RandomDataService {

    private val random: Random = Random(System.currentTimeMillis())
    private var nextValue: Double = 0.0

    fun getAndUpdateNextValue(): Double {
        val diffValue = random.nextDouble(-1.0, 1.0)
        synchronized(this) {
            return nextValue.also { nextValue += diffValue }
        }
    }
}
