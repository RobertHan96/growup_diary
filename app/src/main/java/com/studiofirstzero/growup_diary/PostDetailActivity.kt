package com.studiofirstzero.growup_diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.PointerIcon.load
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_post_detail.*

// 작성완료한 게시글의 상세 정보 출력
// 작성일, 제목, 내용, 사진, 키
class PostDetailActivity : BaseActivity() {
    override fun setupEvents() {

    }

    override fun setValues() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setupEvents()
        setValues()
    }
}