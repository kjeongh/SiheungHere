package com.tukorea.siheunghere

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SuggestActivity : AppCompatActivity() {

    private lateinit var titleBar_suggestBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)

        titleBar_suggestBtn = findViewById<ImageButton>(R.id.title_suggestBtn)

        titleBar_suggestBtn.setOnClickListener() {
            finish()
        }


    }
}