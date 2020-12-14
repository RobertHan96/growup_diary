package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.studiofirstzero.growup_diary.Utils.ConnectionStateMonitor
import com.studiofirstzero.growup_diary.Utils.ErrorHandlerUtils
import com.studiofirstzero.growup_diary.Utils.setOnSingleClickListener
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_post_detail.measuredValueText
import kotlinx.android.synthetic.main.activity_post_detail.postImage
import kotlinx.android.synthetic.main.activity_post_edit.*
import java.io.ByteArrayOutputStream
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PostEditActivity : BaseActivity() {
    private val REQUEST_CODE = 1001
    var db = FirebaseFirestore.getInstance()
    private lateinit var storage : FirebaseStorage
    private lateinit var mPostId : String
    private lateinit var mPostData : Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_edit)
        setValues()
        setupEvents()
    }

    override fun setupEvents() {
        editPostBtn.setOnSingleClickListener {
            if ( isValidPost() ) {
                val postImage = findViewById<ImageView>(R.id.postImage)
                uploadImageAndPost(postImage)
                finish()

            } else {
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.InvaildPost)
            }
        }

        openGalleryBtn.setOnClickListener{
            val myIntent = Intent(Intent.ACTION_PICK)
            myIntent.setType("image/*") //가져올 이미지 파일들의 확장자 결정
            myIntent.setType(MediaStore.Images.Media.CONTENT_TYPE)
            startActivityForResult(myIntent, REQUEST_CODE)
        }

    }

    override fun setValues() {
        ConnectionStateMonitor(mContext, {
            Log.d("log", "네트워크 연결 완료")
            storage = Firebase.storage
            try {
                val postId = intent.getStringExtra("postId")
                mPostId = postId
            } catch (e : Exception) {
                Log.d("log", "게시글 ID 불러오기 실패 ${e}")
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.PostIsEmpty)
                finish()
            }

            val postData = intent.getParcelableExtra<Post>("postData").apply {
                mPostData = this
                Log.d("log", "게시글 정보 불러오기 성공 ${mPostData.toString()}")
                val titleEditView = findViewById<EditText>(R.id.titleEdt)
                val measuredValueEditView = findViewById<EditText>(R.id.measuredValueText)
                val contentEditView = findViewById<EditText>(R.id.contentEdt)
                val postImageView = findViewById<ImageView>(R.id.postImage)
                titleEditView.setText(this.title)
                contentEditView.setText(this.content)
                Glide.with(mContext).load(this.imageUrl).override(300,300).into(postImageView)
                measuredValueEditView.setText(this.measureValue?.toInt().toString())
            }

        }, {
            ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoNetwrokConnetcion)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val postImageView = findViewById<ImageView>(R.id.postImage)
                Glide.with(mContext).load(data?.data).into(postImageView)
            }
        }
    }

    private fun isValidPost() : Boolean {
        val measuereValue = measuredValueText.text
        val title = titleEdt.text
        val content = findViewById<EditText>(R.id.contentEdt).text
        return measuereValue != null && title != null && content != null && postImage.drawable != null
    }

    private fun uploadImageAndPost(imageView : ImageView) {
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val timeStamp = getCurrentTime()
        val fileName = "IMAGE_${timeStamp}.png"
        val imageRef = storage.reference.child("postImages").child(fileName)

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.d("log", "포스트 첨부이미지 업로드 중 오류 발생 ${it}")
        }.addOnSuccessListener { taskSnapshot ->
            val imageUrl = imageRef.downloadUrl.toString()
        }

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val post = getPostInfo(downloadUri.toString())
                editDiary(post)
                Log.d("log", "이미지 업로드 성공 : ${downloadUri}}")
            } else {
                Log.d("log", "포스트 첨부이미지 업로드 중 오류 발생")
            }
        }

    }

    private fun getPostInfo(imageUrl : String) : Post {
        val contentEditView = findViewById<EditText>(R.id.contentEdt)
        val measuereValue = mPostData.measureValue?.toInt()
        val title = titleEdt.text.toString()
        val content = contentEditView.text.toString()
        val imgUrl = imageUrl
        val createdAt = mPostData.createdAt?.subSequence(0,10).toString()
        val id = mPostData.id
        val post = Post(id, measuereValue ,title, content, imgUrl, createdAt)
        return  post
    }

    private fun editDiary(post : Post) {
        val tableName = "posts"
        Log.d("log", post.toString())
        db.collection(tableName).document(mPostId)
            .update(mapOf(
                "title" to post.title,
                "content" to post.content,
                "imageUrl" to post.imageUrl
            ))
            .addOnSuccessListener { Log.d("log", "게시글 ${mPostId} 수정 완료") }
            .addOnFailureListener { Log.d("log", "게시글 ${mPostId} 수정 실패") }
    }

    private fun getCurrentTime() : String {
        val currentDateTime = Calendar.getInstance().time
        var dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)
        return dateFormat
    }

}