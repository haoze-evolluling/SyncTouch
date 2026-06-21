package com.haoze.claudekeyboard.ui.settings

import android.content.SharedPreferences
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.haoze.claudekeyboard.R

class SettingsAdapter(
    private val prefs: SharedPreferences,
    private val onToggleGroupChanged: (key: String, index: Int) -> Unit,
    private val onButtonClick: (SettingsItem.ButtonItem) -> Unit,
    private val onSwitchChanged: (key: String, value: Boolean) -> Unit = { _, _ -> }
) : ListAdapter<SettingsItem, RecyclerView.ViewHolder>(SettingsItemDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SWITCH = 1
        private const val TYPE_SLIDER = 2
        private const val TYPE_BUTTON = 3
        private const val TYPE_TOGGLE_GROUP = 4
        private const val TYPE_INFO = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingsItem.SectionHeader -> TYPE_HEADER
            is SettingsItem.SwitchItem -> TYPE_SWITCH
            is SettingsItem.SliderItem -> TYPE_SLIDER
            is SettingsItem.ButtonItem -> TYPE_BUTTON
            is SettingsItem.ToggleGroupItem -> TYPE_TOGGLE_GROUP
            is SettingsItem.InfoItem -> TYPE_INFO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_settings_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_SWITCH -> {
                val view = inflater.inflate(R.layout.item_settings_switch, parent, false)
                SwitchViewHolder(view)
            }
            TYPE_SLIDER -> {
                val view = inflater.inflate(R.layout.item_settings_slider, parent, false)
                SliderViewHolder(view)
            }
            TYPE_BUTTON -> {
                val view = inflater.inflate(R.layout.item_settings_button, parent, false)
                ButtonViewHolder(view)
            }
            TYPE_TOGGLE_GROUP -> {
                val view = inflater.inflate(R.layout.item_settings_toggle_group, parent, false)
                ToggleGroupViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_settings_info, parent, false)
                InfoViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SettingsItem.SectionHeader -> (holder as HeaderViewHolder).bind(item)
            is SettingsItem.SwitchItem -> (holder as SwitchViewHolder).bind(item)
            is SettingsItem.SliderItem -> (holder as SliderViewHolder).bind(item)
            is SettingsItem.ButtonItem -> (holder as ButtonViewHolder).bind(item)
            is SettingsItem.ToggleGroupItem -> (holder as ToggleGroupViewHolder).bind(item)
            is SettingsItem.InfoItem -> (holder as InfoViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_section_header)

        fun bind(item: SettingsItem.SectionHeader) {
            tvTitle.text = item.title
        }
    }

    inner class SwitchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_switch_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_switch_subtitle)
        private val switch: MaterialSwitch = itemView.findViewById(R.id.switch_item)

        fun bind(item: SettingsItem.SwitchItem) {
            tvTitle.text = item.title
            if (item.subtitle != null) {
                tvSubtitle.text = item.subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }

            switch.setOnCheckedChangeListener(null)
            switch.isChecked = prefs.getBoolean(item.key, item.defaultValue)
            switch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(item.key, isChecked).apply()
                onSwitchChanged(item.key, isChecked)
            }
        }
    }

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_slider_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_slider_subtitle)
        private val tvValue: TextView = itemView.findViewById(R.id.tv_slider_value)
        private val slider: Slider = itemView.findViewById(R.id.slider_item)

        fun bind(item: SettingsItem.SliderItem) {
            tvTitle.text = item.title
            if (item.subtitle != null) {
                tvSubtitle.text = item.subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }

            slider.valueFrom = item.min
            slider.valueTo = item.max
            slider.stepSize = item.stepSize

            val currentValue = prefs.getInt(item.key, item.defaultValue)
            slider.value = currentValue.toFloat()
            tvValue.text = currentValue.toString()

            slider.clearOnChangeListeners()
            slider.addOnChangeListener { _, value, _ ->
                val intVal = value.toInt()
                tvValue.text = intVal.toString()
                prefs.edit().putInt(item.key, intVal).apply()
            }
        }
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_button_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_button_subtitle)
        private val root: View = itemView.findViewById(R.id.btn_item_root)

        fun bind(item: SettingsItem.ButtonItem) {
            tvTitle.text = item.title
            if (item.subtitle != null) {
                tvSubtitle.text = item.subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }
            root.setOnClickListener { onButtonClick(item) }
        }
    }

    inner class ToggleGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val toggleGroup: MaterialButtonToggleGroup = itemView.findViewById(R.id.toggle_group)

        fun bind(item: SettingsItem.ToggleGroupItem) {
            toggleGroup.removeAllViews()
            toggleGroup.clearOnButtonCheckedListeners()

            val context = itemView.context
            val currentIndex = prefs.getInt(item.key, item.defaultIndex)

            item.options.forEachIndexed { index, option ->
                val button = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    text = option
                    id = View.generateViewId()
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    textSize = 11f
                }
                toggleGroup.addView(button)
                if (index == currentIndex) {
                    toggleGroup.check(button.id)
                }
            }

            toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener
                val selectedIndex = toggleGroup.indexOfChild(toggleGroup.findViewById(checkedId))
                prefs.edit().putInt(item.key, selectedIndex).apply()
                onToggleGroupChanged(item.key, selectedIndex)
            }
        }
    }

    inner class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_info_label)
        private val tvValue: TextView = itemView.findViewById(R.id.tv_info_value)
        private val root: View = itemView.findViewById(R.id.info_item_root)

        fun bind(item: SettingsItem.InfoItem) {
            tvLabel.text = item.label
            tvValue.text = item.value
            if (item.onClick != null) {
                val outValue = TypedValue()
                root.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                root.setBackgroundResource(outValue.resourceId)
                root.isClickable = true
                root.setOnClickListener { item.onClick() }
            } else {
                root.background = null
                root.isClickable = false
                root.setOnClickListener(null)
            }
        }
    }

    private class SettingsItemDiffCallback : DiffUtil.ItemCallback<SettingsItem>() {
        override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
            return when {
                oldItem is SettingsItem.SectionHeader && newItem is SettingsItem.SectionHeader ->
                    oldItem.title == newItem.title
                oldItem is SettingsItem.SwitchItem && newItem is SettingsItem.SwitchItem ->
                    oldItem.key == newItem.key
                oldItem is SettingsItem.SliderItem && newItem is SettingsItem.SliderItem ->
                    oldItem.key == newItem.key
                oldItem is SettingsItem.ButtonItem && newItem is SettingsItem.ButtonItem ->
                    oldItem.title == newItem.title
                oldItem is SettingsItem.ToggleGroupItem && newItem is SettingsItem.ToggleGroupItem ->
                    oldItem.key == newItem.key
                oldItem is SettingsItem.InfoItem && newItem is SettingsItem.InfoItem ->
                    oldItem.label == newItem.label
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
            return oldItem == newItem
        }
    }
}