package com.obre.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.obre.R
import com.obre.databinding.FragmentProfileBinding
import com.obre.ui.activity.EditProfileActivity
import com.obre.ui.activity.auth.LoginActivity
import com.obre.ui.custom.CircleImage
import com.obre.ui.viewmodel.LoginViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding
    private lateinit var loginViewModel : LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        UserProfile()

        binding?.btnLogout?.setOnClickListener {
            loginViewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }

    private fun UserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("UserPelanggan").document(currentUser?.uid ?: "")

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userId = document.id
                    val username = document.getString("username")
                    val phoneNumber = document.getString("phoneNumber")
                    val userEmail = document.getString("email")
                    var imageUrl: String?
                    imageUrl = document.getString("photoUrl") ?: ""
                    val fullname = document.getString("fullname")

                    binding?.tvUsername?.text = username
                    binding?.tvProfileId?.text = userId
                    binding?.tvProfilePhoneNumber?.text = phoneNumber
                    binding?.tvProfileEmail?.text = userEmail
                    binding?.tvProfileNameUser?.text = fullname


                    binding?.ivProfilePicture?.let { imageView ->
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get()
                                .load(imageUrl)
                                .resize(300, 300)
                                .centerCrop()
                                .placeholder(R.drawable.user_picture)
                                .error(R.drawable.user_picture)
                                .transform(CircleImage())
                                .into(imageView)
                        } else {
                            Picasso.get()
                                .load(R.drawable.user_picture)
                                .resize(300, 300)
                                .centerCrop()
                                .placeholder(R.drawable.user_picture)
                                .error(R.drawable.user_picture)
                                .transform(CircleImage())
                                .into(imageView)
                        }
                    }

                    binding?.btnEditProfile?.setOnClickListener {
                        val intent = Intent(requireContext(), EditProfileActivity::class.java)
                        intent.putExtra("username", username)
                        intent.putExtra("phoneNumber", phoneNumber)
                        intent.putExtra("imageUrl", imageUrl)
                        startActivity(intent)
                    }
                } else {
                    Log.d("ProfileFragment", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProfileFragment", "get failed with ", exception)
            }
    }

    fun refreshFragment() {
        UserProfile()
    }

    override fun onResume() {
        super.onResume()
        refreshFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
