package com.gmail.nthai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onImportExcel: () -> Unit,
    onSelectSim: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("AutoSms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeActionCard(
                title = "Nhập danh sách Excel",
                subtitle = "Thêm khách hàng và số điện thoại từ file Excel",
                onClick = onImportExcel,
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
            )
            HomeActionCard(
                title = "Danh sách gửi SIM 1",
                subtitle = "Chọn khách hàng và số điện thoại dành riêng cho SIM 1",
                onClick = { onSelectSim(1) },
                icon = { Icon(Icons.Default.SimCard, contentDescription = null) }
            )
            HomeActionCard(
                title = "Danh sách gửi SIM 2",
                subtitle = "Chọn khách hàng và số điện thoại dành riêng cho SIM 2",
                onClick = { onSelectSim(2) },
                icon = { Icon(Icons.Default.SimCard, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Mở")
            }
        }
    }
}
