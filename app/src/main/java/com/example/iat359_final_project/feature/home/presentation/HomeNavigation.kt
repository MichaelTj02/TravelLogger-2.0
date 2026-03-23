package com.example.iat359_final_project.feature.home.presentation

sealed interface HomeNavigation {
    data object None : HomeNavigation
    data object OpenLogs : HomeNavigation
    data object OpenMap : HomeNavigation
    data object OpenTracking : HomeNavigation
    data object OpenWeatherSearch : HomeNavigation
}
