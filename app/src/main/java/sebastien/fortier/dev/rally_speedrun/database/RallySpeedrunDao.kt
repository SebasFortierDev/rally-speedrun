package sebastien.fortier.dev.rally_speedrun.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import java.util.*

/**
 * Interface RallySpeedrunDao
 *
 * Dao permettant l'accès à la base de données
 *
 * @author Sébastien Fortier
 */
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
     * Permet d'obtenir un parcours dans la base de données selon son ID
     *
     * @param id L'id du parcours qu'on veut obtenir
     *
     * @return Le parcours selon l'id donné
     */
    @Query("SELECT * FROM parcours WHERE id=(:id)")
    fun getParcoursSelonId(id: UUID): Flow<Parcours?>

    /**
     * Permet d'updater un parcours déjà présent dans la base de données
     *
     * @param parcours Le parcours qu'on veut updater
     */
    @Update
    fun updateParcours(parcours: Parcours)

    /**
     * Permet d'ajouter un parcours dans la base de données
     *
     * @param parcours Le parcours qu'on veut ajouter
     */
    @Insert
    fun addParcours(parcours: Parcours)
}