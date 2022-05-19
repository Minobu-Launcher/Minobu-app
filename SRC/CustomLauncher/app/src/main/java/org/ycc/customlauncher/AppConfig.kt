package org.ycc.customlauncher

data class AppConfig(
        var app: List<App> = listOf<App>(),
        var back_image: String = "",
        var group_logo: String = "",
        var help_1: String = "",
        var help_2: String = "",
        var help_3: String = "",
        var help_4: String = "",
        var help_5: String = "",
        var welcome: String = ""
)