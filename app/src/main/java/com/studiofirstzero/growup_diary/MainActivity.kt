package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.Utils.ConnectionStateMonitor
import com.studiofirstzero.growup_diary.Utils.ErrorHandlerUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkNetworkConnection(mContext)
        setValues()
        setupEvents()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun checkNetworkConnection(context: Activity) {
        ConnectionStateMonitor(context, {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }, {
            // 코틀린 핵심정리 강의보고 코드 간소화 필요
            ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoNetwrokConnetcion)
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
        ConnectionStateMonitor(mContext, {
            Log.d("log", "네트워크 연결 완료")
        }, {
            ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoNetwrokConnetcion)
        })
    }
}