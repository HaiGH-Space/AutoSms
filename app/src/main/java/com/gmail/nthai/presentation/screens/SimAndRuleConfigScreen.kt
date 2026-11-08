package com.gmail.nthai.presentation.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.presentation.components.config.RuleAndUpdateTab as ComponentRuleAndUpdateTab
import com.gmail.nthai.presentation.components.config.SimManagementTab as ComponentSimManagementTab
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@Composable
fun SimManagementTab(
    viewModel: ConfigViewModel = hiltViewModel(),
    showTopBar: Boolean = false
) {
    ComponentSimManagementTab(viewModel = viewModel, showTopBar = showTopBar)
}

@Composable
fun RuleAndUpdateTab(
    viewModel: ConfigViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
    showTopBar: Boolean = false
) {
    ComponentRuleAndUpdateTab(viewModel = viewModel, updateViewModel = updateViewModel, showTopBar = showTopBar)
}
