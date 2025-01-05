package com.example.breeze

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class loginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        binding.textSignup.setOnClickListener {
            intent = Intent(this,signUp::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {


            val email = binding.emailLogin.text.toString()
            val pass = binding.passLogin.text.toString()

            if (email.isEmpty()) binding.emailLogin.error= "Email cannot be empty."
            if (pass.isEmpty()) Snackbar.make(binding.main,"Password cannot be empty", Snackbar.LENGTH_SHORT).show()
            else{
                binding.progressBar.visibility = View.VISIBLE
                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {

                    if (it.isSuccessful){

                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this,"login successfully !!!", Toast.LENGTH_SHORT).show()
                        intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }

                }

            }

        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)

        binding.gLogin.setOnClickListener{

            googleSignInClient.signOut()
            signInGoogle()
        }





    }

    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (firebaseAuth.currentUser!=null){
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}