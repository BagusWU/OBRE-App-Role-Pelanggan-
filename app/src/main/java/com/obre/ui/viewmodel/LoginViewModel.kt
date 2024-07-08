package com.obre.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.obre.utils.Event

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val sharedPreferences = application.getSharedPreferences("myPreferences", Context.MODE_PRIVATE)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastText = MutableLiveData<Event<String>>()
    val toastText: LiveData<Event<String>> = _toastText

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean>
        get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    init {
        _loginResult.value = false
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null && user!!.isEmailVerified) {
                        _loginResult.value = true
                        saveLoginStatus(true)
                        callback(true, null)
                    } else {
                        _loginResult.value = false
                        val errorMessage = "Email belum diverifikasi. Silakan cek email Anda dan verifikasi."
                        _errorMessage.value = errorMessage
                        callback(false, errorMessage)
                    }
                } else {
                    _loginResult.value = false
                    val errorMessage = "Login gagal. Pastikan email dan password Anda benar."
                    _errorMessage.value = errorMessage
                    callback(false, errorMessage)
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
        _loginResult.value = false
        saveLoginStatus(false)
    }

    fun saveLoginStatus(isLoggedIn: Boolean) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

}