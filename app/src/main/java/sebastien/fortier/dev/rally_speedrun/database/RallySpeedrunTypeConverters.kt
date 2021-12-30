package sebastien.fortier.dev.rally_speedrun.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sebastien.fortier.dev.rally_speedrun.model.Point
import java.lang.reflect.Type
import java.util.*

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

    @TypeConverter
    fun fromListePoints(listePoints: List<Point?>?): String? {
        if (listePoints == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Point?>?>() {}.type
        return gson.toJson(listePoints, type)
    }

    @TypeConverter
    fun toListePoints(listePointsString: String?): List<Point?>? {
        if (listePointsString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Point?>?>() {}.type
        return gson.fromJson(listePointsString, type)
    }
}