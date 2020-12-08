package com.dmytroandriichuk.finallpizzaproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var latLng: LatLng? = null
    private lateinit var addressET: TextInputEditText
    private lateinit var nameET: TextInputEditText
    private lateinit var flatNumberET: TextInputEditText
    private lateinit var phoneET: TextInputEditText
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // init map when it's ready
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        addressET = findViewById(R.id.mapPostalAddressET)
        addressET.setOnClickListener {
            val fieldList = listOf(
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )

            val autocompleteIntent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                fieldList
            ).build(this)
            startActivityForResult(autocompleteIntent, 100)
        }

        val sharedPreferences = getSharedPreferences("user default", Context.MODE_PRIVATE)

        nameET = findViewById(R.id.mapNameET)
        nameET.setText(sharedPreferences.getString("Persons name", ""))

        flatNumberET = findViewById(R.id.mapFlatNumberET)

        phoneET = findViewById(R.id.mapPhoneNumberET)
        phoneET.setText(sharedPreferences.getString("Phone number", ""))

        val nameLayout = findViewById<TextInputLayout>(R.id.mapNameLayout)
        val addressLayout = findViewById<TextInputLayout>(R.id.mapAddressLayout)
        val phoneLayout = findViewById<TextInputLayout>(R.id.mapPhoneNumberLayout)

        val backButton = findViewById<Button>(R.id.mapBackButton)
        backButton.setOnClickListener { finish() }

        val confirmButton = findViewById<Button>(R.id.mapConfirmButton)
        confirmButton.setOnClickListener {
            var flag = true

            val name = nameET.text.toString()
            val address = addressET.text.toString()
            val phone = phoneET.text.toString()
            if (name.isEmpty()) {
                nameLayout.error = "name is empty"
                flag = false
            } else {
                nameLayout.error = ""
            }
            if (address.isEmpty()) {
                addressLayout.error = "address is empty"
                flag = false
            } else {
                addressLayout.error = ""
            }
            if (phone.isEmpty()) {
                flag = false
                phoneLayout.error = "phone number is empty"
            } else {
                phoneLayout.error = ""
            }

            if (!flag) return@setOnClickListener
            val newIntent = Intent(this@MapActivity, PaymentActivity::class.java).apply {
                putExtras(intent)
                putExtra("full name", nameET.text.toString())
                putExtra("address", addressET.text.toString())
                putExtra("flat number", flatNumberET.text.toString())
                putExtra("lat", latLng?.latitude)
                putExtra("lng", latLng?.longitude)
                putExtra("phone number", phoneET.text.toString())
            }
            startActivity(newIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
            addressET.setText(place?.address)
            mMap.clear()
            latLng = place?.latLng
            val address = place?.address

            mMap.addMarker(latLng?.let { MarkerOptions().position(it).title(address) })
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR){
            val message = data?.let { Autocomplete.getStatusFromIntent(it).statusMessage }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    //when map is ready we we activate change mat type button and allowing marker click
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        findViewById<ImageButton>(R.id.changeMapType).setOnClickListener { changeMapType() }
    }
}