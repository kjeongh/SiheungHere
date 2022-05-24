package com.tukorea.siheunghere

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.main_item_point.*

class MapDialog(context : Context, sharedResource: SharedResource) {

    private val dialog = Dialog(context)   //부모 액티비티의 context 가 들어감
    fun showDialog()
    {
        dialog.setContentView(R.layout.main_item_point)
        dialog.show()
        var window : Window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.info_CloseBtn.setOnClickListener {
            dialog.dismiss()
        }
    }
}