package com.obre.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.obre.utils.Event
import com.obre.utils.ProgressResult

class RegisterViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastText = MutableLiveData<Event<String>>()
    val toastText: LiveData<Event<String>> = _toastText

    private val _registerResult = MutableLiveData<ProgressResult>()
    val registerResult: LiveData<ProgressResult>
        get() = _registerResult

    fun register(email: String, password: String, fullname: String, username: String) {
        _isLoading.value = true
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            val userData = hashMapOf(
                                "email" to email,
                                "fullname" to fullname,
                                "username" to username,
                                "createAt" to FieldValue.serverTimestamp()
                            )

                            user.uid.let { userId ->
                                firestore.collection("UserPelanggan")
                                    .document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        _registerResult.value = ProgressResult(isSuccess = true, message = "Registration successful")
                                    }
                                    .addOnFailureListener { e ->
                                        _registerResult.value = ProgressResult(isSuccess = false, message = e.message)
                                    }
                            }
                        }
                        else {
                            _registerResult.value = ProgressResult(isSuccess = false, message = task.exception?.message)
                        }
                    }
                }
            }
    }

    companion object {
        private const val TAG = "RegisterViewModel"
    }
}