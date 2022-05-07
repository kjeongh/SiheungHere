package com.tukorea.siheunghere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
<<<<<<< HEAD
=======
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.naver.maps.geometry.LatLng
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
<<<<<<< HEAD
import com.naver.maps.map.util.FusedLocationSource

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private val LOCATION_PERMISSTION_REQUEST_CODE: Int = 1000
    private lateinit var locationSource: FusedLocationSource // 위치를 반환하는 구현체
    private lateinit var naverMap: NaverMap
=======
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
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

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var dialogView : View
    val infoWindow = InfoWindow()
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
<<<<<<< HEAD

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSTION_REQUEST_CODE)
=======
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        // onMapReady() 콜백 메서드가 호출
        mapView.getMapAsync(this)

        //현위치 받아오기
        locationSource = FusedLocationSource(this, VM.LOCATION_PERMISSTION_REQUEST_CODE)

>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
<<<<<<< HEAD

=======
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
<<<<<<< HEAD

=======
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
<<<<<<< HEAD

=======
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
<<<<<<< HEAD

=======
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
<<<<<<< HEAD

=======
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
<<<<<<< HEAD

=======
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

<<<<<<< HEAD
    override fun onMapReady(p0: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
=======
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
>>>>>>> 19a0f867c256056dc710305dfae221cf8c04d61f
    }
}