package sebastien.fortier.dev.rally_speedrun.parcours

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.RallySpeedrunViewModel
import sebastien.fortier.dev.rally_speedrun.model.Parcours

class ParcoursListActivity : AppCompatActivity() {
    private val rallySpeedrunViewModel: RallySpeedrunViewModel by viewModels()

    private lateinit var parcoursRecyclerView: RecyclerView
    private var adapter: ParcoursAdapter? = ParcoursAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcours_list)
        parcoursRecyclerView = findViewById(R.id.parcours_recycler_view)
        parcoursRecyclerView.layoutManager = LinearLayoutManager(this)
        parcoursRecyclerView.adapter = adapter


    }

    override fun onStart() {
        super.onStart()

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
     * @param preuves La liste des preuves
     */
    private fun updateUI(listeParcours: List<Parcours>) {
        adapter = ParcoursAdapter(listeParcours)
        parcoursRecyclerView.adapter = adapter
    }

    /**
     * Classe PreuveHolder

     */
    private inner class ParcoursHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var parcours: Parcours
        val nomParcours: TextView = itemView.findViewById(R.id.parcours_nom)


        init {
            itemView.setOnClickListener(this)
        }

        /**
         * @param preuve Le preuve à associer au ViewHolder.
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
     */
    private inner class ParcoursAdapter(var listeParcours: List<Parcours>) : RecyclerView.Adapter<ParcoursHolder>() {

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


}