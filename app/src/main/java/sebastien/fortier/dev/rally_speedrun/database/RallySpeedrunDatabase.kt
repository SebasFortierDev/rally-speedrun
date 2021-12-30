package sebastien.fortier.dev.rally_speedrun.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sebastien.fortier.dev.rally_speedrun.model.Parcours

/**
 * Base de données de l'application sauvegardant les parcours
 * et les temps pour chacun
 *
 * @author Sébastien Fortier
 */
@Database(entities = [Parcours::class], version = 1, exportSchema = false)
@TypeConverters(RallySpeedrunTypeConverters::class)
abstract class RallySpeedrunDatabase : RoomDatabase() {
    /**
     * Permet de définir le DAO de la base de données
     */
    abstract fun rallySpeedrunDao(): RallySpeedrunDao
}