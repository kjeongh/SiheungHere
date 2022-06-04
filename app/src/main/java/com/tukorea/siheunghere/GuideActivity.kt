package com.tukorea.siheunghere

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.guide_activity.*


class GuideActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guide_activity)

        var layouts = arrayOf(R.layout.guide_page, R.layout.guide_page)

        Viewpager.adapter = ViewPagerAdapter(layouts)   // 어댑터 생성
        Viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL   // 가로방향


    }
    inner class ViewPagerAdapter(pageList: Array<Int>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
        var item = pageList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder(parent, viewType)

        override fun getItemCount(): Int = 4

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {

        }

        inner class PagerViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder
            (LayoutInflater.from(parent.context).inflate(R.layout.guide_page, parent, false)){
            val v = itemView
        }
    }

}