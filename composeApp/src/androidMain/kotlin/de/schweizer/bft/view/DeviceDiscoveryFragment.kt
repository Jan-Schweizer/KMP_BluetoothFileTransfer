package de.schweizer.bft.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import de.schweizer.bft.view.DeviceDiscoveryScreen

class DeviceDiscoveryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(inflater.context).apply {
            setContent {
                DeviceDiscoveryScreen()
            }
        }
    }
}