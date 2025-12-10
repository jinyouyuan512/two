package com.example.healthapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "首页", Icons.Default.Home)
    object Data : BottomNavItem("data", "数据", Icons.Default.BarChart)
    object Health : BottomNavItem("health", "健康", Icons.Default.FitnessCenter)
    object Profile : BottomNavItem("profile", "我的", Icons.Default.Person)
}