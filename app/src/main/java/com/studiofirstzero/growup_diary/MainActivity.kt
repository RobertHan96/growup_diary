package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    var bt = BluetoothSPP(mContext)
    var db = FirebaseFirestore.getInstance()
    val BLUETOOTH_REQ = 1000
    override fun setupEvents() {
        bt.setOnDataReceivedListener { data, message ->
            makeText(mContext,"데이터 : ${data} / 메세지 : ${message}", LENGTH_SHORT).show()
        }

        find_bt_device_btn.setOnClickListener {

//          장치 연결 여부 확인 => 장치 연결 => 센서 값 계속해서 전송 => 값 옆의 버튼 누르면 해당 값 저장(파이버에이스)
//            onStart()

        }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setValues()
        setupEvents()
    }

    override fun onStart() {
        super.onStart()
        if (!bt.isBluetoothEnabled ) {
            Log.d("log", "블루투스 켜짐, 데이터 수신 시작...")
        } else {
            Log.d("log", "블루투스 꺼짐")
//            bt.autoConnect("HC-06")

//            Toast.makeText(mContext, "블루투스 연결상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            val measuere = Intent( mContext, LoginActivity::class.java)
            bt.startService(BluetoothState.DEVICE_OTHER)
//            val intent = Intent(mContext, DeviceList::class.java)
//            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
//            onActivityResult(BLUETOOTH_REQ, Activity.RESULT_OK, measuere)
//            bt.send("Message", true)
//            bt.setOnDataReceivedListener { data, message ->
//                Log.d("log", "데이터 : ${data} / 메세지 : ${message}")
//            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == BLUETOOTH_REQ) bt.connect(data)
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
                bt.setupService()
//                setup()
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

}