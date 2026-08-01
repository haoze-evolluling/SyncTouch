package com.haoze.claudekeyboard.ui.settings

sealed class SettingsItem {
    data class SectionHeader(val title: String) : SettingsItem()
    data class SwitchItem(
        val key: String,
        val title: String,
        val subtitle: String? = null,
        val defaultValue: Boolean
    ) : SettingsItem()
    data class SliderItem(
        val key: String,
        val title: String,
        val subtitle: String? = null,
        val defaultValue: Int,
        val min: Float,
        val max: Float,
        val stepSize: Float
    ) : SettingsItem()
    data class ButtonItem(
        val title: String,
        val subtitle: String? = null,
        val onClick: () -> Unit = {}
    ) : SettingsItem()
    data class ToggleGroupItem(
        val key: String,
        val title: String,
        val options: List<String>,
        val defaultIndex: Int
    ) : SettingsItem()
    data class InfoItem(
        val label: String,
        val value: String,
        val onClick: (() -> Unit)? = null
    ) : SettingsItem()
}