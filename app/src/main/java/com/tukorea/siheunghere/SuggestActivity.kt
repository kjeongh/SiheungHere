package com.tukorea.siheunghere

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_icon_scroll.*
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*
import okio.utf8Size


class SuggestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)

        // Firebase Firestore 연결
        val db = Firebase.firestore

        //다시 건의글 버튼 누르면 홈화면으로 돌아감
        title_suggestBtn.setOnClickListener() {
            finish()
        }
        
        // 자원 아이콘 선택 다이얼로그
        val dialog = IconDialog(this)   // 건의글 작성 화면으로 변경될 때 실행하도록 추후 변경
        iconEdit.setOnClickListener {
            dialog.showDialog()
        }
        // 위치 선택 다이얼로그
        mapEdit.setOnClickListener {

        }
        // 건의 내용 제한 표시 <100자>
        memoEdit.addTextChangedListener {
            memoLimit.text = memoEdit.text.toString().utf8Size().toString() + "/100"
        }
        // 비밀번호 제한 표시 <20자>
        pwEdit.addTextChangedListener {
            pwLimit.text = pwEdit.text.toString().utf8Size().toString() + "/20"
        }
        // 현재 작성 내용 파이어베이스 제출
        submitBtn.setOnClickListener {
            // 작성되지 않은 칸이 있는지 확인
            if (iconEdit.text.toString().equals("") ||
                memoEdit.text.toString().replace(" ", "").equals("") ||
                pwEdit.text.toString().replace(" ", "").equals("") ){
                Toast.makeText(this, "모두 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }
            // 모두 작성 완료 시
            else{
                // 최종 확인 다이얼로그
                var dlg = AlertDialog.Builder(this)
                dlg.setMessage("제출 하시겠습니까?")
                dlg.setNegativeButton("취소", null)
                dlg.setPositiveButton("제출"){ dialog, which ->
                    // hashMap 생성해서 Firestore 제출
                    val suggest = hashMapOf(
                        "자원종류" to iconEdit.text.toString(),
                        "위치" to mapEdit.text.toString(),
                        "건의사유" to memoEdit.text.toString(),
                        "비밀번호" to pwEdit.text.toString(),
                        "추천수" to 0,
                    )
                    db.collection("suggest")
                        .add(suggest)
                        .addOnSuccessListener { documentReference ->
                            // 제출 성공 시
                            Toast.makeText(this, "건의 글이 제출되었습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            // 제출 실패시
                            Toast.makeText(this, "제출 실패", Toast.LENGTH_SHORT).show()
                            Log.w("FIREBASE", "Error adding document", e)
                        }
                    // 액티비티 새로고침?
                    var intent = getIntent()
                    finish()
                    startActivity(intent)
                }
                // 다이얼로그 출력
                dlg.show()
            }

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

    // 자원선택 스크롤 다이얼로그 띄우고 iconEdit 변경
    inner class IconDialog(context : Context) {
        // 다이얼로그 생성
        private val dialog = Dialog(context)
        
        // 다이얼로그 띄우기
        fun showDialog() {
            dialog.show()
        }

        // 생성자
        init {
            dialog.setContentView(R.layout.main_icon_scroll)    // 뷰설정
            // id 배열
            var iconId = arrayOf(
                dialog.icon_badminton, dialog.icon_baseball, dialog.icon_cafe, dialog.icon_classroom, dialog.icon_experience,
                dialog.icon_cooling_center, dialog.icon_futsal, dialog.icon_parking, dialog.icon_gallery, dialog.icon_livingsport,
                dialog.icon_meeting, dialog.icon_park, dialog.icon_practice_room, dialog.icon_soccer_field, dialog.icon_theater,
                dialog.icon_toilet, dialog.icon_wifi
            )
            // text 배열
            var iconText = arrayOf(
                "배드민턴장", "야구장", "카페", "강의실", "체험/견학",
                "무더위쉼터", "풋살장", "주차장", "갤러리/공방", "생활체육시설",
                "회의실", "공원", "연습실/학원", "잔디구장", "공연장",
                "화장실", "와이파이"
            )
            // 버튼 클릭 이벤트 생성
            for (i in 0..iconId.size - 1){
                iconId[i].setOnClickListener{
                    iconEdit.setText(iconText[i])
                    dialog.dismiss()
                }
            }
        }

    }

}