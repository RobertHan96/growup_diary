package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_write_diary.*

// 사진과 함께 글을 작성하는 곳, 게시판 형태로 작성
// 사진 촬영, 첨부 라이브러리 찾아보기
// 작성 내용 : 제목, 내용, 사진 1장, 키 (다이얼로그 창으로 구현 - 파이버에이스와 연동)

class WriteDiaryActivity : BaseActivity() {
    lateinit var bt : BluetoothSPP
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
                val loginActivity = Intent( mContext, LoginActivity::class.java)
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
        }
    }

    private fun postDiary() {
        Log.d("log", "게시글 작성 완료 기능 구현 예정")
        finish()
    }

}