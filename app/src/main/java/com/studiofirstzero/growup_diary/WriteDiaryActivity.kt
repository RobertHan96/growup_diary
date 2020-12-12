package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.studiofirstzero.growup_diary.Utils.ConnectionStateMonitor
import com.studiofirstzero.growup_diary.Utils.ErrorHandlerUtils
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_write_diary.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WriteDiaryActivity : BaseActivity() {
    private lateinit var bt : BluetoothSPP
    var db = FirebaseFirestore.getInstance()
    private val REQUEST_CODE = 1001
    private lateinit var auth: FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var userID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_diary)
        setValues()
        setupEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.stopService()
    }

    override fun setValues() {
        contentEdt.setSelection(0)
        bt = BluetoothSPP(mContext)
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoBLDevice)
            finish();
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage
    }

    override fun setupEvents() {

        writePostBtn.setOnClickListener {
            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager.activeNetworkInfo.isConnected == true ) {
                val currentUser = auth.currentUser

                if (currentUser == null ) {
                    val loginActivity = Intent(mContext, LoginActivity::class.java)
                    startActivity(loginActivity)
                } else if (currentUser != null && isValidPost() ) {
                    userID = auth.currentUser?.email.toString()
                    val postImage = findViewById<ImageView>(R.id.postImage)
                    uploadImageAndPost(postImage)
                } else {
                    ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.InvaildPost)
                }
            } else {
                runOnUiThread {
                    ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoNetwrokConnetcion)
                }
            }
        }

        openGalleryBtn.setOnClickListener{
            val myIntent = Intent(Intent.ACTION_PICK)
            myIntent.setType("image/*") //가져올 이미지 파일들의 확장자 결정
            myIntent.setType(MediaStore.Images.Media.CONTENT_TYPE)
            startActivityForResult(myIntent, REQUEST_CODE)
        }

        getMeasureValuesBtn.setOnClickListener {
            if (bt.serviceState == BluetoothState.STATE_CONNECTED) {
                bt.disconnect()
            } else {
                val intent = Intent(mContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }
        }

        bt.setOnDataReceivedListener { data, message ->
            measuredValueText.text = message
        }

        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.BLDeviceConnected)
            }

            override fun onDeviceDisconnected() {
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.BLDeviceDisConnected)
            }

            override fun onDeviceConnectionFailed() {
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoBLDevice)
            }
        })
    } //setupEvents

    override fun onStart() {
        super.onStart()
        if (!bt.isBluetoothEnabled ) {
            Log.d("log", "블루투스 켜짐, 데이터 수신 시작...")
            val intent = Intent(mContext, DeviceList::class.java)
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT)
        } else {
            Log.d("log", "블루투스 꺼짐")
            if (!bt.isServiceAvailable) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                bt.connect(data)
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
            } else {
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoBLDevice)
            }
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val postImageView = findViewById<ImageView>(R.id.postImage)
                Glide.with(mContext).load(data?.data).into(postImageView)
            }
        }
    } // onActivityResult



    private fun isValidPost() : Boolean {
        val measuereValue = measuredValueText.text
        val title = titleEdt.text
        val content = contentEdt.text
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
                postDiaryToDB(post)
                Log.d("log", "이미지 업로드 성공 : ${downloadUri}}")
            } else {
                Log.d("log", "포스트 첨부이미지 업로드 중 오류 발생")
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.ImageUploadFail)
            }
        }

    }

    private fun getCurrentTime() : String {
        val currentDateTime = Calendar.getInstance().time
        var dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)
        return dateFormat
    }

    private fun getPostInfo(imageUrl : String) : Post {
        var measuereValue : Number?
        if  (measuredValueText.text == "cm") {
            measuereValue = 0
        } else {
            measuereValue = measuredValueText.text.toString().toInt()
        }
        val title = titleEdt.text.toString()
        val content = contentEdt.text.toString()
        val imgUrl = imageUrl
        val createdAt = getCurrentTime()
        val post = Post(userID, measuereValue ,title, content, imgUrl, createdAt)
        return  post
    }

    private fun postDiaryToDB(post : Post) {
        val tableName = "posts"
        db.collection(tableName)
            .add(post)
            .addOnSuccessListener { documentReference ->
                Log.d("log", "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.d("log", "Error adding document", e)
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.PostUploadFail)
            }
        finish()
    }
}

