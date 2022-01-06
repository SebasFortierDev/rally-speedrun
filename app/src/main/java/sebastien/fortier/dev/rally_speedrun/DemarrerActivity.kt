package sebastien.fortier.dev.rally_speedrun

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import sebastien.fortier.dev.rally_speedrun.parcours.ParcoursListActivity
import sebastien.fortier.dev.rally_speedrun.preferences.ParcoursActuel
import java.lang.reflect.Type
import java.util.ArrayList

private const val EXTRA_PARCOURS_MAP_ACTIVITY = "sebastien.fortier.dev.rally_speedrun.PARCOURS_MAP_ACTIVITY"

/**
 * Activity qui demande les permissions et qui permet de débuter le rally
 *
 * @property rallySpeedrunViewModel ViewModel de l'application
 *
 * @property spinnerParcours Spinner affichant les parcours disponibles
 * @property btnCommencer Bouton permettant de démarrer le parcours
 * @property btnParcours Bouton permettant de voir la liste de ses parcours
 *
 * @property parcours Parcours actuel qui va être démarré
 * @property listeParcoursActuel Liste des parcours actuellement disponible
 *
 * @property requestPermissionLauncher Lancher qui va demander les permissions
 *
 * @author Sébastien Fortier
 */


class DemarrerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val rallySpeedrunViewModel: RallySpeedrunViewModel by viewModels()

    private lateinit var spinnerParcours : Spinner

    private lateinit var btnCommencer: Button
    private lateinit var btnParcours: Button

    private var parcours: Parcours = Parcours(nom = "", points = emptyList())

    private var listeParcoursActuel: List<Parcours> = emptyList()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else -> {
                    dialogRefuse().show()
                }
            }
        }

    /**
     * Initialisation de l'Activity.
     *
     * @param savedInstanceState Les données conservées au changement d'état.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demarrer)

        btnCommencer = findViewById(R.id.btnCommencer)
        btnParcours = findViewById(R.id.btnParcours)

        // Observateur du livedata de la liste de parcours dans la BD
        rallySpeedrunViewModel.listeParcoursLiveData.observe(
            this,
            { listeParcours ->
                listeParcours?.let {

                    listeParcoursActuel = listeParcours

                    spinnerParcours = findViewById(R.id.parcours_spinner)

                    val choixParcours = ArrayList<String>()

                    for (parcours in listeParcours) {
                        choixParcours.add(parcours.nom)
                    }

                    val spinnerArrayAdapter: ArrayAdapter<String> =
                        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, choixParcours)

                    spinnerParcours.adapter = spinnerArrayAdapter
                    spinnerParcours.onItemSelectedListener = this

                    if (ParcoursActuel.getStoredParcours(this) != "") {
                        spinnerParcours.setSelection(ParcoursActuel.getStoredParcours(this).toInt())
                        parcours = listeParcours[ParcoursActuel.getStoredParcours(this).toInt()]
                    }

                    // Mettre à jour le bouton commencer parcours
                    btnCommencer.isEnabled = listeParcoursActuel.isNotEmpty()
                }
            }
        )
    }

    /**
     * Démarrage de l'activity.
     */
    override fun onStart() {
        super.onStart()

        // Mettra à jour le bouton commencer parcours
        btnCommencer.isEnabled = listeParcoursActuel.isNotEmpty()

        btnCommencer.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {


                    val intent = Intent(this, MainActivity::class.java)
                    val nomParcours = parcours.nom
                    val pointsParcours = parcours.points
                    parcours = Parcours(id = parcours.id, nom = nomParcours, points = pointsParcours, essais = parcours.essais)
                    val parcoursString = fromParcours(parcours)
                    intent.putExtra(EXTRA_PARCOURS_MAP_ACTIVITY, parcoursString )

                    startActivity(intent)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                    dialogPermission().show()
                }
                else -> {
                    requestPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        // Est nécessaire pour compter les pas,
                        // donc nécessaire pour l'exercice
                        Manifest.permission.ACTIVITY_RECOGNITION))
                }
            }
        }

        btnParcours.setOnClickListener {
            val intent = Intent(this, ParcoursListActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Création de la boite de dialogue pour expliquer les
     * fonctionnalités mandant des permissionsde
     *
     * @return La boite de dialogue
     */
    private fun dialogPermission(): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_dialog_permission))
            .setMessage(getString(R.string.body_dialog_permission))
            .setPositiveButton(getString(R.string.positif_dialog_base)) { _, _ ->
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    // Est nécessaire pour compter les pas,
                    // donc nécessaire pour l'exercice
                    Manifest.permission.ACTIVITY_RECOGNITION))
            }
            .create()
    }

    /**
     * Création de la boite de dialogue pour demander
     * d'aller activer les permissions dans les paramètres
     *
     * @return La boite de dialogue
     */
    private fun dialogRefuse(): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(getString(R.string.titre_dialog_refuse))
            .setMessage(getString(R.string.body_dialog_refuse))
            .setPositiveButton(getString(R.string.positif_dialog_base)) {_, _ -> }
            .create()
    }

    /**
     * Permet de transformer le parcours actuel en JSON afin de l'envoyer à une activity
     *
     * @param parcours Le parcours qu'on veut transformer en JSON
     *
     * @return Le parcours en JSON
     */
    private fun fromParcours(parcours: Parcours?): String {
        val gson = Gson()
        val type: Type = object : TypeToken<Parcours?>() {}.type
        return gson.toJson(parcours, type)
    }

    /**
     * Permet de faire des actions lorsqu'un item de la liste du spinner est selectionné
     */
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        Log.d("onItemSelected", position.toString())
        ParcoursActuel.setStoredParcours(this, position.toString())
        parcours = listeParcoursActuel[position]
    }

    /**
     * Permet de faire des actions lorsque aucun item de la liste du spinner est selectionné
     */
    override fun onNothingSelected(p0: AdapterView<*>?) {
        // Rien à faire pour le moment
    }
}