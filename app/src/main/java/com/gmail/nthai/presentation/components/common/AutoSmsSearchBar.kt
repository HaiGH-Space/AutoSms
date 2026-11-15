package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AutoSmsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tìm theo tên, số điện thoại..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                placeholder,
                fontSize = 13.sp,
                color = Color(0xFF888888),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF888888)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Xóa tìm kiếm", modifier = Modifier.size(16.dp))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(9999.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedBorderColor = Color(0xFF171717),
            unfocusedBorderColor = Color(0xFFEBEBEB)
        )
    )
}
