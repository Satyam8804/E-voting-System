package com.example.onlinevotingsystem

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var showResultCard: CardView
    private lateinit var viewCandidatesCard: CardView
    private lateinit var register: CardView
    private lateinit var votingFaqCard: CardView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        fetchActiveElections()

        showResultCard = findViewById(R.id.showResult)
        viewCandidatesCard = findViewById(R.id.viewCandidates)
        register = findViewById(R.id.register)
        votingFaqCard = findViewById(R.id.VotingFaq)

        showResultCard.setOnClickListener {
            // Handle click for Show Results CardView
            val resultIntent = Intent(this,PublishResult::class.java)
            startActivity(resultIntent)
        }

        viewCandidatesCard.setOnClickListener {
            // Handle click for View Candidates CardView
            val candidateIntent = Intent(this,ViewCandidate::class.java)
            startActivity(candidateIntent)
        }

        register.setOnClickListener {
            // Handle click for Know the Issues CardView
            val registerIntent = Intent(this, VoterRegistration::class.java)
            startActivity(registerIntent)
        }

        votingFaqCard.setOnClickListener {
            // Handle click for Voting FAQ CardView
            val faqIntent = Intent(this,VotingFAQ::class.java)
            startActivity(faqIntent)

        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation item click listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.action_logout -> {
                    val logoutIntent = Intent(this,Auth::class.java)
                    startActivity(logoutIntent)
                    true
                }
                // Handle other menu items if needed
                else -> false
            }
        }
    }

    private fun displayActiveElections(activeElections: List<VotingModel>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewActiveElections)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ActiveElectionsAdapter(activeElections)
        recyclerView.adapter = adapter
    }

    private fun fetchActiveElections() {
        val activeElectionsRef = FirebaseDatabase.getInstance().getReference("Elections")
            .orderByChild("active")
            .equalTo(true)

        activeElectionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val activeElections = mutableListOf<VotingModel>()
                for (electionSnapshot in dataSnapshot.children) {
                    val election = electionSnapshot.getValue(VotingModel::class.java)
                    election?.let { activeElections.add(it) }
                }
                displayActiveElections(activeElections)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    drawerLayout.openDrawer(navView)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView)
        } else {
            super.onBackPressed()
        }    }
}
