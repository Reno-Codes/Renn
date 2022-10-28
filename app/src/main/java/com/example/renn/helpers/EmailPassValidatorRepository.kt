package com.example.renn.helpers

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import java.util.regex.Pattern

class EmailPassValidatorRepository {
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
                "^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[!@#$%^&*()_+={};:<>,./?])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");
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