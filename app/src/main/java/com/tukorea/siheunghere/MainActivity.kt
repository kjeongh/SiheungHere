package com.tukorea.siheunghere


import android.content.Intent
import android.icu.lang.UCharacter.IndicPositionalCategory.RIGHT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity.RIGHT
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets.Side.RIGHT
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Barrier.RIGHT
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_slidingdrawer.*
import kotlinx.android.synthetic.main.main_title.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.tukorea.siheunghere.VariableOnMap as VM

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {

    // < ----- 구현해야할 것 ----- >
    // < test >
    // 1. 자원을 저장할 데이터 객체(주소, 종류, 전화번호, 사진) -> 생각나면 더 적기
    // - 각 marker 아이콘 설정

    // 2. 현위치와 거리계산
    // - 각 자원과 현위치의 거리를 return
    // - 그 중 일정거리 내에 있는 것들만
    // - 이 위치에서 재검색 누르면 선택한 자원 필터는 초기화

    // 3. 그 외의 것
    // - 자원 필터링 -> 위에서 거리계산된 것들 중에서 선택한 자원만 보여주면 될듯
    // - 마커 생성 함수가 return 하는 marker는 각 객체에 변수로 저장
    // - 필터링으로 걸러지는 자원 제외, marker를 다 null로 설정(안보이게)

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)


        //상단 툴바 설정
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowCustomEnabled(true)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false) //툴바에 타이틀 안보이게
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) //툴바 메뉴버튼 생성
        //getSupportActionBar()!!.setHomeAsUpIndicator(R.drawable.icon_baseball) //메뉴 버튼 모양 설정 - 오류
        menu_navigation.setNavigationItemSelectedListener(this)

        //타이틀바 건의글 게시판 이동 버튼
        title_suggestBtn.setOnClickListener() {
            var intent = Intent(applicationContext, SuggestActivity::class.java)
            startActivity(intent)
        }

        // map fragment 불러오기
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        // 공유자원 database
        val db = Firebase.firestore
        db.collection("shared").get().addOnSuccessListener { result ->
            for (document in result) {
                var address = document.get("address")
                //각 document는 id를 불러올 수 있다. 이 id를 활용해 주소 -> 위도, 경도 변환한 것을 데이터베이스에 넣자
                document.id
            }
        }
            .addOnFailureListener { exception ->
            }

        //navermap 준비되면 호출되는 함수
        mapFragment.getMapAsync(this)

        //현위치 받아오기
        locationSource = FusedLocationSource(this, VM.LOCATION_PERMISSTION_REQUEST_CODE)


        //슬라이딩 드로어 화살표 변경
        slidingdrawer.setOnDrawerOpenListener {
            handle.setImageResource(R.drawable.etc_arrow_down)
        }
        slidingdrawer.setOnDrawerCloseListener {
            handle.setImageResource(R.drawable.etc_arrow_up)
        }
        // 테스트용
        // 슬라이딩 드로어 어댑터 설정
        val mapAdaptor = SlidingDrawerAdapter(this)
        mapListView.adapter = mapAdaptor


        //test - 버튼 누르면 editText에 있는 주소를 위도, 경도로 변환해 그 위치에 마커 표시
        TestBtn.setOnClickListener {
            searchAddress(TestEdt.text.toString());
        }

        ResearchBtn.setOnClickListener {
            val camerapos = naverMap.cameraPosition
            makeMarker(camerapos.target.latitude, camerapos.target.longitude, R.drawable.map_cafe)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        //현위치 요청 결과 코드
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨 -> 지도 TrackingMode를 None으로 설정
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {                             // 권한 승인됨 -> 지도 TrackingMode를 Follow로 설정
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Marker ClickListener 구현 -> Marker 클릭 시 상세정보 Dialog 창
    val listener = Overlay.OnClickListener { overlay ->
        // Marker를 인자로 받아서 그 위치로 어떤 자원인지 구분
        val marker = overlay as Marker

        val dialog = MapDialog(this)
        dialog.showDialog()
        true
    }

    //NaverMap 객체가 준비되면 호출되는 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.minZoom = 8.0
        makeMarker(37.56683771710133, 126.97864942520158, R.drawable.map_badminton)
    }

    //마커 생성 함수
    private fun makeMarker(latitude : Double, longtitude: Double, resourceid: Int): Marker {
        val marker = Marker()
        marker.position = LatLng(latitude, longtitude)
        marker.icon = OverlayImage.fromResource(resourceid)
        marker.width = VM.MARKER_SIZE
        marker.height = VM.MARKER_SIZE
        marker.map = naverMap

        marker.onClickListener = listener
        return marker
    }

    private fun searchAddress(query: String) {
        val retrofit = RetrofitBuilder.getApiService()
            .searchAddress(query)
            .enqueue(object : Callback<GeoResponse> {
                override fun onResponse(
                    call: Call<GeoResponse>,
                    response: Response<GeoResponse>
                ) {
                    val post: GeoResponse? = response.body()
                    val longtitude = post!!.addresses[0].x.toDouble()
                    val latitude = post!!.addresses[0].y.toDouble()
                    makeMarker(latitude, longtitude, R.drawable.map_toilet)
                }

                override fun onFailure(call: Call<GeoResponse?>?, t: Throwable?) {}
            })
    }

}
