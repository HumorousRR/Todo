package com.lrr.todo.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.lrr.todo.R

class MoreEditPopWindow(context: Context) : PopupWindow() {

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.view_more_edit, null)
        contentView.findViewById<TextView>(R.id.renameTv).setOnClickListener {
            dismiss()
        }
        contentView.findViewById<TextView>(R.id.deleteTv).setOnClickListener {
            dismiss()
        }
        isOutsideTouchable = true
        width = context.resources.getDimensionPixelSize(R.dimen.more_edit_width)
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isFocusable = true
    }
}