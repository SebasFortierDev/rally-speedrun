package sebastien.fortier.dev.rally_speedrun.parcours

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.EditText
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
import android.widget.Button
import sebastien.fortier.dev.rally_speedrun.RallySpeedrunRepository
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import com.google.android.material.snackbar.Snackbar

/** Clé pour les requêtes de location */
private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

/**
 * Classe AjoutParcoursActivity
 *
 * Activity permettant la création d'un parcours
 *
 * @property rallySpeedrunRepository Voir [RallySpeedrunRepository]
 *
 * @property btnConfirmerChoix Bouton permettant l'ajout d'un parcours
 * @property btnAnnulerChoix Bouton permettant de supprimer le dernier point ajouter
 *
 * @property mapFragment Fragment de la carte GoogleMap
 * @property googleMap Object GoogleMap
 * @property fusedLocationClient Provider de la location de l'utilisateur
 * @property locationRequest Requête pour obtenir la localisation
 * @property locationCallback Callback de la location
 * @property points Points du parcours ajouté par l'utilisateur
 * @property pointsMarker Markers des points ajouté par l'utilisateur
 * @property requestingLocationUpdates Permet de savoir si on est entrain de faire des requêtes de location
 * @property mapEstChargee Permet de savoir si la carte google map est chargé ou non
 * @property markerPosition Marker affichant la position de l'utilisateur
 *
 * @author Sébastien Fortier
 */
class AjoutParcoursActivity : AppCompatActivity(), OnMapReadyCallback {

    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    private lateinit var btnConfirmerChoix: Button
    private lateinit var btnAnnulerChoix: Button

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var points = arrayListOf<Point>()
    private var pointsMarker = arrayListOf<Marker>()
    private var requestingLocationUpdates = false
    private var mapEstChargee = false
    private var aChargePosition = false
    private var markerPosition: Marker? = null

    /**
     * Initialisation de l'Activity.
     *
     * @param savedInstanceState Les données conservées au changement d'état.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajout_parcours)

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
            if (points.isEmpty()) {
                val mySnackbar = Snackbar.make(findViewById(R.id.map_choix), R.string.erreur_liste_points_vide, Snackbar.LENGTH_SHORT)
                mySnackbar.show()
            }
            else {
                dialogAjouterParcours().show()
            }
        }

        btnAnnulerChoix.setOnClickListener {
            supprimerChoix()
        }
    }

    /**
     * Démarrage de l'activity.
     */
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
     * @param outState Donnée qu'on veut sauvegarder
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    /**
     * Permet d'éxécuter des actions lorsque le chargement de la carte est fait
     *
     * @param map La carte GoogleMap
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setOnMapClickListener {
            val marker = googleMap.addMarker(MarkerOptions().position(it))

            if (marker != null) {
                pointsMarker.add(marker)
                dialogNomPoint(marker).show()
            }
        }

        mapEstChargee = true
    }

    /**
     * Dialog s'affichant lorsque l'utilisateur veut ajouter un point
     *
     * Il contient un champ pour associer un nom au point et un bouton
     * permettant de l'ajouter au parcours
     *
     * @param marker Le marker que l'utilisateur veut ajouter
     *
     * @return Le dialog permettant d'ajouter un point
     */
    private fun dialogNomPoint(marker : Marker): AlertDialog {
        // Champ pour le nom du point
        val nomPointEditText = EditText(this)

        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_ajout_point_dialog))
            .setView(nomPointEditText)
            .setCancelable(false)
            .setMessage(getString(R.string.description_ajout_point_dialog))
            .setPositiveButton(
                getString(R.string.ajouter_point_dialog)
            ) { _, _ ->
                var nomPoint: String = nomPointEditText.text.toString()

                if (nomPoint.isEmpty()) {
                    nomPoint = "Point " + (points.size + 1).toString()
                }

                points.add(Point(LatLng(marker.position.latitude, marker.position.longitude), nomPoint, 160F, 0x0035eaae))
            }
            .setNegativeButton(
                getString(R.string.annuler_ajout)
            ) {_, _ ->
                marker.isVisible = false
            }
            .create()
    }

    /**
     * Dialog s'affichant lorsque l'utilisateur veut ajouter le parcours
     *
     * Il contient un champ pour associer un nom au parcours et un bouton
     * permettant de l'ajouter à ses parcours
     *
     * @return Le dialog permettant d'ajouter un parcours
     */
    private fun dialogAjouterParcours(): AlertDialog {
        val nomParcoursEditText = EditText(this)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_ajout_parcours_dialogue))
            .setView(nomParcoursEditText)
            .setCancelable(false)
            .setMessage(getString(R.string.description_ajout_parcours_dialog))
            .setPositiveButton( getString(R.string.ajouter_point_dialog), null)
            .setNegativeButton(
                getString(R.string.annuler_ajout)
            ) {_, _ ->
                // On ne fait rien
            }
            .create()

        // Overide du click positif d'un alert dialog afin qu'il ne se ferme pas automatiquement
        dialog.setOnShowListener {
            val button = (dialog).getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener {
                val nomParcours: String = nomParcoursEditText.text.toString()

                if (nomParcours.isBlank()) {
                    val mySnackbar = Snackbar.make(findViewById(R.id.map_choix), R.string.erreur_nom_parcours_vide, Snackbar.LENGTH_SHORT)
                    mySnackbar.show()
                }
                else {
                    val nouveauParcours = Parcours(nom = nomParcours, points = points)

                    rallySpeedrunRepository.addParcours(nouveauParcours)
                    this.finish()
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    /**
     * Permet de supprimer le dernier point ajouter
     */
    private fun supprimerChoix() {
        if (points.isNotEmpty()) {
            pointsMarker.last().remove()
            pointsMarker.removeLast()
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

            if (!aChargePosition) {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 15.0f
                    )
                )
                aChargePosition = true
            }
        }
    }
}