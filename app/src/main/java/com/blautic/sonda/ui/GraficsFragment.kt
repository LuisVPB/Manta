package com.blautic.sonda.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blautic.sonda.databinding.FragmentGraficsBinding
import com.blautic.sonda.viewModel.MainViewModel
import com.blautic.sonda.viewModel.MainViewModelFactory
import kotlinx.coroutines.launch

class GraficsFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentGraficsBinding? = null
    private val viewModel: MainViewModel by activityViewModels { MainViewModelFactory(requireContext().applicationContext) }
    /*private lateinit var mainViewModelFactory: MainViewModelFactory
    private lateinit var viewModel: MainViewModel*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGraficsBinding.inflate(inflater, container, false)
        /*mainViewModelFactory = MainViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)*/
        return binding.root
    }

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // Configurar el botón de retroceso
        binding.btBack.setOnClickListener {
            // Navegar hacia atrás cuando se haga clic en el botón de retroceso
            requireActivity().onBackPressed()
        }

        // configurar gráficos
        binding.apply {

            plcSensor1.setLabelSensorName(" Sensor de presión-1")
            //plcSensor1.setScale(100f)
            plcSensor1.setMaxScale(100)
            plcSensor1.setMinScale(0)
            plcSensor2.setLabelSensorName(" Sensor de presión-2")
            plcSensor3.setLabelSensorName(" Sensor de presión-3")
            plcSensor4.setLabelSensorName(" Sensor de presión-4")
            plcSensor5.setLabelSensorName(" Sensor de presión-5")
            plcSensor6.setLabelSensorName(" Sensor de presión-6")
            plcSensor7.setLabelSensorName(" Sensor de presión-7")
        }

        lifecycleScope.launch {
            viewModel.presionFlow().collect() {
                Log.d("graficos", (it?.get(0)?: "nah").toString())
                binding.run {
                    plcSensor1.addEntryLineChart(it?.get(0)?:0f)
                }
            }
        }


    }
}