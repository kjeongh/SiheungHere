package com.tukorea.siheunghere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.title.*


class SuggestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)

        //다시 건의글 버튼 누르면 홈화면으로 돌아감
        title_suggestBtn.setOnClickListener() {
            finish()
        }


    }
}