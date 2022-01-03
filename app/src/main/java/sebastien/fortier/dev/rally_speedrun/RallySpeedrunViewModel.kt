package sebastien.fortier.dev.rally_speedrun

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunRepository
import sebastien.fortier.dev.rally_speedrun.model.Parcours

/**
 * Classe RallySpeedrunViewModel
 *
 * @property rallySpeedrunRepository Voir [RallySpeedrunRepository]
 * @property parcoursLiveData Livedata contenant la liste des parcours
 *
 * @author Sébastien Fortier
 */
class RallySpeedrunViewModel(app: Application) : AndroidViewModel(app) {
    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    val parcoursLiveData = rallySpeedrunRepository.getParcours().asLiveData()

    /**
     * Permet l'update d'un parcours
     *
     * @param parcours Le parcours qu'on veut updater
     */
    fun updateParcours(parcours: Parcours) {
        rallySpeedrunRepository.updateParcours(parcours)
    }

}