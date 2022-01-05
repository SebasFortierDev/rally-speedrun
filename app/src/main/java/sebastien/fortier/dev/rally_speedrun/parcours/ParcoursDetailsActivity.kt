package sebastien.fortier.dev.rally_speedrun.parcours

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.model.Essai
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import java.lang.reflect.Type
import java.time.LocalDate

class ParcoursDetailsActivity : AppCompatActivity() {

    private var parcours: Parcours = Parcours(nom = "")

    private lateinit var txtNomParcours : TextView
    private lateinit var txtMeilleurTemps : TextView
    private lateinit var txtNombreEssais : TextView
    private lateinit var txtEssaisVide : TextView
    private lateinit var pointsLayout : LinearLayoutCompat

    private lateinit var essaisRecyclerView: RecyclerView
    private var adapter: EssaiAdapter? = EssaiAdapter(emptyList())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcours_details)

        val parcoursString = intent.getStringExtra("EXTRA_MAP_ACTIVITY_EXTRA_KEY").toString()
        val parcoursDetails = toParcours(parcoursString)

        if (parcoursDetails != null) {
            parcours = parcoursDetails
        }
        title = parcours.nom

        pointsLayout = findViewById(R.id.layout_details_parcours)

        for (point in parcours.points) {
            val pointTextView = TextView(this)
            pointTextView.textSize = 12f
            pointTextView.text = getString(
                R.string.points_details_position,
                point.nom, point.position.latitude.toString(), point.position.longitude.toString())
            pointTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
            pointsLayout.addView(pointTextView)
        }

        essaisRecyclerView = findViewById(R.id.essais_recycler_view)
        essaisRecyclerView.layoutManager = LinearLayoutManager(this)

        val listeOrdreTemps = parcours.essais.reversed()
        adapter = EssaiAdapter(listeOrdreTemps)
        essaisRecyclerView.adapter = adapter



        txtNomParcours = findViewById(R.id.nom_parcours)
        txtEssaisVide = findViewById(R.id.txt_essai_vide)
        txtMeilleurTemps = findViewById(R.id.meilleur_temps)
        txtNombreEssais = findViewById(R.id.nombre_essais)



    }

    override fun onStart() {
        super.onStart()
        txtNomParcours.text = parcours.nom

        val meilleurTemps = parcours.obtenirMeilleurEssai().dureeTotal
        txtMeilleurTemps.text = if (meilleurTemps.isEmpty()) getString(R.string.meilleur_temps_vide) else meilleurTemps
        txtNombreEssais.text = parcours.obtenirNombresEssais().toString()

        if (parcours.essais.isEmpty()) {
            essaisRecyclerView.visibility = View.GONE;
            txtEssaisVide.visibility = View.VISIBLE;

        }
        else {
            essaisRecyclerView.visibility = View.VISIBLE;
            txtEssaisVide.visibility = View.GONE;

        }
    }

    /**
     * Classe EssaiHolder
     *
     * @property essai Essai du holder
     *
     * @param view Vue du holder
     */
    private inner class EssaiHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var essai: Essai
        val dateEssai: TextView = itemView.findViewById(R.id.date_essai)
        val tempsEssai: TextView = itemView.findViewById(R.id.temps_total)
        val distance: TextView = itemView.findViewById(R.id.distance)

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * @param essai Essai à associer au ViewHolder.
         */
        fun bind(essai: Essai) {
            this.essai = essai
            dateEssai.text = essai.date
            tempsEssai.text = essai.dureeTotal
            distance.text = getString(R.string.distance, essai.distance.toString())
            if (essai == parcours.obtenirMeilleurEssai())
                tempsEssai.setTextColor(ContextCompat.getColor(applicationContext, R.color.vert))

        }

        /**
         * Event click sur un ViewHolder.
         *
         * @param v La vue cliquée.
         */
        override fun onClick(v: View?) {
            val intent = Intent(applicationContext, EssaiDetailsActivity::class.java)
            val nomParcours = parcours.nom
            val pointsParcours = parcours.points
            parcours = Parcours(id = parcours.id, nom = nomParcours, points = pointsParcours, essais = parcours.essais)
            val essaiString = fromEssai(essai)
            val parcoursString = fromParcours(parcours)
            intent.putExtra("EXTRA_MAP_ACTIVITY_EXTRA_KEY", essaiString )
            intent.putExtra("EXTRA_PARCOURS_EXTRA_KEY", parcoursString )
            Log.d("essaiString", essaiString)
            startActivity(intent)
        }
    }

    /**
     * Classe EssaiAdapter
     *
     * @param listeEssais Liste des essais
     */
    private inner class EssaiAdapter(var listeEssais: List<Essai>) :
        RecyclerView.Adapter<EssaiHolder>() {

        /**
         * Création du ViewHolder
         *
         * @param parent Le parent où ajouter notre ViewHolder.
         * @param viewType Le type de la nouvelle vue.
         *
         * @return Le ViewHolder instancié.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EssaiHolder {
            val view = layoutInflater.inflate(R.layout.list_item_essais, parent, false)
            return EssaiHolder(view)
        }

        /**
         * Bind sur le ViewHolder selon la position
         *
         * @param holder Le ViewHolder qui est bindé.
         * @param position La position à charger.
         */
        override fun onBindViewHolder(holder: EssaiHolder, position: Int) {
            val listeEssais: Essai = listeEssais[position]
            holder.bind(listeEssais)
        }

        /**
         * La quantité de données dans le RecyclerView.
         */
        override fun getItemCount(): Int = listeEssais.size
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
     * Permet de transformer un essai en JSON afin de l'envoyer à une activity
     *
     * @param essai Le parcours qu'on veut transformer en JSON
     *
     * @return L'essai en JSON
     */
    private fun fromEssai(essai: Essai?): String {
        val gson = Gson()
        val type: Type = object : TypeToken<Essai?>() {}.type
        return gson.toJson(essai, type)
    }
}