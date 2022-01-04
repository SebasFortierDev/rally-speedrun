@file:Suppress("unused")

package sebastien.fortier.dev.rally_speedrun

import android.app.Application

/**
 * Classe RallySpeedrunApplication
 *
 * @author Sébastien Fortier
 */
class RallySpeedrunApplication : Application() {

    /**
     * Initialisation de l'application RallySpeedrunApplication
     */
    override fun onCreate() {
        super.onCreate()
        RallySpeedrunRepository.initialize(this)
    }
}