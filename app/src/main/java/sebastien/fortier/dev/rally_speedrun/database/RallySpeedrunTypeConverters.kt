package sebastien.fortier.dev.rally_speedrun.database

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.model.Essai
import sebastien.fortier.dev.rally_speedrun.model.Point
import java.lang.reflect.Type
import java.util.*

/**
 * Classe RallySpeedrunTypeConverters
 *
 * Permet de convertir les types afin de pouvoir les storer dans la base de données
 *
 * @author Sébastien Fortier
 */
class RallySpeedrunTypeConverters {

    /**
     * Permet de transformer un UUID en string
     *
     * @param uuid Le UUID dans la base de données
     *
     * @return Un UUID en String
     */
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    /**
     * Permet de créer un UUID à partir d'une String
     *
     * @param uuid Le UUID en String
     *
     * @return Un UUID de type UUID
     */
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    /**
     * Permet de transformer une liste de points en JSON
     *
     * @param listePoints La liste de points qu'on veut transformer
     *
     * @return La liste de points en JSON
     */
    @TypeConverter
    fun fromListePoints(listePoints: List<Point?>?): String? {
        if (listePoints == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Point?>?>() {}.type
        return gson.toJson(listePoints, type)
    }

    /**
     * Permet de transformer une liste de points qui est en JSON en objet
     *
     * @param listePointsString La liste de points en JSON
     *
     * @return Un objet List contenant les points
     */
    @TypeConverter
    fun toListePoints(listePointsString: String?): List<Point?>? {
        if (listePointsString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Point?>?>() {}.type
        return gson.fromJson(listePointsString, type)
    }

    /**
     * Permet de transformer une liste d'essais en JSON
     *
     * @param listeEssais La liste d'essais qu'on veut transformer
     *
     * @return La liste d'essais en JSON
     */
    @TypeConverter
    fun fromListeEssais(listeEssais: List<Essai?>?): String? {
        if (listeEssais == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Essai?>?>() {}.type
        Log.d("allo", gson.toJson(listeEssais, type))
        return gson.toJson(listeEssais, type)
    }

    /**
     * Permet de transformer une liste d'essais qui est en JSON en objet
     *
     * @param listeEssaisString La liste d'essais en JSON
     *
     * @return Un objet List contenant les essais
     */
    @TypeConverter
    fun toListeEssais(listeEssaisString: String?): List<Essai?>? {
        if (listeEssaisString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Essai?>?>() {}.type
        return gson.fromJson(listeEssaisString, type)
    }
}