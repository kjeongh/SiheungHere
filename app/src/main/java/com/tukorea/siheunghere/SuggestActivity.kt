package com.tukorea.siheunghere

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_icon_scroll.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import retrofit2.Call
import com.google.firebase.firestore.*
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.main_title.*
import kotlinx.android.synthetic.main.suggest_activity.*
import kotlinx.android.synthetic.main.suggest_detail_dialog.*
import kotlinx.android.synthetic.main.suggest_item.*
import kotlinx.android.synthetic.main.suggest_map_dialog.*
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.suggest_item.view.*
import okhttp3.internal.notifyAll
import kotlin.collections.ArrayList


class SuggestActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {
    
    // 다이얼로그에서 사용할 지도 객체와 마커
    private lateinit var naverMap: NaverMap
    private val marker = Marker()
    private var longitude : Double = 0.0
    private var latitude : Double = 0.0

    // Firebase Firestore 연결
    private val db = Firebase.firestore
    private var firestore : FirebaseFirestore? = null

    //상세정보 다이얼로그
    private lateinit var suggestDialog : Dialog

    //카카오 로그인 데이터
    lateinit var kakao_id : String
    lateinit var kakao_nickname : String
    lateinit var kakao_email : String
    lateinit var kakao_gender : String
    lateinit var kakao_age_range : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.suggest_activity)
        // kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_key))
        var keyHash = Utility.getKeyHash(this)
        Log.d("TAG", getString(R.string.kakao_native_key))



        // 카카오 로그인 버튼
        LoginBtn.setOnClickListener {
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("TAG", "카카오계정으로 로그인 실패", error)
                    Toast.makeText(this, "카카오계정으로 로그인 실패", Toast.LENGTH_SHORT).show()
                } else if (token != null) {
                    Log.i("TAG", "카카오계정으로 로그인 성공 ${token.accessToken}")
                    getLoginData()
                    kakaoLogin.visibility = View.INVISIBLE
                    suggestList.visibility = View.VISIBLE
                }
            }
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("TAG", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        Log.i("TAG", "카카오톡으로 로그인 성공 ${token.accessToken}")
                        getLoginData()
                        kakaoLogin.visibility = View.INVISIBLE
                        suggestList.visibility = View.VISIBLE
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        // 레이아웃 관련 변수 설정
        var scrollIcons = arrayOf(icon_wifi, icon_cooling_center, icon_park, icon_toilet, icon_parking, icon_badminton, icon_baseball,
            icon_cafe, icon_classroom, icon_experience, icon_futsal, icon_gallery, icon_livingsport, icon_meeting,
            icon_practice_room, icon_soccer_field, icon_theater)

        var sharedTypeName = arrayOf("와이파이", "무더위쉼터", "공원", "화장실", "주차장", "배드민턴장",
            "야구장", "카페", "강의실", "체험/견학", "풋살장", "갤러리/공방",
            "생활체육시설", "회의실", "연습실/학원", "잔디구장", "공연장")

        firestore = FirebaseFirestore.getInstance()

        var recyclerAdapter = RecyclerViewAdapter("wifi") //메인 - wifi부터 보여줌

        suggest_recycler.adapter = recyclerAdapter
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
        getSupportActionBar()!!.setHomeAsUpIndicator(R.mipmap.ic_home) //메뉴 버튼 모양 설정

        //새 건의글 작성
        newSuggestBtn.setOnClickListener() {
            suggestList.visibility = View.INVISIBLE
            suggestWrite.visibility = View.VISIBLE
        }

        //건의글 자원별 필터링
        for (i in scrollIcons.indices) {
            scrollIcons[i].setOnClickListener {
                suggest_recycler.adapter = RecyclerViewAdapter(sharedTypeName[i]) //어댑터 재설정
                recyclerAdapter.notifyDataSetChanged()
            }
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

        // 현재 작성 내용 파이어베이스 제출
        submitBtn.setOnClickListener {
            // 작성되지 않은 칸이 있는지 확인
            if (iconEdit.text.toString().equals("") ||
                memoEdit.text.toString().replace(" ", "").equals("") ){
                Toast.makeText(this, "모두 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }
            // 모두 작성 완료 시
            else{
                // 최종 확인 다이얼로그
                var dlg = AlertDialog.Builder(this)
                dlg.setMessage("제출된 건의글은 수정할 수 없습니다.\n제출 하시겠습니까?")
                dlg.setNegativeButton("취소", null)
                dlg.setPositiveButton("제출"){ dialog, which ->
                    // hashMap 생성해서 Firestore 제출
                    val suggest = hashMapOf(
                        "resourceType" to iconEdit.text.toString(),
                        "suggestAddr" to mapEdit.text.toString(),
                        "suggestReason" to memoEdit.text.toString(),
                        "agreeNum" to 0,
                        "latitude" to latitude,
                        "longitude" to longitude,
                        "timestamp" to Timestamp.now()
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
                            Toast.makeText(this, "제출 실패 : 인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
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

    // 카카오 로그인 데이터 불러오기
    fun getLoginData(){
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("TAG", "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                Log.i("TAG", "사용자 정보 요청 성공")
                // 변수 저장
                kakao_id = "${user.id}"
                kakao_nickname = "${user.kakaoAccount?.profile?.nickname}"
                kakao_email = "${user.kakaoAccount?.email}"
                kakao_gender = "${user.kakaoAccount?.gender}"
                kakao_age_range = "${user.kakaoAccount?.ageRange}"
                Log.i("TAG", "$kakao_id, $kakao_nickname, $kakao_email, $kakao_gender, $kakao_age_range")

                // 사용자 뷰 ID 표시
                kakaoIdText1.text = "ID : ${kakao_id}\nEmail : ${kakao_email}"
                kakaoIdText2.text = "ID : ${kakao_id}\nEmail : ${kakao_email}"
            }
        }
    }

    //툴바에서 메뉴버튼 클릭시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            android.R.id.home -> {
                finish() //메인화면 이동
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_item_contact1-> startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:031-310-2114")))
            R.id.menu_item_contact2-> startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:031-380-5350")))
            R.id.menu_item_ex1->Toast.makeText(this, "임시 메뉴1", Toast.LENGTH_SHORT).show()
            R.id.menu_item_ex2->Toast.makeText(this, "임시 메뉴2", Toast.LENGTH_SHORT).show()
            R.id.menu_item_ex3->Toast.makeText(this, "임시 메뉴3", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onBackPressed() { //뒤로가기 처리
        if(suggestWrite.visibility == View.VISIBLE) {


            var dlg = AlertDialog.Builder(this)
            dlg.setMessage("작성을 취소하시겠습니까?")
            dlg.setNegativeButton("이어 쓰기", null)
            dlg.setPositiveButton("작성 취소") { dlg, which ->

                //나갔다 들어오면 초기화
                iconEdit.setText("")
                mapEdit.setText("")
                memoEdit.setText("")

                suggestWrite.visibility = View.INVISIBLE
                suggestList.visibility = View.VISIBLE
            }
            dlg.show()

        }
        else {
            finish() //main화면으로 돌아감
        }

    }

    //건의글 리사이클러뷰 어댑터 - 건의글 나열
    inner class RecyclerViewAdapter(type : String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        //건의글 배열
        private var suggestList : ArrayList<SuggestData> = arrayListOf()
        private var context : Context = this@SuggestActivity

        init { //메인 화면 - 전체보기

            firestore?.collection("suggests")?.addSnapshotListener { querySnapshot, firebaseFireStoreException ->
                suggestList.clear() //suggest리스트 비워줌

                if (querySnapshot != null) {
                    for(snapshot in querySnapshot.documents) { //suggestList에 데이터 추가
                        var item = snapshot.toObject(SuggestData::class.java)
                        // 문서 id 추가
                        item!!.docId = snapshot.id
                        if(item!!.resourceType == type) { // 선택한 자원과 일치하는 경우에만 배열에 add
                            suggestList.add(item!!)
                        }
                    }
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

            var sortedSuggestList = suggestList.sortedBy { it.timestamp }

            suggestList = ArrayList(sortedSuggestList.reversed()) //최신글을 제일 위로
            viewHolder.suggestList_addr.text = suggestList[position].suggestAddr
            viewHolder.suggestList_agreeNum.text = suggestList[position].agreeNum.toString()
            viewHolder.suggestList_num.text = (position+1).toString()
            viewHolder.setOnClickListener(){ //클릭시 다이얼로그 띄우기
                var dlg = suggestDetailDialog(context, suggestList[position])
                dlg.showDialog()
            }
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

    inner class suggestDetailDialog(context : Context, suggestItem : SuggestData) {
        private var dialog = Dialog(context)

        fun showDialog() {
            dialog.show()

        }
        init {
            dialog.setContentView(R.layout.suggest_detail_dialog)
            dialog.suggestDetail_txtAddr.text = suggestItem.suggestAddr
            dialog.suggestDetail_reason.text = suggestItem.suggestReason

            dialog.detail_agreeBtn.setOnClickListener{


                var dlg = AlertDialog.Builder(context)
                dlg.setMessage("동의하시겠습니까? 한 번 동의하면 취소할 수 없습니다.")
                dlg.setNegativeButton("취소", null)
                dlg.setPositiveButton("동의") { dlg, which ->
                    var map= mutableMapOf<String,Any>()
                    map["agreeNum"] = suggestItem.agreeNum + 1
                    db.collection("suggests").document(suggestItem.docId).update(map)

                    dialog.dismiss()
                    Toast.makeText(context, "이 게시글에 동의하였습니다", Toast.LENGTH_SHORT).show()

                }
                dlg.show()
            }
            dialog.detail_closeBtn.setOnClickListener{
                dialog.dismiss() //닫기
            }
            // 삭제버튼
            dialog.detail_deleteBtn.setOnClickListener {
                // 최종 확인 다이얼로그
                var dlg = AlertDialog.Builder(context)
                dlg.setMessage("삭제 하시겠습니까?")
                dlg.setNegativeButton("취소", null)
                dlg.setPositiveButton("제출") { dlg, which ->
                    db.collection("suggests").document(suggestItem.docId).delete()
                    dialog.dismiss()
                }
                dlg.show()
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