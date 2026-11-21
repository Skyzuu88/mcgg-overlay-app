package com.skyzuu.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("APP_SESSION", MODE_PRIVATE)
        val savedId = prefs.getString("USER_ID", null)
        val savedUsername = prefs.getString("USERNAME", null)

        if (savedId != null && savedUsername != null) {
            goToMain(savedId, savedUsername)
            return
        }

        setContentView(R.layout.activity_login)

        val etUserId = findViewById<EditText>(R.id.etUserId)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val idInput = etUserId.text.toString().trim()
            val passInput = etPassword.text.toString().trim()

            if (idInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(this, "ID dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            db.collection("users").document(idInput).get()
                .addOnSuccessListener { doc ->
                    btnLogin.isEnabled = true
                    if (doc.exists() && doc.getString("password") == passInput) {
                        val username = doc.getString("username") ?: idInput
                        prefs.edit()
                            .putString("USER_ID", idInput)
                            .putString("USERNAME", username)
                            .apply()
                        goToMain(idInput, username)
                    } else {
                        Toast.makeText(this, "ID atau Password salah", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun goToMain(id: String, username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", id)
            putExtra("USERNAME", username)
        }
        startActivity(intent)
        finish()
    }
}
