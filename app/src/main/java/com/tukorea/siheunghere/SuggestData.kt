package com.tukorea.siheunghere

import com.google.firebase.Timestamp

//건의글
data class SuggestData(
    var docId: String = "",                 //문서id
    var kakaoUserId: String = "",           //카카오 유저 id
    var resourceType: String = "",          //자원종류
    var suggestAddr: String = "",           //위치
    var suggestReason: String = "",         //건의 이유
    var agreeNum: Int = 0,                  //동의 수
    var timestamp: Timestamp? = null,       //작성시간
    var latitude: Double = 0.0,             //위도
    var longitude: Double = 0.0,            //경도
    var agreeId: List<String?> = listOf("")    //동의한 사용자 목록
    )