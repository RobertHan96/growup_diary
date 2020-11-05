package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.activity_write_diary.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log
import kotlin.math.roundToInt

class WriteDiaryActivity : BaseActivity() {
    private lateinit var bt : BluetoothSPP
    var db = FirebaseFirestore.getInstance()
    private val REQUEST_CODE = 1001
    private lateinit var auth: FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var mGoogleSignInClient: GoogleSignInClient

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
        bt = BluetoothSPP(mContext)
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(mContext, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
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
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val loginActivity = Intent(mContext, LoginActivity::class.java)
                startActivity(loginActivity)
            } else {
                Log.d("log", "test시작")
                val postImage = findViewById<ImageView>(R.id.postImage)
                uploadImageAndPost(postImage)
                Log.d("log", "test끝")
            }
        }

        getMeasureValuesBtn.setOnClickListener {
            if (bt.serviceState == BluetoothState.STATE_CONNECTED) {
                bt.disconnect()
            } else {
                val intent = Intent(mContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }
        }

        openGalleryBtn.setOnClickListener{
            val myIntent = Intent(Intent.ACTION_PICK)
            myIntent.setType("image/*") //가져올 이미지 파일들의 확장자 결정
            myIntent.setType(MediaStore.Images.Media.CONTENT_TYPE)
            startActivityForResult(myIntent, REQUEST_CODE)
        }

        bt.setOnDataReceivedListener { data, message ->
            measuredValueText.text = message
        }

        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(mContext, "측정기와 연결되었습니다.", Toast.LENGTH_SHORT).show();
            }

            override fun onDeviceDisconnected() {
                Toast.makeText(mContext, "측정기와 연결이 해제되었습니다.", Toast.LENGTH_SHORT).show();
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(mContext, "연결된 측정기가 없습니다, 연결 버튼을 통해 연결해주세요.", Toast.LENGTH_SHORT).show();
            }
        })


    }

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
                Toast.makeText(mContext, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val postImageView = findViewById<ImageView>(R.id.postImage)
                Glide.with(mContext).load(data?.data).into(postImageView)
            }
        }

    }

    private fun uploadImageAndPost(imageView : ImageView) {
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "IMAGE_${timeStamp}.png"
        val imageRef = storage.reference.child("postImages").child(fileName)

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.d("log", "포스트 첨부이지 업로드 중 오류 발생 ${it}")
        }.addOnSuccessListener { taskSnapshot ->
            val imageUrl = taskSnapshot.storage.downloadUrl.toString()
            Log.d("log", "이미지 업로드 성공 : ${imageUrl}}")
            val post = getPostInfo(imageUrl)
            postDiary(post)
        }
    }

    // id null, measue value null 문제 해결 필요
    private fun getPostInfo(imageUrl : String) : Post {
        val userId = getUserName()
//        val measureValueFloat = measuredValueText.text.toString().toFloat()
//        val measuereValue = (measureValueFloat * 10).roundToInt()
        val title = titleEdt.text.toString()
        val content = contentEdt.text.toString()
        val imgUrl = imageUrl
        val createdTime = FieldValue.serverTimestamp()
        val post = Post(userId, 123.45 ,title, content, imgUrl, createdTime)
        return  post
    }

    private fun getUserName() : String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return currentUser?.displayName.toString()
        } else {
            return "1"
        }
    }

    private fun postDiary(post : Post) {
        val tableName = "posts"
        db.collection(tableName)
            .add(post)
            .addOnSuccessListener { documentReference ->
                Log.d("log", "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.d("log", "Error adding document", e)
            }

        finish()
    }

}

