package com.example.setupformaps.application

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {
    companion object {
        @SuppressLint("StaticFieldLeak")
        public lateinit var CONTEXT : Context;
        public const val MAP_STARTUP_REQUEST_CODE = 1;
        public fun isEnabled(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                CONTEXT,
                permission,
            ) == PackageManager.PERMISSION_GRANTED;
        }

        public fun request(permission : String, requestCode: Int, activity: Activity) {
            if (!isEnabled(permission)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
    }
}