package sebastien.fortier.dev.rally_speedrun

import android.app.Application
import androidx.lifecycle.*
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunRepository
import sebastien.fortier.dev.rally_speedrun.model.Parcours

class RallySpeedrunViewModel(private val app: Application) : AndroidViewModel(app) {
    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    val parcoursLiveData = rallySpeedrunRepository.getParcours().asLiveData()

    private val mutableParcoursActuel = MutableLiveData<Parcours>()

    val parcoursActuel: Parcours
        get() = mutableParcoursActuel.value ?: Parcours(nom = "", points = emptyList())

}