package com.studiofirstzero.growup_diary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.PointerIcon.load
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.lang.Exception
import kotlin.math.log

class PostDetailActivity : BaseActivity() {
    var db = FirebaseFirestore.getInstance()
    private lateinit var mPostId : String
    private lateinit var mPostData : Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setupEvents()
        setValues()
    }

    override fun setupEvents() {
        postDeleteBtn.setOnClickListener {
            deletePost()
            finish()
        }

        postModifyBtn.setOnClickListener {
            val postEditActivity = Intent( mContext, PostEditActivity::class.java)
            postEditActivity.putExtra("postData", mPostData)
            postEditActivity.putExtra("postId", mPostData)
            startActivity(postEditActivity)
        }
    }

    override fun setValues() {
        try {
            val postId = intent.getStringExtra("postId")
            mPostId = postId
        } catch (e : Exception) {
            Log.d("log", "게시글 ID 불러오기 실패 ${e}")
            Toast.makeText(mContext, "게시글을 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        val postData = intent.getParcelableExtra<Post>("postData").apply {
            mPostData = this
            Log.d("log", "게시글 정보 불러오기 성공")
            titleText.text = this.title
            contentText.text = this.content
            createdAtText.text = this.createdAt
            measuredValueText.text = this.measureValue.toString()
            Glide.with(mContext).load(this.imageUrl).override(300,300).into(postImage)
        }
    }

    private fun deletePost() {
        db.collection("cities").document(mPostId)
            .delete()
            .addOnSuccessListener { Log.d("log", "게시글 삭제 완료") }
            .addOnFailureListener { e -> Log.d("log", "게시글 삭제 중 오류 발생 : ", e) }
    }

}