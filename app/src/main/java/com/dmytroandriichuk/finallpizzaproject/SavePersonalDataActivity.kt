package com.dmytroandriichuk.finallpizzaproject

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText

//save data to sharred prefferences
class SavePersonalDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_personal_data)

        val sharedPreferences = getSharedPreferences("user default", Context.MODE_PRIVATE)

        val nameET = findViewById<TextInputEditText>(R.id.saveNameET)
        val phoneET = findViewById<TextInputEditText>(R.id.savePhoneNumberET)
        val emailET = findViewById<TextInputEditText>(R.id.saveEmailET)

        nameET.setText(sharedPreferences.getString("Persons name", ""))
        phoneET.setText(sharedPreferences.getString("Phone number", ""))
        emailET.setText(sharedPreferences.getString("Email", ""))

        val button = findViewById<Button>(R.id.saveButton)
        button.setOnClickListener {
            sharedPreferences.edit().apply {
                putString("Persons name", nameET.text.toString())
                putString("Phone number", phoneET.text.toString())
                putString("Email", emailET.text.toString())
            }.apply()
            finish()
        }
    }
}