package com.tukorea.siheunghere

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_icon_scroll.*
import kotlinx.android.synthetic.main.main_item_point.*
import kotlinx.android.synthetic.main.main_slidingdrawer.*
import kotlinx.android.synthetic.main.main_title.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.*
import com.tukorea.siheunghere.VariableOnMap as VM

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {

    // < ----- 구현해야할 것 ----- >
    // < test >
    // 1. 자원을 저장할 데이터 객체(주소, 종류, 전화번호, 사진) -> 완료
    // - 각 marker 아이콘 설정 -> 완료

    // 2. 현위치와 거리계산
    // - 위치 중심으로 그려진 원 안에 있는 자원들만 데이터베이스에서 읽어오기 -> 완료
    // - 이 위치에서 재검색 누르면 선택한 자원 필터는 초기화 -> 완료

    // 3. 그 외의 것
    // - 자원 필터링 -> 위에서 거리계산된 것들 중에서 선택한 자원만 보여주면 될듯 -> test 전
    // - 마커 생성 함수가 return 하는 marker는 각 객체에 변수로 저장 -> 완료
    // - 필터링으로 걸러지는 자원 제외, marker를 다 null로 설정(안보이게) -> 완료

    // 4. 마커 다이얼로그
    // - firebase storage에서 사진 불러오기
    // dialog에 정보 넣기

    // 5. 시청에서 데이터 주면 변환해서 firestore에 넣기

    // 지도 관련 변수
    private lateinit var naverMap: NaverMap                     // 지도 객체
    private lateinit var uiSettings : UiSettings                // 지도 UI 세팅 객체
    private lateinit var locationSource: FusedLocationSource    // 현위치 중심
    private lateinit var circle: CircleOverlay                  // 현위치 or 지도중심점(이 위치에서 재검색 할 경우) 중심으로 그려질 원(거리 계산)
    private lateinit var cameraPos: CameraPosition              // 지도 중심점 위치(LatLng)

    // 공유자원 검색 관련 변수
    // 특정 위치를 중심으로 한 경계 좌표들
    private var northLatitude: Double = 0.0
    private var southLatitude: Double = 0.0
    private var eastLongitude: Double = 0.0
    private var westLongitude: Double = 0.0

    // 사용자가 설정하는 검색할 반경 거리(default: 1km)
    private var distance: Double = VM.DISTANCE_1

    //데이터베이스 관련 변수
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedRef: CollectionReference
    private var sharedList = mutableListOf<SharedResource>()
    private var filteredList = mutableListOf<SharedResource>()
    private lateinit var dialog : Dialog


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

        // 레이아웃 관련 변수 설정
        val scrollIcons = arrayOf(icon_badminton, icon_baseball, icon_cafe, icon_classroom, icon_park, icon_cooling_center,
            icon_experience, icon_futsal, icon_gallery, icon_livingsport, icon_meeting, icon_parking,
            icon_practice_room, icon_soccer_field, icon_theater, icon_toilet, icon_wifi)

        // 마커 다이얼로그 설정
        dialog = Dialog(this)
        dialog.setContentView(R.layout.main_item_point)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        //navermap 준비되면 호출되는 함수
        mapFragment.getMapAsync(this)

        //현위치 받아오기
        locationSource = FusedLocationSource(this, VM.LOCATION_PERMISSTION_REQUEST_CODE)

        // 데이터베이스 관련 변수 설정
        db = Firebase.firestore
        sharedRef = db.collection("shared")


        // 주소를 좌표로 변환해서 database에 넣기
        // changeAddresstoCoord()

        //타이틀바 건의글 게시판 이동 버튼
        title_suggestBtn.setOnClickListener() {
            var intent = Intent(applicationContext, SuggestActivity::class.java)
            startActivity(intent)
        }

        // 테스트용
        // 슬라이딩 드로어 어댑터 설정
        val mapAdaptor = SlidingDrawerAdapter(this)
        mapListView.adapter = mapAdaptor

        //슬라이딩 드로어
        slidingdrawer.setOnDrawerOpenListener {
            // 화살표 변경
            handle.setImageResource(R.drawable.etc_arrow_down)
        }
        slidingdrawer.setOnDrawerCloseListener {
            handle.setImageResource(R.drawable.etc_arrow_up)
        }



        //test - 버튼 누르면 editText에 있는 주소를 위도, 경도로 변환해 그 위치에 마커 표시

/*        test중 - 버튼 누르면 editText에 있는 주소를 위도, 경도로 변환해 그 위치에 마커 표시
        TestBtn.setOnClickListener {
            searchAddress(TestEdt.text.toString());
        }*/

        // 위치 재검색 버튼
        ResearchBtn.setOnClickListener {
            // 지도 중심 좌표 get & 반경 그리기
            cameraPos = naverMap.cameraPosition
            makeCircle(cameraPos.target, distance)

            // 위도 또는 경도 범위에 해당하는 데이터만 담기
            var latResult = mutableSetOf<DocumentSnapshot>()
            var lngResult = mutableSetOf<DocumentSnapshot>()
            // 검색하기 전 리스트에 있던 자원 지우기 & 초기화
            if (sharedList != null) {
                for (sharedresource in sharedList) {
                    sharedresource.marker?.map = null
                }
                sharedList = mutableListOf()
            }
            // 범위 안에 있는 데이터 찾는 query(동서남북 경계 이용)
            sharedRef.whereGreaterThan("latitude", southLatitude)
                .whereLessThan("latitude", northLatitude).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    latResult.add(document)
                }
                if (lngResult != null) {
                    makeResultList(latResult, lngResult)
                }
            }
            sharedRef.whereGreaterThan("longitude", westLongitude)
                .whereLessThan("longitude", eastLongitude).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    lngResult.add(document)
                }
                if (latResult != null) {
                    makeResultList(latResult, lngResult)
                    // 슬라이딩 드로어 리스트 어댑터 갱신
                    mapAdaptor.setList(sharedList)
                }
            }
        }



        // 검색할 반지름 거리 설정 버튼
        DistBtn1.setOnClickListener {
            distance = VM.DISTANCE_1
            ResearchBtn.callOnClick()
        }
        DistBtn2.setOnClickListener {
            distance = VM.DISTANCE_2
            ResearchBtn.callOnClick()
        }
        DistBtn3.setOnClickListener {
            distance = VM.DISTANCE_3
            ResearchBtn.callOnClick()
        }

        // 아이콘 버튼(필터링 기능) clicklistener 정의
        for (i in scrollIcons.indices) {
            scrollIcons[i].setOnClickListener {
                filteredList = mutableListOf()
                var iconName = scrollIcons[i].toString().split("/")[1].replace("}", "")
                iconName = iconName.replace("icon_", "")
                for (sharedresource in sharedList) {
                    if (sharedresource.kind == iconName) {
                        sharedresource.marker?.map = naverMap
                        filteredList.add(sharedresource)
                    } else {
                        sharedresource.marker?.map = null
                    }
                }
                // 슬라이딩 드로어 리스트 어댑터 갱신
                mapAdaptor.setList(filteredList)
                // 아이콘 버튼 클릭 리스너 넣을 것 !

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

    //네비게이션 아이템 클릭시 동작
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

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //현위치 요청 결과 코드
        if (locationSource.onRequestPermissionsResult( requestCode, permissions,grantResults )) {
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
        lateinit var clickedResource: SharedResource
        for (sharedresource in sharedList) {
            if(sharedresource.lat == marker.position.latitude && sharedresource.lng == marker.position.longitude)
                clickedResource = sharedresource
        }
        showDialog(clickedResource)
        true
    }

    // NaverMap 객체가 준비되면 호출되는 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        this.uiSettings = naverMap.uiSettings

        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        uiSettings.isLocationButtonEnabled = true
        naverMap.minZoom = VM.MIN_ZOOM
    }

    // 마커 생성 함수
    private fun makeMarker(pos: LatLng, resourceId: Int): Marker {
        val marker = Marker()
        marker.position = pos
        marker.icon = OverlayImage.fromResource(resourceId)
        marker.width = VM.MARKER_SIZE
        marker.height = VM.MARKER_SIZE
        marker.map = naverMap

        marker.onClickListener = listener
        return marker
    }

    // 지정한 위치를 중심으로 원을 그리는 함수(for 거리 계산)
    private fun makeCircle(center: LatLng, radius: Double){
        circle = CircleOverlay(center, radius)
        circle.map = null
        var boundary = circle.bounds
        northLatitude = boundary.northLatitude
        southLatitude = boundary.southLatitude
        eastLongitude = boundary.eastLongitude
        westLongitude = boundary.westLongitude
    }

    // 검색해서 나온 근처 공유자원 결과를 리스트에 넣는 함수
    private fun makeResultList(latResult: MutableSet<DocumentSnapshot>, lngResult: MutableSet<DocumentSnapshot>){
        var wholeResult = latResult.intersect(lngResult)
        for (doc in wholeResult) {
            var lat = doc.get("latitude").toString().toDouble()
            var lng = doc.get("longitude").toString().toDouble()
            var tel = doc.get("tel").toString()
            var kind = doc.get("kind").toString()
            var name = doc.get("name").toString()
            var address = doc.get("address").toString()
            var icon = resources.getIdentifier("map_" + kind, "drawable", packageName)
            var distance = getDistance(cameraPos.target.latitude, cameraPos.target.longitude, lat, lng).toDouble() / 1000
            var img = doc.get("index").toString()+".png"

            var sharedItem = SharedResource(lat, lng, tel, kind, name, address, distance, img)
            sharedItem.marker = makeMarker(LatLng(lat, lng), icon)

            sharedList.add(sharedItem)

        }
    }

    // dialog 띄우는 함수
    private fun showDialog(clickResource: SharedResource){
        dialog.info_icon.setImageResource(resources.getIdentifier("icon_" + clickResource.kind, "drawable", packageName))
        dialog.info_title.text = clickResource.name
        dialog.info_tel.text = clickResource.tel
        dialog.info_addr.text = clickResource.address
        dialog.info_dist.text = clickResource.distance.toString() + "km"
        dialog.show()
        dialog.info_CloseBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    // 거리 계산하는 함수
    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
        val c = 2 * asin(sqrt(a))
        return (VM.R * c).toInt()
    }

    //db에 있는 공유자원의 주소를 위도, 경도로 변환해 db에 넣음(데이터 준비, 앱 출시할 때는 없어질 코드)
    private fun changeAddresstoCoord(){
        val retrofit = RetrofitBuilder.getRetrofit()
        //db에 있는 모든 document 가져오기
        sharedRef.get().addOnSuccessListener { result ->
            for (document in result) {
                Log.d(VM.TAG, "${document.id} => ${document.data}")
                var address = document.get("address")
                //각 document의 주소를 불러와 searchAddress 함수를 호출해 주소 변환
                var docId = document.id
                retrofit.create(NaverMapApi::class.java).searchAddress(address as String)
                    .enqueue(object : Callback<GeoResponse> {
                        override fun onResponse(
                            call: Call<GeoResponse>,
                            response: Response<GeoResponse>
                        ) {
                            val post: GeoResponse? = response.body()
                            var longitude = post!!.addresses[0].x.toDouble()
                            var latitude = post!!.addresses[0].y.toDouble()
                            val shared = hashMapOf(
                                "name" to "${document.get("name")}",
                                "kind" to "${document.get("kind")}",
                                "tel" to "${document.get("tel")}",
                                "address" to "${address}",
                                "latitude" to latitude,
                                "longitude" to longitude
                            )
                            //위도와 경도까지 추가된 document를 덮어쓰기
                            db.collection("shared").document("${docId}")
                                .set(shared)
                                .addOnSuccessListener { Log.d("db", "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w("db", "Error writing document", e) }
                        }
                        override fun onFailure(call: Call<GeoResponse?>?, t: Throwable?) {}
                    })
            }
        }.addOnFailureListener { exception -> Log.w(VM.TAG, "Error getting documents: ", exception) }
    }


/*    private fun searchAddress(query: String) {
        val retrofit = RetrofitBuilder().retrofit

        retrofit.create(NaverMapApi::class.java).searchAddress(query)
    private fun searchAddress(query: String) {
        val retrofit = RetrofitBuilder.getApiService()
            .searchAddress(query)
            .enqueue(object : Callback<GeoResponse> {
                override fun onResponse(
                    call: Call<GeoResponse>,
                    response: Response<GeoResponse>
                ) {
                    val post: GeoResponse? = response.body()
                    longitude = post!!.addresses[0].x.toDouble()
                    latitude = post!!.addresses[0].y.toDouble()
                }
                override fun onFailure(call: Call<GeoResponse?>?, t: Throwable?) {}
            })
    }*/
}
