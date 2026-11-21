package com.skyzuu.app

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var btnPerizinan: Button

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updatePermissionStatus()
        }

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val serviceIntent = Intent(this, OverlayService::class.java).apply {
                    putExtra("RESULT_CODE", result.resultCode)
                    putExtra("DATA_INTENT", result.data)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this, "Overlay & Mode Rekam Aktif", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin rekam layar dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvUserId = findViewById<TextView>(R.id.tvUserId)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        btnPerizinan = findViewById(R.id.btnPerizinan)
        val btnMulai = findViewById<Button>(R.id.btnMulai)

        val userId = intent.getStringExtra("USER_ID") ?: "-"
        val username = intent.getStringExtra("USERNAME") ?: "User"
        tvUserId.text = "ID: $userId"
        tvUsername.text = username

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        updatePermissionStatus()

        btnPerizinan.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Izin Overlay sudah aktif", Toast.LENGTH_SHORT).show()
            }
        }

        btnMulai.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Aktifkan Perizinan di kiri dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        if (Settings.canDrawOverlays(this)) {
            btnPerizinan.text = "IZIN AKTIF ✓"
        } else {
            btnPerizinan.text = "PERIZINAN"
        }
    }
}
