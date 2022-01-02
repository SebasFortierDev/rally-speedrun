package sebastien.fortier.dev.rally_speedrun.parcours

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.RallySpeedrunViewModel
import sebastien.fortier.dev.rally_speedrun.model.Parcours

/**
 * Classe ParcoursListActivity
 *
 *
 * @property rallySpeedrunViewModel ViewModel de l'application
 *
 * @property btnAjouterParcours Bouton chargeant l'activity permettant l'ajout d'un parcours
 * @property parcoursRecyclerView RecyclerView affichant la liste des parcours
 * @property adapter Adapter pour le recyclerView affichant la liste des parcours
 * @property requestPermissionLauncher Launcher pour demander les permissions à l'utilisateur
 *
 * @author Sébastien Fortier
 */
class ParcoursListActivity : AppCompatActivity() {
    private val rallySpeedrunViewModel: RallySpeedrunViewModel by viewModels()

    private lateinit var btnAjouterParcours: Button
    private lateinit var parcoursRecyclerView: RecyclerView
    private var adapter: ParcoursAdapter? = ParcoursAdapter(emptyList())
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    val intent = Intent(this, AjoutParcoursActivity::class.java)
                    startActivity(intent)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    val intent = Intent(this, AjoutParcoursActivity::class.java)
                    startActivity(intent)
                }
                else -> {
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
        setContentView(R.layout.activity_parcours_list)

        btnAjouterParcours = findViewById(R.id.btn_ajout_parcours)

        parcoursRecyclerView = findViewById(R.id.parcours_recycler_view)
        parcoursRecyclerView.layoutManager = LinearLayoutManager(this)
        parcoursRecyclerView.adapter = adapter
    }

    /**
     * Démarrage de l'activity.
     */
    override fun onStart() {
        super.onStart()

        btnAjouterParcours.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {

                    val intent = Intent(this, AjoutParcoursActivity::class.java)

                    startActivity(intent)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                    dialogPermission().show()
                }
                else -> {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            // Est nécessaire pour compter les pas,
                            // donc nécessaire pour l'exercice
                            Manifest.permission.ACTIVITY_RECOGNITION
                        )
                    )
                }
            }
        }

        rallySpeedrunViewModel.parcoursLiveData.observe(
            this,
            { listeParcours ->
                listeParcours?.let {
                    updateUI(listeParcours)
                }
            }
        )
    }

    /**
     * Met à jour la vue en fonction des données du ViewModel.
     *
     * @param listeParcours Liste des parcours
     */
    private fun updateUI(listeParcours: List<Parcours>) {
        adapter = ParcoursAdapter(listeParcours)
        parcoursRecyclerView.adapter = adapter
    }

    /**
     * Classe PreuveHolder
     *
     * @property parcours Parcours du holder
     * @property nomParcours TextView affichant
     *
     * @param view Vue du holder
     */
    private inner class ParcoursHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var parcours: Parcours
        val nomParcours: TextView = itemView.findViewById(R.id.parcours_nom)

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * @param parcours Parcours à associer au ViewHolder.
         */
        fun bind(parcours: Parcours) {
            this.parcours = parcours
            nomParcours.text = parcours.nom
        }

        /**
         * Event click sur un ViewHolder.
         *
         * @param v La vue cliquée.
         */
        override fun onClick(v: View?) {
            // preuveDetailViewModel.selectIdPreuve(preuve.id)
        }
    }

    /**
     * Classe PreuveAdapter
     *
     * @param listeParcours Liste des parcours
     */
    private inner class ParcoursAdapter(var listeParcours: List<Parcours>) :
        RecyclerView.Adapter<ParcoursHolder>() {

        /**
         * Création du ViewHolder
         *
         * @param parent Le parent où ajouter notre ViewHolder.
         * @param viewType Le type de la nouvelle vue.
         *
         * @return Le ViewHolder instancié.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParcoursHolder {
            val view = layoutInflater.inflate(R.layout.list_item_parcours, parent, false)
            return ParcoursHolder(view)
        }

        /**
         * Bind sur le ViewHolder selon la position
         *
         * @param holder Le ViewHolder qui est bindé.
         * @param position La position à charger.
         */
        override fun onBindViewHolder(holder: ParcoursHolder, position: Int) {
            val listeParcours: Parcours = listeParcours[position]
            holder.bind(listeParcours)
        }

        /**
         * La quantité de données dans le RecyclerView.
         */
        override fun getItemCount(): Int = listeParcours.size
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