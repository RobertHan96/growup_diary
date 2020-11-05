package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import android.graphics.Color
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
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.datas.City
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.activity_write_diary.*
import kotlin.math.roundToInt

class WriteDiaryActivity : BaseActivity() {
    private lateinit var bt : BluetoothSPP
    var db = FirebaseFirestore.getInstance()
    private val REQUEST_CODE = 1001
    private lateinit var auth: FirebaseAuth
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

    override fun setupEvents() {
        writePostBtn.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val loginActivity = Intent(mContext, LoginActivity::class.java)
                startActivity(loginActivity)
            } else {
                postDiary()
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

    private fun postDiary() {
        val tableName = "posts"
        val userId = getUserName()
        val measureValueFloat = measuredValueText.text.toString().toFloat()
        val measuereValue = (measureValueFloat * 10).roundToInt()
        val title = titleEdt.text.toString()
        val content = contentEdt.text.toString()
        val imgUrl = "test"
        val createdTime = FieldValue.serverTimestamp()
        val post = City(userId, measuereValue ,title, content, imgUrl, createdTime)

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

    private fun getUserName() : String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return auth.currentUser?.displayName.toString()
        } else {
            return ""
        }
    }

    private fun getImageBytes() {

    }

}

