package com.studiofirstzero.growup_diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.PointerIcon.load
import com.bumptech.glide.Glide
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_post_detail.*

// 작성완료한 게시글의 상세 정보 출력
// 작성일, 제목, 내용, 사진, 키
class PostDetailActivity : BaseActivity() {
    override fun setupEvents() {

    }

    override fun setValues() {
//        var postList = intent.getParcelableArrayListExtra<Post>("postListData")
//        Log.d("log", "${postList.get(0).title} - ${postList.get(0).id}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setupEvents()
        setValues()
    }
}