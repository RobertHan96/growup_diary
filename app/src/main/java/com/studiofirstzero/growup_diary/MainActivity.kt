package com.studiofirstzero.growup_diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    var bt = BluetoothSPP(mContext)

    override fun setupEvents() {
        find_bt_device_button.setOnClickListener {
//          장치 연결 여부 확인 => 장치 연결 => 센서 값 계속해서 전송 => 값 옆의 버튼 누르면 해당 값 저장(파이버에이스)
            onStart()
        }
        write_diary_button.setOnClickListener {
            val writeDiary = Intent( mContext, WriteDiary::class.java)
            startActivity(writeDiary)
        }
        grwonup_timeline_button.setOnClickListener {
            val timeLine = Intent( mContext, TimeLine::class.java)
            startActivity(timeLine)
        }
    }

    override fun setValues() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        if (!bt.isBluetoothEnabled ) {
            Log.d("log", "블루투스 꺼짐")
        } else {
            Log.d("log", "블루투스 켜짐, 장치 검색 시작...")
            bt.startService(BluetoothState.DEVICE_OTHER)
            val intent = Intent(mContext, DeviceList::class.java)
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
        }
    }

}