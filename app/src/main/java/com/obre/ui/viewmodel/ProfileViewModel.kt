package com.obre.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    val username = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
}