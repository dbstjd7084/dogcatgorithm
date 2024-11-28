package com.example.application.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.application.structs.googlemap.Facility
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FacilityDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "facility_db"
        private const val DATABASE_VERSION = 1

        // 테이블 이름과 칼럼 정의
        private const val TABLE_NAME = "facility"
        private const val COLUMN_ID = "id"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_PLACE_NAME = "place_name"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_PLACE_DESCRIPTION = "place_description"
        private const val COLUMN_ZIP_CODE = "zip_code"
        private const val COLUMN_ROAD_ADDRESS = "road_address"
        private const val COLUMN_NUM_ADDRESS = "num_address"
        private const val COLUMN_EMD_ADDRESS = "emd_address"
        private const val COLUMN_ADDRESS_NUMBER = "address_number"
        private const val COLUMN_OPERATING_HOURS = "operating_hours"
        private const val COLUMN_CLOSED_DAY = "closed_day"
        private const val COLUMN_CALL_NUMBER = "call_number"
        private const val COLUMN_HOMEPAGE_ADDRESS = "homepage_address"
        private const val COLUMN_CAN_BE_ACCOMPANIED_PET = "can_be_accompanied_pet"
        private const val COLUMN_BE_ACCOMPANIED_INFORMATION = "be_accompanied_information"
        private const val COLUMN_BE_ACCOMPANIED_RESTRICTIONS = "be_accompanied_restrictions"
        private const val COLUMN_ADDITIONAL_FEE_FOR_ACCOMPANYING_PET = "additional_fee_for_accompanying_pet"
        private const val COLUMN_CAN_ENTER_PET_SIZE = "can_enter_pet_size"
        private const val COLUMN_FEE = "fee"
        private const val COLUMN_IS_OUTDOOR = "is_outdoor"
        private const val COLUMN_PARKING_AVAILABLE = "parking_available"
        private const val COLUMN_LAST_DATE_CREATED = "last_date_created"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE_QUERY = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LATITUDE TEXT,
                $COLUMN_LONGITUDE TEXT,
                $COLUMN_PLACE_NAME TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_PLACE_DESCRIPTION TEXT,
                $COLUMN_ZIP_CODE TEXT,
                $COLUMN_ROAD_ADDRESS TEXT,
                $COLUMN_NUM_ADDRESS TEXT,
                $COLUMN_EMD_ADDRESS TEXT,
                $COLUMN_ADDRESS_NUMBER TEXT,
                $COLUMN_OPERATING_HOURS TEXT,
                $COLUMN_CLOSED_DAY TEXT,
                $COLUMN_CALL_NUMBER TEXT,
                $COLUMN_HOMEPAGE_ADDRESS TEXT,
                $COLUMN_CAN_BE_ACCOMPANIED_PET TEXT,
                $COLUMN_BE_ACCOMPANIED_INFORMATION TEXT,
                $COLUMN_BE_ACCOMPANIED_RESTRICTIONS TEXT,
                $COLUMN_ADDITIONAL_FEE_FOR_ACCOMPANYING_PET TEXT,
                $COLUMN_CAN_ENTER_PET_SIZE TEXT,
                $COLUMN_FEE TEXT,
                $COLUMN_IS_OUTDOOR TEXT,
                $COLUMN_PARKING_AVAILABLE TEXT,
                $COLUMN_LAST_DATE_CREATED TEXT
            )
        """
        db?.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getFacilityCount(): Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) // 첫 번째 컬럼에서 레코드 수를 가져옴
            }
        }
        return 0 // 기본적으로 0을 반환
    }

    fun clearAllFacilities() {
        val db = writableDatabase
        db.beginTransaction() // 트랜잭션 시작
        try {
            db.delete(TABLE_NAME, null, null) // 테이블의 모든 행 삭제
            db.setTransactionSuccessful() // 트랜잭션 성공으로 설정
        } finally {
            db.endTransaction() // 트랜잭션 종료
        }
        db.close()
    }

    fun insertFacilities(facilities: List<Facility>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (facility in facilities) {
                val values = ContentValues().apply {
                    put(COLUMN_LATITUDE, facility.latitude)
                    put(COLUMN_LONGITUDE, facility.longitude)
                    put(COLUMN_PLACE_NAME, facility.placeName)
                    put(COLUMN_CATEGORY, facility.category)
                    put(COLUMN_PLACE_DESCRIPTION, facility.placeDescription)
                    put(COLUMN_ZIP_CODE, facility.zipCode)
                    put(COLUMN_ROAD_ADDRESS, facility.roadAddress)
                    put(COLUMN_NUM_ADDRESS, facility.numAddress)
                    put(COLUMN_EMD_ADDRESS, facility.emdAddress)
                    put(COLUMN_ADDRESS_NUMBER, facility.addressNumber)
                    put(COLUMN_OPERATING_HOURS, facility.operatingHours)
                    put(COLUMN_CLOSED_DAY, facility.closedDay)
                    put(COLUMN_CALL_NUMBER, facility.callNumber)
                    put(COLUMN_HOMEPAGE_ADDRESS, facility.homepageAddress)
                    put(COLUMN_CAN_BE_ACCOMPANIED_PET, facility.canBeAccompaniedPet)
                    put(COLUMN_BE_ACCOMPANIED_INFORMATION, facility.beAccompaniedInformation)
                    put(COLUMN_BE_ACCOMPANIED_RESTRICTIONS, facility.beAccompaniedRestrictions)
                    put(COLUMN_ADDITIONAL_FEE_FOR_ACCOMPANYING_PET, facility.additionalFeeForAccompanyingPet)
                    put(COLUMN_CAN_ENTER_PET_SIZE, facility.canEnterPetSize)
                    put(COLUMN_FEE, facility.fee)
                    put(COLUMN_IS_OUTDOOR, facility.isOutdoor)
                    put(COLUMN_PARKING_AVAILABLE, facility.parkingAvailable)
                    put(COLUMN_LAST_DATE_CREATED, facility.lastDateCreated)
                }

                // 개별 시설 삽입
                db.insert(TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        db.close()
    }

    fun getFacilities(sido: String, sigungu: MutableList<String>): List<Facility> {
        val facilities = mutableListOf<Facility>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val address = it.getString(it.getColumnIndexOrThrow(COLUMN_NUM_ADDRESS)).split(" ")
                if (address[0].contains(sido)){
                    if (sigungu.isNullOrEmpty() ||
                        sigungu.any{address.contains(it)}) {
                        facilities.add(
                            Facility(
                                latitude = it.getString(it.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                                longitude = it.getString(it.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                                placeName = it.getString(it.getColumnIndexOrThrow(COLUMN_PLACE_NAME)),
                                category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                                placeDescription = it.getString(it.getColumnIndexOrThrow(COLUMN_PLACE_DESCRIPTION)),
                                zipCode = it.getString(it.getColumnIndexOrThrow(COLUMN_ZIP_CODE)),
                                roadAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_ROAD_ADDRESS)),
                                numAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_NUM_ADDRESS)),
                                emdAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_EMD_ADDRESS)),
                                addressNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDRESS_NUMBER)),
                                operatingHours = it.getString(it.getColumnIndexOrThrow(COLUMN_OPERATING_HOURS)),
                                closedDay = it.getString(it.getColumnIndexOrThrow(COLUMN_CLOSED_DAY)),
                                callNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_CALL_NUMBER)),
                                homepageAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_HOMEPAGE_ADDRESS)),
                                canBeAccompaniedPet = it.getString(it.getColumnIndexOrThrow(COLUMN_CAN_BE_ACCOMPANIED_PET)),
                                beAccompaniedInformation = it.getString(it.getColumnIndexOrThrow(COLUMN_BE_ACCOMPANIED_INFORMATION)),
                                beAccompaniedRestrictions = it.getString(it.getColumnIndexOrThrow(COLUMN_BE_ACCOMPANIED_RESTRICTIONS)),
                                additionalFeeForAccompanyingPet = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDITIONAL_FEE_FOR_ACCOMPANYING_PET)),
                                canEnterPetSize = it.getString(it.getColumnIndexOrThrow(COLUMN_CAN_ENTER_PET_SIZE)),
                                fee = it.getString(it.getColumnIndexOrThrow(COLUMN_FEE)),
                                isOutdoor = it.getString(it.getColumnIndexOrThrow(COLUMN_IS_OUTDOOR)),
                                parkingAvailable = it.getString(it.getColumnIndexOrThrow(COLUMN_PARKING_AVAILABLE)),
                                lastDateCreated = it.getString(it.getColumnIndexOrThrow(COLUMN_LAST_DATE_CREATED))
                            )
                        )
                    }
                }
            }
        }
        return facilities
    }

    fun getFacilities(lat: Double, lon: Double, radius: Int): List<Facility> {
        val facilities = mutableListOf<Facility>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        cursor.use {
            while (it.moveToNext()) {
                if (getDistance(lat, lon, it.getString(it.getColumnIndexOrThrow(COLUMN_LATITUDE)).toDouble(), it.getString(it.getColumnIndexOrThrow(COLUMN_LONGITUDE)).toDouble()) <= radius) {
                    facilities.add(
                        Facility(
                            latitude = it.getString(it.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                            longitude = it.getString(it.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                            placeName = it.getString(it.getColumnIndexOrThrow(COLUMN_PLACE_NAME)),
                            category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            placeDescription = it.getString(it.getColumnIndexOrThrow(COLUMN_PLACE_DESCRIPTION)),
                            zipCode = it.getString(it.getColumnIndexOrThrow(COLUMN_ZIP_CODE)),
                            roadAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_ROAD_ADDRESS)),
                            numAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_NUM_ADDRESS)),
                            emdAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_EMD_ADDRESS)),
                            addressNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDRESS_NUMBER)),
                            operatingHours = it.getString(it.getColumnIndexOrThrow(COLUMN_OPERATING_HOURS)),
                            closedDay = it.getString(it.getColumnIndexOrThrow(COLUMN_CLOSED_DAY)),
                            callNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_CALL_NUMBER)),
                            homepageAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_HOMEPAGE_ADDRESS)),
                            canBeAccompaniedPet = it.getString(it.getColumnIndexOrThrow(COLUMN_CAN_BE_ACCOMPANIED_PET)),
                            beAccompaniedInformation = it.getString(it.getColumnIndexOrThrow(COLUMN_BE_ACCOMPANIED_INFORMATION)),
                            beAccompaniedRestrictions = it.getString(it.getColumnIndexOrThrow(COLUMN_BE_ACCOMPANIED_RESTRICTIONS)),
                            additionalFeeForAccompanyingPet = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDITIONAL_FEE_FOR_ACCOMPANYING_PET)),
                            canEnterPetSize = it.getString(it.getColumnIndexOrThrow(COLUMN_CAN_ENTER_PET_SIZE)),
                            fee = it.getString(it.getColumnIndexOrThrow(COLUMN_FEE)),
                            isOutdoor = it.getString(it.getColumnIndexOrThrow(COLUMN_IS_OUTDOOR)),
                            parkingAvailable = it.getString(it.getColumnIndexOrThrow(COLUMN_PARKING_AVAILABLE)),
                            lastDateCreated = it.getString(it.getColumnIndexOrThrow(COLUMN_LAST_DATE_CREATED))
                        )
                    )
                }
            }
        }
        return facilities
    }

    // 거리 계산 함수
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371 // 지구 반지름 (킬로미터)
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radius * c // 거리 반환 (킬로미터)
    }
}