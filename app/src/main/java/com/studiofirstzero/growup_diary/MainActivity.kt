package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    lateinit var bt : BluetoothSPP
    var db = FirebaseFirestore.getInstance()
    val BLUETOOTH_REQ = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt = BluetoothSPP(mContext)
        setValues()
        setupEvents()

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(mContext, "Bluetooth is not available", LENGTH_SHORT).show();
            finish();
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        bt.stopService()
    }
    override fun setupEvents() {
        find_bt_device_btn.setOnClickListener {
            if (bt.serviceState == BluetoothState.STATE_CONNECTED) {
                bt.disconnect()
            } else {
                val intent = Intent(mContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }
        }

        bt.setOnDataReceivedListener { data, message ->
            Toast.makeText(mContext, "vlaue ${message}", LENGTH_SHORT).show();
        }


        bt.setBluetoothConnectionListener(object : BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(mContext, "측정기와 연결되었습니다.", LENGTH_SHORT).show();
            }

            override fun onDeviceDisconnected() {
                Toast.makeText(mContext, "측정기와 연결이 해제되었습니다.", LENGTH_SHORT).show();
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(mContext, "연결된 측정기가 없습니다, 연결 버튼을 통해 연결해주세요.", LENGTH_SHORT).show();
            }
        })

        write_diary_btn.setOnClickListener {
            val user: MutableMap<String, Any> = HashMap()
            user["first"] = "Alan"
            user["middle"] = "Mathison"
            user["last"] = "Turing"
            user["born"] = 1912
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("log", "DocumentSnapshot added with ID: " + documentReference.id) }
                .addOnFailureListener { e -> Log.w("log", "Error adding document", e) }
            val writeDiary = Intent( mContext, WriteDiaryActivity::class.java)
            startActivity(writeDiary)
        }

        grwonup_timeline_btn.setOnClickListener {
            db.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d("log",document.id + " => " + document.data)
                        }
                    } else {
                        Log.w("log", "Error getting documents.", task.exception)
                    }
                }
            val timeLine = Intent( mContext, TimeLineActivity::class.java)
            startActivity(timeLine)
        }

        sign_up_btn.setOnClickListener {
            val signUP = Intent( mContext, SignUpActivity::class.java)
            startActivity(signUP)

        }

        login_btn.setOnClickListener {
            val login = Intent( mContext, LoginActivity::class.java)
            startActivity(login)
        }
    }


    override fun setValues() {

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
                Toast.makeText(mContext, "Bluetooth is not available", LENGTH_SHORT).show();
            }
        }
    }

}