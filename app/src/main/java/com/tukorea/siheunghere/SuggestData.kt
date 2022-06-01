package com.tukorea.siheunghere

import com.google.firebase.Timestamp

//건의글
data class SuggestData(
    var docId: String = "", //문서id
    var resourceType: String = "",  //자원종류
    var suggestAddr: String = "",   //위치
    var suggestReason: String = "", //건의 이유
    var password: String = "",      //비밀번호
    var agreeNum: Int = 0,          //동의 수
    var timestamp: Timestamp? = null       //작성시간
    )