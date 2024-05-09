package com.example.onlinevotingsystem

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var password: EditText
    private lateinit var register: TextView
    private lateinit var loginButton: Button
    private lateinit var email: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        email = view.findViewById(R.id.emailId)
        password = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.loginButton)
        register = view.findViewById(R.id.signupText)
        firebaseAuth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val userEmail = email.text.toString()
            val pass = password.text.toString()

            if(userEmail == "Admin@gmail.com" && pass == "Admin123"){
                val intent = Intent(context, AdminDashboard::class.java)
                startActivity(intent)
                return@setOnClickListener
            }

            if (userEmail.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(userEmail, pass)
                    .addOnCompleteListener { loginTask ->

                        if (loginTask.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            fetchUserDataFromDatabase(userEmail, pass , user?.uid.toString())
                        } else {
                            handleLoginError(loginTask.exception)
                        }
                    }
            }
        }

        register.setOnClickListener {
            val intent = Intent(context, Auth::class.java)
            startActivity(intent)
        }

        sharedPreferences = requireActivity().getSharedPreferences("userData", Context.MODE_PRIVATE)
        email.setText(sharedPreferences.getString("username", ""))
        password.setText(sharedPreferences.getString("password", ""))

        return view
    }

    private fun fetchUserDataFromDatabase(userEmail: String, pass: String , userId:String) {
        val sanitizedEm = userEmail.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        val userName = user.name
                        val userImage = user.image

                        Log.d("User Data", "User Name: $userName, User Image: $userImage")

                        val editor = sharedPreferences.edit()
                        editor.putString("userId",userId)
                        editor.putString("username", userEmail)
                        editor.putString("password", pass)
                        editor.putString("userName", userName)
                        editor.putString("userImage", userImage)
                        editor.apply()

//                        val targetActivity = when (userRole) {
//                            "advocate" -> AdvocateActivity::class.java
//                            else -> MainActivity::class.java // Default interface for unknown roles
//                        }

                        // Start the MainActivity and pass the user data
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("USER_ID", userEmail)
                        intent.putExtra("userName", userName)
                        intent.putExtra("userImage", userImage)
                        startActivity(intent)
                    } else {
                        Log.e("User Data", "Failed to deserialize user data")
                    }
                } else {
                    Log.e("User Data", "DataSnapshot does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Database Error", "Error: ${databaseError.message}")
                handleLoginError(Exception("Database Error: ${databaseError.message}"))
            }
        })

    }

    private fun handleLoginError(exception: Exception?) {
        Toast.makeText(requireContext(), "Login Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
    }
}
