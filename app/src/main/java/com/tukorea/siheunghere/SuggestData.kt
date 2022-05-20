package com.tukorea.siheunghere
//건의글
data class SuggestData (
    var resourceType: String? = null, //자원종류
    var suggestAddr: String? = null, //위치
    var suggestReason: String? = null, //건의 이유
    var password: String? = null, //비밀번호
    var agreeNum: Int //동의 수
    )