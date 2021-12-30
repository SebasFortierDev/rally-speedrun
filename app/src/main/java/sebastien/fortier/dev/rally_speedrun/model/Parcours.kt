package sebastien.fortier.dev.rally_speedrun.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Parcours (
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var nom: String,
    var points: List<Point>
)