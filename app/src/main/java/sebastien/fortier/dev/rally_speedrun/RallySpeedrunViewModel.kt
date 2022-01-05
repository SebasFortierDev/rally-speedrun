package sebastien.fortier.dev.rally_speedrun

import android.app.Application
import androidx.lifecycle.*
import sebastien.fortier.dev.rally_speedrun.RallySpeedrunRepository


/**
 * Classe RallySpeedrunViewModel
 *
 * @property rallySpeedrunRepository Voir [RallySpeedrunRepository]
 * @property listeParcoursLiveData Livedata contenant la liste des parcours
 *
 * @author Sébastien Fortier
 */
class RallySpeedrunViewModel(app: Application) : AndroidViewModel(app) {
    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    val listeParcoursLiveData = rallySpeedrunRepository.getParcours().asLiveData()




}