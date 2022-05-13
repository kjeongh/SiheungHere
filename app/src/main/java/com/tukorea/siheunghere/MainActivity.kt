package com.tukorea.siheunghere

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import android.widget.LinearLayout
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.icon_scroll.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.main_slidingdrawer.*
import com.tukorea.siheunghere.VariableOnMap as VM


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // < ----- 구현해야할 것 ----- >
    // 1. 자원을 저장할 데이터 객체(주소, 종류, 전화번호, 사진) -> 생각나면 더 적기

    // 2. 현위치와 거리계산
    // - 각 자원과 현위치의 거리를 return(mysql은 현위치 반경 특정 거리 이내 데이터만 뽑을 수 있음)
    // - 그 중 일정거리 내에 있는 것들만
    // - 지도 카메라도 그 자원들을 다 비추기 위해 멀어지기(시간 되면 구현 필수 X)
    // - 지도를 움직인 뒤, 지도의 중심을 기반으로도 검색 가능하게끔(네이버지도 이 위치에서 재검색과 동일한 기능)

    // 3. 자원 필터링
    // - 위에서 거리계산된 것들 중에서 선택한 자원만 보여주면 될듯

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var scrollBar: LinearLayout//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scrollBar = findViewById<LinearLayout>(R.id.iconScrollBar)
        scrollBar.bringToFront()

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }


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
      //test중 - 버튼 누르면 editText에 있는 주소를 위도, 경도로 변환해 그 위치에 마커 표시
//        TestBtn.setOnClickListener {
//            searchAddress(TestEdt.text.toString());
//        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //현위치 요청 결과 코드
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨 -> 지도 TrackingMode를 None으로 설정
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            else {                             // 권한 승인됨 -> 지도 TrackingMode를 Follow로 설정
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

        makeMarker(37.56683771710133, 126.97864942520158, R.drawable.map_badminton)
    }

    //마커 생성 함수
    private fun makeMarker(latitude : Double, longtitude: Double, resourceid: Int) {
        val marker = Marker()
        marker.position = LatLng(latitude, longtitude)
        marker.icon = OverlayImage.fromResource(resourceid)
        marker.width = VM.MARKER_SIZE
        marker.height = VM.MARKER_SIZE
        marker.map = naverMap

        marker.onClickListener = listener
    }
    private fun searchAddress(query: String) {
        val retrofit = RetrofitBuilder().retrofit

        retrofit.create(NaverMapApi::class.java).searchAddress(query)
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