package com.example.application.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.application.R
import com.example.application.activities.MainActivity
import com.example.application.adapters.FacilityInfoWindowAdapter
import com.example.application.databases.FacilityDatabaseHelper
import com.example.application.structs.googlemap.Facility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.OnSuccessListener

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var filterBtn: LinearLayout
    private lateinit var filterView: TextView

    private lateinit var facilityDatabaseHelper: FacilityDatabaseHelper

    private val markers: MutableList<Marker> = mutableListOf()

    private fun getResizedBitmapDescriptor(resourceId: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context?.resources, resourceId)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 72, 72, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }
    private fun getResizedBitmapDescriptor(resourceId: Int, size: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context?.resources, resourceId)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    private var cachedZoom: Int = 14
    private var reloading: Boolean = false

    private lateinit var categoryToDrawableMap9f: Map<String, BitmapDescriptor>
    private lateinit var categoryToDrawableMap12f: Map<String, BitmapDescriptor>
    private lateinit var categoryToDrawableMap14f: Map<String, BitmapDescriptor>

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        filterBtn = view.findViewById(R.id.filterBtn)
        filterView = view.findViewById(R.id.filterView)

        filterBtn.setOnClickListener {
            reloading = true
            for (marker in markers) marker.remove()
            showLocationFilterDialog()
            reloading = false
        }

        facilityDatabaseHelper = FacilityDatabaseHelper(mainActivity)
        return view
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        cachedZoom = 14
        // 마커 클릭시 시설 정보창 띄우기
        mMap.setOnMarkerClickListener { marker ->
            val facility = marker.tag as? Facility
            if (facility != null) {
                marker.showInfoWindow()
            }
            true // 클릭 이벤트 소비
        }

        // 시설 정보창 어댑터
        mMap.setInfoWindowAdapter(FacilityInfoWindowAdapter(requireContext()))

        // 시설 정보창 다시 클릭시 띄울 세부정보창
        mMap.setOnInfoWindowClickListener { marker ->
            val facility = marker.tag as? Facility
            if (facility != null) {
                // 시설 세부 정보 창 띄우기
                showDetailedFacilityInfo(facility)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        categoryToDrawableMap14f = mapOf(
            "동물약국" to getResizedBitmapDescriptor(R.drawable.icon_map_pharmacy),
            "미술관" to getResizedBitmapDescriptor(R.drawable.icon_map_artrium),
            "카페" to getResizedBitmapDescriptor(R.drawable.icon_map_caffee),
            "동물병원" to getResizedBitmapDescriptor(R.drawable.icon_map_hospital),
            "반려동물용품" to getResizedBitmapDescriptor(R.drawable.icon_map_shop),
            "미용" to getResizedBitmapDescriptor(R.drawable.icon_map_beauty_salon),
            "문예회관" to getResizedBitmapDescriptor(R.drawable.icon_map_culture_and_arts_center),
            "펜션" to getResizedBitmapDescriptor(R.drawable.icon_map_pension),
            "식당" to getResizedBitmapDescriptor(R.drawable.icon_map_restaurant),
            "여행지" to getResizedBitmapDescriptor(R.drawable.icon_map_tourist_spot),
            "위탁관리" to getResizedBitmapDescriptor(R.drawable.icon_map_management),
            "박물관" to getResizedBitmapDescriptor(R.drawable.icon_map_museum),
            "호텔" to getResizedBitmapDescriptor(R.drawable.icon_map_hotel)
        )

        categoryToDrawableMap12f = mapOf(
            "동물약국" to getResizedBitmapDescriptor(R.drawable.icon_map_pharmacy, 52),
            "미술관" to getResizedBitmapDescriptor(R.drawable.icon_map_artrium, 52),
            "카페" to getResizedBitmapDescriptor(R.drawable.icon_map_caffee, 52),
            "동물병원" to getResizedBitmapDescriptor(R.drawable.icon_map_hospital, 52),
            "반려동물용품" to getResizedBitmapDescriptor(R.drawable.icon_map_shop, 52),
            "미용" to getResizedBitmapDescriptor(R.drawable.icon_map_beauty_salon, 52),
            "문예회관" to getResizedBitmapDescriptor(R.drawable.icon_map_culture_and_arts_center, 52),
            "펜션" to getResizedBitmapDescriptor(R.drawable.icon_map_pension, 52),
            "식당" to getResizedBitmapDescriptor(R.drawable.icon_map_restaurant, 52),
            "여행지" to getResizedBitmapDescriptor(R.drawable.icon_map_tourist_spot, 52),
            "위탁관리" to getResizedBitmapDescriptor(R.drawable.icon_map_management, 52),
            "박물관" to getResizedBitmapDescriptor(R.drawable.icon_map_museum, 52),
            "호텔" to getResizedBitmapDescriptor(R.drawable.icon_map_hotel, 52)
        )

        categoryToDrawableMap9f = mapOf(
            "동물약국" to getResizedBitmapDescriptor(R.drawable.icon_map_pharmacy, 32),
            "미술관" to getResizedBitmapDescriptor(R.drawable.icon_map_artrium, 32),
            "카페" to getResizedBitmapDescriptor(R.drawable.icon_map_caffee, 32),
            "동물병원" to getResizedBitmapDescriptor(R.drawable.icon_map_hospital, 32),
            "반려동물용품" to getResizedBitmapDescriptor(R.drawable.icon_map_shop, 32),
            "미용" to getResizedBitmapDescriptor(R.drawable.icon_map_beauty_salon, 32),
            "문예회관" to getResizedBitmapDescriptor(R.drawable.icon_map_culture_and_arts_center, 32),
            "펜션" to getResizedBitmapDescriptor(R.drawable.icon_map_pension, 32),
            "식당" to getResizedBitmapDescriptor(R.drawable.icon_map_restaurant, 32),
            "여행지" to getResizedBitmapDescriptor(R.drawable.icon_map_tourist_spot, 32),
            "위탁관리" to getResizedBitmapDescriptor(R.drawable.icon_map_management, 32),
            "박물관" to getResizedBitmapDescriptor(R.drawable.icon_map_museum, 32),
            "호텔" to getResizedBitmapDescriptor(R.drawable.icon_map_hotel, 32)
        )

        // 줌 레벨 별 아이콘 크기 조정
        mMap.setOnCameraMoveListener {
            val zoom = getZoomLevel()

            // 아이콘 크기 변화 없을 시 리턴 또는 리로딩 중인 경우
            if (reloading) return@setOnCameraMoveListener
            if (cachedZoom >= 14 && zoom >= 14) return@setOnCameraMoveListener
            else if (cachedZoom in 12..<14 && zoom in 12..<14) return@setOnCameraMoveListener
            else if (cachedZoom < 12 && zoom < 12) return@setOnCameraMoveListener

            // 아이콘 사이즈 설정
            val size = if (zoom >= 14) {
                cachedZoom = 14
                72
            }
            else if (zoom >= 12) {
                cachedZoom = 12
                52
            } else {
                cachedZoom = 9
                32
            }

            markers.forEach {
                val facility = it.tag as? Facility
                val icon = if (facility == null) {
                    getResizedBitmapDescriptor(R.drawable.icon_map_default)
                } else if (zoom >= 14) {
                    categoryToDrawableMap14f[facility.category] ?: getResizedBitmapDescriptor(R.drawable.icon_map_default)
                } else if (zoom >= 12) {
                    categoryToDrawableMap12f[facility.category] ?: getResizedBitmapDescriptor(R.drawable.icon_map_default, size)
                } else {
                    categoryToDrawableMap9f[facility.category] ?: getResizedBitmapDescriptor(R.drawable.icon_map_default, size)
                }
                it.setIcon(icon)
            }
        }

        launchLocation()
    }

    private fun launchLocation() {
        enableUserLocation()
        focusOnUserLocation()
        moveToCurrentLocation()
        showLocationFilterDialog()
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }
    }

    private fun focusOnUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                } else {
                    Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location: Location? = task.result
                if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }
        }
    }

    private fun addMarkersWithin(radius: Int) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener(requireActivity(), OnSuccessListener<Location> { location ->
                    if (location != null) {
                        addMarkers(facilityDatabaseHelper.getFacilities(location.latitude, location.longitude, radius))
                    } else {
                        // GPS를 끈 것처럼 위치가 없을 경우 처리
                        Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDetailedFacilityInfo(facility: Facility) {
        val dialog = Dialog(requireContext())
        // 시설 세부정보 레이아웃 설정
        dialog.setContentView(R.layout.dialog_facility_details)
        dialog.findViewById<TextView>(R.id.dialog_facility_title).text = facility.placeName
        val placeNameView = dialog.findViewById<TextView>(R.id.dialog_facility_title)
        placeNameView.text = facility.placeName
        placeNameView.setOnClickListener {
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra(SearchManager.QUERY, (facility.roadAddress ?: facility.numAddress) + facility.placeName)
            startActivity(intent)
        }
        dialog.findViewById<TextView>(R.id.dialog_facility_category).text = facility.category
        dialog.findViewById<TextView>(R.id.dialog_facility_description).text = facility.placeDescription
        val addressView = dialog.findViewById<TextView>(R.id.dialog_facility_address)
        addressView.text = facility.roadAddress ?: facility.numAddress
        addressView.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", facility.roadAddress ?: facility.numAddress)
            clipboard.setPrimaryClip(clip)

            // 사용자에게 복사 완료 메시지 표시
            mainActivity.showToast("클립보드에 복사되었습니다.")
        }
        var operatingText = facility.operatingHours + if (facility.closedDay == "정보없음") "" else " / " + facility.closedDay
        dialog.findViewById<TextView>(R.id.dialog_facility_operating_hours).text = operatingText
        if (facility.callNumber.isNullOrEmpty()) dialog.findViewById<TextView>(R.id.dialog_facility_call_number).visibility = View.GONE
        else {
            val callNumberView = dialog.findViewById<TextView>(R.id.dialog_facility_call_number)
            callNumberView.text = facility.callNumber
            callNumberView.setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + facility.callNumber))) }
        }
        if (facility.homepageAddress == "정보없음") dialog.findViewById<TextView>(R.id.dialog_facility_homepage).visibility = View.GONE
        else {
            val homepageView = dialog.findViewById<TextView>(R.id.dialog_facility_homepage)
            homepageView.text = facility.homepageAddress
            homepageView.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facility.homepageAddress)))
            }
        }
        if (facility.fee == "없음" || facility.fee == "변동") dialog.findViewById<TextView>(R.id.dialog_facility_fee).visibility = View.GONE
        else dialog.findViewById<TextView>(R.id.dialog_facility_fee).text = facility.fee
        if (facility.additionalFeeForAccompanyingPet == "없음") dialog.findViewById<TextView>(R.id.dialog_facility_additional_fee).visibility = View.GONE
        else dialog.findViewById<TextView>(R.id.dialog_facility_additional_fee).text = "반려동물 동반 추가금: " + facility.additionalFeeForAccompanyingPet
        var restrictionsText = "반려동물 제한사항: " + if (facility.canEnterPetSize == "모두 가능") "" else { facility.canEnterPetSize + ", " }
        restrictionsText += if (facility.beAccompaniedRestrictions == "제한사항 없음" || facility.beAccompaniedRestrictions == "해당없음") "" else { facility.beAccompaniedRestrictions }
        if (restrictionsText.endsWith(", ")) restrictionsText.dropLast(2)
        if (restrictionsText == "반려동물 제한사항: ") restrictionsText+="없음"
        dialog.findViewById<TextView>(R.id.dialog_facility_restrictions).text = restrictionsText
        dialog.findViewById<TextView>(R.id.dialog_facility_can_parking).text = "주차 " + if (facility.parkingAvailable == "Y") "가능" else "불가능"
        if (facility.isOutdoor == "N") dialog.findViewById<TextView>(R.id.dialog_facility_outdoor).visibility = View.GONE
        else dialog.findViewById<TextView>(R.id.dialog_facility_outdoor).text = "야외 시설 있음"


        dialog.findViewById<Button>(R.id.dialog_facility_closeBtn).setOnClickListener {
            dialog.dismiss() // 닫기 버튼 클릭 시 Dialog 닫기
        }

        dialog.show()
    }

    fun showLocationFilterDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_location_filter, null)

        val spinnerSido = dialogView.findViewById<Spinner>(R.id.spinner_sido)
        val scrollSigungu = dialogView.findViewById<ScrollView>(R.id.scroll_sigungu)
        val layoutSigungu = dialogView.findViewById<LinearLayout>(R.id.layout_sigungu)
        val tvSigungu = dialogView.findViewById<TextView>(R.id.tv_sigungu)

        // 시도 목록
        val sidoList = listOf(
            "반경 10km", "반경 20km", "서울", "경기", "인천", "강원", "대전", "세종",
            "충남", "충북", "부산", "울산", "경남", "경북", "대구", "광주", "전남", "전북", "제주"
        )
        val sigunguMap = mapOf(
            "경기" to listOf("가평군", "고양시", "과천시", "광명시", "광주시", "구리시", "군포시", "김포시", "남양주시", "동두천시", "부천시", "성남시", "수원시", "시흥시", "안산시", "안성시", "안양시", "양주시", "양평군", "여주시", "연천군", "오산시", "용인시", "의왕시", "의정부시", "이천시", "파주시", "평택시", "포천시", "하남시", "화성시"),
            "강원" to listOf("강릉시", "고성군", "동해시", "삼척시", "속초시", "양구군", "양양군", "영월군", "원주시", "인제군", "정선군", "철원군", "춘천시", "태백시", "평창군", "홍천군", "화천군", "횡성군"),
            "충남" to listOf("계룡시", "공주시", "금산군", "논산시", "당진시", "보령시", "부여군", "서산시", "서천군", "아산시", "예산군", "천안시", "청양군", "태안군", "홍성군"),
            "충북" to listOf("괴산군", "단양군", "보은군", "영동군", "옥천군", "음성군", "제천시", "증평군", "진천군", "청주시", "충주시"),
            "경남" to listOf("거제시", "거창군", "고성군", "김해시", "남해군", "밀양시", "사천시", "산청군", "양산시", "의령군", "진주시", "창녕군", "창원시", "통영시", "하동군", "함안군", "함양군", "합천군"),
            "경북" to listOf("경산시", "경주시", "고령군", "구미시", "김천시", "문경시", "봉화군", "상주시", "성주군", "안동시", "영덕군", "영양군", "영주시", "영천시", "예천군", "울릉군", "울진군", "의성군", "청도군", "청송군", "칠곡군", "포항시"),
            "전남" to listOf("강진군", "고흥군", "곡성군", "광양시", "구례군", "나주시", "담양군", "목포시", "무안군", "보성군", "순천시", "신안군", "여수시", "영광군", "영암군", "완도군", "장성군", "장흥군", "진도군", "함평군", "해남군", "화순군"),
            "전북" to listOf("고창군", "김제시", "남원시", "무주군", "부안군", "순창군", "완주군", "익산시", "임실군", "장수군", "전주시", "정읍시", "진안군")
        )

        // Spinner 설정
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sidoList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSido.adapter = spinnerAdapter

        // Spinner 아이템 선택 이벤트
        spinnerSido.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSido = sidoList[position]

                // 시군구 레이아웃 초기화
                layoutSigungu.removeAllViews()

                if (sigunguMap.containsKey(selectedSido)) {
                    tvSigungu.visibility = View.VISIBLE
                    scrollSigungu.visibility = View.VISIBLE
                    val sigunguList = sigunguMap[selectedSido] ?: emptyList()
                    sigunguList.forEach { sigungu ->
                        val checkBox = CheckBox(context)
                        checkBox.text = sigungu
                        layoutSigungu.addView(checkBox)
                    }
                } else {
                    tvSigungu.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 다이얼로그 생성
        val customTitle = TextView(mainActivity).apply {
            text = "위치 필터"
            textSize = 18f  // 텍스트 크기 설정
            typeface = ResourcesCompat.getFont(context, R.font.nanumsquare_acebold)
            setPadding(30, 30, 0, 0)
        }
        AlertDialog.Builder(requireContext())
            .setCustomTitle(customTitle)
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val selectedSido = spinnerSido.selectedItem.toString()
                val selectedSigungu = mutableListOf<String>()

                for (i in 0 until layoutSigungu.childCount) {
                    val checkBox = layoutSigungu.getChildAt(i) as CheckBox
                    if (checkBox.isChecked) {
                        selectedSigungu.add(checkBox.text.toString())
                    }
                }
                if (selectedSido.contains("km")) {
                    addMarkersWithin(selectedSido.replace("반경 ", "").replace("km", "").trim().toInt())
                } else {
                    addMarkers(selectedSido, selectedSigungu)
                }
                val filterText = if (selectedSigungu.isNotEmpty()) {
                    if (selectedSigungu.size == 1) {
                        "현재 필터 :  $selectedSido " + selectedSigungu.get(0)
                    } else "현재 필터 :  $selectedSido $selectedSigungu"
                } else {
                    "현재 필터 :  $selectedSido"
                }
                filterView.setText(filterText)
                Log.d("LocationFilter", "Selected Sido: $selectedSido, Sigungu: $selectedSigungu")
            }
            .setCancelable(false)
            .create()
            .show()
    }

    fun addMarkers(sido: String, sigungu: MutableList<String>) {
        val sidoMap = mapOf(
            "충남" to "충청남",
            "충북" to "충청북",
            "경남" to "경상남",
            "경북" to "경상북",
            "전남" to "전라남",
            "전북" to "전라북"
        )
        val sidoString = sidoMap[sido] ?: sido
        facilityDatabaseHelper.getFacilities(sidoString, sigungu).forEach { facility ->
            if (facility.canBeAccompaniedPet == "N") return@forEach
            facility.latitude.let { lat ->
                facility.longitude.let { lng ->
                    val position = LatLng(lat.toDouble(), lng.toDouble())
                    val icon = categoryToDrawableMap14f[facility.category] ?: getResizedBitmapDescriptor(R.drawable.icon_map_default)

                    val marker = mMap.addMarker(
                        AdvancedMarkerOptions()
                            .position(position)
                            .title(facility.placeName) // 혹시 모를 시설명과 주소 적어두기
                            .snippet(facility.emdAddress ?: facility.numAddress)
                            .icon(icon)
                            .anchor(0.5f, 1f)
                    )
                    marker?.tag = facility
                    markers.add(marker!!)
                }
            }
        }
    }

    fun addMarkers(facilities: List<Facility>) {
        facilities.forEach { facility ->
            if (facility.canBeAccompaniedPet == "N") return@forEach
            facility.latitude.let { lat ->
                facility.longitude.let { lng ->
                    val position = LatLng(lat.toDouble(), lng.toDouble())
                    val icon = categoryToDrawableMap14f[facility.category] ?: getResizedBitmapDescriptor(R.drawable.icon_map_default)

                    val marker = mMap.addMarker(
                        AdvancedMarkerOptions()
                            .position(position)
                            .title(facility.placeName) // 혹시 모를 시설명과 주소 적어두기
                            .snippet(facility.emdAddress ?: facility.numAddress)
                            .icon(icon)
                            .anchor(0.5f, 1f)
                    )
                    marker?.tag = facility
                    markers.add(marker!!)
                }
            }
        }
    }

    fun getZoomLevel(): Int {
        return mMap.cameraPosition.zoom.toInt()
    }
}