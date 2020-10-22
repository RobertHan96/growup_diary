package com.studiofirstzero.growup_diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// 사진과 함께 글을 작성하는 곳, 게시판 형태로 작성
// 사진 촬영, 첨부 라이브러리 찾아보기
// 작성 내용 : 제목, 내용, 사진 1장, 키 (다이얼로그 창으로 구현 - 파이버에이스와 연동)

class WriteDiary : BaseActivity() {
    override fun setupEvents() {

    }

    override fun setValues() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_diary)
    }
}