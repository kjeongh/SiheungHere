package com.tukorea.siheunghere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_slidingdrawer.*
import com.tukorea.siheunghere.VariableOnMap as VM

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // < ----- 구현해야할 것 ----- >
    // 1. 자원을 저장할 데이터 객체(주소, 종류, 전화번호, 사진) -> 생각나면 더 적기
    // - 주소 -> 좌표 변환
    // - 각 marker 아이콘 설정

    // 2. 현위치와 거리계산
    // - 각 자원과 현위치의 거리를 return
    // - 그 중 일정거리 내에 있는 것들만
    // - 지도 카메라도 그 자원들을 다 비추기 위해 멀어지기

    // 3. 그 외의 것
    // - 자원 필터링 -> 위에서 거리계산된 것들 중에서 선택한 자원만 보여주면 될듯

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var scrollBar: LinearLayout//

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            map_view.onCreate(savedInstanceState)
            scrollBar = findViewById<LinearLayout>(R.id.iconScrollBar)
            scrollBar.bringToFront()

            // onMapReady() 콜백 메서드가 호출
            map_view.getMapAsync(this)

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


        }

    override fun onStart() {
        super.onStart()
        map_view.onStart()
    }
    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }
    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map_view.onSaveInstanceState(outState)
    }
    override fun onStop() {
        super.onStop()
        map_view.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
        map_view.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        map_view.onLowMemory()
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

        val marker = Marker()
        marker.position = LatLng(37.56683771710133, 126.97864942520158)
        marker.icon = OverlayImage.fromResource(R.drawable.map_badminton)
        marker.width = VM.MARKER_SIZE
        marker.height = VM.MARKER_SIZE
        marker.map = naverMap

        marker.onClickListener = listener
    }
}