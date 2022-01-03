package sebastien.fortier.dev.rally_speedrun.model

import java.util.*

/**
 * Classe Essai
 *
 *
 * @author Sébastien Fortier
 */
data class Essai (
    var id: UUID = UUID.randomUUID(),
    var points: List<Point> = emptyList(),
    var dureeTotal: String = ""
)
