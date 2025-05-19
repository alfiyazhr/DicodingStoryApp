package com.example.dicodingstoryapp.utils

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.example.dicodingstoryapp.R

class CustomEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addTextChangedListener {
            val inputText = it.toString()
            if (inputText.length < 8) {
                error = context.getString(R.string.error_password_too_short)
                setTextColor(Color.RED)
            } else {
                error = null
                setTextColor(Color.BLACK)
            }
        }
    }
}
