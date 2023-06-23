package com.blautic.sonda.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blautic.sonda.ble.device.DeviceManager

class MainViewModelFactory(private val deviceManager: DeviceManager, private val context: Context) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `Class`.
     *
     * Default implementation throws [UnsupportedOperationException].
     *
     * @param modelClass a `Class` whose instance is requested
     * @return a newly created ViewModel
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(deviceManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}