package sebastien.fortier.dev.rally_speedrun

import android.content.Context
import java.util.*

private const val PREFERENCE_FILE_KEY = "sebastien.fortier.dev.rally_speedrun.PREFERENCE_FILE_KEY"
private const val PREF_PARCOURS = "parcours"

object ParcoursActuel {
    fun getStoredParcours(context: Context): String {
        val prefs = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        return prefs.getString(PREF_PARCOURS, "")!!
    }
    fun setStoredParcours(context: Context, query: String) {
        val prefs = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE) ?: return
        with (prefs.edit()) {
            putString(PREF_PARCOURS, query)
            apply()
        }
    }
}