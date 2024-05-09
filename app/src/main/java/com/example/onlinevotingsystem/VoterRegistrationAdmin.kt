package com.example.onlinevotingsystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VoterRegistrationAdmin : AppCompatActivity() {
    private lateinit var recyclerViewRegistrations: RecyclerView
    private lateinit var registrationAdapter: RegistrationAdapter
    private lateinit var registrationsList: MutableList<Registration>
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voter_registration_admin)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerViewRegistrations = findViewById(R.id.recyclerView)
        recyclerViewRegistrations.layoutManager = LinearLayoutManager(this)
        registrationsList = mutableListOf()
        registrationAdapter = RegistrationAdapter(registrationsList)
        recyclerViewRegistrations.adapter = registrationAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("VoterRegistrations")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                registrationsList.clear()
                for (registrationSnapshot in dataSnapshot.children) {
                    val registration = registrationSnapshot.getValue(Registration::class.java)
                    if (registration != null) {
                        registrationsList.add(registration)
                    }
                }
                registrationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}