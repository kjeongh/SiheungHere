package com.tukorea.siheunghere


data class ReverseGeoResponse(
    val results: List<Result>,
    val status: Status
) {
    override fun toString(): String {
        // size가 2라면 roadaddr(도로명주소)가 있는 위치
        if (results.size == 2){
            return "${results[0].region.area1.name} ${results[0].region.area2.name} " +
                    "${results[0].region.area3.name} ${results[0].land.name}${results[0].land.number1} ${results[0].land.addition0.value}"
        }
        // size가 1 이라면 addr(좌표 to 지번 주소)로 표현
        else if (results.size == 1){
            return "${results[0].region.area1.name} ${results[0].region.area2.name} " +
                    "${results[0].region.area3.name} ${results[0].land.number1}"
        }
        else{
            return "위치 표기 오류"
        }
    }

    data class Result(
        val name: String,
        val region: Region,
        val land: Land
    ) {
        data class Land(
            val name: String,   // 도로명
            val number1: String,    // 도로명 번호
            val addition0: Addition0
        ){
            data class Addition0(
                val value : String  // 건물명
            )
        }

        data class Region(
            val area1: Area1,
            val area2: Area2,
            val area3: Area3,
            val area4: Area4
        ) {

            data class Area1(
                val name: String    // 시/도 명칭
            )
            data class Area2(
                val name: String    // 시/군/구 명칭
            )
            data class Area3(
                val name: String    // 읍/면/동 명칭
            )
            data class Area4(
                val name: String    // 리 명칭
            )
        }
    }
    // api 상태
    data class Status(
        val code: Int,
        val message: String,
        val name: String
    )
}