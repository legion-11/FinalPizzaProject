package com.dmytroandriichuk.finallpizzaproject

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

//screen for log in
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

        val sharedPreferences = getSharedPreferences("user default", Context.MODE_PRIVATE)
        emailET.setText(sharedPreferences.getString("Email", ""))

        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        val signInButton = findViewById<Button>(R.id.SignInButton)
        signInButton.setOnClickListener {
            userLogIn()
        }

        val forgetPasswordButton = findViewById<TextView>(R.id.forgetPasswordTV)
        forgetPasswordButton.setOnClickListener {
            goToRestorePasswordActivity()
        }

        val registerButton = findViewById<TextView>(R.id.registertTV)
        registerButton.setOnClickListener {
            goToRegisterActivity()
        }
    }

    //check input and create user
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

        //send email verifiaction letter and create user
        if (!errors && progressBar.visibility == View.GONE ){
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
                            buildDialog("Account is not verified")
                        }
                    }
                } else {
                    if(isOnline()){
                        buildDialog("User not found")
                    } else {
                        buildDialog("Connection error")
                    }
                }
            }
        }
    }

    //show error message
    private fun buildDialog(message: String){
        val dialog = DialogOffline(message)
        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        dialog.show(transaction, "dialog")
    }

    private fun goToRegisterActivity() {
        intent = Intent(this@MainActivity, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun goToRestorePasswordActivity() {
        intent = Intent(this@MainActivity, RestorePasswordActivity::class.java)
        startActivity(intent)
    }

    fun goOfflineClicked() {
        intent = Intent(this@MainActivity, ShowOrdersActivity::class.java)
        startActivity(intent)
    }

    fun sendVerificationLetter() {
        mAuth.currentUser?.sendEmailVerification()
    }

    //check internet connection
    private fun isOnline(): Boolean {
        val connectivityManager =
                this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        Log.i("Internet", "No network")
        return false
    }
}

