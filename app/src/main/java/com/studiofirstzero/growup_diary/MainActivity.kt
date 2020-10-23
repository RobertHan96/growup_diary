package com.studiofirstzero.growup_diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    var bt = BluetoothSPP(mContext)
    var db = FirebaseFirestore.getInstance()
    override fun setupEvents() {
        find_bt_device_button.setOnClickListener {
//          장치 연결 여부 확인 => 장치 연결 => 센서 값 계속해서 전송 => 값 옆의 버튼 누르면 해당 값 저장(파이버에이스)
            onStart()
        }

        write_diary_button.setOnClickListener {
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
            val writeDiary = Intent( mContext, WriteDiary::class.java)
            startActivity(writeDiary)

        }

        grwonup_timeline_button.setOnClickListener {
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
            val timeLine = Intent( mContext, TimeLine::class.java)
            startActivity(timeLine)

        }
    }

    override fun setValues() {
        val imgUrl = "https://ww.namu.la/s/05ec98b51857397ed529d9ddda765c086dca98a7173574e11dee206b99a0e0538e91a96918ab27da1d95f984087a9b172eb9bdfd1b5ad134f0d570b52fc3fde6c30d473d6b9a4c0cf5dd637b3b076156d115b0e6288d4f546e5584f778c6020d"
        Glide.with(mContext).load(imgUrl).into(test_img)
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
            Log.d("log", "블루투스 꺼짐")
        } else {
            Log.d("log", "블루투스 켜짐, 장치 검색 시작...")
            bt.startService(BluetoothState.DEVICE_OTHER)
//            val intent = Intent(mContext, DeviceList::class.java)
//            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
        }
    }

}