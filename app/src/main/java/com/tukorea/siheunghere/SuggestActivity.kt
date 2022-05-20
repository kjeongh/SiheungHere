package com.tukorea.siheunghere

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_icon_scroll.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.lakue.pagingbutton.OnPageSelectListener //페이징
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*
import kotlinx.android.synthetic.main.suggest_item.view.*


class SuggestActivity : AppCompatActivity() {

    // Firebase Firestore 연결
    val db = Firebase.firestore
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)

        firestore = FirebaseFirestore.getInstance()
        suggest_recycler.adapter = RecyclerViewAdapter()
        suggest_recycler.layoutManager = LinearLayoutManager(this)

        //다시 건의글 버튼 누르면 홈화면으로 돌아감
        title_suggestBtn.setOnClickListener() {
            finish()
        }

        //타이틀바 타이틀 버튼 - 홈 화면 이동
        title_titleBtn.setOnClickListener() {
            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }

        //새 건의글 작성
        newSuggestBtn.setOnClickListener() {
            suggestList.visibility = View.INVISIBLE
            suggestWrite.visibility = View.VISIBLE

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
            memoLimit.text = memoEdit.text.toString().length.toString() + "/100"
        }
        // 비밀번호 제한 표시 <20자>
        pwEdit.addTextChangedListener {
            pwLimit.text = pwEdit.text.toString().length.toString() + "/20"
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
                        "resourceType" to iconEdit.text.toString(),
                        "suggestAddr" to mapEdit.text.toString(),
                        "suggestReason" to memoEdit.text.toString(),
                        "password" to pwEdit.text.toString(),
                        "agreeNum" to 0
                    )
                    db.collection("suggests")
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

    }

    //건의글 리사이클러뷰 어댑터
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var suggestList : ArrayList<SuggestData> = arrayListOf()

        init {
            firestore?.collection("suggests")?.addSnapshotListener { querySnapshot, firebaseFireStoreException ->
                suggestList.clear() //suggest리스트 비워줌

                for(snapshot in querySnapshot!!.documents) { //suggestList에 데이터 추가
                    var item = snapshot.toObject(SuggestData::class.java)
                    suggestList.add(item!!)
                }
                notifyDataSetChanged() //업데이트
            }
       }

        //ViewHolder클래스가 추상클래스이므로 구체화하여 사용
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        //xml파일 inflate하여 viewHolder생성하기
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.suggest_item, parent, false)
            return ViewHolder(view)
        }

        //onCreateViewHolder에서 만든 view와 실제 데이터 연결
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
             var viewHolder = (holder as ViewHolder).itemView

            viewHolder.suggestList_addr.text = suggestList[position].suggestAddr
            viewHolder.suggestList_agreeNum.text = suggestList[position].agreeNum.toString()
        }

        override fun getItemCount(): Int {
            return suggestList.size
        }
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