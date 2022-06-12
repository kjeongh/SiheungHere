package com.tukorea.siheunghere

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DialogMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialogMapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var v : View
    private lateinit var parent : ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        v = view
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DialogMapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DialogMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("TAG", "onMapReady() called : fragment in dialog")
        var marker : Marker = Marker()
        var latitude : Double
        var longitude : Double

        latitude = arguments?.getDouble("latitude")!!
        longitude = arguments?.getDouble("longitude")!!

        marker.position = LatLng(latitude, longitude)   // 좌표
        marker.icon = OverlayImage.fromResource(R.drawable.map_point)
        marker.width = VariableOnMap.MARKER_SIZE
        marker.height = VariableOnMap.MARKER_SIZE
        marker.map = naverMap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parent  = v.getParent() as ViewGroup
        if(parent!=null) {
            parent.removeView(v)
        }
    }
}