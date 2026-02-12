package fr.accoradd.gitspine.infrastructure.image

import androidx.compose.ui.graphics.Color
import fr.accoradd.gitspine.core.extension.toMD5
import org.jetbrains.skia.*
import kotlin.math.abs
import org.jetbrains.skia.Canvas as SkiaCanvas

class IdenticonGenerator {
    companion object {
        private const val SIZE = 16
        private const val GRID_SIZE = 5 // Grille 5x5 pour le motif central
    }

    /**
     * Génère un identicon comme Skia Bitmap
     */
    fun generateSkiaBitmap(email: String): Bitmap {
        val hash = hashEmail(email)
        val colors = generateColor(hash)
        val pattern = generatePattern(hash)

        return createSkiaBitmap(colors.first, colors.second, pattern)
    }

    /**
     * Crée un Skia Bitmap avec l'identicon
     */
    private fun createSkiaBitmap(
        backgroundColor: Color,
        foregroundColor: Color,
        pattern: BooleanArray
    ): Bitmap {
        val imageInfo = ImageInfo.makeS32(SIZE, SIZE, ColorAlphaType.PREMUL)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)

        val canvas = SkiaCanvas(bitmap)
        val cellSize = SIZE.toFloat() / GRID_SIZE

        // Paint pour le fond
        val bgPaint = Paint().apply {
            color = backgroundColor.toSkiaColor()
        }

        // Paint pour le motif
        val fgPaint = Paint().apply {
            color = foregroundColor.toSkiaColor()
        }

        // Remplir avec la couleur de fond
        canvas.drawRect(
            Rect.makeXYWH(0f, 0f, SIZE.toFloat(), SIZE.toFloat()),
            bgPaint
        )

        // Dessiner le motif
        for (gridY in 0 until GRID_SIZE) {
            for (gridX in 0 until GRID_SIZE) {
                if (pattern[gridY * GRID_SIZE + gridX]) {
                    val x = gridX * cellSize
                    val y = gridY * cellSize

                    canvas.drawRect(
                        Rect.makeXYWH(x, y, cellSize, cellSize),
                        fgPaint
                    )
                }
            }
        }

        return bitmap
    }


    /**
     * Génère un hash MD5 de l'email
     */
    private fun hashEmail(email: String): ByteArray {
        return email.toMD5().toByteArray()
    }

    /**
     * Génère la couleur de fond
     */
    private fun generateColor(hash: ByteArray): Pair<Color, Color> {
        // Utiliser HSL pour un contrôle total
//        val hue1 = hashBytesToInt(hash, 0) % 360f
//        val hue1= ((hash[0].toInt() and 0xFF) +
//                (hash[1].toInt() and 0xFF) * 256 +
//                (hash[2].toInt() and 0xFF) * 65536) % 360f
        val seed = ((hash[0].toInt() and 0xFF) shl 8) or
                (hash[1].toInt() and 0xFF)
        val hue1 = (seed % 360f)
        val hue2 = (hue1 + 60) % 360

        // Saturation et luminosité FIXES pour cohérence
        val saturation = 0.85f  // Vives mais pas criardes
        val lightness = 0.55f   // Lisibles sur blanc ET noir

        println("$hue2 $hue1 $saturation $lightness")

        return Pair(
            hslToColor(hue1, 0.7f, 0.4f),
            hslToColor(hue2, 0.6f, 0.7f)
        )
    }

    /**
     * Génère un motif symétrique
     */
    private fun generatePattern(hash: ByteArray): BooleanArray {
        val pattern = BooleanArray(GRID_SIZE * GRID_SIZE)
        val halfWidth = GRID_SIZE / 2
        var byteIndex = 2
        var bitIndex = 0

        for (y in 0 until GRID_SIZE) {
            for (x in 0..halfWidth) {
                val byte = hash[byteIndex % hash.size].toInt() and 0xFF
                val bit = (byte shr bitIndex) and 1
                val filled = bit == 1

                pattern[y * GRID_SIZE + x] = filled
                if (x < halfWidth) {
                    pattern[y * GRID_SIZE + (GRID_SIZE - 1 - x)] = filled
                }

                bitIndex++
                if (bitIndex >= 8) {
                    bitIndex = 0
                    byteIndex++
                }
            }
        }

        return pattern
    }

    /**
     * Convertit HSL en Color Compose
     */
    private fun hslToColor(h: Float, s: Float, l: Float): Color {
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(r + m, g + m, b + m)
    }

    /**
     * Convertit Color Compose en couleur Skia (Int ARGB)
     */
    private fun Color.toSkiaColor(): Int {
        val a = (alpha * 255f + 0.5f).toInt()
        val r = (red * 255f + 0.5f).toInt()
        val g = (green * 255f + 0.5f).toInt()
        val b = (blue * 255f + 0.5f).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun hashBytesToInt(hash: ByteArray, offset: Int): Int {
        return (
                ((hash[offset].toInt() and 0xFF) shl 24) or
                        ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                        ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                        (hash[offset + 3].toInt() and 0xFF)
                ).let { abs(it) }
    }
}
