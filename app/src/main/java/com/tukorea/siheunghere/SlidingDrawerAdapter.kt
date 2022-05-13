package com.tukorea.siheunghere

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import kotlinx.android.synthetic.main.main_maplistview.view.*


class SlidingDrawerAdapter(val context : Context) : BaseAdapter(){

    var ImgID = arrayOf(
        R.drawable.testimg, R.drawable.testimg, R.drawable.testimg, R.drawable.testimg, R.drawable.testimg
    )
    var NameText = arrayOf(
        "(ABC행복학습타운) 모임터(210호)", "(ABC행복학습타운) 모임터(210호)", "(ABC행복학습타운) 모임터(210호)", "(ABC행복학습타운) 모임터(210호)", "(ABC행복학습타운) 모임터(210호)"
    )
    var AddressText = arrayOf(
        "경기도 시흥시 소래산길 11", "경기도 시흥시 소래산길 11", "경기도 시흥시 소래산길 11", "경기도 시흥시 소래산길 11", "경기도 시흥시 소래산길 11", "경기도 시흥시 소래산길 11"
    )
    var PhoneText = arrayOf(
        "문의전화 : 031-310-6008", "문의전화 : 031-310-6008", "문의전화 : 031-310-6008", "문의전화 : 031-310-6008", "문의전화 : 031-310-6008", "문의전화 : 031-310-6008"
    )
    var KmText = arrayOf(
        "0.5 km", "1.0 km", "1.5 km", "2.0 km", "2.5 km"
    )


    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val listView: View = LayoutInflater.from(context).inflate(R.layout.main_maplistview, null)

        listView.mapImg.setImageResource(ImgID[p0])
        listView.mapName.text = NameText[p0]
        listView.mapAddress.text = AddressText[p0]
        listView.mapPhone.text = PhoneText[p0]
        listView.mapKm.text = KmText[p0]

        return listView
    }

    override fun getCount(): Int {
        return ImgID.size
    }

    override fun getItem(p0: Int): Any {
        return 0
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

}