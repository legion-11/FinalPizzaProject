package com.dmytroandriichuk.finallpizzaproject

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.system.exitProcess


class OrderPizzaActivity : AppCompatActivity() {
    private var size: Int = 0
    private lateinit var viewPager: ViewPager2
    private var startTime: Long = Date(0).time
        private val toppingsArray = mutableListOf<String>()
    private var price = 0.0
    private var animation: ObjectAnimator? = null
    private lateinit var seekBar: SeekBar
    private var pizzaPrice: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_pizza)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_log_out)
        viewPager = findViewById(R.id.imagePager)

        val typedArray = resources.obtainTypedArray(R.array.pizzzaDrawables)
        val imagesId = IntArray(typedArray.length())
        for (i in imagesId.indices) {
            imagesId[i] = typedArray.getResourceId(i, 0)
        }
        typedArray.recycle()
        val pizzaPrices = resources.getStringArray(R.array.pizzaPrices)

        viewPager.adapter = ViewPagerAdapter(imagesId)
        val pizzaNames = resources.getStringArray(R.array.pizzaNames)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                title = pizzaNames[position]
                pizzaPrice = pizzaPrices[position].toDouble()
                recalculatePrice()
            }
        })

        val tabLayout = findViewById<TabLayout>(R.id.tabDots)
        TabLayoutMediator(tabLayout, viewPager) { _, position ->
            viewPager.setCurrentItem(position, true)
        }.attach()


        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val rescale = 1.1f + progress.toFloat()/seekBar.max
                viewPager.scaleX = rescale
                viewPager.scaleY = rescale
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                animation?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                size = when(seekBar.progress) {
                    in 0..50 -> 0
                    in 51..150 -> 1
                    in 151..250 -> 2
                    else -> 3
                }
                setRadioButton(size)
                recalculatePrice()

                animation = ObjectAnimator.ofInt(seekBar, "progress", getFinalProgressFromCurrent(seekBar.progress))
                animation?.duration = 500 // 0.5 second
                animation?.interpolator = DecelerateInterpolator()
                animation?.start()
            }
        })

        val pizzaSizesRadioGroup = findViewById<RadioGroup>(R.id.pizzaSizesRadioGroup)
        setSizeFromRadiobutton(pizzaSizesRadioGroup.checkedRadioButtonId)
        pizzaSizesRadioGroup.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
            setSizeFromRadiobutton(i)
        }

        val confirmButton = findViewById<Button>(R.id.confirmPizzaButton)
        confirmButton.setOnClickListener { saveOrderAndOpenMapActivity() }
    }

    private fun recalculatePrice() {
        price = pizzaPrice + pizzaPrice * (size.toDouble())/3 + 0.5 * toppingsArray.size
        findViewById<TextView>(R.id.priceTV).text = resources.getString(R.string.format_price).format(price)
    }

    private fun getFinalProgressFromCurrent(progress: Int): Int {
        return when (progress) {
            in 0..50 -> 0
            in 51..150 -> 100
            in 151..250 -> 200
            else -> 300
        }
    }

    private fun setRadioButton(id: Int){
        when(id){
            0 -> findViewById<RadioButton>(R.id.radioButtonSmall ).isChecked = true
            1 -> findViewById<RadioButton>(R.id.radioButtonMedium).isChecked = true
            2 -> findViewById<RadioButton>(R.id.radioButtonLarge ).isChecked = true
            else -> findViewById<RadioButton>(R.id.radioButtonExtra ).isChecked = true
        }
    }

    private fun setSizeFromRadiobutton(radioButtonId: Int) {
        animation?.cancel()
        val scaler = when (radioButtonId) {
            R.id.radioButtonSmall -> 0
            R.id.radioButtonMedium -> 100
            R.id.radioButtonLarge -> 200
            else-> 300
        }
        size = when (radioButtonId) {
            R.id.radioButtonSmall -> 0
            R.id.radioButtonMedium -> 1
            R.id.radioButtonLarge -> 2
            else -> 3
        }
        recalculatePrice()
        animation = ObjectAnimator.ofInt(seekBar, "progress", scaler)
        animation?.duration = 500 // 0.5 second
        animation?.interpolator = DecelerateInterpolator()
        animation?.start()
    }

    private fun saveOrderAndOpenMapActivity() {
        val newIntent = Intent(this@OrderPizzaActivity, MapActivity::class.java).apply {
            putExtra("pizza type", title)
            putExtra("size", size)
            putExtra("toppings", toppingsArray.toTypedArray())
            putExtra("price", price)
        }
        startActivity(newIntent)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - startTime < 2000){
            finishAffinity()
            exitProcess(0)
        } else {
            Toast.makeText(
                    this,
                    "Press one more time to close app.\nYou won't be Logged Out of your account",
                    Toast.LENGTH_LONG
            ).show()
            startTime = System.currentTimeMillis()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            Log.i("OrderActivity", "onOptionsItemSelected: Settings")
            intent = Intent(this@OrderPizzaActivity, SavePersonalDataActivity::class.java).apply {
                putExtra("Payment Success", false)
            }
            startActivity(intent)
            true
        }

        R.id.action_show_history -> {
            // User chose the "History" action
            Log.i("OrderActivity", "onOptionsItemSelected: History")
            intent = Intent(this@OrderPizzaActivity, ShowOrdersActivity::class.java).apply {
                putExtra("Payment Success", false)
            }
            startActivity(intent)
            true
        }

        android.R.id.home -> {
            // User chose the "Log Out" action
            Log.i("OrderActivity", "onOptionsItemSelected: Home")
            FirebaseAuth.getInstance().signOut()
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    fun addRemoveTopping(view: View) {
        if((view as CheckBox).isChecked) {
            toppingsArray.add(view.text.toString())
        } else {
            toppingsArray.remove(view.text.toString())
        }
        recalculatePrice()
    }
}