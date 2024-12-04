package com.unchain.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.fragment.app.Fragment

fun Fragment.showLoading(): Dialog {
    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(com.unchain.R.layout.dialog_loading)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(false)
    dialog.show()
    return dialog
}

fun Dialog.hideLoading() {
    if (isShowing) {
        dismiss()
    }
}
