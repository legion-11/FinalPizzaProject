package com.dmytroandriichuk.finallpizzaproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val card = findViewById<EditText>(R.id.editTextNumber)
        val date = findViewById<EditText>(R.id.editTextDate)
        val cardImage = findViewById<ImageView>(R.id.cardImage)

//      formate card XXXX XXXX XXXX XXXX
        card.addTextChangedListener(object: TextWatcher {
            private var current = ""
            private val nonDigits = Regex("[^0123456789]")

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    val userInput = s.toString().replace(nonDigits,"")
                    if (userInput.length <= 16) {
                        current = userInput.chunked(4).joinToString(" ")
                        s.filters = arrayOfNulls<InputFilter>(0)
                    }
                    s.replace(0, s.length, current, 0, current.length)

                    if (current.isNotEmpty() && current[0] == '4'){
                        cardImage.setImageResource(R.drawable.mastercard)
                    }else{
                        cardImage.setImageResource(R.drawable.visa)
                    }
                }
            }
        })

//      formate date MM/YYYY
        date.addTextChangedListener(object: TextWatcher{
            private var current = ""
            private val nonDigits = Regex("[^0123456789]")

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    val userInput = s.toString().replace(nonDigits,"")
                    if (userInput.length <=6) {
                        current = ""
                        for (i in 0..userInput.length-1){
                            if (i==2) {
                                current += "/"
                            }
                            current += userInput[i]
                            if (i==1){
                                val month = current.substring(0, 2)
                                if (month.toInt()>12){current = "12"}
                            }
                            if (i==5){
                                val year = current.substring(3, 7)
                                if (year.toInt()<2020 || year.toInt()>2035){current = current.substring(0, 2) + "/2020"}
                            }
                            s.filters = arrayOfNulls<InputFilter>(0)
                        }
                    }
                    s.replace(0, s.length, current, 0, current.length)
                }
            }
        })

        findViewById<Button>(R.id.paymentConfirmButton).setOnClickListener {
            if (card.text.length !=19) {
                Toast.makeText(applicationContext, "input card number", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (date.text.length !=7){
                Toast.makeText(applicationContext, "input expiry date", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val newIntent = Intent(this@PaymentActivity, ShowOrdersActivity::class.java).apply {
                putExtras(intent)
                putExtra("Payment Success", true)
            }
            startActivity(newIntent)
        }

        findViewById<Button>(R.id.paymentBackButton).setOnClickListener {
            finish()
        }
    }
}