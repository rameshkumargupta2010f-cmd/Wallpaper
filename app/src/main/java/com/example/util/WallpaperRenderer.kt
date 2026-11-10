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
import com.example.model.Wallpaper
import com.example.model.WallpaperStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object WallpaperRenderer {

    /**
     * Generates or extracts a full-resolution 1080x1920 Bitmap for downloading or applying as wallpaper.
     */
    fun createFullResolutionBitmap(context: Context, wallpaper: Wallpaper, width: Int = 1080, height: Int = 1920): Bitmap {
        if (wallpaper.drawableResId != null) {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            val original = BitmapFactory.decodeResource(context.resources, wallpaper.drawableResId, options)
            if (original != null) {
                // If original matches aspect ratio, we can scale to target or return original
                return if (original.width == width && original.height == height) {
                    original
                } else {
                    Bitmap.createScaledBitmap(original, width, height, true)
                }
            }
        }

        // Procedural offline canvas render for high-resolution output
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        renderToCanvas(canvas, wallpaper, width, height)
        return bitmap
    }

    /**
     * Renders procedural wallpaper on any Android Canvas (used for high-res export and bitmap generation)
     */
    fun renderToCanvas(canvas: Canvas, wallpaper: Wallpaper, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val pColor = (wallpaper.primaryColor or 0xFF000000).toInt()
        val sColor = (wallpaper.secondaryColor or 0xFF000000).toInt()
        val aColor = (wallpaper.accentColor or 0xFF000000).toInt()
        val bgDark = if (wallpaper.isAmoled) Color.rgb(4, 5, 8) else Color.rgb(10, 12, 18)

        when (wallpaper.styleType) {
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
            WallpaperStyle.DRAWABLE_ASSET -> {
                // Fallback if drawable fails
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
        // Deep gradient background
        val bgShader = RadialGradient(
            w * 0.5f, h * 0.5f, h * 0.65f,
            intArrayOf(Color.argb(90, Color.red(primary), Color.green(primary), Color.blue(primary)), bgDark),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Geometric sacred ring / ki seal behind Kanji
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.argb(60, Color.red(primary), Color.green(primary), Color.blue(primary))
        canvas.drawCircle(w * 0.5f, h * 0.48f, w * 0.38f, paint)

        paint.strokeWidth = 1.5f
        paint.color = Color.argb(40, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawCircle(w * 0.5f, h * 0.48f, w * 0.42f, paint)

        // Stardust embers
        val rand = Random(wp.title.hashCode())
        paint.style = Paint.Style.FILL
        for (i in 0..70) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val size = 1f + rand.nextFloat() * 3.5f
            val alpha = 50 + rand.nextInt(180)
            paint.color = Color.argb(alpha, Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawCircle(px, py, size, paint)
        }

        // Circular background glow for Kanji
        val glowShader = RadialGradient(
            w * 0.5f, h * 0.48f, w * 0.30f,
            intArrayOf(Color.argb(120, Color.red(primary), Color.green(primary), Color.blue(primary)), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = glowShader
        canvas.drawCircle(w * 0.5f, h * 0.48f, w * 0.32f, paint)
        paint.shader = null

        // Large stylized Kanji symbol
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = w * 0.48f

        // Outer glow on Kanji
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText(wp.kanjiSymbol, w * 0.5f, h * 0.55f, paint)

        // Core Kanji in pure white / gold
        paint.textSize = w * 0.46f
        paint.color = Color.WHITE
        canvas.drawText(wp.kanjiSymbol, w * 0.5f, h * 0.545f, paint)

        // Kanji meaning and character label below
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.045f
        paint.color = Color.argb(220, 255, 255, 255)
        canvas.drawText(wp.kanjiMeaning.uppercase(), w * 0.5f, h * 0.68f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = w * 0.032f
        paint.color = Color.argb(150, 200, 205, 220)
        canvas.drawText(wp.character.uppercase(), w * 0.5f, h * 0.715f, paint)

        // Bottom quote / saga
        if (wp.quote.isNotEmpty()) {
            paint.textSize = w * 0.028f
            paint.color = Color.argb(110, 180, 185, 200)
            canvas.drawText("\"${wp.quote}\"", w * 0.5f, h * 0.88f, paint)
        }
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
        // Base dark space
        val bgShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(bgDark, Color.rgb(12, 14, 24), bgDark),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Radial energetic aura burst from bottom-center
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

        // Vertical fiery energy flames (blazing aura pillars)
        val rand = Random(wp.title.hashCode())
        paint.style = Paint.Style.STROKE
        for (i in 0..25) {
            val startX = w * (0.25f + rand.nextFloat() * 0.5f)
            val path = Path()
            path.moveTo(startX, h * 0.90f)
            var curX = startX
            var curY = h * 0.90f
            val flameHeight = h * (0.35f + rand.nextFloat() * 0.40f)
            val steps = 10
            val dy = flameHeight / steps
            for (step in 1..steps) {
                curY -= dy
                curX += (rand.nextFloat() - 0.5f) * w * 0.08f
                path.lineTo(curX, curY)
            }
            paint.strokeWidth = 2f + rand.nextFloat() * 6f
            val alpha = 80 + rand.nextInt(140)
            paint.color = if (rand.nextBoolean()) {
                Color.argb(alpha, Color.red(primary), Color.green(primary), Color.blue(primary))
            } else {
                Color.argb(alpha, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            }
            canvas.drawPath(path, paint)
        }

        // Crackling lightning bolts (Super Saiyan 2 / 3 / Beast / Ultra Ego lightning)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        for (i in 0..6) {
            val startX = w * (0.15f + rand.nextFloat() * 0.70f)
            val path = Path()
            path.moveTo(startX, h * (0.20f + rand.nextFloat() * 0.50f))
            var curX = startX
            var curY = h * 0.35f
            for (seg in 0..7) {
                curX += (rand.nextFloat() - 0.5f) * w * 0.12f
                curY += h * 0.05f
                path.lineTo(curX, curY)
            }
            paint.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawPath(path, paint)
        }

        // Core energy blast core
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

        // Character form typography in stylized minimalist aesthetic
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.055f
        paint.color = Color.WHITE
        canvas.drawText(wp.form.uppercase(), w * 0.5f, h * 0.78f, paint)

        paint.textSize = w * 0.035f
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText(wp.character.uppercase(), w * 0.5f, h * 0.82f, paint)
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
        // Deep cosmos background
        paint.style = Paint.Style.FILL
        paint.color = bgDark
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val sphereCenterX = w * 0.5f
        val sphereCenterY = h * 0.45f
        val sphereRadius = w * 0.38f

        // Outer cosmic aura shockwave rings
        paint.style = Paint.Style.STROKE
        for (r in 1..4) {
            paint.strokeWidth = 1.5f + r * 0.8f
            paint.color = Color.argb(120 / r, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            canvas.drawCircle(sphereCenterX, sphereCenterY, sphereRadius * (1f + r * 0.16f), paint)
        }

        // Inward streaming energy particle lines
        val rand = Random(wp.title.hashCode())
        for (i in 0..60) {
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

        // Giant energy sphere gradient
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

        // Stardust embers
        for (i in 0..80) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val size = 1f + rand.nextFloat() * 3f
            paint.color = Color.argb(150, 255, 255, 255)
            canvas.drawCircle(px, py, size, paint)
        }

        // Title and technique name
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText(wp.title.uppercase(), w * 0.5f, h * 0.74f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText("${wp.character.uppercase()} • ${wp.saga.uppercase()}", w * 0.5f, h * 0.78f, paint)
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

        // Diagonal dynamic energy beam (Kamehameha / Final Flash / Special Beam Cannon)
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

        // Corkscrew spiral energy ring (Special Beam Cannon drill / Kamehameha ki coils)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb(200, Color.red(accent), Color.green(accent), Color.blue(accent))
        val rings = 12
        for (i in 0 until rings) {
            val y = h * (0.1f + i * 0.065f)
            val rx = w * (0.12f + (y / h) * 0.18f)
            val ry = h * 0.02f
            canvas.drawOval(beamCenterX - rx, y - ry, beamCenterX + rx, y + ry, paint)
        }

        // Particle sparks
        val rand = Random(wp.title.hashCode())
        paint.style = Paint.Style.FILL
        for (i in 0..100) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val size = 1f + rand.nextFloat() * 4f
            paint.color = Color.argb(180, 255, 255, 255)
            canvas.drawCircle(px, py, size, paint)
        }

        // Typography
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText(wp.title.uppercase(), w * 0.5f, h * 0.88f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawText(wp.character.uppercase(), w * 0.5f, h * 0.915f, paint)
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

        // Celestial nebula in background
        val nebulaShader = RadialGradient(
            w * 0.5f, h * 0.45f, w * 0.7f,
            intArrayOf(Color.argb(120, 255, 145, 0), Color.argb(40, 0, 176, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = nebulaShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Central 4-Star Dragon Ball (large, heroic)
        val cx = w * 0.5f
        val cy = h * 0.45f
        val r = w * 0.28f

        drawSingleDragonBall(canvas, cx, cy, r, 4, paint)

        // 6 other smaller Dragon Balls orbiting in a ring
        val orbitRadius = w * 0.40f
        val otherStars = listOf(1, 2, 3, 5, 6, 7)
        for (i in 0 until 6) {
            val angle = (i * (360f / 6f) - 90f) * (PI.toFloat() / 180f)
            val bx = cx + cos(angle) * orbitRadius
            val by = cy + sin(angle) * orbitRadius
            drawSingleDragonBall(canvas, bx, by, w * 0.09f, otherStars[i], paint)
        }

        // Labels
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.052f
        paint.color = Color.WHITE
        canvas.drawText("THE SEVEN DRAGON BALLS", w * 0.5f, h * 0.78f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.rgb(255, 171, 64)
        canvas.drawText("GRANT ANY WISH • ETERNAL SHENRON", w * 0.5f, h * 0.82f, paint)
    }

    private fun drawSingleDragonBall(canvas: Canvas, x: Float, y: Float, radius: Float, stars: Int, paint: Paint) {
        // Outer amber-orange glass ball gradient
        val ballShader = RadialGradient(
            x - radius * 0.35f, y - radius * 0.35f, radius * 1.35f,
            intArrayOf(
                Color.rgb(255, 235, 150), // Highlight
                Color.rgb(255, 152, 0),   // Rich amber
                Color.rgb(230, 81, 0),    // Deep orange rim
                Color.rgb(180, 50, 0)
            ),
            floatArrayOf(0f, 0.45f, 0.85f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = ballShader
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null

        // Specular highlight gleam
        paint.color = Color.argb(160, 255, 255, 255)
        canvas.drawCircle(x - radius * 0.35f, y - radius * 0.35f, radius * 0.22f, paint)

        // Draw red 5-pointed stars
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(213, 0, 0) // Signature crimson red
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
        // Dramatic battlefield wasteland / Namek / Hyperbolic chamber
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

        // Celestial moon / energy orb
        paint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawCircle(w * 0.75f, h * 0.25f, w * 0.16f, paint)

        // Mountain silhouettes
        val rand = Random(wp.title.hashCode())
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

        // Ki lightning on horizon
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

        // Labels
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
}
