package com.decagon.android.sq007

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*


class MapsActivity : AppCompatActivity(),OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener
{

    private val REQUEST_PERMISSION_LOCATION = 120
    private lateinit var remoteServices:RemoteServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        remoteServices = RemoteServices(this.applicationContext)
        remoteServices.initializeFirebase(LocationServices.getFusedLocationProviderClient(this))
        accessLocationServices()



        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }


    private fun accessLocationServices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
              remoteServices.getLocationUpdates()
              remoteServices.readLocationChanges()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessLocationServices()
            } else {
                Toast.makeText(this, "Access Location permission required", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        remoteServices.onMapReady(googleMap)
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }



}

