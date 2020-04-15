package com.maxciv.scigraph.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxciv.scigraph.model.DataPoint
import com.maxciv.scigraph.repository.DataRepository
import com.maxciv.scigraph.util.Result
import com.scichart.data.model.ISciList
import com.scichart.data.model.ISmartList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
class MainViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    var savedData: Pair<ISmartList<Date>, ISciList<Double>>? = null

    private val _valueToAddOnChart = MutableLiveData<DataPoint>()
    val valueToAddOnChart: LiveData<DataPoint> = _valueToAddOnChart

    private fun setValueToAddOnChart(dataPoint: DataPoint) {
        synchronized(this) {
            if (dataPoint.timeMillis > _valueToAddOnChart.value?.timeMillis ?: Long.MIN_VALUE) {
                _valueToAddOnChart.value = dataPoint
            }
        }
    }

    fun clearValueToAddOnChart() {
        _valueToAddOnChart.value = null
    }

    fun loadDataPoint(timeMillis: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val dataPoint = dataRepository.getDataPoint(timeMillis)) {
                is Result.Success -> {
                    withContext(Dispatchers.Main) {
                        setValueToAddOnChart(dataPoint.data)
                    }
                }
                is Result.Fail -> {
                    Timber.e(dataPoint.throwable, dataPoint.message)
                }
            }
        }
    }
}
