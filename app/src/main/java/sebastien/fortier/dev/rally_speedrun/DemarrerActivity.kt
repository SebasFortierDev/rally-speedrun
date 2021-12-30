package sebastien.fortier.dev.rally_speedrun

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunTypeConverters
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import sebastien.fortier.dev.rally_speedrun.model.Point
import java.lang.reflect.Type


/**
 * Activity qui demande les permissions et qui permet de débuter le rally
 *
 * @property requestPermissionLauncher Le lancher qui va demander les permissions
 * @property btnCommencer Le bouton demandant les permissions et qui permet de commencer le rally
 *
 * @author Sébastien Fortier
 */
class DemarrerActivity : AppCompatActivity() {
    private val rallySpeedrunViewModel: RallySpeedrunViewModel by viewModels()

    private lateinit var btnCommencer: Button

    private lateinit var parcours: Parcours


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
    }

    /**
     * Démarrage de l'activity.
     */
    override fun onStart() {
        super.onStart()

        btnCommencer.setOnClickListener {

            when {
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {

                    // ICI

                    // Envoie du parcours désiré
                    val intent = Intent(this, MainActivity::class.java)

                    val parcoursString = fromParcours(parcours)
                    intent.putExtra("EXTRA_MAP_ACTIVITY_EXTRA_KEY", parcoursString )

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

        rallySpeedrunViewModel.parcoursLiveData.observe(
            this,
            { listeParcours ->
                listeParcours?.let {
                    Log.d("Parcours", "Réponse : $listeParcours")
                    parcours = listeParcours[0]
                    Log.d("Parcours", "Réponse : $parcours")
                }
            }
        )
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

    fun fromParcours(parcours: Parcours?): String {

        val gson = Gson()
        val type: Type = object : TypeToken<Parcours?>() {}.type
        return gson.toJson(parcours, type)
    }
}