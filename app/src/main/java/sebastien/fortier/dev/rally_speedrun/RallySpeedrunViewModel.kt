package sebastien.fortier.dev.rally_speedrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sebastien.fortier.dev.rally_speedrun.database.RallySpeedrunRepository

class RallySpeedrunViewModel : ViewModel() {
    private val rallySpeedrunRepository = RallySpeedrunRepository.get()

    val parcoursLiveData = rallySpeedrunRepository.getParcours().asLiveData()
}