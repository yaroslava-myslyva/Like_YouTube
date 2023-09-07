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
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.fragment.ExploreFragment
import com.example.likeyoutube.fragment.HomeFragment
import com.example.likeyoutube.fragment.LibraryFragment
import com.example.likeyoutube.fragment.SubscriptionsFragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.youtube.YouTubeScopes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okio.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var frameLayout: FrameLayout

    private lateinit var userprofile_image: ImageView

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val RC_SIGN_IN = 100
    private val REQUEST_AUTHORIZATION = 101

    private lateinit var auth: FirebaseAuth
//    private lateinit var user : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.title = ""

        bottomNavigationView = binding.bottomNavigation
        frameLayout = binding.frameLayout

        auth = FirebaseAuth.getInstance()


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

        binding.icon.setOnClickListener {
            showDialogue()
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

        val gsc = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           // .requestIdToken("812820590609-t2kgrbk4esfncnimvghmc1ah41222fpl.apps.googleusercontent.com")
          //  .requestIdToken("812820590609-d6cgvde2vfkolhtf0cd5svpr5t7rvgt2.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope(YouTubeScopes.YOUTUBE))
            .build()


        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi<GoogleSignInOptions>(Auth.GOOGLE_SIGN_IN_API, gsc)
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gsc)
       // mGoogleSignInClient.signOut()


        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("ttt", "email - ${account.email}")
                Log.d("ttt", "token - ${account.idToken}")
                Log.d("ttt", "id - ${account.id}")

                val credential: GoogleAccountCredential = GoogleAccountCredential
                    .usingOAuth2(this, Collections.singleton(YouTubeScopes.YOUTUBE))
                    .setSelectedAccountName(account.email)
                Log.d("ttt", "credential - $credential")

                MainScope().launch(Dispatchers.IO) {
                    Log.d("ttt", "credential token - ${credential.token}")
                    val youTubeApiClient = YouTubeApiClient(credential, this@MainActivity)
                    val list = youTubeApiClient?.getPlaylists()
                    Log.d("ttt", "list - $list")
                }
            } catch (e: Exception) {
                Log.d("ttt", "exception - ${e.message}, ${e.stackTrace} ${e.javaClass}")
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

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }
}

