package sebastien.fortier.dev.rally_speedrun.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import sebastien.fortier.dev.rally_speedrun.model.Parcours
import java.util.concurrent.Executors

/**
 * Permet de définir le nom de la base de données de l'application
 */
private const val DATABASE_NAME = "rallySpeedrun-database"

class RallySpeedrunRepository private constructor(context: Context) {

    private val database: RallySpeedrunDatabase = Room.databaseBuilder(
        context.applicationContext,
        RallySpeedrunDatabase::class.java,
        DATABASE_NAME
    )
        .build()

    private val executor = Executors.newSingleThreadExecutor()
    private val vinDao = database.rallySpeedrunDao()

    /**
     * Permet de faire le lien avec le DAO et la base de données pour la fonction getParcours
     */
    fun getParcours(): Flow<List<Parcours>> = vinDao.getParcours()

    /**
     * Permet de faire le lien avec le DAO et la base de données pour la méthode addParcours
     */
    fun addParcours(parcours: Parcours) {
        executor.execute {
            vinDao.addParcours(parcours)
        }
    }

    /**
     * Companion object contenant l'instance du repository de la base de données
     * Singleton
     *
     * @property INSTANCE l'instance du repository
     */
    companion object {
        private var INSTANCE: RallySpeedrunRepository? = null

        /**
         * Permet d'initialiser le repository
         *
         * @param context Le context de l'application
         */
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = RallySpeedrunRepository(context)
            }
        }

        /**
         * Permet d'obtenir l'instance du repository
         */
        fun get(): RallySpeedrunRepository {
            return INSTANCE ?: throw IllegalStateException("RallySpeedrunRepository must be initialized.")
        }
    }
}