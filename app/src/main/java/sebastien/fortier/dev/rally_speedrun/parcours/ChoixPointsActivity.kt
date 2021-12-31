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
import android.widget.Button
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunRepository
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import java.lang.reflect.Type


private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

class ChoixPointsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    private lateinit var btnConfirmerChoix: Button
    private lateinit var btnAnnulerChoix: Button

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var mapEstChargee = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = false

    private var points = arrayListOf<Point>()

    private var markerPosition: Marker? = null

    private var markerActuel: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choix_points)


        btnConfirmerChoix = findViewById(R.id.btn_confirmer_choix)
        btnAnnulerChoix = findViewById(R.id.btn_annuler_choix)

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


        btnConfirmerChoix.setOnClickListener {
            dialogAjouterParcours().show()
        }

        btnAnnulerChoix.setOnClickListener {
            supprimerChoix()
        }
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
        val nomPointEditText = EditText(this)

        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_ajout_point_dialog))
            .setView(nomPointEditText)
            .setMessage(getString(R.string.description_ajout_point_dialog))
            .setPositiveButton(
                getString(R.string.ajouter_point_dialog)
            ) { _, _ ->

                var nomPoint: String = nomPointEditText.text.toString()

                if (nomPoint.isEmpty()) {
                    nomPoint = "Point " + (points.size + 1).toString()
                }

                points.add(Point(LatLng(marker.position.latitude, marker.position.longitude), nomPoint, 160F, 0x0035eaae))
                markerActuel = marker

                Log.d("listePOits", points.toString())
            }
            .setNegativeButton(
                getString(R.string.annuler_ajout)
            ) {_, _ ->
                marker.isVisible = false
            }
            .create()
    }

    private fun dialogAjouterParcours(): AlertDialog {
        val nomParcoursEditText = EditText(this)

        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_ajout_parcours_dialogue))
            .setView(nomParcoursEditText)
            .setMessage(getString(R.string.description_ajout_parcours_dialog))
            .setPositiveButton(
                getString(R.string.ajouter_point_dialog)
            ) { _, _ ->

                val nomParcours: String = nomParcoursEditText.text.toString()

                val nouveauParcours = Parcours(nom = nomParcours, points = points)

                rallySpeedrunRepository.addParcours(nouveauParcours)
                this.finish()
            }
            .setNegativeButton(
                getString(R.string.annuler_ajout)
            ) {_, _ ->
                //
            }
            .create()
    }

    private fun supprimerChoix() {
        if (points.isNotEmpty()) {
            points.last().marker?.remove()
            points.removeLast()
        }
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