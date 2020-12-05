package com.dmytroandriichuk.finallpizzaproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var addressET: TextInputEditText
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // init map when it's ready
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        addressET = findViewById<TextInputEditText>(R.id.mapPostalAddressET)
        addressET.setOnClickListener {
            val fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)

            val autocompleteIntent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(this)
            startActivityForResult(autocompleteIntent, 100)
        }
        val nameET = findViewById<TextInputEditText>(R.id.mapNameET)
        val flatNumberET = findViewById<TextInputEditText>(R.id.mapFlatNumberET)
        val phoneET = findViewById<TextInputEditText>(R.id.mapPhoneNumberET)

        val backButton = findViewById<Button>(R.id.mapBackButton)
        backButton.setOnClickListener {
            // TODO: 05.12.2020 save inputted data
            finish()
        }
        val confirmButton = findViewById<Button>(R.id.mapConfirmButton)
        confirmButton.setOnClickListener {
            // TODO: 05.12.2020 provide data
            // check input isEmpty
            val newIntent = Intent(this@MapActivity, PaymentActivity::class.java)
            startActivity(newIntent)
        }
        // TODO: 05.12.2020 add autocomplete name
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
            addressET.setText(place?.address)
            mMap.clear()
            val latlng = place?.latLng
            val address = place?.address

            mMap.addMarker(latlng?.let { MarkerOptions().position(it).title(address) })
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 20f))
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