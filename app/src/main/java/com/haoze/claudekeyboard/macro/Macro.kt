package com.haoze.claudekeyboard.macro

import java.util.UUID

/**
 * Data class representing a macro button configuration.
 */
data class Macro(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val command: String,
    val description: String = "",
    val isPreset: Boolean = false,
    val sortOrder: Int = 0,
    val sendEnter: Boolean = false
) {
    companion object {
        /**
         * Create a preset macro.
         */
        fun preset(label: String, command: String, sortOrder: Int, description: String = "", sendEnter: Boolean = false): Macro {
            return Macro(
                id = "preset_${label.replace("/", "_")}",
                label = label,
                command = command,
                description = description,
                isPreset = true,
                sortOrder = sortOrder,
                sendEnter = sendEnter
            )
        }

        /**
         * Create a custom macro.
         */
        fun custom(label: String, command: String, description: String = "", sendEnter: Boolean = false): Macro {
            return Macro(
                label = label,
                command = command,
                description = description,
                isPreset = false,
                sortOrder = 1000, // Custom macros come after presets
                sendEnter = sendEnter
            )
        }
    }
}