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


}
