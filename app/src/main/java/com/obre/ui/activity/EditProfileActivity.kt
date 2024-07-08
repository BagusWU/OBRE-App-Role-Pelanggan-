package com.obre.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.obre.R
import com.obre.databinding.ActivityEditProfileBinding
import com.obre.ui.custom.CircleImage
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var storageRef: StorageReference
    private var selectedImageUri: Uri? = null

    private var imageUrl : String ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar : Toolbar = findViewById(R.id.edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.edit_profile)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        val herePhoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val username = intent.getStringExtra("username") ?: ""

        Log.d("Edit", "$herePhoneNumber")

        binding.etUsername.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                println("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.contains(" ") == true) {
                    binding.etUsername.error = "Spasi tidak diperbolehkan"
                }
            }
            override fun afterTextChanged(s: Editable?) {
                println("Not yet implemented")
            }
        })

        binding.apply {
            val currentUser = FirebaseAuth.getInstance().currentUser

            // Set username
            currentUser?.displayName?.let { displayName ->
                etUsername.setText(displayName)
            } ?: run {
                etUsername.setText(username)
            }

            herePhoneNumber?.takeIf { it.isNotBlank() }?.let { phoneNumber ->
                etPhoneNumber.setText(phoneNumber)
            } ?: run {
                currentUser?.phoneNumber?.let { phoneNumber ->
                    etPhoneNumber.setText(phoneNumber)
                } ?: run {
                    etPhoneNumber.setText("")
                }
            }

            imageUrl?.takeIf { it.isNotBlank() }?.let { photoUrl ->
                Picasso.get().load(photoUrl).resize(300, 300).transform(CircleImage()).centerCrop().into(ivProfilePicture)
            } ?: run {
                currentUser?.photoUrl?.let { photoUri ->
                    Picasso.get().load(photoUri).resize(300, 300).transform(CircleImage()).centerCrop().into(ivProfilePicture)
                } ?: run {
                    ivProfilePicture.setImageResource(R.drawable.user_picture)
                }
            }

            storageRef = FirebaseStorage.getInstance().reference.child("profile_images_costumers")

            btnChooseImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE)
            }
            binding.progressbarEdit.visibility = View.GONE

            btnSaveChanges.setOnClickListener {
                val newUsername = etUsername.text.toString()
                val newPhoneNumber = etPhoneNumber.text.toString()

                if (newUsername.isEmpty() || newUsername.contains(" ")){
                    binding.etUsername.error = "Harap isi tanpa spasi"
                } else if (newPhoneNumber.isEmpty()) {
                    binding.etPhoneNumber.error = "Harap isi nomor telepon"
                } else if (!isValidPhoneNumber(newPhoneNumber)) {
                    binding.etPhoneNumber.error = "Nomor telepon tidak sesuai. Gunakan +62 atau 0"
                }
                else {
                    binding.progressbarEdit.visibility = View.VISIBLE

                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newUsername)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.progressbarEdit.visibility = View.GONE
                                selectedImageUri?.let { uri ->
                                    uploadImageToFirebaseStorage(user.uid, uri, newUsername, newPhoneNumber)
                                } ?: run {
                                    if (imageUrl != null) {
                                        updateUserDetails(user.uid, newUsername, newPhoneNumber, imageUrl)
                                        finish()
                                    } else {
                                        updateUserDetails(user.uid, newUsername, newPhoneNumber, null)
                                        finish()
                                    }
                                }
                            } else {
                                binding.progressbarEdit.visibility = View.GONE
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Failed to update profile",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = "^\\+?((62)|0)[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,4}[-.\\s]?\\d{1,9}\$".toRegex()
        return phoneNumber.matches(phoneRegex)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.ivProfilePicture.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToFirebaseStorage(uid: String, imageUri: Uri, username: String, phoneNumber: String) {
        val imageRef = storageRef.child("$uid.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateUserDetails(uid, username, phoneNumber, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EditProfileActivity, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserDetails(uid: String, username: String, phoneNumber: String, imageUrl: String?) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("UserPelanggan").document(uid)

        val userData = hashMapOf(
            "username" to username,
            "phoneNumber" to phoneNumber,
            "photoUrl" to imageUrl
        ).toMap()

        userRef.update(userData)
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EditProfileActivity, "Failed to update profile: $e", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_IMAGE = 100
    }
}
