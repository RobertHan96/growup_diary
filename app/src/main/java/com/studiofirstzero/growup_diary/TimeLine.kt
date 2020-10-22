package com.studiofirstzero.growup_diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// 여태까지 작성했던 글들을 몰아서 보는 곳 : 리스트뷰로 구
// SNS 공유 기능 라이브러리 찾아보기

class TimeLine : BaseActivity() {
    override fun setupEvents() {

    }

    override fun setValues() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)
    }
}