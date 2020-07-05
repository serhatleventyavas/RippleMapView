package com.serhatleventyavas.ripplemapview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        this@MainActivity.googleMap = googleMap
        val markerOptions =  MarkerOptions().position(LatLng(41.009146, 29.034022))
        this@MainActivity.googleMap?.addMarker(markerOptions)

        this@MainActivity.googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.009146, 29.034022), 16f))
        val rippleMapView = this.googleMap?.let {
            RippleMapView.Builder(this, it)
                .fillColor(resources.getColor(R.color.colorPrimaryDark))
                .strokeColor(resources.getColor(R.color.colorPrimaryDark))
                .latLng(LatLng(41.009146, 29.034022))
                .numberOfRipples(3)
                .build()
        }
        rippleMapView?.startRippleMapAnimation()
    }
}
