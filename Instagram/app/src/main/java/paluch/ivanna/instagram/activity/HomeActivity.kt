package paluch.ivanna.instagram.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import paluch.ivanna.instagram.R

class HomeActivity : BaseActivity(0) {

    private val TAG = "HomeActivity"
    private lateinit var mAuth :FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        mAuth = FirebaseAuth.getInstance()

        signout_text.setOnClickListener{
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser==null){
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
