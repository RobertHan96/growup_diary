package com.studiofirstzero.growup_diary

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.studiofirstzero.growup_diary.Utils.ConnectionStateMonitor
import com.studiofirstzero.growup_diary.Utils.ErrorHandlerUtils
import com.studiofirstzero.growup_diary.adapters.PostAdapter
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_time_line.*
import kotlin.collections.ArrayList

class TimeLineActivity : BaseActivity() {
    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var mDocumentId = arrayListOf<String>()
    private lateinit var mUserID : String
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val mPosts = ArrayList<Post>()
    // 코드 후반부에 값초기화가 있을 경우를 대비해서 나중에 init한다는 키워드 추가
    lateinit var mPostAdapter : PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)
        setupEvents()
        setValues()
    }

    override fun onRestart() {
        super.onRestart()
        mPostAdapter.notifyDataSetChanged()
    }

    override fun setupEvents() {
        postListView.setOnItemClickListener { parent, view, position, id ->
            val postDetail = Intent(mContext, PostDetailActivity::class.java)
            val clickedPost = mPosts.get(position)
            val clickedPostId = mDocumentId?.get(position)
            postDetail.putExtra("postData", clickedPost)
            postDetail.putExtra("postId", clickedPostId)
            mPostAdapter.notifyDataSetChanged()
            startActivity(postDetail)
        }
    }

    override fun setValues() {
        ConnectionStateMonitor(mContext,
            {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)
                auth = FirebaseAuth.getInstance()
                mUserID = auth.currentUser?.email.toString()
                val userName = auth.currentUser?.displayName.toString()
                userNameText.text = "${userName}의 타임라인"
                mPostAdapter = PostAdapter(mContext, R.layout.post_list_item, mPosts)
                getPosts()

                mPostAdapter.notifyDataSetChanged()
                postListView.adapter = mPostAdapter
                Handler().postDelayed({
                    mPostAdapter.notifyDataSetChanged()
                    postListView.emptyView = emptyPostNotifyText

                }, 2000)

            },
            {
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.NoNetwrokConnetcion)
            }
        )
    }

    fun getPosts() {
        val docRef = db.collection("posts")
        docRef
            .whereEqualTo("id", mUserID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("log", "Listen failed.", error)
                    return@addSnapshotListener
                }
                mDocumentId.clear()
                mPosts.clear()

                for (postData in value!! ) {
                    val post = getDatafromSnapshot(postData)
                    val postDocumentId = postData.id
                    mDocumentId.add(postDocumentId)
                    mPosts.add(post)
                }
            if (mPosts.isEmpty()){
                ErrorHandlerUtils().toastError(mContext, ErrorHandlerUtils.MessageType.PostIsEmpty)

            }
        }
    }

    fun getDatafromSnapshot(data: QueryDocumentSnapshot) : Post {
        val id = data.get("id") as String
        val measureValue = data.get("measureValue") as Number
        val title = data.get("title") as String
        val content = data.get("content") as String
        val imageUrl = data.get("imageUrl") as String
        val createdAt = data.get("createdAt") as String
        val post = Post(id, measureValue, title, content, imageUrl, createdAt)
        return post
    }

}

class NoContentFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.activity_no_content, container, false)
}