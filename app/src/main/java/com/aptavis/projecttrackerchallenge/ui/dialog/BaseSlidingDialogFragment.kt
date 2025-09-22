package com.aptavis.projecttrackerchallenge.ui.dialog

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.aptavis.projecttrackerchallenge.R
import android.view.ViewGroup

abstract class BaseSlidingDialogFragment(@LayoutRes layoutId: Int) : DialogFragment(layoutId) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.App_Dialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setWindowAnimations(R.style.DialogAnim_Save)
        }
    }

    protected fun playSaveExit() {
        dialog?.window?.setWindowAnimations(R.style.DialogAnim_Save)
        dismissAllowingStateLoss()
    }

    protected fun playDeleteExit() {
        dialog?.window?.setWindowAnimations(R.style.DialogAnim_Delete)
        dismissAllowingStateLoss()
    }
}
