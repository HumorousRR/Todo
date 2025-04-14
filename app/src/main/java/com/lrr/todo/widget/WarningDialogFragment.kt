package com.lrr.todo.widget

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.lrr.todo.R

/**
 * @author moweiye
 * @email   moweiye@cvte.com
 * @version 1.0.0
 * @time 2023/11/6 17:00
 * @describe
 */
class WarningDialogFragment private constructor(): DialogFragment() {

    interface OnConfirmClickListener {
        fun onClick(view: View)
    }

    interface OnCancelClickListener {
        fun onClick(view: View)
    }

    private var title:String = ""
    private var summary:String = ""

    private var confirmText:String=""
    private var cancelText:String=""
    private var onConfirmClickListener :OnConfirmClickListener?=null
    private var onCancelClickListener :OnCancelClickListener?=null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 创建对话框并设置内容
        val builder = AlertDialog.Builder(requireActivity(), R.style.CommonDialog)
        val view = requireActivity().layoutInflater.inflate(R.layout.layout_warning_dialog, null)
        initView(view)
        builder.setView(view)
        return builder.create()
    }


    private fun initView(contentView: View){
        contentView.findViewById<AppCompatTextView>(R.id.tvWarningTitle).text = title
        contentView.findViewById<AppCompatTextView>(R.id.tvWarningSummary).text = summary
        contentView.findViewById<AppCompatButton>(R.id.btnCancel).let{
            it.text = cancelText
            it.setOnClickListener{
                onCancelClickListener?.onClick(it)
                dismiss()
            }
        }

        contentView.findViewById<AppCompatButton>(R.id.btnConfirm).let{
            it.text = confirmText
            it.setOnClickListener{
                onConfirmClickListener?.onClick(it)
                dismiss()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(resources.getDimensionPixelSize(R.dimen.warning_dialog_width),resources.getDimensionPixelSize(R.dimen.warning_dialog_height))
    }

    class Builder{
        private var title:String = ""
        private var summary:String = ""
        private var confirmText:String=""
        private var cancelText:String=""
        private var onConfirmClickListener :OnConfirmClickListener?=null
        private var onCancelClickListener :OnCancelClickListener?=null

        constructor(){

        }

        fun setTitle(title:String):Builder{
            this.title = title
            return  this
        }


        fun setSummary(summary:String):Builder{
            this.summary = summary
            return  this
        }

        fun setConfirmText(confirmText:String):Builder{
            this.confirmText = confirmText
            return  this
        }

        fun setCancelText(cancelText:String):Builder{
            this.cancelText = cancelText
            return  this
        }

        fun setOnConfirmClickListener(onConfirmClickListener: OnConfirmClickListener):Builder{
            this.onConfirmClickListener = onConfirmClickListener
            return  this
        }

        fun setOnCancelClickListener(onCancelClickListener: OnCancelClickListener):Builder{
            this.onCancelClickListener = onCancelClickListener
            return  this
        }

        fun builder():WarningDialogFragment{
            val inputDialogFragment = WarningDialogFragment()
            inputDialogFragment.title = title
            inputDialogFragment.summary = summary
            inputDialogFragment.confirmText = confirmText
            inputDialogFragment.cancelText = cancelText
            inputDialogFragment.onConfirmClickListener = onConfirmClickListener
            inputDialogFragment.onCancelClickListener = onCancelClickListener
            return inputDialogFragment
        }


    }
}