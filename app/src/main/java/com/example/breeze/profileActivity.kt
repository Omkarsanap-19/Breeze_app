package com.example.breeze

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.databinding.ActivityMainBinding
import com.example.breeze.databinding.ActivityProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.system.exitProcess

class profileActivity : AppCompatActivity() {
    object UniversalVariable {
        var sharedValue: String = "en"
    }


    lateinit var binding: ActivityProfileBinding
    lateinit var bottom_dialog: BottomSheetDialog
    lateinit var fileRefer: DatabaseReference
    lateinit var dialog: Dialog
    lateinit var bhasha: String
    lateinit var passDialog : BottomSheetDialog
    lateinit var gender_Dialog : BottomSheetDialog
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
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        fetchGenderFromFirebase()

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

            FirebaseAuth.getInstance().signOut()

            Toast.makeText(this@profileActivity, "Logout successfully!!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,loginActivity::class.java))


        }

        binding.changePass.setOnClickListener {

            changePassword()

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
                    binding.etName.setText(name)
                    binding.etName.isCursorVisible = false
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

        val lang = bottom_dialog.findViewById<TextView>(R.id.lang)
        val theme = bottom_dialog.findViewById<TextView>(R.id.theme)
        val logout = bottom_dialog.findViewById<TextView>(R.id.log_out)

        lang?.setOnClickListener {
            val options = arrayOf("English", "Hindi", "Bengali")
            val builder1 = AlertDialog.Builder(this)
            builder1.setTitle("Choose Language")
            builder1.setSingleChoiceItems(
                options,
                -1,
                DialogInterface.OnClickListener { dialog, which ->
                    UniversalVariable.sharedValue = options[which]
                    dialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this@profileActivity, "Language changed successfully to ${UniversalVariable.sharedValue}", Toast.LENGTH_SHORT).show()
                })
            builder1.show()
        }
        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            Toast.makeText(this@profileActivity, "Logout successfully!!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,loginActivity::class.java))
            


        }
        theme?.setOnClickListener {
            val options = arrayOf("Light", "Dark")
            val builder1 = AlertDialog.Builder(this)
            builder1.setTitle("Choose Theme")
            builder1.setSingleChoiceItems(options, -1, DialogInterface.OnClickListener { dialog, which ->
//                    val them = options[which]
//                if (them == "Light"){
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                }else{
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                }
                    dialog.dismiss()

                })
            builder1.show()
        }
    }
}