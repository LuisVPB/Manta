package com.blautic.sonda.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.blautic.sonda.R
import com.blautic.sonda.databinding.FragmentMainBinding
import com.blautic.sonda.viewModel.MainViewModel
import com.blautic.sonda.viewModel.MainViewModelFactory
import com.diegulog.ble.gatt.ConnectionState
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentMainBinding? = null
    private lateinit var mainViewModelFactory: MainViewModelFactory
    private lateinit var viewModel: MainViewModel //by activityViewModels()

    private val exportExcelActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            data?.let {
                viewModel.generatedExcel(
                    requireContext(),
                    it.data!!,
                    "resultados"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        mainViewModelFactory = MainViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)

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

        binding.tvVersion.text = "versión: ${viewModel.getAppVersion(requireContext())}"
        Log.d("info", viewModel.getAppVersion(requireContext()))

        // Configurar respuestas a clic
        binding.switchCapture.setOnCheckedChangeListener { buttonView, isChecked ->

            viewModel.capturandoDatos = isChecked
            Log.d("info", "selector de captura pasa a: $isChecked")

            if (viewModel.capturandoDatos) {

                Log.d("info", "NO recolectando datos, Se procede a exportar los datos guardados hasta ahora")

                binding.switchCapture.apply {
                    thumbDrawable.setColorFilter(
                        resources.getColor(R.color.teal_700),
                        PorterDuff.Mode.MULTIPLY
                    )

                    trackDrawable.setColorFilter(
                        resources.getColor(R.color.teal_700),
                        PorterDuff.Mode.MULTIPLY
                    )
                }

                // Activo la recogida de datos recogidos en excel:
                viewModel.subirFase()
                viewModel.collectDataExp(requireContext())
                //binding.btCaptura.setImageResource(R.drawable.ic_xls)

            } else {

                Log.d("info", "Recolectando datos")

                binding.switchCapture.apply {
                    thumbDrawable.clearColorFilter()
                    trackDrawable.clearColorFilter()
                }

                viewModel.userCode = binding.etUsuario.text.toString()
                Log.d("info", "usuario recogido: ${viewModel.userCode}")

                // Activo la exportación a excel:
                viewModel.resetFase()
                viewModel.startExport(exportExcelActivityResult)
                //binding.btCaptura.setImageResource(R.drawable.ic_collect)
            }

        }

        binding.btSubirFase.setOnClickListener {
            if (viewModel.capturandoDatos){
                viewModel.subirFase()
                Log.d("fase", viewModel.numFase.toString())
            }
        }

        binding.btConnect.setOnClickListener {
            var estadoConex = viewModel.conected
            if(estadoConex) {
                viewModel.disconnect("77:77:77:77:77:77")
            } else {
                viewModel.connect("77:77:77:77:77:77")
            }
        }

        binding.btCharts.setOnClickListener {

            val bundle = bundleOf()
            NavHostFragment.findNavController(this).navigate(R.id.graficsFragment, bundle)

        }

        // Mostrar estado de batería:
        lifecycleScope.launch {
            viewModel.statusFlow().collect {
                binding.ivBattery.setImageResource(viewModel.getBatteryLevelDrawable(it))
                binding.tvBattery.text = "${it?:""}%"
            }

        }

        // Mostrar valores de sensores de presión:
        lifecycleScope.launch {

            viewModel.presionFlow().collect {
                binding.apply {
                    presSensor1.text= "P1: ${String.format("%.1f", it?.get(0)?: 0F)} %"
                    presSensor2.text= "P2: ${String.format("%.1f", it?.get(1)?: 0F)} %"
                    presSensor3.text= "P3: ${String.format("%.1f", it?.get(2)?: 0F)} %"
                    presSensor4.text= "P4: ${String.format("%.1f", it?.get(3)?: 0F)} %"
                    presSensor5.text= "P5: ${String.format("%.1f", it?.get(4)?: 0F)} %"
                    presSensor6.text= "P6: ${String.format("%.1f", it?.get(5)?: 0F)} %"
                    presSensor7.text= "P7: ${String.format("%.1f", it?.get(6)?: 0F)} %"
                }
            }
        }

        // Mpu (por ahora no se usa este flow)
        lifecycleScope.launch {
            viewModel.mpuFlow().collect {

            }
        }

        // Mostrar valores de angulos:
        lifecycleScope.launch {
            viewModel.anglesFlow().collect {

                binding.percentFlex.text = "${(it?.getOrNull(0)?.roundToInt()?.absoluteValue ?: 0)}º"
                binding.percentIncl.text = "${(it?.getOrNull(1)?.roundToInt()?.absoluteValue ?: 0)}º"

                binding.progressFlex.setPolarProgress(it?.getOrNull(0)?.roundToInt() ?: 0)
                binding.progressIncl.setPolarProgress(it?.getOrNull(1)?.roundToInt() ?: 0)
            }
        }

        // Mostrar estado de conexión:
        viewModel.connectionState().observeForever {
            var estado = it
            binding.progressConnection.isVisible = it == ConnectionState.CONNECTING
            binding.tvConectando.isVisible = it == ConnectionState.CONNECTING

            when(it){
                ConnectionState.DISCONNECTED -> {
                    binding.switchCapture.isEnabled = false
                    binding.btCharts.isEnabled = false
                    binding.ivConexion.setColorFilter(Color.parseColor("#CF1313"))
                    binding.ivBattery.setImageResource(viewModel.getBatteryLevelDrawable(null))
                    binding.btConnect.text = "Conectar"
                    binding.tvBattery.text = ""
                }
                ConnectionState.CONNECTING -> {
                    Log.i("conexion","conectando......")
                }
                ConnectionState.CONNECTED -> {
                    binding.switchCapture.isEnabled = true
                    binding.btCharts.isEnabled = true
                    binding.ivConexion.setColorFilter(Color.parseColor("#4CAF50"))
                    binding.btConnect.text = "Desconectar"
                }
                ConnectionState.DISCONNECTING -> {

                }
                ConnectionState.FAILED -> {
                    //binding.tvConexion.text = "disconnected"
                    binding.ivConexion.setColorFilter(Color.parseColor("#CF1313"))
                    binding.btConnect.text = "Conectar"
                    binding.tvBattery.text = ""
                    Toast.makeText(requireContext(), "se ha perdido la conexión con el dispositivo", Toast.LENGTH_LONG).show()
                }

            }
        }

        // Configurar espacio de texto para identificador de paciente:
        binding.etUsuario.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Quitar el foco al EditText cuando se presione "Hecho"
                    this.clearFocus()
                    // Oculta el teclado virtual
                    val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)

                    true
                } else {
                    false
                }
            }


        }


    }

    private fun CircularProgressIndicator.setPolarProgress(value: Int) {
        if (value < 0) {
            indicatorDirection = CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE
        } else {
            indicatorDirection = CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
        }
        progress =  (value.absoluteValue * 100) / 180
    }
}