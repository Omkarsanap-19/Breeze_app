package com.example.breeze

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class signUp : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        binding.textLogin.setOnClickListener {
            intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
        }


        binding.registerBtn.setOnClickListener {
            val email = binding.emailSign.text.toString()
            val pass = binding.passSign.text.toString()
            val cnf_pass = binding.cnfpass.text.toString()

            if (email.isEmpty()) binding.emailSign.error = "Enter your Email"
            if (pass.isEmpty()) Toast.makeText(this,"Enter the password",Toast.LENGTH_SHORT).show()
            if (cnf_pass.isEmpty()) Snackbar.make(
                binding.main,
                "Re-enter the password",
                Snackbar.LENGTH_SHORT
            ).show()
            else {

                if (pass == cnf_pass) {

                    binding.progressBar.visibility = View.VISIBLE
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {

                            binding.progressBar.visibility = View.GONE
                            intent = Intent(this, loginActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Register successfully!!", Toast.LENGTH_SHORT)
                                .show()

                        } else {

                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                        }
                    }
                } else {

                    Snackbar.make(binding.main, "Password is not matching", Snackbar.LENGTH_SHORT)
                        .show()

                }

            }

        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.gSignup.setOnClickListener {
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



}