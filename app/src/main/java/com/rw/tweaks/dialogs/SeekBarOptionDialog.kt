package com.rw.tweaks.dialogs

import android.os.Bundle
import android.view.View
import com.rw.tweaks.R
import com.rw.tweaks.util.getSetting
import com.rw.tweaks.util.writeSetting
import kotlinx.android.synthetic.main.seekbar_dialog.view.*
import tk.zwander.seekbarpreference.SeekBarView

class SeekBarOptionDialog : BaseOptionDialog(), SeekBarView.SeekBarListener {
    companion object {
        const val ARG_MIN = "minValue"
        const val ARG_MAX = "maxValue"
        const val ARG_UNITS = "units"
        const val ARG_DEFAULT = "defaultValue"
        const val ARG_SCALE = "scale"

        fun newInstance(key: String, min: Int = 0, max: Int = 100, default: Int = min, units: String? = null, scale: Float = 1.0f): SeekBarOptionDialog {
            return SeekBarOptionDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEY, key)
                    putInt(ARG_MIN, min)
                    putInt(ARG_MAX, max)
                    putInt(ARG_DEFAULT, default)
                    putString(ARG_UNITS, units)
                    putFloat(ARG_SCALE, scale)
                }
            }
        }
    }

    override val layoutRes = R.layout.seekbar_dialog

    private val min by lazy { arguments?.getInt(ARG_MIN, 0) ?: 0 }
    private val max by lazy { arguments?.getInt(ARG_MAX, 100) ?: 100 }
    private val default by lazy { arguments?.getInt(ARG_DEFAULT, min) ?: min }
    private val units by lazy { arguments?.getString(ARG_UNITS) }
    private val scale by lazy { arguments?.getFloat(ARG_SCALE, 1.0f) ?: 1.0f }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val progress = (view.context.getSetting(type, writeKey)?.toFloat() ?: (default * scale)) / scale

        view.seekbar_view.onBind(min, max, progress.toInt(), default, scale, units, "", this@SeekBarOptionDialog)
    }

    override fun onProgressAdded() {}
    override fun onProgressReset() {}
    override fun onProgressSubtracted() {}
    override fun onProgressChanged(newValue: Int, newScaledValue: Float) {
        requireContext().writeSetting(type, writeKey, newScaledValue)
        notifyChanged(newScaledValue)
    }
}