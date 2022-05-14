package com.tukorea.siheunghere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*


class SuggestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)

        //다시 건의글 버튼 누르면 홈화면으로 돌아감
        title_suggestBtn.setOnClickListener() {
            finish()
        }

        val suggestList = ArrayList<SuggestData>(5)
        suggestList.add(SuggestData(10,"경기도 시흥시 새재로 19윤성빌딩 5층 502호",1000))
        suggestList.add(SuggestData(11,"경기도 시흥시 새재로 19윤성빌딩 5층 502호",1020))
        suggestList.add(SuggestData(12,"경기도 시흥시 새재로 19윤성빌딩 5층 502호",1230))
        suggestList.add(SuggestData(13,"경기도 시흥시 새재로 19윤성빌딩 5층 502호",4320))
        suggestList.add(SuggestData(14,"경기도 시흥시 새재로 19윤성빌딩 5층 502호",1520))




        val suggestListAdapter = SuggestListViewAdapter(this, suggestList)
        suggestListView.adapter = suggestListAdapter

    }
}