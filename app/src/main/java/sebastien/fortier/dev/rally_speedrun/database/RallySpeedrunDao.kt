package sebastien.fortier.dev.rally_speedrun.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sebastien.fortier.dev.rally_speedrun.model.Parcours

@Dao
interface RallySpeedrunDao {
    /**
     * Permet d'obtenir toutes les parcours de la base de données
     *
     * @return La liste des parcours dans la base de données
     */
    @Query("SELECT * FROM parcours")
    fun getParcours(): Flow<List<Parcours>>

    /**
     * Permet d'ajouter un parcours dans la base de données
     *
     * @param parcours Le parcours qu'on veut ajouter
     */
    @Insert
    fun addParcours(parcours: Parcours)
}