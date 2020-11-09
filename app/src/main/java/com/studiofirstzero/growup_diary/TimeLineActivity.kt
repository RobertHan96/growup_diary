package com.studiofirstzero.growup_diary

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.adapters.PostAdapter
import com.studiofirstzero.growup_diary.datas.Post
import kotlinx.android.synthetic.main.activity_time_line.*
import kotlin.collections.ArrayList

// 여태까지 작성했던 글들을 몰아서 보는 곳 : 리스트뷰로 구현
// SNS 공유 기능 라이브러리 찾아보기

class TimeLineActivity : BaseActivity() {
    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var documentsId = arrayListOf<String>()
    private lateinit var userID : String
    private lateinit var mGoogleSignInClient: GoogleSignInClient


    val postList = ArrayList<Post>()
    // 코드 후반부에 값초기화가 있을 경우를 대비해서 나중에 init한다는 키워드 추가
    private lateinit var mPostAdapter : PostAdapter

    override fun setupEvents() {
        postListView.setOnItemClickListener { parent, view, position, id ->
            val postDetail = Intent(mContext, PostDetailActivity::class.java)
            val clickedPost = postList.get(position)
            val clickedPostId = documentsId?.get(position)
            postDetail.putExtra("postData", clickedPost)
            postDetail.putExtra("postId", clickedPostId)
            startActivity(postDetail)
        }
    }

    override fun setValues() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.email.toString()
        mPostAdapter = PostAdapter(mContext, R.layout.post_list_item, postList)
        getPosts()

        postListView.adapter = mPostAdapter
        Handler().postDelayed({
            mPostAdapter.notifyDataSetChanged()

        }, 2000)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)
        setupEvents()
        setValues()
    }

    fun getPosts() {
        db.collection("posts")
            .whereEqualTo("id", userID)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentId = document.id
                    documentsId.add(documentId)
                    val postData = document.data.toMutableMap()
                    val post = convertToPost(postData)
                    Log.d("log", "게시글 불러오기 성공 : ${document.id} - ${post} ")
                    postList.add(post)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("log", "Error getting documents: ", exception)
            }
    }

    fun convertToPost(data: MutableMap<String, Any>) : Post {
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