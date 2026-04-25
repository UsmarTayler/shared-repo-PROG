package com.example.budgie_budgetapp.utils

import java.security.MessageDigest

const val PREFS_NAME = "budgie_prefs"
const val KEY_USER_ID = "user_id"
const val KEY_USERNAME = "username"
const val KEY_EMAIL = "email"

fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
