package sebastien.fortier.dev.rally_speedrun.parcours

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.model.Essai
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import java.lang.reflect.Type
import android.widget.LinearLayout
import kotlin.math.roundToLong

private const val EXTRA_ESSAI_DETAILS_ACTIVITY = "sebastien.fortier.dev.rally_speedrun.ESSAI_DETAILS_ACTIVITY"
private const val EXTRA_PARCOURS_ESSAI_DETAILS_ACTIVITY = "sebastien.fortier.dev.rally_speedrun.PARCOURS_ESSAI_DETAILS_ACTIVITY"

/**
 * Classe EssaiDetailsActivity
 *
 * @property essai L'essai qu'on voit les détails
 * @property parcours Le parcours de l'essai qu'on voit les détails
 *
 * @property txtDateEssai TextView affichant la date
 * @property txtDureeEssai TextView affichant la durée
 * @property txtNoEssai TextView affichant le numéro
 * @property txtDistance TextView affichant la distance parcourue
 * @property pointsLayout Layout faisant afficher les points et leur temps
 *
 * @author Sébastien Fortier
 */
class EssaiDetailsActivity : AppCompatActivity() {

    private var essai: Essai = Essai()
    private var parcours: Parcours = Parcours(nom = "")

    private lateinit var txtDateEssai : TextView
    private lateinit var txtDureeEssai : TextView
    private lateinit var txtNoEssai : TextView
    private lateinit var txtDistance : TextView

    private lateinit var pointsLayout : LinearLayoutCompat

    /**
     * Initialisation de l'Activity.
     *
     * @param savedInstanceState Les données conservées au changement d'état.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_essai_details)

        val essaiString = intent.getStringExtra(EXTRA_ESSAI_DETAILS_ACTIVITY).toString()
        val essaiDetails = toEssai(essaiString)

        val parcoursString = intent.getStringExtra(EXTRA_PARCOURS_ESSAI_DETAILS_ACTIVITY).toString()
        val parcoursDetail = toParcours(parcoursString)

        if (essaiDetails != null) {
            essai = essaiDetails
        }

        if (parcoursDetail != null) {
            parcours = parcoursDetail
        }

        txtDateEssai = findViewById(R.id.date_essai)
        txtDureeEssai = findViewById(R.id.temps_total)
        txtNoEssai = findViewById(R.id.numero_essai)
        txtDistance = findViewById(R.id.distance)

        pointsLayout = findViewById(R.id.layout_details_points)

        // Affichage des points
        essai.points.forEachIndexed { index, point ->

            val tempsPoint = point.tempsVisite.replace(':', '.').toDouble()
            val meilleurTempsPoints =  parcours.obtenirMeilleurEssai().points[index].tempsVisite.replace(':', '.').toDouble()
            val differenceTemps = tempsPoint - meilleurTempsPoints

            val layout = LinearLayout(this)
            layout.layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layout.orientation = LinearLayout.HORIZONTAL
            pointsLayout.addView(layout)


            val txtNomPoint = TextView(this)
            txtNomPoint.textSize = 14f
            txtNomPoint.text = getString(
                R.string.points_details_nom, point.nom)
            txtNomPoint.setTextColor(ContextCompat.getColor(this, R.color.black))
            layout.addView(txtNomPoint)

            val imageTempsPoint = ImageView(this)
            imageTempsPoint.setImageResource(R.drawable.ic_baseline_watch_later_24)
            val layoutParams = LinearLayout.LayoutParams(50, 50)
            layoutParams.setMargins(5, 0, 5, 0,)
            imageTempsPoint.layoutParams = layoutParams
            layout.addView(imageTempsPoint)

            val txtTempsEssai = TextView(this)
            txtTempsEssai.textSize = 14f
            txtTempsEssai.text = point.tempsVisite
            var couleurText = R.color.rouge_700
            if ( tempsPoint <= meilleurTempsPoints) {
                couleurText = R.color.vert
            }
            txtTempsEssai.setTextColor(ContextCompat.getColor(this, couleurText))
            layout.addView(txtTempsEssai)

            val txtBarre = TextView(this)
            txtBarre.textSize = 14f
            txtBarre.text = " | "
            txtBarre.setTextColor(ContextCompat.getColor(this, R.color.black))
            layout.addView(txtBarre)

            val imageMeilleurTemps = ImageView(this)
            imageMeilleurTemps.setImageResource(R.drawable.ic_baseline_stars_24)
            val layoutParamsMeilleurTemps = LinearLayout.LayoutParams(50, 50)
            layoutParamsMeilleurTemps.setMargins(5, 0, 5, 0,)
            imageMeilleurTemps.layoutParams = layoutParamsMeilleurTemps
            layout.addView(imageMeilleurTemps)

            val txtMeilleurTemps = TextView(this)
            txtMeilleurTemps.textSize = 14f
            txtMeilleurTemps.text = parcours.obtenirMeilleurEssai().points[index].tempsVisite
            txtMeilleurTemps.setPadding(0, 0, 0, 20)
            if (meilleurTempsPoints <= tempsPoint) {
                couleurText = R.color.vert
            }
            txtMeilleurTemps.setTextColor(ContextCompat.getColor(this, couleurText))
            layout.addView(txtMeilleurTemps)

            val txtBarre2 = TextView(this)
            txtBarre2.textSize = 14f
            txtBarre2.text = " | "
            txtBarre2.setTextColor(ContextCompat.getColor(this, R.color.black))
            layout.addView(txtBarre2)

            val txtDifferenceTemps = TextView(this)
            txtDifferenceTemps.textSize = 14f
            var signe = ""
            if (differenceTemps > 0) {
                couleurText = R.color.rouge_700
                signe = "+"
            }
            else {
                couleurText = R.color.vert
                signe = "-"
            }
            val differenceTempsRound = ((differenceTemps * 100.0).roundToLong() / 100.0).toFloat().toString().replace('.', ':')
            txtDifferenceTemps.text = getString(R.string.difference_temps, signe, differenceTempsRound)
            txtDifferenceTemps.setTextColor(ContextCompat.getColor(this, couleurText))
            layout.addView(txtDifferenceTemps)
        }
    }


    /**
     * Démarrage de l'activity.
     */
    override fun onStart() {
        super.onStart()

        val numeroEssai = parcours.essais.indexOf(essai) + 1
        title = getString(R.string.titre_details_essai, numeroEssai.toString())

        txtDateEssai.text = essai.date
        txtDureeEssai.text = essai.dureeTotal
        txtNoEssai.text = numeroEssai.toString()
        txtDistance.text = getString(R.string.distance, essai.distance.toString())
    }

    /**
     * Permet de transformer un essai qui est en JSON en objet
     *
     * @param essaiString Objet parcours en JSON
     *
     * @return Un objet essai
     */
    private fun toEssai(essaiString: String?): Essai? {
        if (essaiString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Essai?>() {}.type
        return gson.fromJson(essaiString, type)
    }

    private fun toParcours(parcoursString: String?): Parcours? {
        if (parcoursString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Parcours?>() {}.type
        return gson.fromJson(parcoursString, type)
    }
}