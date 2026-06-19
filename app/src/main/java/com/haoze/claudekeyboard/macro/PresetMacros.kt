package com.haoze.claudekeyboard.macro

import android.content.Context
import com.haoze.claudekeyboard.R

/**
 * Preset macros for Claude Code CLI.
 */
object PresetMacros {

    /**
     * Get the list of preset macros.
     */
    fun getPresets(context: Context): List<Macro> {
        return listOf(
            Macro.preset("/clear", "/clear", 1, context.getString(R.string.preset_clear_desc), sendEnter = true),
            Macro.preset("/compact", "/compact", 2, context.getString(R.string.preset_compact_desc), sendEnter = true),
            Macro.preset("/model", "/model", 3, context.getString(R.string.preset_model_desc), sendEnter = true),
            Macro.preset("/btw", "/btw", 4, context.getString(R.string.preset_btw_desc), sendEnter = true)
        )
    }
}
