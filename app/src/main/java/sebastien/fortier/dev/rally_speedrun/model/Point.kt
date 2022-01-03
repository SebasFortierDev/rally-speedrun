package sebastien.fortier.dev.rally_speedrun.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

/**
 * Classe Point
 *
 * @property position La position gps du point
 * @property nom Le nom associé au point
 * @property couleurHue La couleur du marqueur sous format hue
 * @property couleurHexa La couleur du marqueur sous format hexadecimal (sans opacité)
 * @property estVisite Détermine si le point a été visité ou non (Vrai si visité)
 * @property tempsVisite Le temps auquel le point a été visité
 * @property marker Le Marqueur sur la map GoogleMap
 *
 * @author Sébastien Fortier
 */
data class Point(
    var position: LatLng,
    var nom: String,
    var couleurHue: Float,
    var couleurHexa: Int,
    var estVisite: Boolean = false,
    var tempsVisite: String = "",
    var marker: Marker? = null,
)



