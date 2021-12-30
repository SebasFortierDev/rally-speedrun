package sebastien.fortier.dev.rally_speedrun.parcours

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunRepository
import sebastien.fortier.dev.rally_speedrun.model.Parcours

class AjoutParcoursActivity : AppCompatActivity() {
    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    private lateinit var btnAjouterParcours: Button
    private lateinit var btnChoisirPoints: Button
    private lateinit var nomParcoursEditText: EditText
    private lateinit var parcours: Parcours

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    val intent = Intent(this, ChoixPointsActivity::class.java)
                    startActivity(intent)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    val intent = Intent(this, ChoixPointsActivity::class.java)
                    startActivity(intent)
                } else -> {
                dialogRefuse().show()
            }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajout_parcours)

        parcours = Parcours(nom = "", points = emptyList())
        btnAjouterParcours = findViewById(R.id.btn_confirmer_ajout_parcours)
        btnChoisirPoints = findViewById(R.id.btn_choisir_points)
        nomParcoursEditText = findViewById(R.id.nom_parcours)
    }

    override fun onStart() {
        super.onStart()

        btnAjouterParcours.setOnClickListener {
            rallySpeedrunRepository.addParcours(parcours)
            this.finish()
        }

        btnChoisirPoints.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                    val intent = Intent(this, ChoixPointsActivity::class.java)
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

        val nomWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Vide
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                parcours.nom = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // Vide
            }
        }
        nomParcoursEditText.addTextChangedListener(nomWatcher)

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
}