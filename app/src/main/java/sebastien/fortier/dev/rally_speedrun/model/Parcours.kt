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
 *
 * @author Sébastien Fortier
 */
@Entity
data class Parcours (
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var nom: String,
    var points: List<Point>
)