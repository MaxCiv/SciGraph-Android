package com.maxciv.scigraph.repository

import com.maxciv.scigraph.model.DataPoint
import com.maxciv.scigraph.util.Result

/**
 * @author maxim.oleynik
 * @since 15.04.2020
 */
interface DataRepository {

    fun getDataPoint(timeMillis: Long): Result<DataPoint>
}
