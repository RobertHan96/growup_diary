package com.studiofirstzero.growup_diary.Utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ErrorHandlerUtils() {

    enum class Message {
        LoginFail, NeedAuthentication, InvaildPost, PostIsEmpty, PostUploadFail, PostDeleteFail, ImageUploadFail, BLDeviceConnected, BLDeviceDisConnected, NoBLDevice, NoNetwrokConnetcion
    }

    enum class MessageType(val type : String) {
        LoginFail("로그인 실패, 입력한 계정 정보를 확인해주세요."),
        NeedAuthentication("기능 사용을 위해 로그인해주세요."),
        InvaildPost("내용 작성, 사진을 첨부해주세요."),
        PostIsEmpty("작성한 게시글 내역이 없습니다."),
        PostUploadFail("게시글 작성 실패 - 1001"),
        PostDeleteFail("게시글 작성 실패 - 1001"),
        ImageUploadFail("이미지를 저장하지 못했습니다. 잠시 후 다시 시도해주세요."),
        BLDeviceConnected("측정기와 연결되었습니다."),
        BLDeviceDisConnected("측정기와 연결이 해제되었습니다."),
        NoBLDevice("연결된 측정기가 없습니다, 연결 버튼을 통해 연결해주세요."),
        NoNetwrokConnetcion("네트워크 연결 상태를 확인해주세요.")
    }

    fun toastError(activity : AppCompatActivity, error_type : MessageType) {
            Toast.makeText(activity, error_type.type , Toast.LENGTH_SHORT).show()
    }

}