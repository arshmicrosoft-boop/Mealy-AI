package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.screens.onboarding.FirstLaunchExperienceScreen
import com.example.ui.viewmodel.KitchenViewModel

@Composable
fun AuthScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    FirstLaunchExperienceScreen(
        viewModel = viewModel,
        modifier = modifier
    )
}
