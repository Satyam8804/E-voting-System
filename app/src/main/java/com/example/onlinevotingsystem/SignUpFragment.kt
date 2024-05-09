package com.example.onlinevotingsystem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class SignUpFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var circleImage: CircleImageView
    private var linkImg: String = ""
    private var role: String = ""
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        nameEditText = view.findViewById(R.id.editTextName)
        emailEditText = view.findViewById(R.id.editTextEmail)
        passwordEditText = view.findViewById(R.id.editTextPassword)
        confirmPasswordEditText = view.findViewById(R.id.editTextConfirmPassword)
        registerButton = view.findViewById(R.id.buttonRegister)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        circleImage = view.findViewById(R.id.profile)
        role = "client"

        circleImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose the image"), 1)
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            realtimeDatabase(name, email, password, user.uid, role)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to register: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        Log.e("SignUpFragment", "Failed to register", task.exception)
                    }
                }
        }
        return view
    }

    private fun realtimeDatabase(name: String, email: String, password: String, userId: String, role: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        val user = User(name, email, password, linkImg)
        reference.setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to the next screen or perform any other necessary action
                } else {
                    Toast.makeText(requireContext(), "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SignUpFragment", "Failed to save user data", task.exception)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                Glide.with(this).load(imageUri).into(circleImage)
                onImageAdd(imageUri)
            } else {
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun onImageAdd(imageUri: Uri) {
        storageReference =
            FirebaseStorage.getInstance().reference.child("Images/${System.currentTimeMillis()}.jpg")

        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        linkImg = downloadUrl.toString()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("SignUpFragment", "Failed to upload image", e)
            }
    }
}
