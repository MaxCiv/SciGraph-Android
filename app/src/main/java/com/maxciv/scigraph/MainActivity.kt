package com.maxciv.scigraph

import android.os.Bundle
import android.view.Gravity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.maxciv.scigraph.viewmodel.MainViewModel
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker
import com.scichart.core.annotations.Orientation
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.sci_chart_surface
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
private const val UPDATE_INTERVAL_MILLIS = 1_000L

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var scheduler: ScheduledFuture<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.elevation = 0f

        SciChartBuilder.init(this)
        val sciChartBuilder = SciChartBuilder.instance()

        val xAxis: IAxis = sciChartBuilder.newDateAxis()
                .withAxisTitle(getString(R.string.time))
                .withAutoRangeMode(AutoRange.Always)
                .withVisibleRange(Date(), Calendar.getInstance().apply { add(Calendar.SECOND, 2) }.time)
                .build()
        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle(getString(R.string.value))
                .withAutoRangeMode(AutoRange.Always)
                .build()

        val lineData = sciChartBuilder
                .newXyDataSeries(Date::class.java, Double::class.javaObjectType)
                .withSeriesName(getString(R.string.line_chart))
                .build()
        val scatterData = sciChartBuilder
                .newXyDataSeries(Date::class.java, Double::class.javaObjectType)
                .withSeriesName(getString(R.string.scatter_chart))
                .build()

        viewModel.savedData?.let {
            lineData.append(it.first, it.second)
            scatterData.append(it.first, it.second)
        }

        val lineSeries = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData)
                .withStrokeStyle(ColorUtil.White, 2f, true)
                .build()

        val pointMarker = sciChartBuilder.newPointMarker(EllipsePointMarker())
                .withFill(ColorUtil.GreenYellow)
                .withStroke(ColorUtil.DarkGreen, 2f)
                .withSize(10)
                .build()
        val scatterSeries = sciChartBuilder.newScatterSeries()
                .withDataSeries(scatterData)
                .withPointMarker(pointMarker)
                .build()

        val legendModifier = sciChartBuilder.newModifierGroup()
                .withLegendModifier()
                .withOrientation(Orientation.VERTICAL)
                .withPosition(Gravity.START or Gravity.BOTTOM, 10).build()
                .build()

        val annotation = sciChartBuilder.newHorizontalLineAnnotation()
                .withStroke(2f, ColorUtil.Yellow)
                .withAnnotationLabel()
                .build()

        sci_chart_surface.yAxes.add(yAxis)
        sci_chart_surface.xAxes.add(xAxis)
        sci_chart_surface.renderableSeries.addAll(listOf(lineSeries, scatterSeries))
        sci_chart_surface.chartModifiers.add(legendModifier)
        sci_chart_surface.annotations.add(annotation)

        viewModel.valueToAddOnChart.observe(this, Observer {
            it?.let { valueDataItem ->
                val date = Date(valueDataItem.timeMillis)
                lineData.append(date, valueDataItem.value)
                scatterData.append(date, valueDataItem.value)
                annotation.y1 = valueDataItem.value
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.savedData = sci_chart_surface.renderableSeries.firstOrNull()?.let {
            val series = it.dataSeries as IXyDataSeries<Date, Double>
            series.xValues to series.yValues
        }
    }

    override fun onStart() {
        super.onStart()
        startScheduler()
    }

    override fun onStop() {
        super.onStop()
        stopScheduler()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearValueToAddOnChart()
    }

    private fun startScheduler() {
        if (scheduler?.isDone != false) {
            scheduler = scheduledExecutorService.scheduleWithFixedDelay({
                viewModel.loadDataPoint(System.currentTimeMillis())
            }, 0, UPDATE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopScheduler() {
        scheduler?.cancel(true)
    }
}
