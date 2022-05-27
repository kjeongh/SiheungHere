package com.tukorea.siheunghere

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import kotlinx.android.synthetic.main.main_maplistview.view.*


class SlidingDrawerAdapter(val context : Context) : BaseAdapter(){

    var ImgID = mutableListOf<String>()
    var NameText = mutableListOf<String>()
    var AddressText = mutableListOf<String>()
    var TelText = mutableListOf<String>()
    var DistanceText = mutableListOf<String>()

    fun setList(p : List<SharedResource>){
        ImgID = mutableListOf()
        NameText = mutableListOf()
        AddressText = mutableListOf()
        TelText = mutableListOf()
        DistanceText = mutableListOf()

        val sortedResource = p.sortedWith(compareBy({it.distance}))

        for (resource in sortedResource){
            NameText.add(resource.name)
            AddressText.add(resource.address)
            TelText.add(resource.tel)
            DistanceText.add(resource.distance.toString())
        }
        notifyDataSetChanged()


    }


    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val listView: View = LayoutInflater.from(context).inflate(R.layout.main_maplistview, null)

        listView.mapImg.setImageResource(R.drawable.testimg)
        listView.mapName.text = NameText[p0]
        listView.mapAddress.text = AddressText[p0]
        listView.mapTel.text = "문의전화 : " + TelText[p0]
        listView.mapDistance.text = DistanceText[p0] + "km"

        return listView
    }

    override fun getCount(): Int {
        return NameText.size
    }

    override fun getItem(p0: Int): Any {
        return 0
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

}