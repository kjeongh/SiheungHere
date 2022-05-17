package com.tukorea.siheunghere

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.main_maplistview.view.*
import kotlinx.android.synthetic.main.suggest_item.view.*

class SuggestListViewAdapter(val context: Context, val suggestList: ArrayList<QueryDocumentSnapshot>) : BaseAdapter() {

    val listView: View = LayoutInflater.from(context).inflate(R.layout.suggest_item, null)

   override fun getCount(): Int {
        return suggestList.size
    }

    override fun getItem(p0: Int): Any {
        return suggestList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0 //사용 x
    }


    fun getDataList() : ArrayList<QueryDocumentSnapshot> {
        return suggestList
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val suggestListView: View = LayoutInflater.from(context).inflate(R.layout.main_maplistview, null)

        suggestListView.suggestList_num.text = getDataList().get(0).toString()
        suggestListView.suggestList_addr.text = getDataList().get(0).toString()
        suggestListView.suggestList_agreeNum.text = getDataList().get(0).toString()

        return listView
    }

}