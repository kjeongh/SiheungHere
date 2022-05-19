package com.tukorea.siheunghere


import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
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
import kotlinx.android.synthetic.main.main_slidingdrawer.*
import kotlinx.android.synthetic.main.main_title.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.tukorea.siheunghere.VariableOnMap as VM

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // < ----- 구현해야할 것 ----- >
    // < test >
    // 1. 자원을 저장할 데이터 객체(주소, 종류, 전화번호, 사진)
    // - 각 marker 아이콘 설정

    // 2. 현위치와 거리계산
    // - 위치 중심으로 그려진 원 안에 있는 자원들만 데이터베이스에서 읽어오기
    // - 이 위치에서 재검색 누르면 선택한 자원 필터는 초기화

    // 3. 그 외의 것
    // - 자원 필터링 -> 위에서 거리계산된 것들 중에서 선택한 자원만 보여주면 될듯
    // - 마커 생성 함수가 return 하는 marker는 각 객체에 변수로 저장
    // - 필터링으로 걸러지는 자원 제외, marker를 다 null로 설정(안보이게)

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
    private var eastLongtitude: Double = 0.0
    private var westLongtitude: Double = 0.0

    //데이터베이스 관련 변수
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // map fragment 불러오기
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        //navermap 준비되면 호출되는 함수
        mapFragment.getMapAsync(this)
        //현위치 받아오기
        locationSource = FusedLocationSource(this, VM.LOCATION_PERMISSTION_REQUEST_CODE)

        db = Firebase.firestore
        sharedRef = db.collection("shared")
        // 공유자원 database
        //changeAddresstoCoord()

        //타이틀바 건의글 게시판 이동 버튼
        title_suggestBtn.setOnClickListener() {
            var intent = Intent(applicationContext, SuggestActivity::class.java)
            startActivity(intent)
        }


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


        //test중 - 버튼 누르면 editText에 있는 주소를 위도, 경도로 변환해 그 위치에 마커 표시
//        TestBtn.setOnClickListener {
//            searchAddress(TestEdt.text.toString());
//        }

        // 위치 재검색 버튼
        ResearchBtn.setOnClickListener {
            cameraPos = naverMap.cameraPosition
            makeMarker(cameraPos.target, R.drawable.map_cafe)
        }

        // 검색할 반지름 거리 설정 버튼
        DistBtn1.setOnClickListener {
            makeCircle(cameraPos.target, VM.DISTANCE_1)
        }
        DistBtn2.setOnClickListener {
            makeCircle(cameraPos.target,  VM.DISTANCE_2)
        }
        DistBtn3.setOnClickListener {
            makeCircle(cameraPos.target,  VM.DISTANCE_3)
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

        val dialog = MapDialog(this)
        dialog.showDialog()
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
        makeMarker(LatLng(37.56683771710133, 126.97864942520158), R.drawable.map_badminton)
    }

    // 마커 생성 함수
    private fun makeMarker(pos: LatLng, resourceid: Int): Marker {
        val marker = Marker()
        marker.position = pos
        marker.icon = OverlayImage.fromResource(resourceid)
        marker.width = VM.MARKER_SIZE
        marker.height = VM.MARKER_SIZE
        marker.map = naverMap

        marker.onClickListener = listener
        return marker
    }

    // 지정한 위치를 중심으로 원을 그리는 함수(for 거리 계산)
    private fun makeCircle(center: LatLng, radius: Double){
        circle = CircleOverlay(center, radius)
        circle.map = naverMap
        var boundary = circle.bounds
        northLatitude = boundary.northLatitude
        southLatitude = boundary.southLatitude
        eastLongtitude = boundary.eastLongitude
        westLongtitude = boundary.westLongitude

        // 경계 위도와 경도 받기 -> 완료
        // 경계에 포함되어 있는 document만 불러오기
        // 그것만 marker 띄우기
    }

    //db에 있는 공유자원의 주소를 위도, 경도로 변환해 db에 넣음(데이터 준비, 앱 출시할 때는 없어질 코드)
    private fun changeAddresstoCoord(){
        val retrofit = RetrofitBuilder().retrofit
        //db에 있는 모든 document 가져오기
        sharedRef.get().addOnSuccessListener { result ->
            for (document in result) {
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
                            var longtitude = post!!.addresses[0].x.toDouble()
                            var latitude = post!!.addresses[0].y.toDouble()
                            val shared = hashMapOf(
                                "name" to "${document.get("name")}",
                                "kind" to "${document.get("kind")}",
                                "tel" to "${document.get("tel")}",
                                "address" to "${address}",
                                "latitude" to latitude,
                                "longtitude" to longtitude
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
        }.addOnFailureListener { exception -> }
    }

//    private fun searchAddress(query: String) {
//        val retrofit = RetrofitBuilder().retrofit
//
//        retrofit.create(NaverMapApi::class.java).searchAddress(query)
//            .enqueue(object : Callback<GeoResponse> {
//                override fun onResponse(
//                    call: Call<GeoResponse>,
//                    response: Response<GeoResponse>
//                ) {
//                    val post: GeoResponse? = response.body()
//                    longtitude = post!!.addresses[0].x.toDouble()
//                    latitude = post!!.addresses[0].y.toDouble()
//                }
//                override fun onFailure(call: Call<GeoResponse?>?, t: Throwable?) {}
//            })
//    }

}
