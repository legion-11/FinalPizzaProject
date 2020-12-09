package com.dmytroandriichuk.finallpizzaproject

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmytroandriichuk.finallpizzaproject.dataClasses.AdminLocation
import com.dmytroandriichuk.finallpizzaproject.dataClasses.Order
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

//provide list of current user orders
class ShowOrdersActivity : AppCompatActivity(), OrdersAdapter.OnOrderClickListener {

    private lateinit var retryButton: Button
    private lateinit var progerssBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var querry: Query

    private var listener: ValueEventListener? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private var ordersLiveData: MutableLiveData<List<Order>> = MutableLiveData()

    private lateinit var itsTimeTV: TextView
    private lateinit var itsTimeImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_orders)
        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database

        progerssBar = findViewById(R.id.showOrdersProgressBar)
        retryButton = findViewById(R.id.tryAgainButton)
        retryButton.setOnClickListener{saveOrderToDB()}
        itsTimeTV = findViewById(R.id.showOrdersItsTimeTV)
        itsTimeImage = findViewById(R.id.showOrdersNoOrdersImage)
        recyclerView = findViewById(R.id.ordersReciclerView)

        if (intent.getBooleanExtra("Payment Success", false)){
            saveOrderToDB()
        }

        progerssBar.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        //get data from internet
        ordersLiveData.observe(this, {
            recyclerView.adapter = OrdersAdapter(it, this)
        })
        if (mAuth.currentUser != null) { loadFromFireBaseDB(this) }

        if (!isOnline()) {
            loadFromLocalDB()
        }
        progerssBar.visibility = View.GONE
    }

    private fun loadFromLocalDB() {
       // TODO("Not yet implemented")
    }

    // load from firebase orders of current user and update it
    private fun loadFromFireBaseDB(context: Context) {
        querry = database.getReference("Order").orderByChild("userId").equalTo(mAuth.currentUser?.uid)
        listener = object: ValueEventListener  {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                if (snapshot.exists()) {
                    itsTimeImage.visibility = View.GONE
                    itsTimeTV.visibility = View.GONE

                    for (childSnapshot in snapshot.children) {
                        val order = childSnapshot.getValue(Order::class.java)
                        order?.let {orders.add(it)}
                    }
                    orders.reverse()
                    ordersLiveData.value = orders
                } else {
                    ordersLiveData.value = orders
                    itsTimeImage.visibility = View.VISIBLE
                    itsTimeTV.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Something wrong happened", Toast.LENGTH_LONG).show()
            }
        }
        querry.addValueEventListener(listener as ValueEventListener)
    }

    // send order to firebase db
    private fun saveOrderToDB() {
        retryButton.visibility = View.GONE

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


            val order = Order(
                currentUser.uid, name, address, flatNumber,
                lat, lng, phoneNumber, pizzaType, size, toppings, price, date
            )
            progerssBar.visibility = View.VISIBLE
            database.getReference("Order").push().setValue(order)
                .addOnSuccessListener {
                    saveToLocalDB()
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                    intent.removeExtra("Payment Success")
                }
                .addOnFailureListener {
                Toast.makeText(this, "Failed to send order. Try again", Toast.LENGTH_LONG).show()
                retryButton.visibility = View.VISIBLE
            }
            progerssBar.visibility = View.GONE
        }
    }

    private fun saveToLocalDB() {
       // TODO("Not yet implemented")
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

    override fun onDestroy() {
        super.onDestroy()
        if (listener != null) { querry.removeEventListener(listener as ValueEventListener) }
    }

    // show admin location if  order status == 1
    override fun onOrderClick(position: Int) {
        val clicked = ordersLiveData.value?.get(position)
        if (clicked?.adminId != null) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_map)

            MapsInitializer.initialize(this)

            val mMapView = dialog.findViewById<MapView>(R.id.mapView)
            MapsInitializer.initialize(this)
            mMapView.onCreate(dialog.onSaveInstanceState())
            mMapView.onResume() // needed to get the map to display immediately

            // update admin marker
            val listener =object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val loc = snapshot.getValue(AdminLocation::class.java)
                        if (loc != null) {
                            adminLocationLiveData.value = loc.lng?.let { lng-> loc.lat?.let { lat-> LatLng(lat, lng) } }
                        }
                        Log.i("TAG", "onDataChange: $loc")
                    } else {
                        Log.i("TAG", "onDataChange: no admin")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("TAG", "onCancelled: cancel")
                }
            }

            val refference = database.getReference("Admins").child(clicked.adminId!!)
            mMapView.getMapAsync { googleMap ->
                var locationMarker: Marker? = null
                adminLocationLiveData.observe(this, {
                    if (locationMarker == null){
                        locationMarker = googleMap.addMarker(MarkerOptions().apply {
                            position(it)
                        })
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
                    } else {
                        locationMarker?.position = it
                    }
                })
                refference.addValueEventListener(listener)
            }
            dialog.setOnDismissListener {
                refference.removeEventListener(listener)
                adminLocationLiveData.removeObservers(this)
            }
            dialog.show()
        }
    }
    companion object {
        val adminLocationLiveData: MutableLiveData<LatLng> = MutableLiveData()
    }
}