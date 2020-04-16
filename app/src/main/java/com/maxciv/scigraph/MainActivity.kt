package com.maxciv.scigraph

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.maxciv.scigraph.model.DataPoint
import com.maxciv.scigraph.viewmodel.MainViewModel
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.visuals.annotations.AnnotationCoordinateMode
import com.scichart.charting.visuals.annotations.HorizontalAnchorPoint
import com.scichart.charting.visuals.annotations.HorizontalLineAnnotation
import com.scichart.charting.visuals.annotations.TextAnnotation
import com.scichart.charting.visuals.annotations.VerticalAnchorPoint
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker
import com.scichart.core.annotations.Orientation
import com.scichart.core.framework.UpdateSuspender
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.sci_chart_surface
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
private const val LOAD_DATA_INTERVAL_SLOW_MILLIS = 1_000L
private const val LOAD_DATA_INTERVAL_FAST_MILLIS = 1L
private const val RENDER_INTERVAL_MILLIS = 16L

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private val scheduledExecutorService = Executors.newScheduledThreadPool(2)
    private var scheduledLoader: ScheduledFuture<*>? = null
    private var scheduledRenderer: ScheduledFuture<*>? = null

    private var lineData: XyDataSeries<Date, Double>? = null
    private var scatterData: XyDataSeries<Date, Double>? = null
    private var lineAnnotation: HorizontalLineAnnotation? = null
    private var textAnnotation: TextAnnotation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.elevation = 0f

        initChartSurface()

        viewModel.savedData?.let {
            lineData?.append(it.first, it.second)
            scatterData?.append(it.first, it.second)
        }

        viewModel.dataLoadMode.observe(this, Observer {
            it?.let {
                stopLoaderScheduler()
                startLoaderScheduler()
                invalidateOptionsMenu()
                supportActionBar?.subtitle = getString(R.string.template_update_interval, getCurrentDataLoadMode().intervalMillis)
            }
        })
    }

    private fun initChartSurface() {
        SciChartBuilder.init(this)
        val sciChartBuilder = SciChartBuilder.instance()

        val xAxis: IAxis = sciChartBuilder.newDateAxis()
                .withAxisTitle(getString(R.string.time))
                .withAutoRangeMode(AutoRange.Always)
                .build()
        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle(getString(R.string.value))
                .withAutoRangeMode(AutoRange.Always)
                .build()

        lineData = sciChartBuilder
                .newXyDataSeries(Date::class.java, Double::class.javaObjectType)
                .withSeriesName(getString(R.string.line_chart))
                .build()
        scatterData = sciChartBuilder
                .newXyDataSeries(Date::class.java, Double::class.javaObjectType)
                .withSeriesName(getString(R.string.scatter_chart))
                .build()

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

        lineAnnotation = sciChartBuilder.newHorizontalLineAnnotation()
                .withStroke(2f, ColorUtil.Yellow)
                .withAnnotationLabel()
                .build()

        textAnnotation = sciChartBuilder.newTextAnnotation()
                .withX1(0.0)
                .withY1(0.0)
                .withFontStyle(14f, ColorUtil.LightSteelBlue)
                .withPadding(4)
                .withCoordinateMode(AnnotationCoordinateMode.Relative)
                .withHorizontalAnchorPoint(HorizontalAnchorPoint.Left)
                .withVerticalAnchorPoint(VerticalAnchorPoint.Top)
                .withTextGravity(Gravity.START)
                .build()

        sci_chart_surface.yAxes.add(yAxis)
        sci_chart_surface.xAxes.add(xAxis)
        sci_chart_surface.renderableSeries.addAll(listOf(lineSeries, scatterSeries))
        sci_chart_surface.chartModifiers.add(legendModifier)
        sci_chart_surface.annotations.addAll(listOf(lineAnnotation, textAnnotation))
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
        startRendererScheduler()
    }

    override fun onStop() {
        super.onStop()
        stopRendererScheduler()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoaderScheduler()
    }

    private fun startRendererScheduler() {
        if (scheduledRenderer?.isDone != false) {
            scheduledRenderer = scheduledExecutorService.scheduleWithFixedDelay({
                renderNewData(viewModel.getDataPointsForRender())
            }, 0, RENDER_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopRendererScheduler() {
        scheduledRenderer?.cancel(true)
    }

    private fun startLoaderScheduler() {
        if (scheduledLoader?.isDone != false) {
            scheduledLoader = scheduledExecutorService.scheduleWithFixedDelay({
                viewModel.loadDataPointToBuffer(System.currentTimeMillis())
            }, 0, getCurrentDataLoadMode().intervalMillis, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopLoaderScheduler() {
        scheduledLoader?.cancel(true)
    }

    private fun renderNewData(items: List<DataPoint>) {
        if (items.isEmpty()) return

        val xValues = items.map { Date(it.timeMillis) }
        val yValues = items.map { it.value }

        UpdateSuspender.using(sci_chart_surface) {
            lineData?.append(xValues, yValues)
            scatterData?.append(xValues, yValues)
            lineAnnotation?.y1 = yValues.last()
            textAnnotation?.text = getString(R.string.template_points_count, lineData?.count)
        }
    }

    private fun getCurrentDataLoadMode(): DataLoadMode {
        return viewModel.dataLoadMode.value ?: DataLoadMode.SLOW
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)

        val colorId = if (getCurrentDataLoadMode() == DataLoadMode.FAST) R.color.colorAccent else R.color.colorWhite
        menu.findItem(R.id.action_change_load_mode)?.icon?.setTint(resources.getColor(colorId))

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_load_mode -> {
                viewModel.toggleLoadMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

enum class DataLoadMode(val intervalMillis: Long) {
    FAST(LOAD_DATA_INTERVAL_FAST_MILLIS),
    SLOW(LOAD_DATA_INTERVAL_SLOW_MILLIS);
}
