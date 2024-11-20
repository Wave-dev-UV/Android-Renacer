package com.example.gestrenacer.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentStatsBinding
import com.example.gestrenacer.viewmodel.StatsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private lateinit var binding: FragmentStatsBinding
    private val statsViewModel: StatsViewModel by viewModels()  // Obtener el ViewModel con Dagger Hilt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inicializa el binding
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simulamos los datos de feligreses por mes
        val datosFeligreses = mapOf(
            "Enero" to 10,
            "Febrero" to 25,
            "Marzo" to 15,
            "Abril" to 30,
            "Mayo" to 18,
            "Junio" to 20,
            "Julio" to 22,
            "Agosto" to 13,
            "Septiembre" to 17,
            "Octubre" to 19,
            "Noviembre" to 25,
            "Diciembre" to 28
        )

        // Convertimos los datos simulados a las entradas del gráfico
        val entries = mutableListOf<BarEntry>()
        var index = 0f

        for ((mes, cantidad) in datosFeligreses) {
            // Añadimos una entrada para cada mes
            entries.add(BarEntry(index++, cantidad.toFloat()))
        }

        // Configuramos el conjunto de datos
        val dataSet = BarDataSet(entries, "Feligreses por Mes")
        dataSet.color = Color.BLUE // Color de las barras

        // Creamos el objeto de datos para el gráfico
        val barData = BarData(dataSet)

        // Configuramos el gráfico de barras usando ViewBinding
        val barChart: BarChart = binding.barChart
        barChart.data = barData
        barChart.invalidate()  // Redibujamos el gráfico

        // Configuración adicional para mejorar la legibilidad
        val meses = datosFeligreses.keys.toList()
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(meses) // Formato para mostrar los meses en el eje X
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM)
        xAxis.granularity = 1f
        barChart.axisLeft.granularity = 1f
    }
}
