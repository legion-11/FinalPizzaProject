package com.dmytroandriichuk.finallpizzaproject

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dmytroandriichuk.finallpizzaproject.dataClasses.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class ShowOrdersActivity : AppCompatActivity() {

    private lateinit var querry: Query

    private var listener: ValueEventListener? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private var orders: List<Order> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_orders)
        mAuth = FirebaseAuth.getInstance();
        database = Firebase.database


        if (intent.getBooleanExtra("AddPizza", false)){ saveOrderToDB() }

        loadFromFireBaseDB(this)
        if (isOnline()) {

        } else {
            loadFromLocalDB()
        }
    }

    private fun loadFromLocalDB() {
       // TODO("Not yet implemented")
    }

    private fun loadFromFireBaseDB(context: Context) {
        querry = database.getReference("Order").orderByChild("userId").equalTo(mAuth.currentUser?.uid)

        listener = object: ValueEventListener  {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    orders = emptyList()
                    for (childSnapshot in snapshot.children) {
                        val order = childSnapshot.getValue(Order::class.java)
                        orders.plus(order)
                        Log.i("loadFromFireBaseDB", "onDataChange: " + order.toString())
                    }
                } else {
                    Toast.makeText(context, "Something wrong happened", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Something wrong happened", Toast.LENGTH_LONG).show()
            }
        }
        querry.addValueEventListener(listener as ValueEventListener)
       // TODO("Not yet implemented")
    }

    private fun saveOrderToDB() {
        mAuth.currentUser?.let { currentUser ->
            val name = intent.getStringExtra("full name")
            val address = intent.getStringExtra("address")
            val flatNumber = intent.getStringExtra("flat number")
            val lat = intent.getDoubleExtra("lat", 0.0)
            val lng = intent.getDoubleExtra("lng", 0.0)
            val phoneNumber = intent.getStringExtra("phone number")
            val pizzaType = intent.getStringExtra("pizza type")
            val size = intent.getIntExtra("size", 0)
            val toppings = intent.getStringArrayListExtra("toppings")
            val price = intent.getDoubleExtra("price", 0.0)
            val date = Date().time


            val order = Order(currentUser.uid, name, address, flatNumber,
                    lat, lng, phoneNumber, pizzaType, size, toppings, price, date, 0)

            database.getReference("Order").push().setValue(order).addOnCompleteListener {
                if (it.isSuccessful) {
                    saveToLocalDB()
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                } else {
                    // TODO: 05.12.2020 button to try again appearing
                    Toast.makeText(this, "Failed to send order. Try again", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveToLocalDB() {
       // TODO("Not yet implemented")
    }

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

    override fun onDestroy() {
        super.onDestroy()
        if (listener != null) { querry.removeEventListener(listener as ValueEventListener) }
    }
}