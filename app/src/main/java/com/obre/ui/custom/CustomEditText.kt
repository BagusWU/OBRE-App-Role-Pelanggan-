package com.obre.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText
import com.obre.R

class CustomEditText : TextInputEditText {

    private var isError: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }


    private fun init() {

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val input = p0.toString()
                when (inputType) {
                    EMAIL -> {
                        if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                            error = context.getString(R.string.email_validation)
                            isError = true
                        } else {
                            isError = false
                        }
                    }
                    PASSWORD -> {
                        isError = if (input.length < 8) {
                            setError(context.getString(R.string.password_length), null)
                            true
                        } else {
                            false
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                when (inputType) {
                    EMAIL -> {
                        if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                            error = context.getString(R.string.email_validation)
                            isError = true
                        } else {
                            isError = false
                        }
                    }
                    PASSWORD -> {
                        isError = if (input.length < 8) {
                            setError(context.getString(R.string.password_length), null)
                            true
                        } else {
                            false
                        }
                    }
                }
            }
        })
    }

    companion object {
        const val EMAIL = 33
        const val PASSWORD = 129
    }
}
