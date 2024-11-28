package com.example.application.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.application.activities.MainActivity
import com.example.application.R
import com.example.application.structs.PetInfo
import java.util.Calendar

class HomeFragment : Fragment() {
    lateinit var petImage: ImageView
    var savedUri: Uri? = null
    lateinit var setPetInfo: Button
    lateinit var typeInfoSelectLayout: LinearLayout
    lateinit var typeInfoWhat: ImageView
    lateinit var typeInfoHorizontalScrollview: HorizontalScrollView
    lateinit var typeInfoCat: ImageView
    lateinit var typeInfoDog: ImageView
    lateinit var InfoEditGridlayout: GridLayout
    lateinit var nameInfoEdit: EditText
    lateinit var birthEditInfoView: TextView
    lateinit var birthEditInfoEdit: ImageView
    lateinit var likeInfoEdit: EditText
    lateinit var hateInfoEdit: EditText
    lateinit var savePetInfo: Button
    var editCal: Calendar? = null
    var petInfo: PetInfo? = null
    lateinit var petInfoLinearlayout: LinearLayout
    lateinit var editInfo: ImageView
    lateinit var typeView: ImageView
    lateinit var nameInfoView: TextView
    lateinit var birthInfoView: TextView
    lateinit var likeInfoView: TextView
    lateinit var hateInfoView: TextView

    var petType: String? = null

    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        petImage = view.findViewById(R.id.myPetImage)
        setPetInfo = view.findViewById(R.id.setPetInfo)
        typeInfoSelectLayout = view.findViewById(R.id.typeInfoSelectLayout)
        typeInfoWhat = view.findViewById(R.id.typeInfoWhat)
        typeInfoHorizontalScrollview = view.findViewById(R.id.typeInfoHorizontalScrollview)
        typeInfoCat = view.findViewById(R.id.typeInfoCat)
        typeInfoDog = view.findViewById(R.id.typeInfoDog)
        InfoEditGridlayout = view.findViewById(R.id.InfoEditGridlayout)
        savePetInfo = view.findViewById(R.id.savePetInfo)
        nameInfoEdit = view.findViewById(R.id.nameInfoEdit)
        birthEditInfoView = view.findViewById(R.id.birthEditInfoView)
        birthEditInfoEdit = view.findViewById(R.id.birthEditInfoEdit)
        likeInfoEdit = view.findViewById(R.id.likeInfoEdit)
        hateInfoEdit = view.findViewById(R.id.hateInfoEdit)
        petInfoLinearlayout = view.findViewById(R.id.petInfoLinearlayout)
        editInfo = view.findViewById(R.id.editInfo)
        typeView = view.findViewById(R.id.typeView)
        nameInfoView = view.findViewById(R.id.nameInfoView)
        birthInfoView = view.findViewById(R.id.birthInfoView)
        likeInfoView = view.findViewById(R.id.likeInfoView)
        hateInfoView = view.findViewById(R.id.hateInfoView)

        // 반려동물 사진 불러오기
        savedUri = mainActivity.loadPetImage()
        if (savedUri != null) {
            petImage.setImageURI(savedUri)
        } else {
            petImage.setImageResource(R.drawable.empty_type)
            mainActivity.savePetImage(mainActivity.getDrawableUri(R.drawable.empty_type))
        }

        // 반려동물 정보가 있을 때 GUI 로드
        petInfo = mainActivity.loadPetInfo()
        if (petInfo != null) {
            petType = petInfo!!.type
            setPetInfo.visibility = View.GONE
            callInfoUI()
        } else {
            petInfo = PetInfo("", "", null, null, null)
        }

        // 반려동물 사진 클릭시 컨텍스트 메뉴 접근 안내
        petImage.setOnClickListener {
            mainActivity.showToast("사진 편집 시 길게 눌러주세요!")
            // 이미지 확대
            val dialog = Dialog(mainActivity)
            dialog.setContentView(R.layout.dialog_image_zoom) // 확대된 이미지를 보여줄 레이아웃 파일

            val zoomedImageView = dialog.findViewById<ImageView>(R.id.zoomed_image_view)

            // Glide를 사용하여 이미지 로드
            Glide.with(mainActivity)
                .load(petImage.drawable)
                .into(zoomedImageView)

            zoomedImageView.setOnClickListener {
                dialog.dismiss() // 이미지 클릭 시 다이얼로그 닫기
            }

            dialog.show() // 다이얼로그 표시
        }

        // 컨텍스트 메뉴들 추가
        registerForContextMenu(petImage)
        registerForContextMenu(typeView)
        registerForContextMenu(nameInfoView)
        registerForContextMenu(birthInfoView)
        registerForContextMenu(likeInfoView)
        registerForContextMenu(hateInfoView)

        // 반려동물 정보 입력하기 버튼 클릭시
        setPetInfo.setOnClickListener {
            setPetInfo.visibility = View.GONE
            typeInfoSelectLayout.visibility = View.VISIBLE
            InfoEditGridlayout.visibility = View.VISIBLE
            savePetInfo.visibility = View.VISIBLE
        }

        // 종류 설정 이미지 클릭시
        typeInfoWhat.setOnClickListener {
            typeInfoWhat.visibility = View.GONE
            typeInfoHorizontalScrollview.visibility = View.VISIBLE
        }

        // 종류 설정에서 강아지 클릭시
        typeInfoDog.setOnClickListener {
            typeInfoHorizontalScrollview.visibility = View.GONE
            typeInfoWhat.visibility = View.VISIBLE
            typeInfoWhat.setImageResource(R.drawable.dog)
            petType = "dog"
            typeView.setImageResource(R.drawable.dog)
        }

        // 종류 설정에서 고양이 클릭시
        typeInfoCat.setOnClickListener {
            typeInfoHorizontalScrollview.visibility = View.GONE
            typeInfoWhat.visibility = View.VISIBLE
            typeInfoWhat.setImageResource(R.drawable.cat)
            petType = "cat"
            typeView.setImageResource(R.drawable.cat)
        }

        // 종류 설정에서 달력 이미지 클릭시
        birthEditInfoEdit.setOnClickListener {
            // java.util 에서 캘린더를 가져와 현재 날짜 가져오기
            val cal = Calendar.getInstance()
            
            // DatePickerDialog 창을 띄우고, 날짜 설정 완료시 birthInfoView<TextView>에 출력하기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                birthEditInfoView.text = " ${year}년 ${month+1}월 ${dayOfMonth}일"
                editCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
            }
            DatePickerDialog(mainActivity, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 반려동물 정보 설정에서 저장하기 버튼 클릭시
        savePetInfo.setOnClickListener {
            // isEmpty()는 오직 ""일 때만, isBlank()는 ""외에도 " "(띄어쓰기) 같은 걸 포함
            if (nameInfoEdit.text.isBlank() ||
                petType == null
                ) {
                mainActivity.showToast("반려동물의 종과 이름은 필수입니다!")
            } else {
                if (petInfo != null &&
                    petInfo!!.name == nameInfoEdit.text.toString() &&
                    petInfo!!.type == petType &&
                    petInfo!!.like == likeInfoEdit.text.toString() &&
                    petInfo!!.hate == hateInfoEdit.text.toString()) {
                    if (petInfo!!.birth == null && editCal == null) {
                        mainActivity.showToast("변경사항이 없습니다!")
                        typeInfoSelectLayout.visibility = View.GONE
                        InfoEditGridlayout.visibility = View.GONE
                        savePetInfo.visibility = View.GONE
                        petInfoLinearlayout.visibility = View.VISIBLE
                        return@setOnClickListener
                    } else if (petInfo!!.birth != null && editCal != null) {
                        if (petInfo!!.birth!!.equals(editCal)) {
                            mainActivity.showToast("변경사항이 없습니다!")
                            typeInfoSelectLayout.visibility = View.GONE
                            InfoEditGridlayout.visibility = View.GONE
                            savePetInfo.visibility = View.GONE
                            petInfoLinearlayout.visibility = View.VISIBLE
                            return@setOnClickListener
                        }
                    }
                }
                val petInfoToSave = PetInfo(
                    nameInfoEdit.text.toString(),
                    petType!!,
                    likeInfoEdit.text.toString(),
                    hateInfoEdit.text.toString(),
                    editCal
                )
                mainActivity.savePetInfo(petInfoToSave)
                petInfo = petInfoToSave
                mainActivity.showToast("반려동물의 정보를 성공적으로 저장했습니다!")
                
                // UI 업데이트
                typeInfoSelectLayout.visibility = View.GONE
                InfoEditGridlayout.visibility = View.GONE
                savePetInfo.visibility = View.GONE
                callInfoUI()
            }
        }

        // 반려동물 정보에서 수정 아이콘 클릭시
        editInfo.setOnClickListener {
            petInfoLinearlayout.visibility = View.GONE
            typeInfoSelectLayout.visibility = View.VISIBLE
            InfoEditGridlayout.visibility = View.VISIBLE
            savePetInfo.visibility = View.VISIBLE
            nameInfoEdit.setText(petInfo!!.name)
            typeInfoHorizontalScrollview.visibility = View.GONE
            typeInfoWhat.visibility = View.VISIBLE
            typeInfoWhat.setImageResource(R.drawable.dog)
            if (petType == "cat") {
                typeInfoWhat.setImageResource(R.drawable.cat)
            }
            likeInfoEdit.setText(petInfo!!.like)
            hateInfoEdit.setText(petInfo!!.hate)
            if (petInfo!!.birth != null) {
                editCal = Calendar.getInstance().apply {
                    set(petInfo!!.birth!!.get(Calendar.YEAR), petInfo!!.birth!!.get(Calendar.MONTH), petInfo!!.birth!!.get(Calendar.DAY_OF_MONTH))
                }
                birthEditInfoView.text = " " + petInfo!!.birth!!.get(Calendar.YEAR) + "년 " +
                        (petInfo!!.birth!!.get(Calendar.MONTH).toLong() + 1) + "월 " +
                        petInfo!!.birth!!.get(Calendar.DAY_OF_MONTH) + "일"
            }
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    fun callInfoUI() {
        petInfoLinearlayout.visibility = View.VISIBLE
        if (petType == "cat") {
            typeView.setImageResource(R.drawable.cat)
        }
        nameInfoView.text = " " + petInfo!!.name
        if (petInfo!!.birth == null) {
            birthInfoView.text = " 미입력"
            birthInfoView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        } else {
            val birth: Calendar = petInfo!!.birth!!
            birthInfoView.text = " " + birth.get(Calendar.YEAR) + "년 " +
                    (birth.get(Calendar.MONTH).toLong() + 1) + "월 " +
                    birth.get(Calendar.DAY_OF_MONTH) + "일"
            birthInfoView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        if (petInfo!!.like == null) {
            likeInfoView.text = " 미입력"
            likeInfoView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        } else {
            likeInfoView.text = " " + petInfo!!.like
            likeInfoView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        if (petInfo!!.hate == null) {
            hateInfoView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        } else {
            hateInfoView.text = " " + petInfo!!.hate
            hateInfoView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val mInflater = mainActivity.menuInflater
        // 반려동물 사진란 클릭시 컨텍스트 메뉴 띄우기
        if (v === petImage) {
            menu!!.setHeaderTitle("내 반려동물")
            mInflater.inflate(R.menu.options_pet_image_menu, menu)
        }
        // 클릭시 반려동물 정보 수정창 띄우기
        if (v === typeView ||
            v === nameInfoView ||
            v === birthInfoView ||
            v === likeInfoView ||
            v === hateInfoView) {
            petInfoLinearlayout.visibility = View.GONE
            typeInfoSelectLayout.visibility = View.VISIBLE
            InfoEditGridlayout.visibility = View.VISIBLE
            savePetInfo.visibility = View.VISIBLE
            nameInfoEdit.setText(petInfo!!.name)
            typeInfoHorizontalScrollview.visibility = View.GONE
            typeInfoWhat.visibility = View.VISIBLE
            typeInfoWhat.setImageResource(R.drawable.dog)
            if (petType == "cat") {
                typeInfoWhat.setImageResource(R.drawable.cat)
            }
            likeInfoEdit.setText(petInfo!!.like)
            hateInfoEdit.setText(petInfo!!.hate)
            if (petInfo!!.birth != null) {
                editCal = Calendar.getInstance().apply {
                    set(petInfo!!.birth!!.get(Calendar.YEAR), petInfo!!.birth!!.get(Calendar.MONTH), petInfo!!.birth!!.get(Calendar.DAY_OF_MONTH))
                }
                birthEditInfoView.text = " " + petInfo!!.birth!!.get(Calendar.YEAR) + "년 " +
                        (petInfo!!.birth!!.get(Calendar.MONTH).toLong() + 1) + "월 " +
                        petInfo!!.birth!!.get(Calendar.DAY_OF_MONTH) + "일"
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 반려동물 사진 기본으로 변경 버튼 클릭시 이벤트
            R.id.myPetImageClear -> {
                petImage.setImageResource(R.drawable.empty_type)
                mainActivity.savePetImage(mainActivity.getDrawableUri(R.drawable.empty_type))
                return true
            }
            // 반려동물 사진 가져오는 버튼 클릭시 갤러리 열기
            R.id.myPetImageEdit -> {
                mainActivity.requestPermissionsAndOpenGallery()
                return true
            }
        }
        return false
    }
}