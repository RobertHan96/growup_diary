package com.studiofirstzero.growup_diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList


class MainActivity : BaseActivity() {
    var bt = BluetoothSPP(mContext)

    override fun setupEvents() {

    }

    override fun setValues() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onStart()

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