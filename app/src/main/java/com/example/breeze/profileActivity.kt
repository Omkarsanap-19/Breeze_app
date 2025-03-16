package com.example.breeze

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.Global.putString
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.databinding.ActivityMainBinding
import com.example.breeze.databinding.ActivityProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlin.system.exitProcess
import android.Manifest
import android.os.Handler
import android.widget.EditText

class profileActivity : AppCompatActivity() {
    object UniversalVariable {
        var sharedValue: String = "en"
    }


    lateinit var binding: ActivityProfileBinding
    lateinit var bottom_dialog: BottomSheetDialog
    lateinit var fileRefer: DatabaseReference
    lateinit var dialog: Dialog


    lateinit var passDialog: BottomSheetDialog
    lateinit var gender_Dialog: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        bottom_dialog = BottomSheetDialog(this)
        bottom_dialog.setContentView(R.layout.bottom_dialog)
        bottom_dialog.setCancelable(true)
        bottom_dialog.setCanceledOnTouchOutside(true)

        bottomDialog()

        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        binding.userMail.text = userEmail

        binding.arrowBack.setOnClickListener {
            onBackPressed()
        }

        fetchGenderFromFirebase()

        theme_dialog()

        binding.gender.setOnClickListener {

            gender_Dialog = BottomSheetDialog(this)
            gender_Dialog.setContentView(R.layout.gender_dialog)
            gender_Dialog.setCancelable(true)
            gender_Dialog.setCanceledOnTouchOutside(true)

            gender_Dialog.show()
            val male = gender_Dialog.findViewById<TextView>(R.id.male)
            val female = gender_Dialog.findViewById<TextView>(R.id.female)

            male?.setOnClickListener {
                binding.genData.text = male.text
                if (binding.genData.text.toString().isNotEmpty()) {
                    saveGenderToFirebase(binding.genData.text.toString())
                }
                gender_Dialog.dismiss()
            }

            female?.setOnClickListener {
                binding.genData.text = female.text
                if (binding.genData.text.toString().isNotEmpty()) {
                    saveGenderToFirebase(binding.genData.text.toString())
                }
                gender_Dialog.dismiss()
            }

        }

        binding.name.setOnClickListener {

            showInputDialog()

        }

        fetchNameFromFirebase()

        binding.etName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val enteredName = binding.etName.text.toString()

                if (enteredName.isNotEmpty()) {

                    saveNameToFirebase(enteredName)

                    // Close the keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etName.windowToken, 0)

                    binding.etName.isCursorVisible = false

                    true
                } else {
                    binding.etName.error = "Name cannot be empty"
                    false
                }
            } else {
                false
            }
        }

        val database = FirebaseDatabase.getInstance()
        fileRefer = database.reference.child("bookmarks").child(userId)

        countFiles()

        binding.logoutBtn.setOnClickListener {

            askatoleave()


        }


        binding.changePass.setOnClickListener {

            changePassword()

        }

        binding.lang.setOnClickListener {
            binding.setting.performClick()
        }


    }




    private fun changePassword() {
        passDialog = BottomSheetDialog(this)
        passDialog.setContentView(R.layout.change_pass)
        passDialog.setCancelable(true)
        passDialog.setCanceledOnTouchOutside(true)

        val cr_pass = passDialog.findViewById<TextInputEditText>(R.id.cr_pass)
        val new_pass = passDialog.findViewById<TextInputEditText>(R.id.new_pass)
        val submit = passDialog.findViewById<Button>(R.id.submit)
        val cancel = passDialog.findViewById<Button>(R.id.cancel)

        submit?.setOnClickListener {
            val currentPassword = cr_pass?.text.toString()
            val newPassword = new_pass?.text.toString()

            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    // Re-authenticate the user with the current password
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            // Re-authentication successful, now update the password
                            updatePassword(newPassword)
                        }
                        .addOnFailureListener { e ->
                            // Error in re-authentication
                            Toast.makeText(
                                this,
                                "Authentication failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(
                    this,
                    "Please enter both current and new password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        cancel?.setOnClickListener {

            passDialog.dismiss()
        }

        passDialog.show()

    }

    private fun updatePassword(newPassword: String) {

        val user = FirebaseAuth.getInstance().currentUser

        if (newPassword.length >= 6) {

            user?.updatePassword(newPassword)?.addOnSuccessListener {
                Toast.makeText(this, "Password updated successfully!!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }?.addOnFailureListener {
                Toast.makeText(this, "Password change failed: Error.", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT)
                .show()
        }

    }


    private fun countFiles() {

        fileRefer.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val fileCount = snapshot.childrenCount

                binding.noBook.text = " $fileCount"
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@profileActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }

        })


    }

    private fun saveNameToFirebase(name: String) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("user_name").child(userId)

        reference.setValue(name)
            .addOnSuccessListener {
                Toast.makeText(this, "Name saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save name: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGenderToFirebase(gender: String) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("gender").child(userId)

        reference.setValue(gender)
            .addOnSuccessListener {
                Toast.makeText(this, "Gender saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save gender: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun fetchNameFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("user_name").child(userId)

        reference.get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java)
                if (!name.isNullOrEmpty()) {
                    binding.nameCnf.text = name

                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch name: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun fetchGenderFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("gender").child(userId)


        reference.get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java)
                if (!name.isNullOrEmpty()) {
                    binding.genData.setText(name)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch gender: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun bottomDialog() {
        binding.setting.setOnClickListener {
            bottom_dialog.show()
        }

        val bengali = bottom_dialog.findViewById<TextView>(R.id.ben)
        val hindi = bottom_dialog.findViewById<TextView>(R.id.hindi)
        val english = bottom_dialog.findViewById<TextView>(R.id.eng)

        english?.setOnClickListener {
            UniversalVariable.sharedValue = "English"
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            Toast.makeText(
                this@profileActivity,
                "Language changed successfully to ${UniversalVariable.sharedValue}",
                Toast.LENGTH_SHORT
            ).show()
        }
        hindi?.setOnClickListener {
            UniversalVariable.sharedValue = "Hindi"
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            Toast.makeText(
                this@profileActivity,
                "Language changed successfully to ${UniversalVariable.sharedValue}",
                Toast.LENGTH_SHORT
            ).show()
        }

        bengali?.setOnClickListener {
            UniversalVariable.sharedValue = "Bengali"
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            Toast.makeText(
                this@profileActivity,
                "Language changed successfully to ${UniversalVariable.sharedValue}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    private fun theme_dialog() {

        binding.theme.setOnClickListener {

            gender_Dialog = BottomSheetDialog(this)
            gender_Dialog.setContentView(R.layout.gender_dialog)
            gender_Dialog.setCancelable(true)
            gender_Dialog.setCanceledOnTouchOutside(true)

            gender_Dialog.show()
            val male = gender_Dialog.findViewById<TextView>(R.id.male)
            val female = gender_Dialog.findViewById<TextView>(R.id.female)

            male?.text = "Light"
            female?.text = "Dark"

            male?.setOnClickListener {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                gender_Dialog.dismiss()
            }

            female?.setOnClickListener {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                gender_Dialog.dismiss()
            }

        }
    }

    private fun askatoleave() {
        val builder= AlertDialog.Builder(this)
        builder.setTitle("Confirm logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setIcon(R.drawable.baseline_logout_24)
        builder.setPositiveButton("YES",DialogInterface.OnClickListener { dialog, which ->

            FirebaseAuth.getInstance().signOut()

            Toast.makeText(this@profileActivity, "Logout successfully!!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, loginActivity::class.java))

            finish()

        })
        builder.setNegativeButton("CANCEL",DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.show()


    }

    private fun showInputDialog() {
        passDialog = BottomSheetDialog(this)
        passDialog.setContentView(R.layout.user_name)
        passDialog.setCancelable(true)
        passDialog.setCanceledOnTouchOutside(true)

        val cr_pass = passDialog.findViewById<TextInputEditText>(R.id.cr_name)
        val submit = passDialog.findViewById<Button>(R.id.submit)
        val cancel = passDialog.findViewById<Button>(R.id.cancel)

        submit?.setOnClickListener {
            val name = cr_pass?.text.toString()
            if (name.isNotEmpty()) {
                saveNameToFirebase(name)

                Handler().postDelayed({
                    passDialog.dismiss()
                    fetchNameFromFirebase()
                },500)

            }else{
                Toast.makeText(this,"Enter your name.",Toast.LENGTH_SHORT).show()
            }

        }

        cancel?.setOnClickListener {

            passDialog.dismiss()
        }

        passDialog.show()
    }





}