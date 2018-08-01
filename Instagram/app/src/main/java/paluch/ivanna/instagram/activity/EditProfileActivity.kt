package paluch.ivanna.instagram.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_edit_profile.*
import paluch.ivanna.instagram.R
import paluch.ivanna.instagram.models.User
import paluch.ivanna.instagram.utils.CameraPictureTaker
import paluch.ivanna.instagram.utils.FirebaseHelper
import paluch.ivanna.instagram.views.PasswordDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : AppCompatActivity() , PasswordDialog.Listener{
    private val TAG ="EditProfileActivity"
    private lateinit var mUser: User
    private lateinit var mPandingUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var mImageUri: Uri
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var cameraPictureTaker: CameraPictureTaker
    private  val TAKE_PICTURE_REQUEST_CODE =1

    val simpleDateFormat  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        cameraPictureTaker = CameraPictureTaker(this)

        close_img.setOnClickListener{ finish() }
        save_img.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { cameraPictureTaker.takeCameraPicture() }

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference
        mDatabase.child("users").child(mAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                    mUser = it.getValue(User::class.java)!!
                    name_input.setText(mUser.name)
                    username_input.setText(mUser.username)
                    website_input.setText(mUser.website)
                    bio_input.setText(mUser.bio)
                    email_input.setText(mUser.email)
                    phone_input.setText(mUser.phone?.toString())
                    profile_image.loadUserPhoto(mUser.photo)
                })
    }


    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager)!=null){
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(this,
                    "paluch.ivanna.instagram.fileprovider",
                    imageFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }

    private fun createImageFile() :File{
        // Create an image file name
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${simpleDateFormat.format(Date())}_",
                ".jpg",
                storageDir
        )


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == cameraPictureTaker.REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebaseHelper.uploadUserPhoto(cameraPictureTaker.imageUri!!) {
                val photoUrl = it.downloadUrl.toString()
                mFirebaseHelper.updateUserPhoto(photoUrl) {
                    mUser = mUser.copy(photo = photoUrl)
                    profile_image.loadUserPhoto(mUser.photo)
                }
            }
        }
    }

    private fun updateProfile() {
        //get user
        //validate
        mPandingUser = readInputs()
        val error = validate(mPandingUser)
        if(error==null){
            if(mPandingUser.email==mUser.email){
                updateUser(mPandingUser)
            }else {
                PasswordDialog().show(supportFragmentManager, "password_dialog")

            }
        }else{
            showToast(error)
        }
    }

    private fun readInputs(): User {
        return User(
                name = name_input.text.toString(),
                username = username_input.text.toString(),
                email = email_input.text.toString(),
                website = website_input.text.toStringOrNull(),
                bio = bio_input.text.toStringOrNull(),
                phone = phone_input.text.toString().toLongOrNull()
        )

    }
    private fun updateUser(user: User){
        val updatesMap = mutableMapOf<String, Any?>()
        if(user.name!=mUser.name) updatesMap["name"]=user.name
        if(user.username!=mUser.username) updatesMap["username"]=user.username
        if(user.website!=mUser.website) updatesMap["website"]=user.website
        if(user.bio!=mUser.bio) updatesMap["bio"]=user.bio
        if(user.email!=mUser.email) updatesMap["email"]=user.email
        if(user.phone!=mUser.phone) updatesMap["phone"]=user.phone

        mDatabase.updateUser(mAuth.currentUser!!.uid, updatesMap) {
            showToast("Profile saved")
            finish()
        }
    }

    private fun validate(user: User):String? =
        when{
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else->null
        }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mAuth.currentUser!!.reauthenticate(credential) {
                mAuth.currentUser!!.updateEmail(mPandingUser.email) {
                    updateUser(mPandingUser)
                }
            }
        } else {
            showToast("You should enter your password")
        }
    }
    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit) {
        updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
    private fun DatabaseReference.updateUser(uid: String, updates: Map<String, Any?>,
                                             onSuccess: () -> Unit) {
        child("users").child(uid).updateChildren(updates)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess()
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
    }
}


