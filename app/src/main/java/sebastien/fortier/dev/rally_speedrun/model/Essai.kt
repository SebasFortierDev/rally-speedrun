package sebastien.fortier.dev.rally_speedrun.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Classe Essai
 *
 * @property id Id de l'essai
 * @property points Liste de points du parcours de l'essai
 * @property dureeTotal Durée total de l'essai
 * @property date Date du début de l'essai
 * @property distance Distance parcourue lors de l'essai
 *
 * @author Sébastien Fortier
 */
data class Essai (
    var id: UUID = UUID.randomUUID(),
    var points: List<Point> = emptyList(),
    var dureeTotal: String = "",
    var date: String = LocalDate.now().toString() + " | " + LocalTime.now().hour + ":" + LocalTime.now().minute.toString() + ":" + LocalTime.now().second.toString(),
    var distance: Float = 0f
)
