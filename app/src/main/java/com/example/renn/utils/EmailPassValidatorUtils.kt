package com.example.renn.utils

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class EmailPassValidatorUtils {
    // Validate email
    fun isValidEmail(email: String): Boolean{
        @Suppress("RegExpRedundantEscape", "RegExpDuplicateCharacterInClass")
        val emailAddressPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return emailAddressPattern.matcher(email).matches()
    }

    // Validate password
    fun isValidPassword(password: String): Boolean{

        // Validate password using regex
        val passwordREGEX = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+={};:<>,./?])(?=\\S+$).{8,}$"
        )
        return passwordREGEX.matcher(password).matches()
    }

    // Show password alert dialog
    fun passwordAlert(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)

        dialogBuilder.setMessage("a minimum of 1 lower case letter [a-z]\n" +
                "a minimum of 1 upper case letter [A-Z]\n" +
                "a minimum of 1 numeric character [0-9]\n" +
                "a minimum of 1 special character: !@#\$%^&*()_+={};:<>,./?\n" +
                "a minimum length of 8 characters")
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                // Do something on Ok
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Passwords must contain:")
        alert.show()
    }
}