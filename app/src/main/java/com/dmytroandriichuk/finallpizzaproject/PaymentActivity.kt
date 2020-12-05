package com.dmytroandriichuk.finallpizzaproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        findViewById<Button>(R.id.paymentConfirmButton).setOnClickListener {
            val newIntent = Intent(this@PaymentActivity, ShowOrdersActivity::class.java).apply {
                putExtras(intent)
            }
            startActivity(newIntent)
        }
    }
}