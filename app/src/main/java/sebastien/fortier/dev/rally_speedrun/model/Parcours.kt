package sebastien.fortier.dev.rally_speedrun.model

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

/**
 * Classe Parcours
 *
 * @property id Id du parcours
 * @property nom Nom du parcours
 * @property points Liste de points du parcours
 *
 * @author Sébastien Fortier
 */
@Entity
data class Parcours (
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var nom: String,
    var points: List<Point> = emptyList(),
    var essais: MutableList<Essai> = mutableListOf(),
) {
    fun obtenirMeilleurTemps() : String {
        var meilleurTemps = 0.0

        for (essai in essais) {
            essai.dureeTotal.replace(':', '.')
            val dureeTotalDouble = essai.dureeTotal.replace(':', '.').toDouble()

            if (dureeTotalDouble > meilleurTemps) {
               meilleurTemps = dureeTotalDouble
            }
        }
        return meilleurTemps.toString()
    }

}