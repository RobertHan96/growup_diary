package com.studiofirstzero.growup_diary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.Utils.ErrorHandlerUtils
import com.studiofirstzero.growup_diary.Utils.setOnSingleClickListener
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.lang.Exception

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
            val alert = AlertDialog.Builder(mContext).apply {
                this.setTitle("삭제 안내")
                this.setMessage("정말 삭제하시겠습니까?")
                this.setPositiveButton("네", { dialog, which ->
                    deletePost()
                    finish()
                })
                this.setNegativeButton("취소",  { dialog, which ->
                    finish()
                })
            }.show()
        }

        postModifyBtn.setOnSingleClickListener {
            val postEditActivity = Intent( mContext, PostEditActivity::class.java)
            postEditActivity.putExtra("postData", mPostData)
            postEditActivity.putExtra("postId", mPostId)
            startActivity(postEditActivity)
        }
    }

    override fun setValues() {
        try {
            val postId = intent.getStringExtra("postId")
            mPostId = postId
        } catch (e : Exception) {
            Log.d("log", "게시글 ID 불러오기 실패 ${e}")
            finish()
        }

        getPostData()

        val postData = intent.getParcelableExtra<Post>("postData").apply {
            mPostData = this
            Log.d("log", "최초 게시글 정보 불러오기 성공")
            titleText.text = this.title
            contentText.text = this.content
            createdAtText.text = this.createdAt
            measuredValueText.text = this.measureValue.toString()
            Glide.with(mContext).load(this.imageUrl).override(300,300).into(postImage)
        }
    }

    private fun deletePost() {
        Log.d("log", "게시글 삭제 ID - ${mPostId}")
        db.collection("posts").document(mPostId)
            .delete()
            .addOnSuccessListener { Log.d("log", "게시글 삭제 완료") }
            .addOnFailureListener { e ->
                Log.d("log", "게시글 삭제 중 오류 발생 : ", e)
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.PostDeleteFail)
            }
            finish()
    }

    private fun getPostData() {
        val docRef = db.collection("posts").document(mPostId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("log", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val currentPostData =  snapshot.data?.toMutableMap().apply {
                    mPostData = convertToPost(this)
                    val year = mPostData.createdAt?.subSequence(0,4).toString()
                    val month = mPostData.createdAt?.subSequence(5,7).toString()
                    titleText.text = "${year}년 ${month}월의 기록"
                    contentText.text = mPostData.content
                    createdAtText.text = mPostData.createdAt
                    measuredValueText.text = mPostData.measureValue?.toInt().toString()
                    Glide.with(mContext).load(mPostData.imageUrl).override(300,300).into(postImage)

                }
                Log.d("log", "최신 게시글 정보 : ${mPostData}")

            } else {
                Log.d("log", "Current data: null")
            }
        }
    }

    fun convertToPost(data: MutableMap<String, Any>?) : Post {
        val id = data?.get("id") as String
        val measureValue = data?.get("measureValue") as Number
        val title = data.get("title") as String
        val content = data.get("content") as String
        val imageUrl = data.get("imageUrl") as String
        val createdAt = data.get("createdAt") as String
        val post = Post(id, measureValue, title, content, imageUrl, createdAt)
        return post
    }

}