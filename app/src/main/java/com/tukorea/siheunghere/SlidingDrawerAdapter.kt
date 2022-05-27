package com.tukorea.siheunghere

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.main_maplistview.view.*


class SlidingDrawerAdapter(val context : Context) : BaseAdapter(){

    private var storageRef = Firebase.storage.reference
    var Img = mutableListOf<String>()
    var NameText = mutableListOf<String>()
    var AddressText = mutableListOf<String>()
    var TelText = mutableListOf<String>()
    var DistanceText = mutableListOf<String>()

    fun setList(p : List<SharedResource>){
        Img = mutableListOf()
        NameText = mutableListOf()
        AddressText = mutableListOf()
        TelText = mutableListOf()
        DistanceText = mutableListOf()

        // 거리순으로 정렬
        val sortedResource = p.sortedWith(compareBy({it.distance}))

        // 각자 리스트에 나누어서 담기
        for (resource in sortedResource){
            Img.add(resource.img)
            NameText.add(resource.name)
            AddressText.add(resource.address)
            TelText.add(resource.tel)
            DistanceText.add(resource.distance.toString())
        }
        //갱신
        notifyDataSetChanged()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        
        val listView: View = LayoutInflater.from(context).inflate(R.layout.main_maplistview, null)
        // 이미지 갱신
        storageRef.child(Img[p0]).downloadUrl.addOnSuccessListener {
            Glide.with(context).load(it).into(listView.mapImg)
        }// 다운로드 실패시 기본 no_image 출력
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