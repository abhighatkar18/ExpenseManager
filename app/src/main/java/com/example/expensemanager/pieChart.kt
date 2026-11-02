package com.example.expensemanager
import android.graphics.Color
import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
@Composable
fun DynamicPieChart(pieEntries: List<PieEntry>, modifier: Modifier = Modifier) {
    if (pieEntries.isEmpty()) {
        Text(text="No data available",)
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                PieChart(context).apply {
                    description.isEnabled = false
                    setDrawEntryLabels(false)
                    isRotationEnabled = true
                    setUsePercentValues(true)
                    setEntryLabelTextSize(12f)
                    setEntryLabelColor(Color.BLACK)
                    setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                    setCenterTextSize(18f)
                    setHoleColor(Color.WHITE)
                    legend.isEnabled = true
                }
            },
            update = { pieChart ->
                val pieDataSet = PieDataSet(pieEntries, "Expenses")
                pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
                pieDataSet.valueTextColor = Color.BLACK
                pieDataSet.valueTextSize = 16f

                val pieData = PieData(pieDataSet)
                pieChart.data = pieData
                pieChart.invalidate() // refresh chart
            }
        )
    }
}
