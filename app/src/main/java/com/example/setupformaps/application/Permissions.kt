package com.example.setupformaps.application

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.setupformaps.CONTEXT

class Permissions {
    companion object {
        public const val MAP_STARTUP_REQUEST_CODE = 1;
        public fun isEnabled(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                CONTEXT,
                permission,
            ) == PackageManager.PERMISSION_GRANTED;
        }

        public fun request(permission : String, requestCode: Int, activity: Activity) : Boolean {
            if (!isEnabled(permission)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permission),
                    requestCode
                )
                return false;
            }
            return true;
        }
    }
}