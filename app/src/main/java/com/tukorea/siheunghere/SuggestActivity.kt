package com.tukorea.siheunghere

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_icon_scroll.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.lakue.pagingbutton.OnPageSelectListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*
import kotlinx.android.synthetic.main.suggest_map_dialog.*


class SuggestActivity : AppCompatActivity(), OnMapReadyCallback {
    
    // 다이얼로그에서 사용할 지도 객체와 마커
    private lateinit var naverMap: NaverMap
    private val marker = Marker()

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
        val Mapdialog = MapDialog(this)
        mapEdit.setOnClickListener {
            // NaverMap 객체 얻어오기
            val fm = supportFragmentManager
            val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
                ?: MapFragment.newInstance().also {
                    fm.beginTransaction().add(R.id.map_fragment, it).commit()
                }
            mapFragment.getMapAsync(this)
            // 다이얼로그 출력
            Mapdialog.showDialog()
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



        var collection : CollectionReference = db.collection("suggests")
        var suggests = ArrayList<QueryDocumentSnapshot>()
        collection.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot>{
            fun onComplete(task : Task<QuerySnapshot>) {
                if(task.isSuccessful) {
                    for(document : QueryDocumentSnapshot in task.result) {
                        suggests.add(document)
                    }
                }
            }
        })

        var suggestListAdapter = SuggestListViewAdapter(this, suggests)
        suggestListView.adapter = suggestListAdapter


        //임시로 지정해둔 최대 페이지 번호
        var max_page = 10

        //한 번에 표시되는 버튼 수 (기본값 : 5)
        paging_btnList.setPageItemCount(4)


        //총 페이지 버튼 수와 현재 페이지 설정
        paging_btnList.addBottomPageButton(max_page, 1)

        //페이지 리스너를 클릭했을 때의 이벤트
        paging_btnList.setOnPageSelectListener(object : OnPageSelectListener {
            //PrevButton Click
            override fun onPageBefore(now_page: Int) {
                //prev 버튼클릭 - 버튼 재설정되고 그려짐
                paging_btnList.addBottomPageButton(max_page, now_page)
                //해당 페이지에 대한 소스 코드 작성
                //...
            }

            override fun onPageCenter(now_page: Int) {
                //...
            }

            //NextButton Click
            override fun onPageNext(now_page: Int) {
                //next 버튼클릭 - 버튼 재설정되고 그려짐
                paging_btnList.addBottomPageButton(max_page, now_page)
                //해당 페이지에 대한 소스 코드 작성
                //...
            }
        })
    }

    // 위치선택 다이얼로그
    inner class MapDialog(context : Context){
        // 다이얼로그 생성
        val dialog = Dialog(context)

        // 다이얼로그 띄우기
        fun showDialog() {
            dialog.show()
        }
        init {
            dialog.setContentView(R.layout.suggest_map_dialog)

            //확인 버튼
            dialog.positiveBtn.setOnClickListener {
                dialog.dismiss()
            }
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

    // 네이버 지도 준비
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        // 네이버 지도에서 선택시 마커 표시
        naverMap.setOnMapClickListener { point, coord ->
            mapEdit.setText(coord.latitude.toString() + ", " + coord.longitude.toString())
            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.icon = OverlayImage.fromResource(R.drawable.etc_location)
            marker.width = VariableOnMap.MARKER_SIZE
            marker.height = VariableOnMap.MARKER_SIZE
            marker.map = naverMap

        }

    }

}