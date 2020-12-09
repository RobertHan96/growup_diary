package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.Utils.ConnectionStateMonitor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setValues()
        setupEvents()
    }

    override fun onResume() {
        super.onResume()
        checkNetworkConnection(mContext)
    }

    override fun onStop() {
        super.onStop()
        checkNetworkConnection(mContext)
    }

    private fun checkNetworkConnection(context: Activity) {
        ConnectionStateMonitor(context, {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }, {
            Toast.makeText(context, "원활한 기능 사용을 인터넷을 연결해주세요.", LENGTH_SHORT).show()
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        })
    }

    override fun setupEvents() {
        write_diary_btn.setOnClickListener {
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

        mypageBtn.setOnClickListener {
            val signUP = Intent( mContext, LoginActivity::class.java)
            startActivity(signUP)
        }

    }

    override fun setValues() {

    }
}