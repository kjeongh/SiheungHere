package com.tukorea.siheunghere

//지도 액티비티에서 사용할 변수를 정의하는 곳
object VariableOnMap {
    const val MARKER_SIZE = 120                                 // Marker 설정 크기
    const val LOCATION_PERMISSTION_REQUEST_CODE = 1000          // 현위치 요청 코드
    const val MIN_ZOOM = 8.0                                    // 줌 아웃 최대치
    const val DISTANCE_1 = 500.0                                // 검색할 반지름 거리 값(m)
    const val DISTANCE_2 = 1000.0
    const val DISTANCE_3 = 1500.0
    const val TAG = "Firestore"
    const val R = 6372.8 * 1000
}