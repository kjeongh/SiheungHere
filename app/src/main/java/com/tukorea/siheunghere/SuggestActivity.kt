package com.tukorea.siheunghere

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_icon_scroll.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import com.google.firebase.firestore.*
import com.lakue.pagingbutton.OnPageSelectListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*
import kotlinx.android.synthetic.main.suggest_map_dialog.*
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.suggest_item.view.*




class SuggestActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {
    
    // 다이얼로그에서 사용할 지도 객체와 마커
    private lateinit var naverMap: NaverMap
    private val marker = Marker()
    private var longitude : Double = 0.0
    private var latitude : Double = 0.0

    // Firebase Firestore 연결
    val db = Firebase.firestore
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)

        firestore = FirebaseFirestore.getInstance()
        suggest_recycler.adapter = RecyclerViewAdapter()
        suggest_recycler.layoutManager = LinearLayoutManager(this)

        //타이틀바 타이틀 버튼 - 홈 화면 이동
        title_titleBtn.setOnClickListener() {
            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }

        //상단 툴바 설정
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowCustomEnabled(true)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false) //툴바에 타이틀 안보이게
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) //툴바 메뉴버튼 생성
        //getSupportActionBar()!!.setHomeAsUpIndicator(R.drawable.icon_baseball) //메뉴 버튼 모양 설정
        //menu_navigation.setNavigationItemSelectedListener(this)

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
                        "agreeNum" to 0,
                        "latitude" to latitude,
                        "longitude" to longitude,

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

    //툴바에서 메뉴버튼 클릭시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            android.R.id.home -> {
                main_drawer_layout.openDrawer(GravityCompat.START) //메뉴드로어 열기
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_item_contact->Toast.makeText(this, "연락처", Toast.LENGTH_SHORT).show()
            R.id.menu_item_ex1->Toast.makeText(this, "메뉴1", Toast.LENGTH_SHORT).show()
            R.id.menu_item_ex2->Toast.makeText(this, "메뉴2", Toast.LENGTH_SHORT).show()
            R.id.menu_item_ex3->Toast.makeText(this, "메뉴3", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onBackPressed() { //뒤로가기 처리
        if(main_drawer_layout.isDrawerOpen(GravityCompat.START)){
            main_drawer_layout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
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
        // 현재 위치에서 시작
        naverMap.locationSource = FusedLocationSource(this, VariableOnMap.LOCATION_PERMISSTION_REQUEST_CODE)
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        // 해당 좌표 클릭 시 이벤트
        naverMap.setOnMapClickListener { point, coord ->
            //네이버 지도에서 선택시 마커 표시
            marker.position = LatLng(coord.latitude, coord.longitude)   // 좌표
            // 데이터 베이스에 저장할 좌표 설정
            latitude = coord.latitude
            longitude = coord.longitude
            marker.icon = OverlayImage.fromResource(R.drawable.etc_location)
            marker.width = VariableOnMap.MARKER_SIZE
            marker.height = VariableOnMap.MARKER_SIZE
            marker.map = naverMap
            var coords = coord.longitude.toString() + "," + coord.latitude.toString()
            
            // 좌표 -> 주소 변환 APi 실행
            RetrofitBuilder.getApiService().reverseGeo(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, coords, "json", "roadaddr,addr").enqueue(object : Callback<ReverseGeoResponse> {
                // api 호출 성공시
                override fun onResponse(call: Call<ReverseGeoResponse>, response: Response<ReverseGeoResponse>) {
                    // 위치 표시 텍스트에 주소 출력
                    mapEdit.setText(response.body().toString())
                    //Log.d("Test", response.body().toString())
                }
                // api 호출 실패시
                override fun onFailure(call: Call<ReverseGeoResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "실패", Toast.LENGTH_SHORT).show()
                }
            })

        }

    }

}