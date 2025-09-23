package com.aptavis.projecttrackerchallenge.ui.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.aptavis.projecttrackerchallenge.R
import com.aptavis.projecttrackerchallenge.domain.model.Status

object StatusUi {

    data class Chip(
        @ColorRes val bg: Int,
        @ColorRes val fg: Int,
        @DrawableRes val dot: Int,
        val label: String
    )

    fun chip(status: Status): Chip = when (status) {
        Status.Draft -> Chip(
            bg = R.color.status_draft_bg,
            fg = R.color.brand_onPrimary,
            dot = R.drawable.circle_gray,
            label = "Draft"
        )
        Status.InProgress -> Chip(
            bg = R.color.status_inprogress_bg,
            fg = R.color.brand_onPrimary,
            dot = R.drawable.circle_orange,
            label = "In Progress"
        )
        Status.Done -> Chip(
            bg = R.color.status_done_bg,
            fg = R.color.brand_onPrimary,
            dot = R.drawable.circle_green,
            label = "Done"
        )
    }
}
