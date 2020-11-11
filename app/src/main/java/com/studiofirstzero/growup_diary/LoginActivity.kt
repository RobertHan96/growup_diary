package com.studiofirstzero.growup_diary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.studiofirstzero.growup_diary.Utils.ConnectionStateMonitor
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {
    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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
            Toast.makeText(context, "인터넷 연결 됨", Toast.LENGTH_SHORT).show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }, {
            Toast.makeText(context, "원활한 기능 사용을 인터넷을 연결해주세요.", Toast.LENGTH_SHORT).show()
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        })
    }

    override fun setupEvents() {
        loginBtn.setOnClickListener {
            signUp()
        }

        logoutBtn.setOnClickListener {
            signOut()
        }

        revokeBtn.setOnClickListener {
            revokeUser()
        }
    }

    override fun setValues() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("log", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("log", "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(mContext, "${user?.displayName}님 환영합니다.", Toast.LENGTH_SHORT).show()
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(mContext, "로그인 실패 : 네트워크 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(user : FirebaseUser?) {
        if (user != null) {
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl
            val uid = user.uid
            userIdText.text = name
            userEmailText.text = email
            loginBtn.isEnabled = false
            logoutBtn.isEnabled = true
            revokeBtn.isEnabled = true
        } else if (user != null && isSignupAlready(user) == false) {
            registerUserInfo(user)
        } else {
            Toast.makeText(mContext, "기능 사용을 위해 로그인해주세요.", Toast.LENGTH_SHORT).show()
            logoutBtn.isEnabled = false
            revokeBtn.isEnabled = false
        }
    }

    private fun registerUserInfo(user : FirebaseUser?) {
        user.let {
            val user: MutableMap<String, Any> = HashMap()
            val name = it?.displayName.toString()
            val id = it?.email.toString()
            user["name"] =  name
            user["id"] = id
            db.collection("users").document(id).set(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("log", "DocumentSnapshot added with ID: " + id) }
                .addOnFailureListener { e -> Log.w("log", "Error adding document", e) }
        }

    }

    private fun isSignupAlready(user : FirebaseUser?) : Boolean {
        val docRef = db.collection("users").document(user?.email.toString())
        var isSignupAlready = false
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("log", "DocumentSnapshot data: ${document.data}")
                    isSignupAlready = true
                } else {
                    Log.d("log", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("log", "get failed with ", exception)
            }

        return isSignupAlready
    }

    private fun signUp() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        val user = FirebaseAuth.getInstance().currentUser
        auth.signOut()
        finish()
    }

    private fun revokeUser() {
        val user = FirebaseAuth.getInstance().currentUser
        val id = user?.email.toString()
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("log", "User account deleted. ${id}")
            }
        }

        db.collection("users").document(id)
            .delete()
            .addOnSuccessListener { Log.d("log", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("log", "Error deleting document", e) }
        finish()
    }

}