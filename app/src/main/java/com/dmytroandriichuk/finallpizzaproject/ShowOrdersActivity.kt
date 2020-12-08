package com.dmytroandriichuk.finallpizzaproject

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmytroandriichuk.finallpizzaproject.dataClasses.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class ShowOrdersActivity : AppCompatActivity(), OrdersAdapter.OnOrderClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var querry: Query

    private var listener: ValueEventListener? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private var ordersLiveData: MutableLiveData<List<Order>> = MutableLiveData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_orders)
        mAuth = FirebaseAuth.getInstance();
        database = Firebase.database


        if (intent.getBooleanExtra("Payment Success", false)){
            saveOrderToDB()
            intent.removeExtra("Payment Success")
        }
        // TODO("add progress bar")
        // progress bar start

        recyclerView = findViewById(R.id.ordersReciclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))

        ordersLiveData.observe(this, {
            recyclerView.adapter = OrdersAdapter(it, this)
        })
        if (mAuth.currentUser != null) { loadFromFireBaseDB(this) }

        if (!isOnline()) {
            loadFromLocalDB()
        }
        // progress bar end
    }

    private fun loadFromLocalDB() {
       // TODO("Not yet implemented")
    }

    private fun loadFromFireBaseDB(context: Context) {
        querry = database.getReference("Order").orderByChild("userId").equalTo(mAuth.currentUser?.uid)

        listener = object: ValueEventListener  {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val orders = mutableListOf<Order>()
                    for (childSnapshot in snapshot.children) {
                        val order = childSnapshot.getValue(Order::class.java)
                        order?.let {orders.add(it)}
                        // TODo childSnapshot.key tracking
                        Log.i("loadFromFireBaseDB", "onDataChange: " + childSnapshot.key)
                    }
                    ordersLiveData.value = orders
                } else {
                    Toast.makeText(context, "Something wrong happened", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Something wrong happened", Toast.LENGTH_LONG).show()
            }
        }
        querry.addValueEventListener(listener as ValueEventListener)
       // TODO("save in db somehow")
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
                    lat, lng, phoneNumber, pizzaType, size, toppings, price, date)

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

    override fun onOrderClick(position: Int) {
        Log.i("TAG", "onOrderClick: " + ordersLiveData.value?.get(position).toString())
        val clicked = ordersLiveData.value?.get(position)
    }
}