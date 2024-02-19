package com.blautic.sonda.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blautic.sonda.R
import com.blautic.sonda.databinding.FragmentGraficsBinding
import com.blautic.sonda.databinding.FragmentMainBinding

class GraficsFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentGraficsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGraficsBinding.inflate(inflater, container, false)
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

        // Configurar el botón de retroceso
        binding.btBack.setOnClickListener {
            // Navegar hacia atrás cuando se haga clic en el botón de retroceso
            requireActivity().onBackPressed()
        }
    }
}