package com.decagon.android.sq007

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class RemoteServices(val appContext:Context): ViewModel(){



    private lateinit var mMap: GoogleMap
    var myMarker: Marker? = null
    private var user: LatLng? = null
    private var myMarker2: Marker? = null
    lateinit var dbReference: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    fun initializeFirebase( fusedLocationClient: FusedLocationProviderClient) {
        this.fusedLocationClient = fusedLocationClient
        dbReference = FirebaseDatabase.getInstance().reference
        dbReference.addValueEventListener(locationLogging)
    }










    private val locationLogging = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            if (snapshot.exists()) {
                val userLocation = snapshot.child("HASSAN").getValue(UserLocation::class.java)
                var userLat = userLocation?.latitude
                var userLong = userLocation?.longitude
                if (userLat != null && userLong != null) {
                    user = LatLng(userLat, userLong)
                    // val myMarker = mMap.addMarker(MarkerOptions().position(user).title("HASSAN"))
                    Log.d("TAG", "onDataChange: $myMarker2")
                    //mMap.addMarker(myMarker)
                    //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(user, 16.0f))
                    Toast.makeText(
                            appContext,
                            "Location accessed from database",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Toast.makeText(appContext, "Could not read from database", Toast.LENGTH_LONG)
                    .show()
        }

    }



     fun readLocationChanges() {
         if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             return
         }
         fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        )
    }


    fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationUpdate: LocationResult?) {
                if (locationUpdate?.locations!!.isNotEmpty()) {
                    val location = locationUpdate?.lastLocation
                    UserLocation.latitude = location.latitude
                    UserLocation.longitude = location.longitude
                    dbReference.child("JOBS").setValue(UserLocation)
                            .addOnSuccessListener {
                                Toast.makeText(appContext, "Locations written into the database", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(appContext, it.message, Toast.LENGTH_LONG).show()
                            }
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        /*
                        ** To create a marker to indicate a User on the map
                         */
                        val markerOptions = MarkerOptions()
                        markerOptions.position(latLng)
                        markerOptions.title("JOBS")
                        markerOptions.icon(
                                BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN
                                ))
                        val markerOptions2 = MarkerOptions().position(user!!).title("HASSAN").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        if (myMarker == null) { // Add marker and move camera on first time
                            myMarker = mMap.addMarker(markerOptions)
                            myMarker2 = mMap.addMarker(markerOptions2)
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20F))
                        } else { // Update existing marker position and move camera if required
                            mMap.clear()
                            myMarker = mMap.addMarker(markerOptions)
                            myMarker2 = mMap.addMarker(markerOptions2)
                            myMarker?.position = latLng
                            myMarker2?.position = user
//                          mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13))
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
                    }
                }
            }
        }
    }



         fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
             if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                 return
             }
             mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

        }

    }

