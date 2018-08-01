package paluch.ivanna.instagram.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*
import paluch.ivanna.instagram.R
import paluch.ivanna.instagram.models.User

class ProfileActivity : BaseActivity(4) {
    private val TAG = "ProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")



        edit_profile_btn.setOnClickListener{
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
            username_toolbar
        }

        val auth = FirebaseAuth.getInstance()
        val user =auth.currentUser
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(user!!.uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled", error.toException())
            }

            override fun onDataChange(data: DataSnapshot) {
                val user = data.getValue(User::class.java)
                username_toolbar.setText(user!!.username, TextView.BufferType.EDITABLE)
            }

        })
    }
}
