package com.tukorea.siheunghere

import com.naver.maps.map.overlay.Marker

class SharedResource {
    var marker: Marker? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
    var tel: String = ""
    var kind: String = ""
    var name: String = ""
    var address: String = ""
    var distance: Double = 0.0
    var img: String = ""

    constructor()
    constructor(lat_: Double, lng_: Double, tel_: String, kind_: String, name_: String, address_: String, distance_: Double, img_: String) {
        this.lat = lat_
        this.lng = lng_
        this.tel = tel_
        this.kind = kind_
        this.name = name_
        this.address = address_
        this.distance = distance_
        this.img = img_
    }

}