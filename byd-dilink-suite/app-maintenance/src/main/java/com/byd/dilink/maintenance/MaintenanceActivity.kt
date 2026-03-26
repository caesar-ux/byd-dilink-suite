package com.byd.dilink.maintenance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.maintenance.navigation.MaintenanceNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MaintenanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiLinkTheme {
                MaintenanceNavHost()
            }
        }
    }
}
