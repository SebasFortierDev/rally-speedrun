package sebastien.fortier.dev.rally_speedrun.parcours

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.model.Point
import java.util.concurrent.TimeUnit
import android.text.Editable




private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

class ChoixPointsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var mapEstChargee = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = false


    private val points = arrayListOf<Point>()

    private var markerPosition: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choix_points)

        // Charge la gestion de Position GPS
        if (savedInstanceState != null) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                REQUESTING_LOCATION_UPDATES_KEY, false)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(10)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                update(locationResult.lastLocation)
            }
        }

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_choix) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }

    }

    /**
     * Permet de sauvegarder l'état de la demande à travers les états de l'application
     *
     * @param outState la donnée qu'on veut sauvegarder
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setOnMapClickListener {

            val marker = googleMap.addMarker(MarkerOptions().position(it))

            if (marker != null) {
                dialogNomPoint(marker).show()
            }

        }

        mapEstChargee = true
    }

    private fun dialogNomPoint(marker : Marker): AlertDialog {
        val nomPointEditText : EditText = EditText(this)

        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_ajout_point_dialog))
            .setView(nomPointEditText)
            .setMessage("Veuillez saisir le nom du point si désiré")
            .setPositiveButton(
                getString(R.string.ajouter_point_dialog)
            ) { _, _ ->

                var nomPoint: String = nomPointEditText.text.toString()

                if (nomPoint.isEmpty()) {
                    nomPoint = "Point " + (points.size + 1).toString()
                }

                val point =
                    marker.position.let { pos -> LatLng(pos.latitude, pos.longitude) }
                        .let { it -> Point(it, nomPoint, 260F, 0x006e35e3) }

                points.add(point)

                Log.d("listePOits", points.toString())
            }
            .setNegativeButton(
                getString(R.string.annuler_ajout)
            ) {_, _ ->
                marker.isVisible = false
            }
            .create()
    }

    /**
     * Fonction exécuter lorsque l'application passe en mode 'Resume'
     */
    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    /**
     * Fonction exécuter lorsque l'application passe en mode 'Pause'
     */
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    /**
     * Permet d'arrêter le suivie de la position en temps réels
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    /**
     * Permet de commencer le suivie de la position en temps réels.
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // Erreur bidon de l'IDE, la permission est déjà présente dans le manifest, tout fonctionne
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /**
     * Ajuster l'interface de l'application selon les mouvements de l'utilisateur
     */
    private fun update(location: Location) {
        showLocation(location)

    }

    /**
     * Permet d'afficher le marqueur sur la carte est d'ajuster la vue de la caméra sur la carte
     *
     * @param location La location du téléphone actuelle
     */
    private fun showLocation(location: Location) {
        if (mapEstChargee) {
            markerPosition?.remove()

            markerPosition = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title("Utilisateur")
            )

            val bitmap = AppCompatResources.getDrawable(this, R.drawable.ic_baseline_elderly_24)?.toBitmap()

            markerPosition?.setIcon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
            markerPosition?.alpha  = 10F
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
        }
    }
}