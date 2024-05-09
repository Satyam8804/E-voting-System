package com.example.onlinevotingsystem

import android.app.DatePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class VoterRegistration : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var editTextDob: EditText
    private lateinit var editTextVoterId: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewStatus: TextView
    private lateinit var ageTextView: TextView
    private lateinit var voterIdTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voter_registration)
        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@VoterRegistration, R.color.primary)))
            title = "Voting Registration"
            setDisplayHomeAsUpEnabled(true)
        }

        editTextDob = findViewById(R.id.editTextDob)
        editTextVoterId = findViewById(R.id.editTextVoterId)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewStatus = findViewById(R.id.textViewStatus)
        ageTextView = findViewById(R.id.ageTextView)
        voterIdTextView = findViewById(R.id.voteIdTextView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val reference = FirebaseDatabase.getInstance().getReference("VoterRegistrations").child(userId)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val voterRegistration = snapshot.getValue(Registration::class.java)
                    if(voterRegistration != null){
                        if (voterRegistration.isVerified) {
                            // User registration is verified
                            showEligibleStatus()
                        } else {
                            // User registration is pending or not found
                            showPendingStatus()
                        }
                    }else{
                        showForm()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to fetch user data: ${error.message}")
                }
            })
        }
        editTextDob.setOnClickListener {
            showDatePickerDialog()
        }

        buttonRegister.setOnClickListener {
            registerVoter()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, this, year, month, day).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(year, month, dayOfMonth)
        val age = calculateAge(selectedCalendar.timeInMillis)
        editTextDob.setText("$dayOfMonth/${month + 1}/$year")
    }

    private fun calculateAge(dobMillis: Long): Int {
        val dobCalendar = Calendar.getInstance().apply { timeInMillis = dobMillis }
        val todayCalendar = Calendar.getInstance()

        var age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun registerVoter() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val dobString = editTextDob.text.toString()
        val voterId = editTextVoterId.text.toString()

        if (userId != null && dobString.isNotEmpty() && voterId.isNotEmpty()) {
            val age = calculateAgeFromDOB(dobString)
            if (age >= 18 && (voterId.length >= 12)) {
                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference("VoterRegistrations")
                val voterRegistration = Registration(userId, age, voterId, isVerified = false)

                reference.child(userId).setValue(voterRegistration)
                    .addOnSuccessListener {
                        showToast("Registration successful!")
                        buttonRegister.isActivated = false
                    }
                    .addOnFailureListener { exception ->
                        showToast("Registration failed: ${exception.message}")
                    }
            } else {
                showToast("Age must be 18 or older.")
            }
        } else {
            showToast("Please fill out all fields.")
        }
    }

    private fun calculateAgeFromDOB(dob: String): Int {
        // Extract day, month, and year from the DOB string
        val parts = dob.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        // Calculate age based on the current date and the provided DOB
        val dobCalendar = Calendar.getInstance().apply { set(year, month - 1, day) }
        val todayCalendar = Calendar.getInstance()

        var age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun showEligibleStatus() {
        // Show eligibility status
        textViewStatus.text = "You Are Eligible for Voting"
        editTextDob.visibility = View.GONE
        editTextVoterId.visibility = View.GONE
        buttonRegister.visibility = View.GONE
        textViewStatus.visibility = View.VISIBLE
        ageTextView.visibility = View.GONE
        voterIdTextView.visibility = View.GONE
    }
    private fun showForm() {
        // Show voter registration form
        editTextDob.visibility = View.VISIBLE
        editTextVoterId.visibility = View.VISIBLE
        buttonRegister.visibility = View.VISIBLE
        textViewStatus.visibility = View.GONE
    }
    private fun showPendingStatus() {
        // Show pending status
        textViewStatus.text = "Registration Verification Pending ..."
        editTextDob.visibility = View.GONE
        editTextVoterId.visibility = View.GONE
        buttonRegister.visibility = View.GONE
        textViewStatus.visibility = View.VISIBLE
        ageTextView.visibility = View.GONE
        voterIdTextView.visibility = View.GONE
    }

}
