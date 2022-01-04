package sebastien.fortier.dev.rally_speedrun.parcours

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.R
import sebastien.fortier.dev.rally_speedrun.RallySpeedrunViewModel
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import java.lang.reflect.Type
import java.util.*

class ParcoursDetailsActivity : AppCompatActivity() {

    private var parcours: Parcours = Parcours(nom = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcours_details)

        val parcoursString = intent.getStringExtra("EXTRA_MAP_ACTIVITY_EXTRA_KEY").toString()
        val parcoursDetails = toParcours(parcoursString)

        if (parcoursDetails != null) {
            parcours = parcoursDetails
        }
    }

    override fun onStart() {
        super.onStart()

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