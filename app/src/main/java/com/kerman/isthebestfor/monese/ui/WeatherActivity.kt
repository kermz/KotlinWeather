package com.kerman.isthebestfor.monese.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.multidex.MultiDex
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.kerman.isthebestfor.monese.R
import com.kerman.isthebestfor.monese.api.DaggerOpenWeatherAPIComponent
import com.kerman.isthebestfor.monese.api.OpenWeatherAPIModule
import com.kerman.isthebestfor.monese.database.DataBaseHandler
import com.kerman.isthebestfor.monese.enums.ErrorTypes
import com.kerman.isthebestfor.monese.models.WeatherModel
import com.kerman.isthebestfor.monese.presenter.DataConverter
import com.kerman.isthebestfor.monese.presenter.MainPresenter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.fragment_weather.*
import java.util.*

class WeatherActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, MainView {

    private val TAG = "MainActivity"
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    var latitude = ""
    var longitude = ""
    lateinit var locationManager: LocationManager
    val presenter = MainPresenter(this)

    private fun injectDI() {
        DaggerOpenWeatherAPIComponent.builder()
                .openWeatherAPIModule(OpenWeatherAPIModule())
                .build()
                .inject(presenter)
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, getString(R.string.connectionSuspended))
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, getString(R.string.connectionFailed) + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()

    }

    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        }
        startLocationUpdates()

        val fusedLocationProviderClient:
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener(this, { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mLocation = location
                        latitude = mLocation.latitude.toString()
                        longitude = mLocation.longitude.toString()

                        //Get weather only when we already have latitude and longitude
                        getWeather(latitude, longitude)
                    }
                })
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.enableLocation))
                .setMessage(getString(R.string.alertMessage))
                .setPositiveButton(getString(R.string.locationSettings), { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton(getString(R.string.cancel), { paramDialogInterface, paramInt -> })
        dialog.show()
    }

    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDI()
        setContentView(R.layout.activity_weather)
        loadingSpinner.visibility = GONE
        setSupportActionBar(toolbar)
        loadDB()

        MultiDex.install(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkLocation()

        fab.setOnClickListener { view ->
            checkLocation()
            getWeather(latitude, longitude)
        }
    }

    fun loadDB() {
        val db = DataBaseHandler(this)
        val data = db.readData()
        //Check if data isn't empty
        if (!data.isEmpty()) {
            //Must verify that we have data and then check if last update is before 24h
            if (data[0].date.before(getDaysAgo(1))) {
                noPreviousData()
                return
            }
            windSpeed.text = getString(R.string.windSpeed) + ": " + data[0].wind.speed.toString() + getString(R.string.windMetersPerSecond)
            temperature.text = getString(R.string.temperature) + ": " + data[0].main.temp.toString() + getString(R.string.temperatureCelsius)
            windDirection.text = getString(R.string.windDirection) + ": " + DataConverter().getDirection(data[0].wind.deg, resources.getStringArray(R.array.directionsArray).toList())
            weatherCondition.text = data[0].weather[0].description.capitalize()
            lastUpdated.text = getString(R.string.lastUpdated) + ": " + data[0].date.toString()
            weatherImage.setImageBitmap(data[0].iconBitmap)
            hideSpinner()
        }
        //Show no previous data message
        else {
            noPreviousData()
        }
    }

    fun noPreviousData() {
        weatherCondition.text = getString(R.string.errorNoPreviousData)
        weatherCondition.visibility = VISIBLE
        windSpeed.visibility = GONE
        temperature.visibility = GONE
        windDirection.visibility = GONE
        weatherImage.visibility = GONE
        lastUpdated.visibility = GONE
    }

    override fun showSpinner() {
        loadingSpinner.visibility = VISIBLE
        windSpeed.visibility = GONE
        temperature.visibility = GONE
        windDirection.visibility = GONE
        weatherImage.visibility = GONE
        weatherCondition.visibility = GONE
        lastUpdated.visibility = GONE
    }

    override fun hideSpinner() {
        loadingSpinner.visibility = GONE
        windSpeed.visibility = VISIBLE
        temperature.visibility = VISIBLE
        windDirection.visibility = VISIBLE
        weatherImage.visibility = VISIBLE
        weatherCondition.visibility = VISIBLE
        lastUpdated.visibility = VISIBLE
    }

    override fun updateWeather(weather: List<WeatherModel>) {
        if (weather.isEmpty()) return
        val db = DataBaseHandler(this)
        db.deleteData()

        windSpeed.text = getString(R.string.windSpeed) + ": " + weather[0].wind.speed.toString() + getString(R.string.windMetersPerSecond)
        temperature.text = getString(R.string.temperature) + ": " + weather[0].main.temp.toString() + getString(R.string.temperatureCelsius)
        windDirection.text = getString(R.string.windDirection) + ": " + DataConverter().getDirection(weather[0].wind.deg, resources.getStringArray(R.array.directionsArray).toList())
        weatherCondition.text = weather[0].weather[0].description.capitalize()
        lastUpdated.text = getString(R.string.lastUpdated) + ": " + weather[0].date.toString()

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this))
        val imageLoader = ImageLoader.getInstance()
        Thread({
            //Do some Network Request
            weather[0].iconBitmap = imageLoader.loadImageSync("http://openweathermap.org/img/w/" + weather[0].weather[0].icon + ".png")

            runOnUiThread({
                //Update UI
                weatherImage.setImageBitmap(weather[0].iconBitmap)
            })
        }).start()

        db.insertData(weather[0])
    }

    override fun showErrorToast(errorType: ErrorTypes) {
        when (errorType) {
            ErrorTypes.CALL_ERROR -> toast(getString(R.string.errorConnection))
            ErrorTypes.NO_RESULT_FOUND -> toast(getString(R.string.errorResult))
        }
        loadingSpinner.visibility = GONE
        loadDB()
    }

    private fun getWeather(lat: String, lon: String) = presenter.getWeather(latitude, longitude)

    inline fun <reified T> Any.safeCast() = this as? T

    private fun Activity.toast(toastMessage: String, duration: Int = Toast.LENGTH_LONG) {
        //Check if we can show toast
        if (!(this).isFinishing) {
            //show toast
            Toast.makeText(this, toastMessage, duration).show()
        }
    }

    private fun getDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

}
