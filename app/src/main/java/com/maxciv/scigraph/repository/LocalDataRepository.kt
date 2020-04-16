package com.maxciv.scigraph.repository

import com.maxciv.scigraph.local.RandomDataService
import com.maxciv.scigraph.model.DataPoint
import com.maxciv.scigraph.util.Result

/**
 * @author maxim.oleynik
 * @since 15.04.2020
 */
class LocalDataRepository(private val randomDataService: RandomDataService) : DataRepository {

    override fun getDataPoint(timeMillis: Long): Result<DataPoint> {
        return Result.Success(DataPoint(timeMillis, randomDataService.getAndUpdateNextValue()))
    }
}
