package com.dmytroandriichuk.finallpizzaproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            Log.i("auth", "onStart: registered")
            intent = Intent(this@MainActivity, OrderPizzaActivity::class.java)
            startActivity(intent)
        } else {
            Log.i("auth", "onStart: not registered")
        }

        emailET = findViewById(R.id.emailET)
        passwordET = findViewById(R.id.passwordET)
        progressBar = findViewById(R.id.logInProgressBar)

        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        val signInButton = findViewById<Button>(R.id.SignInButton)
        signInButton.setOnClickListener {
            userLogIn()
        }

        val forgetPasswordButton = findViewById<TextView>(R.id.ForgetPasswordTV)
        forgetPasswordButton.setOnClickListener {
            // TODO not implemented
        }

        // Write a message to the database
//        val database = Firebase.database
//        val myRef = database.getReference("message")
//
//        myRef.setValue("Hello, World!")

        val registerButton = findViewById<TextView>(R.id.registertTV)
        registerButton.setOnClickListener {
            goToRegisterActivity()
        }
    }

    private fun userLogIn() {
        val email = emailET.text.toString().trim()
        val password = passwordET.text.toString().trim()

        var errors = false
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "invalid email"
            errors = true
        } else {
            emailLayout.error = ""
        }

        if (email.isEmpty()) {
            emailLayout.error = "email is required"
            errors = true
        } else {
            emailLayout.error = ""
        }

        if (password.isEmpty()) {
            passwordLayout.error = "password is required"
            errors = true
        } else {
            passwordLayout.error = ""
        }

        if (password.length < 6) {
            passwordLayout.error = "password must be at least 6 characters"
            errors = true
        } else {
            passwordLayout.error = ""
        }

        if (!errors){
            progressBar.visibility = View.VISIBLE
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                progressBar.visibility = View.GONE
                if(it.isSuccessful){
                    val user = mAuth.currentUser
                    if (user != null) {
                        if (user.isEmailVerified){
                            intent = Intent(this@MainActivity, OrderPizzaActivity::class.java)
                            startActivity(intent)
                        } else {
                            user.sendEmailVerification()
                            Toast.makeText(this, "Account is not verified", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to sign in", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToRegisterActivity() {
        intent = Intent(this@MainActivity, RegistrationActivity::class.java)
        startActivity(intent)
    }
}

