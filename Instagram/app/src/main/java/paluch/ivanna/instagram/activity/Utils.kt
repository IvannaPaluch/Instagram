package paluch.ivanna.instagram.activity

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import paluch.ivanna.instagram.R
import paluch.ivanna.instagram.utils.GlideApp

class ValueEventListenerAdapter(val handler: (DataSnapshot) -> Unit) : ValueEventListener {
        private val TAG = "ValueEventListenerAdapt"
        override fun onDataChange(data: DataSnapshot) {
            handler(data)
        }
        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled: ", error.toException())
        }
    }

    fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
    }

    fun cordinateBntandInput(btn: Button, vararg inputs: EditText){
    val watcher = object : TextWatcher{
        override fun afterTextChanged(p0: Editable?) {
            btn.isEnabled = inputs.all {it.text.isNotEmpty()}
        }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
    inputs.forEach{it.addTextChangedListener(watcher)}
    btn.isEnabled = inputs.all {it.text.isNotEmpty()}
    }



    fun ImageView.loadUserPhoto(photoUrl: String?) {
        if (!(context as Activity).isDestroyed) {
            GlideApp.with(this).load(photoUrl).fallback(R.drawable.person_icon).into(this)
        }
    }

    fun Editable.toStringOrNull(): String? {
        val str = toString()
        return if (str.isEmpty()) null else str
    }