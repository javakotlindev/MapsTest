package com.tellit.mapstestneva.presentation.main_page

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.tellit.mapstestneva.R
import com.tellit.mapstestneva.databinding.ActivityMainBinding
import com.tellit.mapstestneva.utils.ZoomEvent
import com.tellit.mapstestneva.utils.getBitmapDescriptorFromVector
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var permissionDenied = false
    private var tashkentLatLng = LatLng(41.311081, 69.240562)
    private var myPositionLatLng: LatLng? = null
    private var myPositionMarker: Marker? = null
    var placesClient: PlacesClient? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private var zoom: Float = 14f


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val apiKey = getString(R.string.google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)

        val mf = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mf.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        binding.locationFab.setOnClickListener {
            if (isLocationEnabled()) {
                enableMap()
            } else {
                geoReqAlertDialog().show()
            }
        }
        binding.searchTxt.setOnClickListener {
            openGetLocation()
        }

        binding.zoomPlus.setOnClickListener {
            zoomOnClick(ZoomEvent.PLUS.name)
        }

        binding.zoomMinus.setOnClickListener {
            zoomOnClick(ZoomEvent.MINUS.name)
        }
    }


    private fun openGetLocation() {
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun geoReqAlertDialog() = AlertDialog.Builder(this).apply {
        setMessage(getString(R.string.enable_location_to_continue))
        setNegativeButton(getString(R.string.no_thanks)) { _, _ ->

        }
        setPositiveButton("OK") { _, _ ->
            enableMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tashkentLatLng, zoom))

    }

    private fun zoomOnClick(type: String){
        when(type){
            ZoomEvent.PLUS.name-> {
                if (zoom !=21f){
                zoom += 1f
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tashkentLatLng, zoom))
            }
            ZoomEvent.MINUS.name-> {
                if (zoom !=0f){
                    zoom -= 1f
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tashkentLatLng, zoom))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private fun enableMap() {
        if (!::mMap.isInitialized) return
        if (checkPermission()) {
            if (isLocationEnabled()) {

                mMap.apply {
                    isMyLocationEnabled = false
                    uiSettings.isMyLocationButtonEnabled = false
                    uiSettings.isMapToolbarEnabled = false // TODO could be useful to add
                }

                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        myPositionLatLng = LatLng(location.latitude, location.longitude).also {
                            val myPositionIcon = MarkerOptions().apply {
                                position(it)
                                icon(
                                    getBitmapDescriptorFromVector(
                                        this@MainActivity,
                                        R.drawable.ic_my_position
                                    )
                                )
                            }
                            tashkentLatLng = it
                            myPositionMarker?.remove()
                            myPositionMarker = mMap.addMarker(myPositionIcon)
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    tashkentLatLng,
                                    14f
                                )
                            )
                        }
                    } else {
                        requestNewLocationData()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.enable_location_to_continue),
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE,
            )
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            myPositionLatLng =
                LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            enableMap()
        } else {
            // Permission was denied. Display an error message
            permissionDenied = true
        }
    }

    private fun isPermissionGranted(
        grantPermissions: Array<String>, grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionErrorAndOpenAppSettings()
            permissionDenied = false
        }
    }


    private fun showMissingPermissionErrorAndOpenAppSettings() {
        Toast.makeText(this, getString(R.string.enable_location), Toast.LENGTH_LONG)
            .show()
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        binding.searchTxt.text = place.name

                        place.latLng?.let { tashkentLatLng = it }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tashkentLatLng, 11f))


                        val myPositionIcon = MarkerOptions().apply {
                            place.latLng?.let { it1 -> position(it1) }
                            icon(
                                getBitmapDescriptorFromVector(
                                    this@MainActivity,
                                    R.drawable.ic_position
                                )
                            )
                        }
                        myPositionMarker?.remove()
                        myPositionMarker = mMap.addMarker(myPositionIcon)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i("TAG", status.statusMessage)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}


