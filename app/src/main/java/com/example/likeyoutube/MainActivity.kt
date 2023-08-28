package com.example.likeyoutube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.fragment.ExploreFragment
import com.example.likeyoutube.fragment.HomeFragment
import com.example.likeyoutube.fragment.LibraryFragment
import com.example.likeyoutube.fragment.SubscriptionsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var frameLayout: FrameLayout

    private lateinit var userprofile_image: ImageView

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    private lateinit var auth: FirebaseAuth
//    private lateinit var user : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setTitle("")

        bottomNavigationView = binding.bottomNavigation
        frameLayout = binding.frameLayout

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        userprofile_image = binding.userProfileImage

        val gsc = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode("812820590609-digu3ln7t2ue7p3o5835du2lo5hq7kre.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gsc)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    val homeFragment = HomeFragment()
                    selectedFragment(homeFragment)
                    true
                }
                R.id.explore -> {
                    val exploreFragment = ExploreFragment()
                    selectedFragment(exploreFragment)
                    true
                }
                R.id.publish -> {
                    Toast.makeText(this, "Add a video", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.subscriptions -> {
                    val subscriptionsFragment = SubscriptionsFragment()
                    selectedFragment(subscriptionsFragment)
                    true
                }
                R.id.library -> {
                    val libraryFragment = LibraryFragment()
                    selectedFragment(libraryFragment)
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.home

        userprofile_image.setOnClickListener {
            if (user != null) {
                Toast.makeText(this, "User Already Sign In", Toast.LENGTH_SHORT).show()
            } else {
                showDialogue()
            }

        }
    }

    private fun showDialogue() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)

        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val view = LayoutInflater.from(applicationContext)
            .inflate(R.layout.item_signin_dialogue, viewGroup, false)

        builder.setView(view)
        val txt_google_signIn = view.findViewById<TextView>(R.id.txt_google_signIn)
        txt_google_signIn.setOnClickListener {
            signIn()
        }
        builder.create().show()
    }

    private fun signIn() {
        mGoogleSignInClient.signOut()
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("ttt", "email - ${account.email}")

                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (task.isSuccessful) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser

                        val map = HashMap<String, Any?>()
                        map.put("username", account.displayName)
                        map.put("email", account.email)
                        map.put("profile", account.photoUrl)
                        map.put("uid", firebaseUser?.uid)
                        map.put("search", account.displayName?.toLowerCase())

                        val reference = FirebaseDatabase.getInstance().getReference().child("Users")
                        firebaseUser?.uid?.let { it -> reference.child(it).setValue(map) }
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()

                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectedFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notification -> {
                Toast.makeText(this, "Notification", Toast.LENGTH_SHORT).show()
            }
            R.id.search -> {
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show()

            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

}
