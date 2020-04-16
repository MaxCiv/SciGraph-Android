package com.maxciv.scigraph.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxciv.scigraph.DataLoadMode
import com.maxciv.scigraph.model.DataPoint
import com.maxciv.scigraph.repository.DataRepository
import com.maxciv.scigraph.util.DataBuffer
import com.maxciv.scigraph.util.Result
import com.scichart.data.model.ISciList
import com.scichart.data.model.ISmartList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
class MainViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    private val dataBuffer: DataBuffer<DataPoint> = DataBuffer()
    private var lastRenderedTime: Long = Long.MIN_VALUE

    var savedData: Pair<ISmartList<Date>, ISciList<Double>>? = null

    private val _dataLoadMode = MutableLiveData<DataLoadMode>(DataLoadMode.SLOW)
    val dataLoadMode: LiveData<DataLoadMode> = _dataLoadMode

    fun toggleLoadMode() {
        _dataLoadMode.value = if (_dataLoadMode.value == DataLoadMode.FAST) {
            DataLoadMode.SLOW
        } else {
            DataLoadMode.FAST
        }
    }

    fun loadDataPointToBuffer(timeMillis: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val dataPoint = dataRepository.getDataPoint(timeMillis)) {
                is Result.Success -> {
                    dataBuffer.addItem(dataPoint.data)
                }
                is Result.Fail -> {
                    Timber.e(dataPoint.throwable, dataPoint.message)
                }
            }
        }
    }

    fun getDataPointsForRender(): List<DataPoint> {
        val dataPoints = dataBuffer.getItemsAndReset()
                .sortedBy { it.timeMillis }
                .filter { it.timeMillis > lastRenderedTime }
        return dataPoints.also { updateLastTime(dataPoints) }
    }

    private fun updateLastTime(dataPoints: List<DataPoint>) {
        if (dataPoints.isNotEmpty()) {
            lastRenderedTime = dataPoints.last().timeMillis
        }
    }
}
