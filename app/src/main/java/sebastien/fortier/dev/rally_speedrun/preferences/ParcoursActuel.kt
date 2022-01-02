package sebastien.fortier.dev.rally_speedrun.preferences

import android.content.Context

/** Clé pour le fichier contenant les préférences */
private const val PREFERENCE_FILE_KEY = "sebastien.fortier.dev.rally_speedrun.PREFERENCE_FILE_KEY"
/** Constante contenant l'index du parcours actuel */
private const val PREF_PARCOURS = "parcours"

/**
 * Object ParcoursActuel
 *
 * @author Sébastien Fortier
 */
object ParcoursActuel {
    /**
     * Permet d'obtenir la position du parcours dans la liste affichant les choix
     *
     * @param context Le contexte de l'application
     *
     * @return Index du parcours dans la liste affichant les choix
     */
    fun getStoredParcours(context: Context): String {
        val prefs = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        return prefs.getString(PREF_PARCOURS, "")!!
    }

    /**
     * Permet d'établir l'index du parcours dans la liste affichant les choix
     *
     * @param context Contexte de l'application
     * @param index Index du parcours dans la liste
     *
     */
    fun setStoredParcours(context: Context, index: String) {
        val prefs = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE) ?: return
        with (prefs.edit()) {
            putString(PREF_PARCOURS, index)
            apply()
        }
    }
}