package com.example.application.structs.googlemap

import com.google.gson.annotations.SerializedName

data class Facility(
    @SerializedName("위도") val latitude: String,
    @SerializedName("경도") val longitude: String,
    @SerializedName("시설명") val placeName: String,
    @SerializedName("카테고리3") val category: String,
    @SerializedName("기본 정보_장소설명") val placeDescription: String,
    @SerializedName("우편번호") val zipCode: String?,
    @SerializedName("도로명주소") val roadAddress: String?,
    @SerializedName("지번주소") val numAddress: String,
    @SerializedName("법정읍면동명칭") val emdAddress: String?,
    @SerializedName("번지") val addressNumber: String?,
    @SerializedName("운영시간") val operatingHours: String,
    @SerializedName("휴무일") val closedDay: String,
    @SerializedName("전화번호") val callNumber: String?,
    @SerializedName("홈페이지") val homepageAddress: String,
    @SerializedName("반려동물 동반 가능정보") val canBeAccompaniedPet: String,
    @SerializedName("반려동물 전용 정보") val beAccompaniedInformation: String,
    @SerializedName("반려동물 제한사항") val beAccompaniedRestrictions: String,
    @SerializedName("애견 동반 추가 요금") val additionalFeeForAccompanyingPet: String,
    @SerializedName("입장 가능 동물 크기") val canEnterPetSize: String,
    @SerializedName("입장(이용료)가격 정보") val fee: String,
    @SerializedName("장소(실외)여부") val isOutdoor: String,
    @SerializedName("주차 가능여부") val parkingAvailable: String,
    @SerializedName("최종작성일") val lastDateCreated: String
)