package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.InventoryRepository
import com.example.data.local.InventoryDatabase
import com.example.ui.InventoryViewModel
import com.example.ui.PortgearInventoryApp
import com.example.ui.theme.PortgearInventoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = InventoryDatabase.getInstance(applicationContext)
        val repository = InventoryRepository(database.inventoryDao())

        setContent {
            PortgearInventoryTheme {
                val inventoryViewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModel.Factory(repository)
                )
                PortgearInventoryApp(viewModel = inventoryViewModel)
            }
        }
    }
}
