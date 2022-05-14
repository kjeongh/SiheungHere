package com.tukorea.siheunghere

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.suggest_item.view.*

class SuggestListViewAdapter(val context: Context, val suggestList: ArrayList<SuggestData>) : BaseAdapter() {

    override fun getCount(): Int {
        return suggestList.size
    }

    override fun getItem(p0: Int): Any {
        return suggestList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0 //사용 x
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val listView: View = LayoutInflater.from(context).inflate(R.layout.suggest_item, null)

        listView.suggestList_num.text = suggestList[p0].num.toString()
        listView.suggestList_addr.text = suggestList[p0].addr
        listView.suggestList_agreeNum.text = suggestList[p0].agreeNum.toString()

        return listView
    }

}