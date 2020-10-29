package com.studiofirstzero.growup_diary

import android.icu.util.Measure
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.studiofirstzero.growup_diary.adapters.MeasureAdapter
import kotlinx.android.synthetic.main.activity_time_line.*

// 여태까지 작성했던 글들을 몰아서 보는 곳 : 리스트뷰로 구현
// SNS 공유 기능 라이브러리 찾아보기

class TimeLineActivity : BaseActivity() {
    val measureList = ArrayList<com.studiofirstzero.growup_diary.datas.Measure>()
    // 코드 후반부에 값초기화가 있을 경우를 대비해서 나중에 init한다는 키워드 추가
    private lateinit var mMeasureAdapter : MeasureAdapter

    override fun setupEvents() {

    }

    override fun setValues() {
        fetchMeasure()
        mMeasureAdapter = MeasureAdapter(mContext, R.layout.measure_list_item, measureList)
        measureListView.adapter = mMeasureAdapter
        
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)
        setupEvents()
        setValues()
    }

    fun fetchMeasure() {
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))
        measureList.add(com.studiofirstzero.growup_diary.datas.Measure(1, 100, "2020-07-12"))

    }

}