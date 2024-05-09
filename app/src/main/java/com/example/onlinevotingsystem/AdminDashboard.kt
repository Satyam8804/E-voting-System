package com.example.onlinevotingsystem

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboard : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24)

        drawerLayout = findViewById(R.id.drawer_layout)


        navView = findViewById(R.id.nav_view)

        // Set up the action bar toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        fetchActiveElections()


        // Set up navigation item click listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_add_party -> {
                    val partyIntent = Intent(this,AddParty::class.java)
                    startActivity(partyIntent)
                    true
                }
                R.id.nav_organize_election -> {
                    val electionIntent = Intent(this,AddElections::class.java)
                    startActivity(electionIntent)
                    true
                }
                R.id.nav_add_candidates -> {
                    val candidateIntent = Intent(this,AddCandidate::class.java)
                    startActivity(candidateIntent)
                    true
                }
                R.id.show_election_list -> {
                    val showElectionIntent = Intent(this,ShowElectionList::class.java)
                    startActivity(showElectionIntent)
                    true
                }
                R.id.show_candidate_list -> {
                    val candidateIntent = Intent(this,ViewCandidate::class.java)
                    startActivity(candidateIntent)
                    true
                }
                R.id.verify_voters -> {
                    val verifyIntent = Intent(this,VoterRegistrationAdmin::class.java)
                    startActivity(verifyIntent)
                    true
                }
                R.id.logout -> {
                    val logoutIntent = Intent(this,Auth::class.java)
                    startActivity(logoutIntent)
                    true
                }
                // Handle other menu items if needed
                else -> false
            }
        }
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
    private fun displayActiveElections(activeElections: List<VotingModel>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewActiveElections)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ActiveStatsAdapter(activeElections)
        recyclerView.adapter = adapter
    }

}