package com.maxciv.scigraph.repository

import com.maxciv.scigraph.model.DataPoint
import com.maxciv.scigraph.util.Result
import kotlin.random.Random

/**
 * @author maxim.oleynik
 * @since 15.04.2020
 */
class RandomDataRepository : DataRepository {

    private val random: Random = Random(System.currentTimeMillis())
    private var nextValue: Double = 0.0

    private fun getAndUpdateNextValue(): Double {
        val diffValue = random.nextDouble(-1.0, 1.0)
        synchronized(this) {
            return nextValue.also { nextValue += diffValue }
        }
    }

    override fun getDataPoint(timeMillis: Long): Result<DataPoint> {
        return Result.Success(DataPoint(timeMillis, getAndUpdateNextValue()))
    }
}
