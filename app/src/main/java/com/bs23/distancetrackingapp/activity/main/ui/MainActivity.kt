package com.bs23.distancetrackingapp.activity.main.ui

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.bs23.distancetrackingapp.R
import com.bs23.distancetrackingapp.activity.main.viewModel.MainActivityViewModel
import com.bs23.distancetrackingapp.activity.main.viewModel.MainActivityViewModelFactory
import com.bs23.distancetrackingapp.extensionFunction.nonNull
import com.bs23.distancetrackingapp.extensionFunction.observe
import com.bs23.distancetrackingapp.helper.GoogleMapHelper
import com.bs23.distancetrackingapp.helper.MarkerAnimationHelper
import com.bs23.distancetrackingapp.helper.UiHelper
import com.bs23.distancetrackingapp.listeners.IPositiveNegativeListener
import com.bs23.distancetrackingapp.util.AppRxSchedulers
import com.bs23.distancetrackingapp.util.LatLngInterpolator
import com.bs23.distancetrackingapp.util.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs
import com.google.maps.DirectionsApi
import org.joda.time.DateTime
import com.google.maps.model.TravelMode
import com.google.maps.model.DirectionsResult
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3568
    }


    private val uiHelper = UiHelper()
    private lateinit var googleMapHelper: GoogleMapHelper
    private val appRxSchedulers = AppRxSchedulers()

    private lateinit var googleMap: GoogleMap
    private lateinit var viewModel: MainActivityViewModel
    
    private var firstTimeFlag = true
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        googleMapHelper = GoogleMapHelper(resources)
        val viewModelFactory = MainActivityViewModelFactory(googleMapHelper, appRxSchedulers, locationProviderClient, uiHelper.getLocationRequest())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
        if (!uiHelper.isPlayServicesAvailable(this)) {
            Toast.makeText(this, "Play Services did not installed!", Toast.LENGTH_SHORT).show()
            finish()
        } else checkLocationPermission()
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMapHelper.defaultMapSettings(it)
            googleMap = it
            startListenNewLocation()
        }
        currentLocationImageButton.setOnClickListener {
            googleMapHelper.animateCamera(marker?.position, googleMap)

            viewModel.currentLocation()
                    .nonNull()
                    .observe(this) {
                        if (SharedPreferencesManager.getFloat(applicationContext,"userLastLat" ) != 0f
                                && SharedPreferencesManager.getFloat(applicationContext,"userLastLng") != 0f) {
                            val tempLat = SharedPreferencesManager.getFloat(applicationContext,"userLastLat" )
                            val tempLng =  SharedPreferencesManager.getFloat(applicationContext,"userLastLng")
                            val dist = getDistance( LatLng(tempLat.toDouble(), tempLng.toDouble() ) , LatLng(it.latitude, it.longitude))
                            Toast.makeText(applicationContext,  dist + " meter" , Toast.LENGTH_LONG).show()
                            if (dist.toFloat() > 2f)
                                showOrAnimateMarker(it)

                        } else {
                            SharedPreferencesManager.putFloat(applicationContext,"userLastLat",it.latitude.toFloat())
                            SharedPreferencesManager.putFloat(applicationContext,"userLastLng",it.longitude.toFloat())
                        }
                    }
        }
    }

    private fun  getDistance(latlngA :LatLng , latlngB : LatLng ) : String {
        val locationA : Location  = Location("point A")

        locationA.setLatitude(latlngA.latitude)
        locationA.setLongitude(latlngA.longitude)

        val locationB : Location = Location("point B")

        locationB.setLatitude(latlngB.latitude)
        locationB.setLongitude(latlngB.longitude)

        val distance = locationA.distanceTo(locationB)
        return String.format("%.2f", distance)
        // returns meter
    }

    /*private fun getDirectionsDetails(origin: String, destination: String, mode: TravelMode): DirectionsResult? {
        val now = DateTime()
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await()
        } catch (e: ApiException) {
            e.printStackTrace()
            return null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }*/
   /* private fun getGeoContext() : GeoApiContext  {
        val geoApiContext : GeoApiContext = GeoApiContext(requestHandler Request)
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_distance_api))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }
*/
    private fun startListenNewLocation() {

        viewModel.currentLocation()
                .nonNull()
                .observe(this) {
                    if (firstTimeFlag) {
                        firstTimeFlag = false
                        googleMapHelper.animateCamera(LatLng(it.latitude, it.longitude), googleMap)
                        startDistanceTracking()
                    }
                    showOrAnimateMarker(it)
                }
    }

    private fun startDistanceTracking() {
        viewModel.startLocationTracking()
        viewModel.distanceTracker()
                .nonNull()
                .observe(this) {
                    Log.e("Hello", it)
                    distanceCoveredTextView.text = it
                }
    }

    private fun showOrAnimateMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (marker == null)
            marker = googleMap.addMarker(googleMapHelper.getCurrentMarkerOptions(latLng))
        else MarkerAnimationHelper.animateMarkerToGB(marker, latLng, LatLngInterpolator.Spherical())
    }

    private fun checkLocationPermission() {
        if (!uiHelper.isHaveLocationPermission(this)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
        if (uiHelper.isLocationProviderEnabled(this))
            uiHelper.showPositiveDialogWithListener(this, resources.getString(R.string.need_location), resources.getString(R.string.location_content), object : IPositiveNegativeListener {
                override fun onPositive() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }, "Turn On", false)
        viewModel.requestLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            val value = grantResults[0]
            if (value == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            } else if (value == PackageManager.PERMISSION_GRANTED) viewModel.requestLocationUpdates()
        }
    }
}




