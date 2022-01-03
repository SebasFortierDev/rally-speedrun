package sebastien.fortier.dev.rally_speedrun

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.Transliterator
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Chronometer
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunRepository
import sebastien.fortier.dev.rally_speedrun.model.Essai
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import sebastien.fortier.dev.rally_speedrun.model.Point
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit


private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

/**
 * Activity principal qui contient la carte de Google map
 *
 * @property rallySpeedrunRepository Voir [RallySpeedrunRepository]
 * @property txtPas Texte affichant le nombre de pas
 * @property chrono Le chronomètre dans la vue
 * @property mapFragment Le fragment contenant la carte
 * @property googleMap L'objet Google Map permettant d'accéder au méthodes de la carte
 * @property mapEstChargee Permet de savoir si la carte a été chargé ou non
 * @property parcours Parcours actuel
 * @property essai Essai actuel
 * @property fusedLocationClient Client pour accéder au location provider
 * @property locationRequest Permet de faire les requêtes pour la location
 * @property locationCallback Permet d'obtenir le retour du provider
 * @property requestingLocationUpdates Permet de savoir si on est entrain de demander la location
 * @property points Liste des points du rally
 * @property markerPosition Marker indiquant la position de l'utilisateur
 * @property distance La distance parcourue durant l'essai
 * @property derniereLocation Location depuis le dernier update de la position
 * @property compteurPas Le compteur de pas depuis le début du rally
 * @property compteurPasBase Le nombre de pas au début du rally
 * @property sensorManager Permet de gérer l'utilisations des sensors du téléphone
 * @property sensor Le sensor utilisé
 *
 * @author Sébastien Fortier
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {
    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    private lateinit var txtPas: TextView
    private lateinit var chrono: Chronometer

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var mapEstChargee = false

    private var parcours: Parcours = Parcours(nom = "", points = emptyList())

    private var essai: Essai = Essai()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = false

    private var points = emptyList<Point>()

    private var markerPosition: Marker? = null

    private var distance = 0f
    private var derniereLocation : Location? = null
    private var compteurPas = 0f
    private var compteurPasBase = -1f
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null


    /**
     * Initialisation de l'Activity.
     *
     * @param savedInstanceState Les données conservées au changement d'état.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar?.hide()
        // Charge l'affichage du nb de pas et du chronometre
        txtPas = findViewById(R.id.nb_pas)
        chrono = findViewById(R.id.chronometer)
        chrono.start()

        // Charge le sensor pour le nb de pas
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

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

        // Récupération du parcours envoyé
        val parcoursString = intent.getStringExtra("EXTRA_MAP_ACTIVITY_EXTRA_KEY").toString()
        val parcoursMap = toParcours(parcoursString)

        if (parcoursMap != null) {
            parcours = parcoursMap
            Log.d("parcoursMapRecu" , parcoursMap.toString())
            Log.d("parcoursMapAssocié" , parcours.toString())
        }

        // Charge les points voulus dans la liste
        points = parcours.points

        // Charge la carte
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    /**
     * Permet de faire des actions lorsque les valeurs du sensor changent
     *
     * @param event L'évènement du sensor
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (compteurPasBase == -1f) {
            compteurPasBase = event.values[0]
        } else {
            compteurPas = event.values[0] - compteurPasBase
            txtPas.text = getString(R.string.nb_pas, String.format("%.0f", compteurPas))
        }
    }

    /**
     * Permet de faire des actions lorsque la précision du sensor change
     *
     * @param p0 Le sensor
     * @param p1 Le int du p1
     */
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // ...
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

        txtPas.text = getString(R.string.nb_pas, "0")
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
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

    /**
     * Permet de savoir si la carte a été chargé ou non
     *
     * @param map la carte
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapEstChargee = true

        chargerMarqueurs()
    }

    /**
     * Permet de charger les points dans la liste de point
     * sur la carte en tant que marqueur
     */
    private fun chargerMarqueurs() {
        val opaciteContour = 0xFF000000
        val opaciteInterieur = 0x50000000

        for (point in points) {
            point.marker = googleMap.addMarker(
                MarkerOptions()
                    .position(point.position)
                    .title(point.nom)
            )

            point.marker?.setIcon(BitmapDescriptorFactory.defaultMarker(point.couleurHue))

            googleMap.addCircle(CircleOptions().apply {
                strokeColor((point.couleurHexa + opaciteContour).toInt())
                fillColor(point.couleurHexa + opaciteInterieur)
                strokeWidth(3.00f)
                center(point.position)
                radius(100.0)
            })
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
        estDansCercle(location)
        calculerDistance(location)
        detecterFin()
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

    /**
     * Détecte si l'utilisateur a finit son parcours et gère la fin
     */
    private fun detecterFin() {
        var toutVisite = true

        for(point in points) {
            if (!point.estVisite) {
                toutVisite = false
                break
            }
        }

        if (toutVisite) {
            stopLocationUpdates()
            chrono.stop()
            dialogResultat().show()
        }
    }

    /**
     * Permet de vérifier si l'utilisateur est le cercle d'un des points
     *
     * @param location La location de l'utilisateur
     */
    private fun estDansCercle(location: Location) {
        val resultats: FloatArray = floatArrayOf(0.00f)

        points.forEachIndexed { index, point ->
            Location.distanceBetween(location.latitude, location.longitude, point.position.latitude, point.position.longitude, resultats)

            if (resultats[0] <= 100) {
                if (index == 0) {
                    val bitmap = AppCompatResources.getDrawable(this, R.drawable.ic_baseline_check_circle_outline_24)?.toBitmap()
                    point.marker?.setIcon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                    point.estVisite = true
                    point.tempsVisite = chrono.text.toString()
                }
                else if (points[index - 1].estVisite) {
                    val bitmap = AppCompatResources.getDrawable(this, R.drawable.ic_baseline_check_circle_outline_24)?.toBitmap()
                    point.marker?.setIcon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                    point.estVisite = true
                    point.tempsVisite = chrono.text.toString()
                }
            }
        }
    }


    /**
     * Permet de calculer la distance entre la position de l'utilisateur et sa dernière position sauvegardé
     * afin de calculer la distance totale parcourue
     *
     * @param location La location de l'utilisateur
     *
     */
    private fun calculerDistance(location: Location?) {
        val resultats: FloatArray = floatArrayOf(0.00f)

        if (derniereLocation == null) {
            derniereLocation = location
        }
        else {
            if (location != null) {
                Location.distanceBetween(location.latitude, location.longitude, derniereLocation!!.latitude, derniereLocation!!.longitude, resultats)
                distance += resultats[0]
                derniereLocation = location
            }
        }
    }
    /**
     * Création de la boite de dialogue pour afficher les résultats du rally
     *
     * @return La boite de dialogue
     */
    private fun dialogResultat(): AlertDialog {
        val contenueDialog = "Temps: " + chrono.text +
                System.lineSeparator() + "Nombre de pas: " + String.format("%.0f", compteurPas)
        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.fin_dialogue_titre))
            .setMessage(contenueDialog)
            .setPositiveButton(
                getString(R.string.fin_dialogue_positif)
            ) { _, _ ->
                essai.dureeTotal = chrono.text.toString()
                essai.distance = (Math.round((distance / 1000) * 100.0) / 100.0).toFloat()
                parcours.essais.add(essai)

                val pointsEssai = ArrayList<Point>()
                for (point in points) {
                    pointsEssai.add(Point(point.position, point.nom, point.couleurHue, point.couleurHexa, point.estVisite, point.tempsVisite))
                }

                essai.points = pointsEssai
                rallySpeedrunRepository.updateEssai(parcours.id, parcours.essais)

                this.finish()
            }
            .create()
    }


    /**
     * Permet de transformer un parcours qui est en JSON en objet
     *
     * @param parcoursString Objet parcours en JSON
     *
     * @return Un objet parcours
     */
    private fun toParcours(parcoursString: String?): Parcours? {
        if (parcoursString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Parcours?>() {}.type
        return gson.fromJson(parcoursString, type)
    }
}