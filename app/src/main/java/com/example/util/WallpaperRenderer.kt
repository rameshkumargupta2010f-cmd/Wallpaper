package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.util.LruCache
import com.example.model.Wallpaper
import com.example.model.WallpaperStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object WallpaperRenderer {

    // Fast in-memory thumbnail cache (120 cached bitmaps) for butter-smooth 60fps scrolling
    private val thumbnailCache = object : LruCache<String, Bitmap>(120) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    /**
     * Retrieves or renders a lightweight thumbnail for the grid (approx 270x480).
     */
    fun getThumbnailBitmap(context: Context, wallpaper: Wallpaper, thumbWidth: Int = 270, thumbHeight: Int = 480): Bitmap {
        val cacheKey = "${wallpaper.id}_${thumbWidth}x${thumbHeight}"
        thumbnailCache.get(cacheKey)?.let { return it }

        if (wallpaper.drawableResId != null) {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565 // Memory efficient for grid
                inSampleSize = 2
            }
            val original = BitmapFactory.decodeResource(context.resources, wallpaper.drawableResId, options)
            if (original != null) {
                val scaled = Bitmap.createScaledBitmap(original, thumbWidth, thumbHeight, true)
                thumbnailCache.put(cacheKey, scaled)
                return scaled
            }
        }

        val bitmap = Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        renderToCanvas(canvas, wallpaper, thumbWidth, thumbHeight)
        thumbnailCache.put(cacheKey, bitmap)
        return bitmap
    }

    /**
     * Generates or extracts a full-resolution 1440x2560 Bitmap for downloading or applying as wallpaper,
     * optionally graded with a RealisticTonePreset.
     */
    fun createFullResolutionBitmap(
        context: Context,
        wallpaper: Wallpaper,
        width: Int = 1440,
        height: Int = 2560,
        tonePreset: com.example.model.RealisticTonePreset = com.example.model.RealisticTonePreset.STUDIO_RAW
    ): Bitmap {
        val baseBitmap = if (wallpaper.drawableResId != null) {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            val original = BitmapFactory.decodeResource(context.resources, wallpaper.drawableResId, options)
            if (original != null) {
                if (original.width == width && original.height == height) {
                    original
                } else {
                    Bitmap.createScaledBitmap(original, width, height, true)
                }
            } else {
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                renderToCanvas(Canvas(bmp), wallpaper, width, height)
                bmp
            }
        } else {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            renderToCanvas(canvas, wallpaper, width, height)
            bitmap
        }

        if (tonePreset == com.example.model.RealisticTonePreset.STUDIO_RAW) {
            return baseBitmap
        }
        return applyRealisticToneBitmap(baseBitmap, tonePreset)
    }

    fun getColorMatrixArray(preset: com.example.model.RealisticTonePreset): FloatArray {
        return when (preset) {
            com.example.model.RealisticTonePreset.STUDIO_RAW -> floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            com.example.model.RealisticTonePreset.CINEMATIC_HDR -> floatArrayOf(
                1.18f, 0f, 0f, 0f, -14f,
                0f, 1.18f, 0f, 0f, -14f,
                0f, 0f, 1.22f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f
            )
            com.example.model.RealisticTonePreset.AMOLED_VIVID -> floatArrayOf(
                1.28f, -0.05f, -0.05f, 0f, -26f,
                -0.05f, 1.28f, -0.05f, 0f, -26f,
                -0.04f, -0.04f, 1.32f, 0f, -24f,
                0f, 0f, 0f, 1f, 0f
            )
            com.example.model.RealisticTonePreset.GOLDEN_RIM -> floatArrayOf(
                1.20f, 0.04f, 0f, 0f, 4f,
                0.02f, 1.10f, 0f, 0f, -6f,
                0f, 0f, 0.96f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
        }
    }

    private fun applyRealisticToneBitmap(
        source: Bitmap,
        preset: com.example.model.RealisticTonePreset
    ): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val matrix = android.graphics.ColorMatrix(getColorMatrixArray(preset))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /**
     * Renders procedural wallpaper on any Android Canvas (used for high-res export and bitmap generation)
     */
    fun renderToCanvas(canvas: Canvas, wallpaper: Wallpaper, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val pColor = (wallpaper.primaryColor or 0xFF000000).toInt()
        val sColor = (wallpaper.secondaryColor or 0xFF000000).toInt()
        val aColor = (wallpaper.accentColor or 0xFF000000).toInt()
        val bgDark = if (wallpaper.isAmoled) Color.rgb(2, 3, 5) else Color.rgb(10, 12, 18)

        when (wallpaper.styleType) {
            WallpaperStyle.CANVAS_FACE_AND_BODY -> {
                CharacterFigureRenderer.drawDualFaceAndBodyComposition(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_FULL_BODY_HERO -> {
                CharacterFigureRenderer.drawFullBodyHeroicComposition(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_CINEMATIC_FACE -> {
                CharacterFigureRenderer.drawCinematicFaceComposition(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_KANJI -> {
                drawKanjiWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_AURA -> {
                drawAuraWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_KI_SPHERE -> {
                drawKiSphereWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_BEAM -> {
                drawBeamWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_DRAGON_BALLS -> {
                drawDragonBallsWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_SCENE -> {
                drawSceneWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_CYBER_TECH -> {
                drawCyberTechWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_MANGA_SKETCH -> {
                drawMangaSketchWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_COSMIC_VOID -> {
                drawCosmicVoidWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.CANVAS_LIGHTNING_STORM -> {
                drawLightningStormWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
            WallpaperStyle.DRAWABLE_ASSET -> {
                drawAuraWallpaper(canvas, wallpaper, width, height, pColor, sColor, aColor, bgDark, paint)
            }
        }
    }

    private fun drawKanjiWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        val bgShader = RadialGradient(
            w * 0.5f, h * 0.5f, h * 0.65f,
            intArrayOf(Color.argb(90, Color.red(primary), Color.green(primary), Color.blue(primary)), bgDark),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Outer ki seal rings
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.argb(70, Color.red(primary), Color.green(primary), Color.blue(primary))
        canvas.drawCircle(w * 0.5f, h * 0.48f, w * 0.38f, paint)

        paint.strokeWidth = 1.5f
        paint.color = Color.argb(45, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawCircle(w * 0.5f, h * 0.48f, w * 0.42f, paint)

        val rand = Random(wp.id.hashCode())
        paint.style = Paint.Style.FILL
        for (i in 0..50) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val size = 1f + rand.nextFloat() * 3.5f
            val alpha = 40 + rand.nextInt(160)
            paint.color = Color.argb(alpha, Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawCircle(px, py, size, paint)
        }

        val glowShader = RadialGradient(
            w * 0.5f, h * 0.48f, w * 0.30f,
            intArrayOf(Color.argb(130, Color.red(primary), Color.green(primary), Color.blue(primary)), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = glowShader
        canvas.drawCircle(w * 0.5f, h * 0.48f, w * 0.32f, paint)
        paint.shader = null

        val displaySymbol = if (wp.kanjiSymbol.isNotEmpty()) wp.kanjiSymbol else "氣"

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = w * 0.45f

        // Outer glow on Kanji
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText(displaySymbol, w * 0.5f, h * 0.55f, paint)

        // Core Kanji in pure white
        paint.textSize = w * 0.43f
        paint.color = Color.argb(120, 255, 255, 255)
        canvas.drawText(displaySymbol, w * 0.5f, h * 0.545f, paint)

        // Render Character Face & Full Body Stance in front of Kanji
        val archetype = CharacterFigureRenderer.getArchetype(wp.character)
        val palette = CharacterFigureRenderer.resolvePalette(wp, archetype)
        CharacterFigureRenderer.drawCharacterFace(canvas, archetype, wp, w * 0.5f, h * 0.18f, w * 0.19f, palette, paint, isCloseUp = true)
        CharacterFigureRenderer.drawCharacterWholeBody(canvas, archetype, wp, w * 0.5f, h * 0.68f, h * 0.44f, palette, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.045f
        paint.color = Color.WHITE
        canvas.drawText(wp.form.uppercase(), w * 0.5f, h * 0.92f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText("${wp.character.uppercase()} • ${wp.resolution}", w * 0.5f, h * 0.955f, paint)
    }

    private fun drawAuraWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        val bgShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(bgDark, Color.rgb(12, 14, 24), bgDark),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        val auraShader = RadialGradient(
            w * 0.5f, h * 0.62f, h * 0.55f,
            intArrayOf(
                Color.argb(170, Color.red(primary), Color.green(primary), Color.blue(primary)),
                Color.argb(80, Color.red(secondary), Color.green(secondary), Color.blue(secondary)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = auraShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        val rand = Random(wp.id.hashCode())
        paint.style = Paint.Style.STROKE
        for (i in 0..24) {
            val startX = w * (0.22f + rand.nextFloat() * 0.56f)
            val path = Path()
            path.moveTo(startX, h * 0.90f)
            var curX = startX
            var curY = h * 0.90f
            val flameHeight = h * (0.35f + rand.nextFloat() * 0.40f)
            val steps = 8
            val dy = flameHeight / steps
            for (step in 1..steps) {
                curY -= dy
                curX += (rand.nextFloat() - 0.5f) * w * 0.08f
                path.lineTo(curX, curY)
            }
            paint.strokeWidth = 2f + rand.nextFloat() * 5f
            val alpha = 80 + rand.nextInt(140)
            paint.color = if (rand.nextBoolean()) {
                Color.argb(alpha, Color.red(primary), Color.green(primary), Color.blue(primary))
            } else {
                Color.argb(alpha, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            }
            canvas.drawPath(path, paint)
        }

        // Lightning
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        for (i in 0..5) {
            val startX = w * (0.15f + rand.nextFloat() * 0.70f)
            val path = Path()
            path.moveTo(startX, h * (0.25f + rand.nextFloat() * 0.45f))
            var curX = startX
            var curY = h * 0.35f
            for (seg in 0..6) {
                curX += (rand.nextFloat() - 0.5f) * w * 0.12f
                curY += h * 0.06f
                path.lineTo(curX, curY)
            }
            paint.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawPath(path, paint)
        }

        // Core energy
        paint.style = Paint.Style.FILL
        val coreShader = RadialGradient(
            w * 0.5f, h * 0.58f, w * 0.22f,
            intArrayOf(Color.WHITE, Color.argb(160, Color.red(primary), Color.green(primary), Color.blue(primary)), Color.TRANSPARENT),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = coreShader
        canvas.drawCircle(w * 0.5f, h * 0.58f, w * 0.25f, paint)
        paint.shader = null

        // Render Character Face & Full Body Stance
        val archetype = CharacterFigureRenderer.getArchetype(wp.character)
        val palette = CharacterFigureRenderer.resolvePalette(wp, archetype)
        CharacterFigureRenderer.drawCharacterFace(canvas, archetype, wp, w * 0.5f, h * 0.20f, w * 0.20f, palette, paint, isCloseUp = true)
        CharacterFigureRenderer.drawCharacterWholeBody(canvas, archetype, wp, w * 0.5f, h * 0.68f, h * 0.44f, palette, paint)

        // Typography
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.045f
        paint.color = Color.WHITE
        canvas.drawText(wp.form.uppercase(), w * 0.5f, h * 0.92f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText("${wp.character.uppercase()} • ${wp.resolution}", w * 0.5f, h * 0.955f, paint)
    }

    private fun drawKiSphereWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = bgDark
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val sphereCenterX = w * 0.5f
        val sphereCenterY = h * 0.45f
        val sphereRadius = w * 0.36f

        paint.style = Paint.Style.STROKE
        for (r in 1..4) {
            paint.strokeWidth = 1.5f + r * 0.8f
            paint.color = Color.argb(120 / r, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            canvas.drawCircle(sphereCenterX, sphereCenterY, sphereRadius * (1f + r * 0.16f), paint)
        }

        val rand = Random(wp.id.hashCode())
        for (i in 0..45) {
            val angle = rand.nextFloat() * 2 * PI.toFloat()
            val distOuter = sphereRadius * (1.3f + rand.nextFloat() * 0.8f)
            val distInner = sphereRadius * (1.02f + rand.nextFloat() * 0.1f)
            val x1 = sphereCenterX + cos(angle) * distOuter
            val y1 = sphereCenterY + sin(angle) * distOuter
            val x2 = sphereCenterX + cos(angle) * distInner
            val y2 = sphereCenterY + sin(angle) * distInner
            paint.strokeWidth = 1.5f + rand.nextFloat() * 2f
            paint.color = Color.argb(80 + rand.nextInt(120), Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        paint.style = Paint.Style.FILL
        val sphereShader = RadialGradient(
            sphereCenterX - sphereRadius * 0.2f, sphereCenterY - sphereRadius * 0.2f, sphereRadius * 1.1f,
            intArrayOf(
                Color.WHITE,
                Color.argb(240, Color.red(primary), Color.green(primary), Color.blue(primary)),
                Color.argb(200, Color.red(secondary), Color.green(secondary), Color.blue(secondary)),
                Color.argb(40, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            ),
            floatArrayOf(0f, 0.35f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = sphereShader
        canvas.drawCircle(sphereCenterX, sphereCenterY, sphereRadius, paint)
        paint.shader = null

        // Render Character Face and Whole Body holding sphere
        val archetypeSphere = CharacterFigureRenderer.getArchetype(wp.character)
        val paletteSphere = CharacterFigureRenderer.resolvePalette(wp, archetypeSphere)
        CharacterFigureRenderer.drawCharacterFace(canvas, archetypeSphere, wp, w * 0.5f, h * 0.18f, w * 0.18f, paletteSphere, paint, isCloseUp = true)
        CharacterFigureRenderer.drawCharacterWholeBody(canvas, archetypeSphere, wp, w * 0.5f, h * 0.65f, h * 0.44f, paletteSphere, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.045f
        paint.color = Color.WHITE
        canvas.drawText(wp.form.uppercase(), w * 0.5f, h * 0.92f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText("${wp.character.uppercase()} • ${wp.resolution}", w * 0.5f, h * 0.955f, paint)
    }

    private fun drawBeamWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = bgDark
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val beamCenterX = w * 0.5f
        val path = Path().apply {
            moveTo(beamCenterX - w * 0.08f, 0f)
            lineTo(beamCenterX + w * 0.08f, 0f)
            lineTo(beamCenterX + w * 0.35f, h.toFloat())
            lineTo(beamCenterX - w * 0.35f, h.toFloat())
            close()
        }

        val beamShader = LinearGradient(
            beamCenterX - w * 0.25f, 0f, beamCenterX + w * 0.25f, 0f,
            intArrayOf(
                Color.argb(0, Color.red(secondary), Color.green(secondary), Color.blue(secondary)),
                Color.argb(220, Color.red(primary), Color.green(primary), Color.blue(primary)),
                Color.WHITE,
                Color.argb(220, Color.red(primary), Color.green(primary), Color.blue(primary)),
                Color.argb(0, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            ),
            floatArrayOf(0f, 0.35f, 0.5f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = beamShader
        canvas.drawPath(path, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f
        paint.color = Color.argb(190, Color.red(accent), Color.green(accent), Color.blue(accent))
        val rings = 10
        for (i in 0 until rings) {
            val y = h * (0.1f + i * 0.075f)
            val rx = w * (0.12f + (y / h) * 0.18f)
            val ry = h * 0.02f
            canvas.drawOval(beamCenterX - rx, y - ry, beamCenterX + rx, y + ry, paint)
        }

        // Render Character Face and Whole Body firing beam
        val archetypeBeam = CharacterFigureRenderer.getArchetype(wp.character)
        val paletteBeam = CharacterFigureRenderer.resolvePalette(wp, archetypeBeam)
        CharacterFigureRenderer.drawCharacterFace(canvas, archetypeBeam, wp, w * 0.5f, h * 0.20f, w * 0.20f, paletteBeam, paint, isCloseUp = true)
        CharacterFigureRenderer.drawCharacterWholeBody(canvas, archetypeBeam, wp, w * 0.5f, h * 0.68f, h * 0.44f, paletteBeam, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.045f
        paint.color = Color.WHITE
        canvas.drawText(wp.form.uppercase(), w * 0.5f, h * 0.92f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText("${wp.character.uppercase()} • ${wp.resolution}", w * 0.5f, h * 0.955f, paint)
    }

    private fun drawDragonBallsWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = bgDark
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val nebulaShader = RadialGradient(
            w * 0.5f, h * 0.45f, w * 0.7f,
            intArrayOf(Color.argb(120, 255, 145, 0), Color.argb(40, 0, 176, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = nebulaShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        val cx = w * 0.5f
        val cy = h * 0.45f
        val r = w * 0.28f

        drawSingleDragonBall(canvas, cx, cy, r, 4, paint)

        val orbitRadius = w * 0.40f
        val otherStars = listOf(1, 2, 3, 5, 6, 7)
        for (i in 0 until 6) {
            val angle = (i * (360f / 6f) - 90f) * (PI.toFloat() / 180f)
            val bx = cx + cos(angle) * orbitRadius
            val by = cy + sin(angle) * orbitRadius
            drawSingleDragonBall(canvas, bx, by, w * 0.09f, otherStars[i], paint)
        }

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText(wp.title.uppercase(), w * 0.5f, h * 0.78f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.rgb(255, 171, 64)
        canvas.drawText("${wp.character.uppercase()} • 7 SACRED ORBS", w * 0.5f, h * 0.82f, paint)
    }

    private fun drawCyberTechWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = bgDark
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Scouter grid lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(40, Color.red(primary), Color.green(primary), Color.blue(primary))
        val step = w * 0.10f
        var x = 0f
        while (x < w) {
            canvas.drawLine(x, 0f, x, h.toFloat(), paint)
            x += step
        }
        var y = 0f
        while (y < h) {
            canvas.drawLine(0f, y, w.toFloat(), y, paint)
            y += step
        }

        // Central targeting reticle
        val cx = w * 0.5f
        val cy = h * 0.46f
        paint.strokeWidth = 2f
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawCircle(cx, cy, w * 0.28f, paint)
        canvas.drawCircle(cx, cy, w * 0.15f, paint)

        // Reticle brackets
        paint.strokeWidth = 4f
        canvas.drawLine(cx - w * 0.32f, cy, cx - w * 0.24f, cy, paint)
        canvas.drawLine(cx + w * 0.24f, cy, cx + w * 0.32f, cy, paint)
        canvas.drawLine(cx, cy - w * 0.32f, cx, cy - w * 0.24f, paint)
        canvas.drawLine(cx, cy + w * 0.24f, cx, cy + w * 0.32f, paint)

        // Scouter readout
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = w * 0.075f
        paint.color = Color.argb(230, Color.red(primary), Color.green(primary), Color.blue(primary))
        canvas.drawText("BP: 9,001,000+", cx, cy + w * 0.025f, paint)

        paint.textSize = w * 0.042f
        paint.color = Color.WHITE
        canvas.drawText(wp.character.uppercase(), cx, h * 0.76f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(180, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
        canvas.drawText("TARGET: ${wp.form.uppercase()}", cx, h * 0.80f, paint)
    }

    private fun drawMangaSketchWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        // Deep pure black background for manga
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val cx = w * 0.5f
        val cy = h * 0.48f

        // Dynamic manga speedlines radiating outward
        paint.style = Paint.Style.STROKE
        val rand = Random(wp.id.hashCode())
        val linesCount = 70
        for (i in 0 until linesCount) {
            val angle = (i * (360f / linesCount) + rand.nextFloat() * 4f) * (PI.toFloat() / 180f)
            val rInner = w * (0.28f + rand.nextFloat() * 0.08f)
            val rOuter = w * (1.1f + rand.nextFloat() * 0.4f)
            val x1 = cx + cos(angle) * rInner
            val y1 = cy + sin(angle) * rInner
            val x2 = cx + cos(angle) * rOuter
            val y2 = cy + sin(angle) * rOuter

            paint.strokeWidth = 1f + rand.nextFloat() * 3.5f
            val alpha = 100 + rand.nextInt(150)
            paint.color = Color.argb(alpha, 255, 255, 255)
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // Halftone dots in corners
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(80, Color.red(primary), Color.green(primary), Color.blue(primary))
        for (r in 0..15) {
            for (c in 0..15) {
                if (rand.nextFloat() > 0.4f) {
                    canvas.drawCircle(c * (w * 0.04f), r * (h * 0.02f), 2f, paint)
                    canvas.drawCircle(w - c * (w * 0.04f), h - r * (h * 0.02f), 2f, paint)
                }
            }
        }

        // Kanji or silhouette character mark
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = w * 0.28f
        paint.color = Color.WHITE
        val mark = if (wp.kanjiSymbol.isNotEmpty()) wp.kanjiSymbol else wp.character.take(1)
        canvas.drawText(mark, cx, cy + w * 0.10f, paint)

        // Bold manga banner at bottom
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.06f
        paint.color = Color.argb(230, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText(wp.title.uppercase(), cx, h * 0.82f, paint)
    }

    private fun drawCosmicVoidWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        val bgShader = RadialGradient(
            w * 0.5f, h * 0.40f, h * 0.6f,
            intArrayOf(
                Color.argb(160, Color.red(secondary), Color.green(secondary), Color.blue(secondary)),
                Color.argb(90, Color.red(primary), Color.green(primary), Color.blue(primary)),
                Color.BLACK
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Star particles
        val rand = Random(wp.id.hashCode())
        paint.style = Paint.Style.FILL
        for (i in 0..120) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val size = 1f + rand.nextFloat() * 3f
            paint.color = Color.argb(80 + rand.nextInt(170), 255, 255, 255)
            canvas.drawCircle(px, py, size, paint)
        }

        // Divine Ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.argb(200, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.28f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText(wp.form.uppercase(), w * 0.5f, h * 0.76f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(180, Color.red(primary), Color.green(primary), Color.blue(primary))
        canvas.drawText(wp.character.uppercase(), w * 0.5f, h * 0.80f, paint)
    }

    private fun drawLightningStormWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = bgDark
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val rand = Random(wp.id.hashCode())
        paint.style = Paint.Style.STROKE

        // Massive electric discharges across canvas
        for (b in 0..10) {
            val path = Path()
            var curX = rand.nextFloat() * w
            var curY = 0f
            path.moveTo(curX, curY)
            val segments = 12
            val dy = h.toFloat() / segments
            for (s in 1..segments) {
                curY += dy
                curX += (rand.nextFloat() - 0.5f) * w * 0.25f
                path.lineTo(curX, curY)
            }
            paint.strokeWidth = 2f + rand.nextFloat() * 4f
            paint.color = Color.argb(180 + rand.nextInt(75), Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawPath(path, paint)
        }

        // Inner glowing core
        paint.style = Paint.Style.FILL
        val coreShader = RadialGradient(
            w * 0.5f, h * 0.50f, w * 0.35f,
            intArrayOf(
                Color.argb(140, Color.red(primary), Color.green(primary), Color.blue(primary)),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = coreShader
        canvas.drawCircle(w * 0.5f, h * 0.50f, w * 0.35f, paint)
        paint.shader = null

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText(wp.title.uppercase(), w * 0.5f, h * 0.82f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText("${wp.character.uppercase()} • ELECTRIC STRIKE", w * 0.5f, h * 0.86f, paint)
    }

    private fun drawSceneWallpaper(
        canvas: Canvas,
        wp: Wallpaper,
        w: Int,
        h: Int,
        primary: Int,
        secondary: Int,
        accent: Int,
        bgDark: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        val skyShader = LinearGradient(
            0f, 0f, 0f, h * 0.7f,
            intArrayOf(Color.rgb(10, 14, 25), Color.rgb(25, 30, 50), Color.rgb(45, 20, 60)),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = skyShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawCircle(w * 0.75f, h * 0.25f, w * 0.16f, paint)

        val mountainPath = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w * 0.25f, h * 0.58f)
            lineTo(w * 0.50f, h * 0.65f)
            lineTo(w * 0.75f, h * 0.52f)
            lineTo(w.toFloat(), h * 0.68f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        paint.color = Color.rgb(8, 10, 16)
        canvas.drawPath(mountainPath, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.argb(220, Color.red(primary), Color.green(primary), Color.blue(primary))
        val boltPath = Path().apply {
            moveTo(w * 0.5f, h * 0.35f)
            lineTo(w * 0.48f, h * 0.45f)
            lineTo(w * 0.53f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.62f)
        }
        canvas.drawPath(boltPath, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText(wp.title.uppercase(), w * 0.5f, h * 0.84f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(180, 200, 210, 230)
        canvas.drawText("${wp.character.uppercase()} • ${wp.saga.uppercase()}", w * 0.5f, h * 0.88f, paint)
    }

    private fun drawSingleDragonBall(canvas: Canvas, x: Float, y: Float, radius: Float, stars: Int, paint: Paint) {
        val ballShader = RadialGradient(
            x - radius * 0.35f, y - radius * 0.35f, radius * 1.35f,
            intArrayOf(
                Color.rgb(255, 235, 150),
                Color.rgb(255, 152, 0),
                Color.rgb(230, 81, 0),
                Color.rgb(180, 50, 0)
            ),
            floatArrayOf(0f, 0.45f, 0.85f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = ballShader
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null

        paint.color = Color.argb(160, 255, 255, 255)
        canvas.drawCircle(x - radius * 0.35f, y - radius * 0.35f, radius * 0.22f, paint)

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(213, 0, 0)
            style = Paint.Style.FILL
        }

        val starRadius = radius * 0.16f
        when (stars) {
            1 -> drawStar(canvas, x, y, starRadius, starPaint)
            2 -> {
                drawStar(canvas, x - starRadius * 1.4f, y, starRadius, starPaint)
                drawStar(canvas, x + starRadius * 1.4f, y, starRadius, starPaint)
            }
            3 -> {
                drawStar(canvas, x, y - starRadius * 1.3f, starRadius, starPaint)
                drawStar(canvas, x - starRadius * 1.3f, y + starRadius * 1.1f, starRadius, starPaint)
                drawStar(canvas, x + starRadius * 1.3f, y + starRadius * 1.1f, starRadius, starPaint)
            }
            4 -> {
                drawStar(canvas, x - starRadius * 1.2f, y - starRadius * 1.2f, starRadius, starPaint)
                drawStar(canvas, x + starRadius * 1.2f, y - starRadius * 1.2f, starRadius, starPaint)
                drawStar(canvas, x - starRadius * 1.2f, y + starRadius * 1.2f, starRadius, starPaint)
                drawStar(canvas, x + starRadius * 1.2f, y + starRadius * 1.2f, starRadius, starPaint)
            }
            5 -> {
                drawStar(canvas, x, y, starRadius, starPaint)
                drawStar(canvas, x - starRadius * 1.4f, y - starRadius * 1.4f, starRadius, starPaint)
                drawStar(canvas, x + starRadius * 1.4f, y - starRadius * 1.4f, starRadius, starPaint)
                drawStar(canvas, x - starRadius * 1.4f, y + starRadius * 1.4f, starRadius, starPaint)
                drawStar(canvas, x + starRadius * 1.4f, y + starRadius * 1.4f, starRadius, starPaint)
            }
            6 -> {
                for (s in 0..5) {
                    val a = s * 60f * (PI.toFloat() / 180f)
                    drawStar(canvas, x + cos(a) * starRadius * 1.4f, y + sin(a) * starRadius * 1.4f, starRadius * 0.9f, starPaint)
                }
            }
            7 -> {
                drawStar(canvas, x, y, starRadius * 0.9f, starPaint)
                for (s in 0..5) {
                    val a = s * 60f * (PI.toFloat() / 180f)
                    drawStar(canvas, x + cos(a) * starRadius * 1.5f, y + sin(a) * starRadius * 1.5f, starRadius * 0.85f, starPaint)
                }
            }
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        val innerRadius = radius * 0.45f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = (i * 36 - 90) * (PI.toFloat() / 180f)
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}
