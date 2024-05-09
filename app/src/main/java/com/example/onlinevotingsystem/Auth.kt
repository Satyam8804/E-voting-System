package com.example.onlinevotingsystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Auth : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        val fragmentManager = supportFragmentManager

        // Initially display the LoginFragment
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, LoginFragment())
        transaction.commit()

        // Set onClickListeners for switching between fragments
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, LoginFragment())
            transaction.commit()
        }

        findViewById<Button>(R.id.btnSignup).setOnClickListener {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, SignUpFragment())
            transaction.commit()
        }


    }
}