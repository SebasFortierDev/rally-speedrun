package sebastien.fortier.dev.rally_speedrun.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Classe Parcours
 *
 * @property id Id du parcours
 * @property nom Nom du parcours
 * @property points Liste de points du parcours
 * @property essais Liste d'essai pour ce parcours
 *
 * @author Sébastien Fortier
 */
@Entity
data class Parcours(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var nom: String,
    var points: List<Point> = emptyList(),
    var essais: MutableList<Essai> = mutableListOf(),
) {

    /**
     * Permet d'obtenir l'essai avec le temps total le plus petit
     *
     * @return Le meilleur essai pour ce parcours
     */
    fun obtenirMeilleurEssai(): Essai {
        var meilleurTemps = 3000000.0 // Chiffre trop grand
        var meilleurEssai = Essai()

        for (essai in essais) {
            essai.dureeTotal.replace(':', '.')
            val dureeTotalDouble = essai.dureeTotal.replace(':', '.').toDouble()

            if (dureeTotalDouble < meilleurTemps) {
                meilleurTemps = dureeTotalDouble
                meilleurEssai = essai
            }
        }
        return meilleurEssai
    }

    /**
     * Permet d'obtenir le nombre d'essai pour ce parcours
     */
    fun obtenirNombresEssais(): Int {
        return essais.size
    }

}