package com.dmytroandriichuk.finallpizzaproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.system.exitProcess


class OrderPizzaActivity : AppCompatActivity() {
    private var size: Int? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var imageSizes: Array<String>
    private var startTime: Long = System.currentTimeMillis()
    private val toppingsArray = emptyArray<String>()
    private var price = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_pizza)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_log_out);
        viewPager = findViewById<ViewPager2>(R.id.imagePager)

        val typedArray = resources.obtainTypedArray(R.array.pizzzaDrawables)
        val imagesId = IntArray(typedArray.length())
        for (i in imagesId.indices) {
            imagesId[i] = typedArray.getResourceId(i, 0)
        }
        typedArray.recycle()
        viewPager.adapter = ViewPagerAdapter(imagesId)

        val names = resources.getStringArray(R.array.pizzaNames)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                title = names[position]
            }
        })

        val tabLayout = findViewById<TabLayout>(R.id.tabDots)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        imageSizes = resources.getStringArray(R.array.sizes)
        val pizzaSizesRadioGroup = findViewById<RadioGroup>(R.id.pizzaSizesRadioGroup)
        rescaleImage(pizzaSizesRadioGroup.checkedRadioButtonId)
        pizzaSizesRadioGroup.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
            rescaleImage(i)
        }

        val confirmButton = findViewById<Button>(R.id.confirmPizzaButton)
        confirmButton.setOnClickListener {
            saveOrderAndOpenMapActivity()
        }
        // TODO: 05.12.2020 price calculation

    }

    private fun saveOrderAndOpenMapActivity() {
        val newIntent = Intent(this@OrderPizzaActivity, MapActivity::class.java).apply {
            putExtra("pizza type", title)
            putExtra("size", size)
            putExtra("toppings", toppingsArray)
            putExtra("price", price)
        }
        startActivity(newIntent)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - startTime < 2000){
            finishAffinity()
            exitProcess(0);
        } else {
            Toast.makeText(this,
                "Press one more time to close app.\nYou won't be Logged Out of your account",
                Toast.LENGTH_LONG).show()
            startTime = System.currentTimeMillis()
        }
    }

    private fun rescaleImage(radioButtonId: Int) {
        val scalerIndex = when (radioButtonId) {
            R.id.radioButtonSmall -> 0
            R.id.radioButtonMedium -> 1
            R.id.radioButtonLarge -> 2
            else-> 3
        }
        size = scalerIndex
        viewPager.scaleX = imageSizes[scalerIndex].toFloat()
        viewPager.scaleY = imageSizes[scalerIndex].toFloat()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            Log.i("OrderActivity", "onOptionsItemSelected: Settings")
            true
        }

        R.id.action_show_history -> {
            // User chose the "History" action
            Log.i("OrderActivity", "onOptionsItemSelected: History")
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
}