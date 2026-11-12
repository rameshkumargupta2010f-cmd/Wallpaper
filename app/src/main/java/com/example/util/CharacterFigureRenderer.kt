package com.example.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.Wallpaper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance vector canvas renderer for Dragon Ball characters.
 * Draws detailed character faces (facial structure, fierce eyes, signature hair)
 * and whole heroic bodies (muscular anatomy, martial gi/armor, power stance, explosive aura).
 */
object CharacterFigureRenderer {

    enum class CharacterArchetype {
        SAIYAN_GOKU,
        SAIYAN_VEGETA,
        SAIYAN_GOHAN,
        SAIYAN_TRUNKS,
        SAIYAN_BROLY,
        SAIYAN_FUSION,
        NAMEKIAN_PICCOLO,
        BIO_FRIEZA,
        BIO_CELL,
        MAJIN_BUU,
        DEITY_BEERUS,
        DEITY_WHIS,
        DRAGON_SHENRON,
        ANDROID_CYBORG,
        MARTIAL_HERO
    }

    fun getArchetype(characterName: String): CharacterArchetype {
        val name = characterName.lowercase()
        return when {
            name.contains("goku") || name.contains("kakarot") || name.contains("bardock") -> CharacterArchetype.SAIYAN_GOKU
            name.contains("vegeta") -> CharacterArchetype.SAIYAN_VEGETA
            name.contains("gohan") -> CharacterArchetype.SAIYAN_GOHAN
            name.contains("trunks") -> CharacterArchetype.SAIYAN_TRUNKS
            name.contains("broly") -> CharacterArchetype.SAIYAN_BROLY
            name.contains("vegito") || name.contains("gogeta") || name.contains("gotenks") -> CharacterArchetype.SAIYAN_FUSION
            name.contains("piccolo") -> CharacterArchetype.NAMEKIAN_PICCOLO
            name.contains("frieza") || name.contains("cooler") || name.contains("frost") -> CharacterArchetype.BIO_FRIEZA
            name.contains("cell") -> CharacterArchetype.BIO_CELL
            name.contains("buu") || name.contains("uub") -> CharacterArchetype.MAJIN_BUU
            name.contains("beerus") || name.contains("champa") -> CharacterArchetype.DEITY_BEERUS
            name.contains("whis") || name.contains("vados") || name.contains("grand priest") -> CharacterArchetype.DEITY_WHIS
            name.contains("shenron") || name.contains("dragon") || name.contains("porunga") -> CharacterArchetype.DRAGON_SHENRON
            name.contains("17") || name.contains("18") || name.contains("16") || name.contains("gamma") -> CharacterArchetype.ANDROID_CYBORG
            else -> CharacterArchetype.MARTIAL_HERO
        }
    }

    data class CharacterPalette(
        val hairColor: Int,
        val hairHighlight: Int,
        val eyeColor: Int,
        val skinColor: Int,
        val skinShadow: Int,
        val primaryAttire: Int,
        val secondaryAttire: Int,
        val auraCore: Int,
        val auraGlow: Int
    )

    fun resolvePalette(wp: Wallpaper, archetype: CharacterArchetype): CharacterPalette {
        val formLower = wp.form.lowercase()
        val titleLower = wp.title.lowercase()
        val p = (wp.primaryColor or 0xFF000000).toInt()
        val s = (wp.secondaryColor or 0xFF000000).toInt()
        val a = (wp.accentColor or 0xFF000000).toInt()

        val isSilver = formLower.contains("ultra instinct") || formLower.contains("beast") || formLower.contains("silver")
        val isBlue = formLower.contains("blue") || formLower.contains("ssgss")
        val isGod = formLower.contains("god") || formLower.contains("red")
        val isEgo = formLower.contains("ego") || formLower.contains("purple")
        val isRose = formLower.contains("rose") || formLower.contains("rosé")
        val isGold = formLower.contains("super saiyan") || formLower.contains("ssj") || formLower.contains("golden")
        val isGreen = formLower.contains("legendary") || formLower.contains("broly") || formLower.contains("lssj")

        val hairColor = when {
            isSilver -> Color.rgb(235, 240, 250)
            isBlue -> Color.rgb(0, 229, 255)
            isGod -> Color.rgb(255, 23, 68)
            isEgo -> Color.rgb(186, 104, 200)
            isRose -> Color.rgb(255, 128, 171)
            isGreen -> Color.rgb(118, 255, 3)
            isGold -> Color.rgb(255, 214, 0)
            archetype == CharacterArchetype.NAMEKIAN_PICCOLO -> Color.rgb(76, 175, 80)
            archetype == CharacterArchetype.BIO_FRIEZA -> Color.rgb(220, 210, 245)
            archetype == CharacterArchetype.BIO_CELL -> Color.rgb(30, 30, 30)
            archetype == CharacterArchetype.MAJIN_BUU -> Color.rgb(244, 143, 177)
            archetype == CharacterArchetype.DEITY_BEERUS -> Color.rgb(156, 110, 180)
            archetype == CharacterArchetype.DEITY_WHIS -> Color.rgb(245, 245, 255)
            archetype == CharacterArchetype.DRAGON_SHENRON -> Color.rgb(0, 200, 83)
            else -> Color.rgb(25, 25, 30)
        }

        val eyeColor = when {
            isSilver -> Color.rgb(220, 225, 235)
            isBlue -> Color.rgb(0, 176, 255)
            isGod -> Color.rgb(255, 23, 68)
            isEgo -> Color.rgb(213, 0, 249)
            isRose -> Color.rgb(170, 0, 255)
            isGreen -> Color.rgb(178, 255, 89)
            isGold -> Color.rgb(0, 230, 118)
            archetype == CharacterArchetype.BIO_FRIEZA -> Color.rgb(255, 23, 68)
            archetype == CharacterArchetype.BIO_CELL -> Color.rgb(255, 214, 0)
            archetype == CharacterArchetype.DEITY_BEERUS -> Color.rgb(255, 235, 59)
            archetype == CharacterArchetype.DRAGON_SHENRON -> Color.rgb(255, 23, 68)
            else -> Color.rgb(33, 33, 33)
        }

        val skinColor = when (archetype) {
            CharacterArchetype.NAMEKIAN_PICCOLO -> Color.rgb(102, 187, 106)
            CharacterArchetype.BIO_FRIEZA -> Color.rgb(245, 240, 255)
            CharacterArchetype.MAJIN_BUU -> Color.rgb(244, 143, 177)
            CharacterArchetype.DEITY_BEERUS -> Color.rgb(179, 136, 200)
            CharacterArchetype.DEITY_WHIS -> Color.rgb(129, 212, 250)
            CharacterArchetype.DRAGON_SHENRON -> Color.rgb(0, 200, 83)
            else -> Color.rgb(255, 218, 185)
        }

        val skinShadow = when (archetype) {
            CharacterArchetype.NAMEKIAN_PICCOLO -> Color.rgb(56, 142, 60)
            CharacterArchetype.BIO_FRIEZA -> Color.rgb(179, 157, 219)
            CharacterArchetype.MAJIN_BUU -> Color.rgb(233, 30, 99)
            CharacterArchetype.DEITY_BEERUS -> Color.rgb(123, 80, 145)
            CharacterArchetype.DEITY_WHIS -> Color.rgb(79, 195, 247)
            CharacterArchetype.DRAGON_SHENRON -> Color.rgb(0, 121, 107)
            else -> Color.rgb(224, 168, 130)
        }

        val primaryAttire = when (archetype) {
            CharacterArchetype.SAIYAN_VEGETA -> Color.rgb(21, 101, 192)
            CharacterArchetype.NAMEKIAN_PICCOLO -> Color.rgb(106, 27, 154)
            CharacterArchetype.SAIYAN_TRUNKS -> Color.rgb(40, 53, 147)
            CharacterArchetype.SAIYAN_FUSION -> if (titleLower.contains("gogeta")) Color.rgb(20, 20, 25) else Color.rgb(25, 118, 210)
            CharacterArchetype.BIO_FRIEZA -> Color.rgb(240, 240, 245)
            CharacterArchetype.BIO_CELL -> Color.rgb(46, 125, 50)
            CharacterArchetype.MAJIN_BUU -> Color.rgb(245, 245, 245)
            CharacterArchetype.DEITY_BEERUS -> Color.rgb(21, 21, 21)
            CharacterArchetype.DEITY_WHIS -> Color.rgb(183, 28, 28)
            else -> Color.rgb(245, 124, 0) // Classic Goku orange
        }

        val secondaryAttire = when (archetype) {
            CharacterArchetype.SAIYAN_VEGETA -> Color.rgb(240, 240, 245) // White armor
            CharacterArchetype.NAMEKIAN_PICCOLO -> Color.rgb(245, 245, 250) // White cape
            CharacterArchetype.SAIYAN_TRUNKS -> Color.rgb(255, 214, 0) // Sword strap / yellow boots
            CharacterArchetype.SAIYAN_FUSION -> Color.rgb(255, 145, 0) // Orange puffy neck
            CharacterArchetype.BIO_FRIEZA -> Color.rgb(142, 36, 170) // Purple plates
            CharacterArchetype.BIO_CELL -> Color.rgb(20, 20, 20) // Black wings
            CharacterArchetype.MAJIN_BUU -> Color.rgb(255, 214, 0) // Gold M belt
            CharacterArchetype.DEITY_BEERUS -> Color.rgb(0, 188, 212) // Cyan collar
            CharacterArchetype.DEITY_WHIS -> Color.rgb(21, 21, 21)
            else -> Color.rgb(25, 118, 210) // Blue undershirt & belt
        }

        return CharacterPalette(
            hairColor = hairColor,
            hairHighlight = Color.argb(160, 255, 255, 255),
            eyeColor = eyeColor,
            skinColor = skinColor,
            skinShadow = skinShadow,
            primaryAttire = primaryAttire,
            secondaryAttire = secondaryAttire,
            auraCore = if (isSilver) Color.rgb(240, 245, 255) else p,
            auraGlow = if (isBlue) Color.rgb(0, 229, 255) else s
        )
    }

    /**
     * Masterpiece Composition: Upper Face Close-up + Lower Whole Body Heroic Stance!
     */
    fun drawDualFaceAndBodyComposition(
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
        val archetype = getArchetype(wp.character)
        val palette = resolvePalette(wp, archetype)

        // 1. Deep dynamic background with cosmic ki nebula
        val bgShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(bgDark, Color.argb(120, Color.red(primary), Color.green(primary), Color.blue(primary)), bgDark),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Dramatic speedlines radiating from center rift
        val rand = Random(wp.id.hashCode())
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        for (i in 0..30) {
            val angle = rand.nextFloat() * 2 * PI.toFloat()
            val startR = w * 0.15f
            val endR = w * 0.9f
            val x1 = w * 0.5f + cos(angle) * startR
            val y1 = h * 0.46f + sin(angle) * startR
            val x2 = w * 0.5f + cos(angle) * endR
            val y2 = h * 0.46f + sin(angle) * endR
            val alpha = 20 + rand.nextInt(50)
            paint.color = Color.argb(alpha, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // Floating energy sparks in space
        paint.style = Paint.Style.FILL
        for (i in 0..40) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val rad = 1f + rand.nextFloat() * 3f
            paint.color = Color.argb(40 + rand.nextInt(120), Color.red(accent), Color.green(accent), Color.blue(accent))
            canvas.drawCircle(px, py, rad, paint)
        }

        // 2. UPPER HALF: Epic Close-up of Character Face (with glowing eyes and signature hair)
        val faceCx = w * 0.5f
        val faceCy = h * 0.22f
        val faceR = w * 0.24f

        // Soft backglow behind head
        val headGlowShader = RadialGradient(
            faceCx, faceCy, faceR * 1.8f,
            intArrayOf(Color.argb(110, Color.red(palette.auraCore), Color.green(palette.auraCore), Color.blue(palette.auraCore)), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = headGlowShader
        canvas.drawCircle(faceCx, faceCy, faceR * 1.8f, paint)
        paint.shader = null

        drawCharacterFace(canvas, archetype, wp, faceCx, faceCy, faceR, palette, paint, isCloseUp = true)

        // 3. MIDDLE DIVIDER: Radiant Energy Rift & Kanji Crest
        drawEnergyRiftDivider(canvas, w, h, wp, palette, primary, accent, paint)

        // 4. LOWER HALF: Full Body Heroic Combat Stance (Muscular anatomy, attire, boots, explosive aura)
        val bodyCx = w * 0.5f
        val bodyCy = h * 0.73f
        val bodyH = h * 0.42f

        drawCharacterWholeBody(canvas, archetype, wp, bodyCx, bodyCy, bodyH, palette, paint)

        // 5. Heroic Character Banner Footer
        drawBannerFooter(canvas, w, h, wp, accent, paint)
    }

    /**
     * Heroic Full Body Focus Wallpaper
     */
    fun drawFullBodyHeroicComposition(
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
        val archetype = getArchetype(wp.character)
        val palette = resolvePalette(wp, archetype)

        // Aura burst background
        val bgShader = RadialGradient(
            w * 0.5f, h * 0.55f, h * 0.65f,
            intArrayOf(Color.argb(140, Color.red(primary), Color.green(primary), Color.blue(primary)), bgDark),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // High vertical lightning forks
        val rand = Random(wp.id.hashCode() + 99)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        for (i in 0..4) {
            paint.color = Color.argb(180, 255, 255, 255)
            drawLightningBolt(canvas, (0.15f + rand.nextFloat() * 0.7f) * w, 0f, (0.2f + rand.nextFloat() * 0.6f) * w, h * 0.9f, rand, paint)
        }

        // Concentrated Ki Sphere in background
        val sphereShader = RadialGradient(
            w * 0.5f, h * 0.35f, w * 0.32f,
            intArrayOf(Color.WHITE, palette.auraCore, palette.auraGlow, Color.TRANSPARENT),
            floatArrayOf(0f, 0.25f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = sphereShader
        canvas.drawCircle(w * 0.5f, h * 0.35f, w * 0.32f, paint)
        paint.shader = null

        // Large full body figure in dynamic power stance
        drawCharacterWholeBody(canvas, archetype, wp, w * 0.5f, h * 0.58f, h * 0.62f, palette, paint)

        // Banner footer
        drawBannerFooter(canvas, w, h, wp, accent, paint)
    }

    /**
     * Fierce Face Close-Up Focus Wallpaper
     */
    fun drawCinematicFaceComposition(
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
        val archetype = getArchetype(wp.character)
        val palette = resolvePalette(wp, archetype)

        // Dark moody background with dramatic side glow
        paint.style = Paint.Style.FILL
        val bgShader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(bgDark, Color.argb(80, Color.red(primary), Color.green(primary), Color.blue(primary)), bgDark),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Large macro face rendering
        val faceCx = w * 0.5f
        val faceCy = h * 0.45f
        val faceR = w * 0.40f

        drawCharacterFace(canvas, archetype, wp, faceCx, faceCy, faceR, palette, paint, isCloseUp = true)

        // Ki sparks jumping across hair
        val rand = Random(wp.id.hashCode() + 42)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.WHITE
        for (i in 0..6) {
            val sx = faceCx + (rand.nextFloat() - 0.5f) * faceR * 1.8f
            val sy = faceCy - rand.nextFloat() * faceR * 1.2f
            drawLightningBolt(canvas, sx, sy, sx + (rand.nextFloat() - 0.5f) * 60f, sy + 60f, rand, paint)
        }

        // Subtitle badge
        drawBannerFooter(canvas, w, h, wp, accent, paint)
    }

    // =========================================================================
    // CHARACTER FACE RENDERING ENGINE
    // =========================================================================

    fun drawCharacterFace(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        r: Float,
        palette: CharacterPalette,
        paint: Paint,
        isCloseUp: Boolean
    ) {
        // 1. Neck and Shoulders base
        paint.style = Paint.Style.FILL
        paint.color = palette.skinShadow
        val neckPath = Path().apply {
            moveTo(cx - r * 0.35f, cy + r * 0.5f)
            lineTo(cx - r * 0.55f, cy + r * 1.3f)
            lineTo(cx + r * 0.55f, cy + r * 1.3f)
            lineTo(cx + r * 0.35f, cy + r * 0.5f)
            close()
        }
        canvas.drawPath(neckPath, paint)

        // Trapezius and collarbone lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.03f
        paint.color = Color.argb(120, 0, 0, 0)
        canvas.drawLine(cx - r * 0.25f, cy + r * 0.8f, cx - r * 0.45f, cy + r * 1.2f, paint)
        canvas.drawLine(cx + r * 0.25f, cy + r * 0.8f, cx + r * 0.45f, cy + r * 1.2f, paint)

        // 2. Anime Jawline & Head Structure
        paint.style = Paint.Style.FILL
        paint.color = palette.skinColor
        val jawPath = Path().apply {
            moveTo(cx - r * 0.65f, cy - r * 0.1f) // Left cheekbone
            lineTo(cx - r * 0.50f, cy + r * 0.55f) // Left jaw
            lineTo(cx, cy + r * 0.85f)             // Chin tip
            lineTo(cx + r * 0.50f, cy + r * 0.55f) // Right jaw
            lineTo(cx + r * 0.65f, cy - r * 0.1f) // Right cheekbone
            // Top skull dome curve
            quadTo(cx, cy - r * 0.85f, cx - r * 0.65f, cy - r * 0.1f)
            close()
        }
        canvas.drawPath(jawPath, paint)

        // Chin & jaw shadow for anime depth
        paint.style = Paint.Style.FILL
        paint.color = palette.skinShadow
        val jawShadow = Path().apply {
            moveTo(cx - r * 0.45f, cy + r * 0.58f)
            lineTo(cx, cy + r * 0.85f)
            lineTo(cx + r * 0.45f, cy + r * 0.58f)
            lineTo(cx, cy + r * 0.72f)
            close()
        }
        canvas.drawPath(jawShadow, paint)

        // 3. Ears & Species Adornments
        drawEars(canvas, archetype, cx, cy, r, palette, paint)

        // 4. Intense DBZ Eyes & Eyebrows
        drawEyesAndBrows(canvas, wp, archetype, cx, cy, r, palette, paint)

        // 5. Anime Nose & Mouth (Determined battle smirk / gritted teeth)
        drawNoseAndMouth(canvas, cx, cy, r, paint)

        // 6. Facial Battle Scuffs / Markings
        drawBattleMarkings(canvas, wp, archetype, cx, cy, r, paint)

        // 7. Signature Hairstyle & Head Crown
        drawHairstyle(canvas, archetype, wp, cx, cy, r, palette, paint)

        // 8. Capsule Corp Scouter HUD Overlay (if Saiyan / Scouter saga)
        if (wp.tags.any { it.lowercase().contains("scouter") } || wp.form.lowercase().contains("scouter")) {
            drawScouterOverlay(canvas, cx, cy, r, paint)
        }
    }

    private fun drawEars(
        canvas: Canvas,
        archetype: CharacterArchetype,
        cx: Float,
        cy: Float,
        r: Float,
        palette: CharacterPalette,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        if (archetype == CharacterArchetype.NAMEKIAN_PICCOLO) {
            // Elongated pointed Namekian ears
            paint.color = palette.skinColor
            val leftEar = Path().apply {
                moveTo(cx - r * 0.62f, cy + r * 0.05f)
                lineTo(cx - r * 1.05f, cy - r * 0.15f) // Sharp point
                lineTo(cx - r * 0.58f, cy + r * 0.35f)
                close()
            }
            canvas.drawPath(leftEar, paint)

            val rightEar = Path().apply {
                moveTo(cx + r * 0.62f, cy + r * 0.05f)
                lineTo(cx + r * 1.05f, cy - r * 0.15f)
                lineTo(cx + r * 0.58f, cy + r * 0.35f)
                close()
            }
            canvas.drawPath(rightEar, paint)

            // Inner pink ear lining
            paint.color = Color.rgb(220, 100, 140)
            canvas.drawCircle(cx - r * 0.75f, cy + r * 0.05f, r * 0.08f, paint)
            canvas.drawCircle(cx + r * 0.75f, cy + r * 0.05f, r * 0.08f, paint)
        } else if (archetype == CharacterArchetype.DEITY_BEERUS) {
            // Tall Egyptian feline ears
            paint.color = palette.skinColor
            val leftEar = Path().apply {
                moveTo(cx - r * 0.45f, cy - r * 0.5f)
                lineTo(cx - r * 0.75f, cy - r * 1.4f) // Tall ear tip
                lineTo(cx - r * 0.15f, cy - r * 0.75f)
                close()
            }
            canvas.drawPath(leftEar, paint)

            val rightEar = Path().apply {
                moveTo(cx + r * 0.45f, cy - r * 0.5f)
                lineTo(cx + r * 0.75f, cy - r * 1.4f)
                lineTo(cx + r * 0.15f, cy - r * 0.75f)
                close()
            }
            canvas.drawPath(rightEar, paint)
        } else {
            // Classic Saiyan / Human rounded anime ears
            paint.color = palette.skinColor
            canvas.drawOval(RectF(cx - r * 0.78f, cy - r * 0.05f, cx - r * 0.58f, cy + r * 0.38f), paint)
            canvas.drawOval(RectF(cx + r * 0.58f, cy - r * 0.05f, cx + r * 0.78f, cy + r * 0.38f), paint)

            // Inner ear contour lines
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = r * 0.025f
            paint.color = palette.skinShadow
            canvas.drawArc(RectF(cx - r * 0.74f, cy + r * 0.02f, cx - r * 0.62f, cy + r * 0.30f), 0f, 360f, false, paint)
            canvas.drawArc(RectF(cx + r * 0.62f, cy + r * 0.02f, cx + r * 0.74f, cy + r * 0.30f), 0f, 360f, false, paint)

            // Golden Potara earring for Vegito
            if (archetype == CharacterArchetype.SAIYAN_FUSION && (palette.primaryAttire == Color.rgb(25, 118, 210))) {
                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(255, 235, 59)
                canvas.drawCircle(cx - r * 0.68f, cy + r * 0.42f, r * 0.07f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(cx - r * 0.70f, cy + r * 0.40f, r * 0.025f, paint)
            }
        }
    }

    private fun drawEyesAndBrows(
        canvas: Canvas,
        wp: Wallpaper,
        archetype: CharacterArchetype,
        cx: Float,
        cy: Float,
        r: Float,
        palette: CharacterPalette,
        paint: Paint
    ) {
        val browY = cy - r * 0.12f
        val browColor = if (wp.form.lowercase().contains("ultra ego")) palette.skinShadow else palette.hairColor

        // 1. Angular Furrowed Eyebrows
        paint.style = Paint.Style.FILL
        paint.color = browColor

        // Left Eyebrow: angled sharply down to center
        val leftBrow = Path().apply {
            moveTo(cx - r * 0.58f, browY - r * 0.12f) // Outer high corner
            lineTo(cx - r * 0.12f, browY + r * 0.02f) // Inner low bridge
            lineTo(cx - r * 0.12f, browY + r * 0.12f)
            lineTo(cx - r * 0.55f, browY)
            close()
        }
        canvas.drawPath(leftBrow, paint)

        // Right Eyebrow: mirrored
        val rightBrow = Path().apply {
            moveTo(cx + r * 0.58f, browY - r * 0.12f)
            lineTo(cx + r * 0.12f, browY + r * 0.02f)
            lineTo(cx + r * 0.12f, browY + r * 0.12f)
            lineTo(cx + r * 0.55f, browY)
            close()
        }
        canvas.drawPath(rightBrow, paint)

        // 2. Fierce Eyes (Rectangular / Trapezoid Toriyama design)
        val eyeTopY = browY + r * 0.06f
        val eyeBottomY = browY + r * 0.28f

        // Sclera (White eye background)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        val leftEyePath = Path().apply {
            moveTo(cx - r * 0.52f, eyeTopY)
            lineTo(cx - r * 0.16f, eyeTopY + r * 0.04f)
            lineTo(cx - r * 0.18f, eyeBottomY)
            lineTo(cx - r * 0.48f, eyeBottomY - r * 0.04f)
            close()
        }
        canvas.drawPath(leftEyePath, paint)

        val rightEyePath = Path().apply {
            moveTo(cx + r * 0.52f, eyeTopY)
            lineTo(cx + r * 0.16f, eyeTopY + r * 0.04f)
            lineTo(cx + r * 0.18f, eyeBottomY)
            lineTo(cx + r * 0.48f, eyeBottomY - r * 0.04f)
            close()
        }
        canvas.drawPath(rightEyePath, paint)

        // Glowing Irises
        paint.color = palette.eyeColor
        val leftIrisCx = cx - r * 0.32f
        val rightIrisCx = cx + r * 0.32f
        val irisCy = eyeTopY + (eyeBottomY - eyeTopY) * 0.5f
        val irisRx = r * 0.075f
        val irisRy = r * 0.095f

        canvas.drawOval(RectF(leftIrisCx - irisRx, irisCy - irisRy, leftIrisCx + irisRx, irisCy + irisRy), paint)
        canvas.drawOval(RectF(rightIrisCx - irisRx, irisCy - irisRy, rightIrisCx + irisRx, irisCy + irisRy), paint)

        // Dark Pupils
        paint.color = Color.BLACK
        canvas.drawCircle(leftIrisCx, irisCy, r * 0.04f, paint)
        canvas.drawCircle(rightIrisCx, irisCy, r * 0.04f, paint)

        // Specular White Highlights (Life gleam in eyes)
        paint.color = Color.WHITE
        canvas.drawCircle(leftIrisCx - r * 0.02f, irisCy - r * 0.03f, r * 0.02f, paint)
        canvas.drawCircle(rightIrisCx - r * 0.02f, irisCy - r * 0.03f, r * 0.02f, paint)

        // Bold Black Eyeliner Outlines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.035f
        paint.color = Color.BLACK
        canvas.drawLine(cx - r * 0.54f, eyeTopY, cx - r * 0.14f, eyeTopY + r * 0.04f, paint)
        canvas.drawLine(cx + r * 0.54f, eyeTopY, cx + r * 0.14f, eyeTopY + r * 0.04f, paint)
        canvas.drawLine(cx - r * 0.48f, eyeBottomY - r * 0.04f, cx - r * 0.18f, eyeBottomY, paint)
        canvas.drawLine(cx + r * 0.48f, eyeBottomY - r * 0.04f, cx + r * 0.18f, eyeBottomY, paint)

        // Intense Under-eye battle lines
        paint.strokeWidth = r * 0.02f
        paint.color = Color.argb(180, 50, 40, 40)
        canvas.drawLine(cx - r * 0.45f, eyeBottomY + r * 0.06f, cx - r * 0.30f, eyeBottomY + r * 0.08f, paint)
        canvas.drawLine(cx + r * 0.45f, eyeBottomY + r * 0.06f, cx + r * 0.30f, eyeBottomY + r * 0.08f, paint)

        // Forehead furrow wrinkle between brows
        canvas.drawLine(cx - r * 0.04f, browY - r * 0.05f, cx - r * 0.04f, browY + r * 0.10f, paint)
        canvas.drawLine(cx + r * 0.04f, browY - r * 0.05f, cx + r * 0.04f, browY + r * 0.10f, paint)
    }

    private fun drawNoseAndMouth(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        // Anime Nose (angled sharp shadow)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(160, 80, 50, 40)
        val nosePath = Path().apply {
            moveTo(cx, cy + r * 0.18f)
            lineTo(cx - r * 0.06f, cy + r * 0.34f)
            lineTo(cx, cy + r * 0.34f)
            close()
        }
        canvas.drawPath(nosePath, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.025f
        paint.color = Color.BLACK
        canvas.drawLine(cx, cy + r * 0.18f, cx - r * 0.06f, cy + r * 0.34f, paint)
        canvas.drawLine(cx - r * 0.06f, cy + r * 0.34f, cx + r * 0.04f, cy + r * 0.34f, paint)

        // Determined Battle Smirk / Gritted Teeth Mouth
        val mouthY = cy + r * 0.54f
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(cx - r * 0.22f, mouthY - r * 0.04f, cx + r * 0.22f, mouthY + r * 0.04f), 4f, 4f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.03f
        paint.color = Color.BLACK
        canvas.drawLine(cx - r * 0.24f, mouthY, cx + r * 0.24f, mouthY, paint)

        // Teeth vertical notches
        paint.strokeWidth = r * 0.015f
        canvas.drawLine(cx - r * 0.08f, mouthY - r * 0.03f, cx - r * 0.08f, mouthY + r * 0.03f, paint)
        canvas.drawLine(cx, mouthY - r * 0.03f, cx, mouthY + r * 0.03f, paint)
        canvas.drawLine(cx + r * 0.08f, mouthY - r * 0.03f, cx + r * 0.08f, mouthY + r * 0.03f, paint)

        // Lower lip shadow accent
        paint.strokeWidth = r * 0.025f
        paint.color = Color.argb(180, 100, 60, 50)
        canvas.drawLine(cx - r * 0.08f, mouthY + r * 0.08f, cx + r * 0.08f, mouthY + r * 0.08f, paint)
    }

    private fun drawBattleMarkings(
        canvas: Canvas,
        wp: Wallpaper,
        archetype: CharacterArchetype,
        cx: Float,
        cy: Float,
        r: Float,
        paint: Paint
    ) {
        val titleLower = wp.title.lowercase()
        val charLower = wp.character.lowercase()

        // Majin "M" emblem on forehead for Majin Vegeta
        if (titleLower.contains("majin") || charLower.contains("majin")) {
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            val mPath = Path().apply {
                val mY = cy - r * 0.38f
                moveTo(cx - r * 0.22f, mY)
                lineTo(cx - r * 0.15f, mY - r * 0.18f)
                lineTo(cx, mY - r * 0.05f)
                lineTo(cx + r * 0.15f, mY - r * 0.18f)
                lineTo(cx + r * 0.22f, mY)
                lineTo(cx + r * 0.16f, mY)
                lineTo(cx + r * 0.11f, mY - r * 0.10f)
                lineTo(cx, mY)
                lineTo(cx - r * 0.11f, mY - r * 0.10f)
                lineTo(cx - r * 0.16f, mY)
                close()
            }
            canvas.drawPath(mPath, paint)
        }

        // Bardock's Cross Cheek Scar
        if (charLower.contains("bardock")) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = r * 0.035f
            paint.color = Color.rgb(183, 28, 28)
            val scarX = cx - r * 0.40f
            val scarY = cy + r * 0.38f
            canvas.drawLine(scarX - r * 0.12f, scarY - r * 0.12f, scarX + r * 0.12f, scarY + r * 0.12f, paint)
            canvas.drawLine(scarX - r * 0.10f, scarY + r * 0.10f, scarX + r * 0.10f, scarY - r * 0.10f, paint)
        }
    }

    private fun drawHairstyle(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        r: Float,
        palette: CharacterPalette,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = palette.hairColor

        val hairPath = Path()

        when (archetype) {
            CharacterArchetype.SAIYAN_VEGETA -> {
                // Iconic upright flame hair with widow's peak
                hairPath.moveTo(cx, cy - r * 0.35f) // Center widow's peak
                hairPath.lineTo(cx - r * 0.30f, cy - r * 0.50f)
                hairPath.lineTo(cx - r * 0.65f, cy - r * 0.30f)
                hairPath.lineTo(cx - r * 0.85f, cy - r * 0.85f) // Far left flame spike
                hairPath.lineTo(cx - r * 0.55f, cy - r * 0.95f)
                hairPath.lineTo(cx - r * 0.45f, cy - r * 1.50f) // Tall left main spike
                hairPath.lineTo(cx - r * 0.15f, cy - r * 1.25f)
                hairPath.lineTo(cx, cy - r * 1.65f)            // Supreme central flame point
                hairPath.lineTo(cx + r * 0.15f, cy - r * 1.25f)
                hairPath.lineTo(cx + r * 0.45f, cy - r * 1.50f) // Tall right main spike
                hairPath.lineTo(cx + r * 0.55f, cy - r * 0.95f)
                hairPath.lineTo(cx + r * 0.85f, cy - r * 0.85f) // Far right flame spike
                hairPath.lineTo(cx + r * 0.65f, cy - r * 0.30f)
                hairPath.lineTo(cx + r * 0.30f, cy - r * 0.50f)
                hairPath.close()
                canvas.drawPath(hairPath, paint)
            }

            CharacterArchetype.SAIYAN_GOHAN -> {
                // Teen SSJ2 / Beast Gohan: Sharp angled spikes with single dramatic front bang falling to eye
                hairPath.moveTo(cx - r * 0.55f, cy - r * 0.30f)
                hairPath.lineTo(cx - r * 0.90f, cy - r * 0.90f)
                hairPath.lineTo(cx - r * 0.60f, cy - r * 1.10f)
                hairPath.lineTo(cx - r * 0.35f, cy - r * 1.65f) // Massive vertical Beast/SSJ2 spike
                hairPath.lineTo(cx + r * 0.15f, cy - r * 1.20f)
                hairPath.lineTo(cx + r * 0.55f, cy - r * 1.45f)
                hairPath.lineTo(cx + r * 0.80f, cy - r * 0.70f)
                hairPath.lineTo(cx + r * 0.55f, cy - r * 0.30f)
                hairPath.close()
                canvas.drawPath(hairPath, paint)

                // Dramatic long falling front bang
                val bang = Path().apply {
                    moveTo(cx - r * 0.15f, cy - r * 0.45f)
                    lineTo(cx - r * 0.32f, cy + r * 0.15f) // Tip hanging down past brow
                    lineTo(cx - r * 0.05f, cy - r * 0.20f)
                    close()
                }
                canvas.drawPath(bang, paint)
            }

            CharacterArchetype.SAIYAN_TRUNKS -> {
                // Center-parted curtain bangs framing temples
                hairPath.moveTo(cx, cy - r * 0.45f)
                hairPath.lineTo(cx - r * 0.40f, cy - r * 0.05f) // Left curtain bang
                hairPath.lineTo(cx - r * 0.65f, cy - r * 0.25f)
                hairPath.lineTo(cx - r * 0.80f, cy - r * 0.75f)
                hairPath.lineTo(cx, cy - r * 0.95f)
                hairPath.lineTo(cx + r * 0.80f, cy - r * 0.75f)
                hairPath.lineTo(cx + r * 0.65f, cy - r * 0.25f)
                hairPath.lineTo(cx + r * 0.40f, cy - r * 0.05f) // Right curtain bang
                hairPath.close()
                canvas.drawPath(hairPath, paint)
            }

            CharacterArchetype.BIO_FRIEZA -> {
                // Sleek bio-helmet with purple oval jewel on forehead
                paint.color = Color.rgb(240, 240, 250)
                canvas.drawCircle(cx, cy - r * 0.45f, r * 0.55f, paint)

                // Central Forehead Gem
                paint.color = Color.rgb(142, 36, 170)
                canvas.drawOval(RectF(cx - r * 0.22f, cy - r * 0.65f, cx + r * 0.22f, cy - r * 0.30f), paint)
                paint.color = Color.WHITE
                canvas.drawCircle(cx - r * 0.08f, cy - r * 0.52f, r * 0.04f, paint)
            }

            CharacterArchetype.BIO_CELL -> {
                // Crown horns spreading outward
                paint.color = Color.rgb(20, 20, 20)
                val leftHorn = Path().apply {
                    moveTo(cx - r * 0.35f, cy - r * 0.40f)
                    lineTo(cx - r * 1.15f, cy - r * 1.25f) // Far outer horn tip
                    lineTo(cx - r * 0.50f, cy - r * 0.80f)
                    close()
                }
                canvas.drawPath(leftHorn, paint)

                val rightHorn = Path().apply {
                    moveTo(cx + r * 0.35f, cy - r * 0.40f)
                    lineTo(cx + r * 1.15f, cy - r * 1.25f)
                    lineTo(cx + r * 0.50f, cy - r * 0.80f)
                    close()
                }
                canvas.drawPath(rightHorn, paint)

                // Purple triangular cheek plates
                paint.color = Color.rgb(123, 31, 162)
                val leftCheek = Path().apply {
                    moveTo(cx - r * 0.62f, cy + r * 0.10f)
                    lineTo(cx - r * 0.50f, cy + r * 0.55f)
                    lineTo(cx - r * 0.35f, cy + r * 0.35f)
                    close()
                }
                canvas.drawPath(leftCheek, paint)
                val rightCheek = Path().apply {
                    moveTo(cx + r * 0.62f, cy + r * 0.10f)
                    lineTo(cx + r * 0.50f, cy + r * 0.55f)
                    lineTo(cx + r * 0.35f, cy + r * 0.35f)
                    close()
                }
                canvas.drawPath(rightCheek, paint)
            }

            CharacterArchetype.MAJIN_BUU -> {
                // Smooth pink head with head tentacle
                paint.color = palette.skinColor
                canvas.drawCircle(cx, cy - r * 0.35f, r * 0.50f, paint)

                val tentacle = Path().apply {
                    moveTo(cx - r * 0.15f, cy - r * 0.75f)
                    quadTo(cx + r * 0.35f, cy - r * 1.35f, cx + r * 0.55f, cy - r * 1.05f)
                    quadTo(cx + r * 0.15f, cy - r * 0.85f, cx + r * 0.15f, cy - r * 0.75f)
                    close()
                }
                canvas.drawPath(tentacle, paint)
            }

            CharacterArchetype.DRAGON_SHENRON -> {
                // Antler horns & whiskers
                paint.color = Color.rgb(180, 100, 30) // Horn brown
                val leftHorn = Path().apply {
                    moveTo(cx - r * 0.45f, cy - r * 0.40f)
                    lineTo(cx - r * 1.05f, cy - r * 1.30f)
                    lineTo(cx - r * 0.70f, cy - r * 0.75f)
                    close()
                }
                canvas.drawPath(leftHorn, paint)
                val rightHorn = Path().apply {
                    moveTo(cx + r * 0.45f, cy - r * 0.40f)
                    lineTo(cx + r * 1.05f, cy - r * 1.30f)
                    lineTo(cx + r * 0.70f, cy - r * 0.75f)
                    close()
                }
                canvas.drawPath(rightHorn, paint)

                // Whiskers
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = r * 0.04f
                paint.color = Color.rgb(255, 235, 59)
                val leftWhisker = Path().apply {
                    moveTo(cx - r * 0.20f, cy + r * 0.30f)
                    cubicTo(cx - r * 0.70f, cy + r * 0.50f, cx - r * 1.10f, cy + r * 0.80f, cx - r * 1.30f, cy + r * 1.20f)
                }
                canvas.drawPath(leftWhisker, paint)
                val rightWhisker = Path().apply {
                    moveTo(cx + r * 0.20f, cy + r * 0.30f)
                    cubicTo(cx + r * 0.70f, cy + r * 0.50f, cx + r * 1.10f, cy + r * 0.80f, cx + r * 1.30f, cy + r * 1.20f)
                }
                canvas.drawPath(rightWhisker, paint)
            }

            else -> {
                // Classic Goku / Saiyan / Fusion Master Spikes!
                // 5 iconic giant spikes curving outwards + 3 forehead bangs
                hairPath.moveTo(cx - r * 0.45f, cy - r * 0.30f)
                // Spike 1 (lower left wide tuft)
                hairPath.lineTo(cx - r * 1.15f, cy - r * 0.25f)
                hairPath.lineTo(cx - r * 0.75f, cy - r * 0.55f)
                // Spike 2 (middle left dramatic flare)
                hairPath.lineTo(cx - r * 1.30f, cy - r * 0.85f)
                hairPath.lineTo(cx - r * 0.70f, cy - r * 0.95f)
                // Spike 3 (top left towering spike)
                hairPath.lineTo(cx - r * 0.95f, cy - r * 1.45f)
                hairPath.lineTo(cx - r * 0.40f, cy - r * 1.25f)
                // Spike 4 (central crown spike)
                hairPath.lineTo(cx - r * 0.10f, cy - r * 1.65f)
                hairPath.lineTo(cx + r * 0.25f, cy - r * 1.25f)
                // Spike 5 (top right spike)
                hairPath.lineTo(cx + r * 0.80f, cy - r * 1.35f)
                hairPath.lineTo(cx + r * 0.65f, cy - r * 0.85f)
                // Spike 6 (lower right wide spike)
                hairPath.lineTo(cx + r * 1.15f, cy - r * 0.50f)
                hairPath.lineTo(cx + r * 0.55f, cy - r * 0.25f)
                hairPath.close()
                canvas.drawPath(hairPath, paint)

                // 3 Signature forehead bangs hanging between brows
                val bang1 = Path().apply {
                    moveTo(cx - r * 0.45f, cy - r * 0.40f)
                    lineTo(cx - r * 0.35f, cy - r * 0.05f)
                    lineTo(cx - r * 0.20f, cy - r * 0.30f)
                    close()
                }
                canvas.drawPath(bang1, paint)

                val bang2 = Path().apply {
                    moveTo(cx - r * 0.25f, cy - r * 0.40f)
                    lineTo(cx - r * 0.05f, cy + r * 0.02f)
                    lineTo(cx + r * 0.08f, cy - r * 0.30f)
                    close()
                }
                canvas.drawPath(bang2, paint)

                val bang3 = Path().apply {
                    moveTo(cx + r * 0.12f, cy - r * 0.35f)
                    lineTo(cx + r * 0.28f, cy - r * 0.08f)
                    lineTo(cx + r * 0.42f, cy - r * 0.30f)
                    close()
                }
                canvas.drawPath(bang3, paint)
            }
        }
    }

    private fun drawScouterOverlay(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        val scouterX = cx - r * 0.32f
        val scouterY = cy + r * 0.05f

        // Translucent green HUD lens
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(130, 0, 230, 118)
        canvas.drawRoundRect(RectF(scouterX - r * 0.35f, scouterY - r * 0.25f, scouterX + r * 0.35f, scouterY + r * 0.25f), 12f, 12f, paint)

        // Frame
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.04f
        paint.color = Color.rgb(238, 255, 65)
        canvas.drawRoundRect(RectF(scouterX - r * 0.35f, scouterY - r * 0.25f, scouterX + r * 0.35f, scouterY + r * 0.25f), 12f, 12f, paint)

        // Headband clip going to ear
        paint.strokeWidth = r * 0.05f
        paint.color = Color.rgb(180, 190, 205)
        canvas.drawLine(scouterX - r * 0.35f, scouterY, cx - r * 0.72f, scouterY, paint)

        // Digital HUD reticle lines
        paint.strokeWidth = 2f
        paint.color = Color.rgb(255, 23, 68)
        canvas.drawCircle(scouterX, scouterY, r * 0.12f, paint)
        canvas.drawLine(scouterX - r * 0.18f, scouterY, scouterX + r * 0.18f, scouterY, paint)
        canvas.drawLine(scouterX, scouterY - r * 0.18f, scouterX, scouterY + r * 0.18f, paint)
    }

    // =========================================================================
    // WHOLE BODY HEROIC COMBAT STANCE ENGINE
    // =========================================================================

    fun drawCharacterWholeBody(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        bodyH: Float,
        palette: CharacterPalette,
        paint: Paint
    ) {
        val headH = bodyH * 0.18f
        val torsoH = bodyH * 0.32f
        val pelvisH = bodyH * 0.10f
        val legsH = bodyH * 0.40f

        val topY = cy - bodyH * 0.5f
        val headCy = topY + headH * 0.6f
        val shouldersY = topY + headH
        val waistY = shouldersY + torsoH
        val pelvisY = waistY + pelvisH
        val feetY = pelvisY + legsH

        // 1. Explosive Ki Aura around whole body
        val auraRadius = bodyH * 0.55f
        val auraShader = RadialGradient(
            cx, cy - bodyH * 0.05f, auraRadius,
            intArrayOf(Color.argb(160, Color.red(palette.auraCore), Color.green(palette.auraCore), Color.blue(palette.auraCore)), Color.argb(80, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow)), Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = auraShader
        canvas.drawCircle(cx, cy - bodyH * 0.05f, auraRadius, paint)
        paint.shader = null

        // Crackling lightning bolts around stance
        val rand = Random(wp.id.hashCode() + 17)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.WHITE
        for (i in 0..4) {
            val lx1 = cx + (rand.nextFloat() - 0.5f) * bodyH * 0.5f
            val ly1 = cy + (rand.nextFloat() - 0.5f) * bodyH * 0.6f
            drawLightningBolt(canvas, lx1, ly1, lx1 + (rand.nextFloat() - 0.5f) * 80f, ly1 + 90f, rand, paint)
        }

        // Ground Impact Crater and Dust Clouds
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.argb(180, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow))
        canvas.drawOval(RectF(cx - bodyH * 0.38f, feetY - 20f, cx + bodyH * 0.38f, feetY + 20f), paint)

        // 2. Muscular Legs in Wide Grounded Power Stance
        val leftFootX = cx - bodyH * 0.20f
        val rightFootX = cx + bodyH * 0.20f

        paint.style = Paint.Style.FILL
        paint.color = palette.primaryAttire

        // Left Leg
        val leftLeg = Path().apply {
            moveTo(cx - bodyH * 0.08f, waistY + pelvisH * 0.5f)
            lineTo(leftFootX - bodyH * 0.06f, feetY - bodyH * 0.08f) // Knee/Shin
            lineTo(leftFootX + bodyH * 0.04f, feetY - bodyH * 0.08f)
            lineTo(cx, pelvisY)
            close()
        }
        canvas.drawPath(leftLeg, paint)

        // Right Leg
        val rightLeg = Path().apply {
            moveTo(cx + bodyH * 0.08f, waistY + pelvisH * 0.5f)
            lineTo(rightFootX + bodyH * 0.06f, feetY - bodyH * 0.08f)
            lineTo(rightFootX - bodyH * 0.04f, feetY - bodyH * 0.08f)
            lineTo(cx, pelvisY)
            close()
        }
        canvas.drawPath(rightLeg, paint)

        // 3. Combat Boots / Sabatons
        paint.color = if (archetype == CharacterArchetype.SAIYAN_VEGETA) Color.WHITE else Color.rgb(20, 40, 90)
        // Left Boot
        val leftBoot = Path().apply {
            moveTo(leftFootX - bodyH * 0.06f, feetY - bodyH * 0.08f)
            lineTo(leftFootX - bodyH * 0.09f, feetY) // Toe point
            lineTo(leftFootX + bodyH * 0.05f, feetY)
            lineTo(leftFootX + bodyH * 0.04f, feetY - bodyH * 0.08f)
            close()
        }
        canvas.drawPath(leftBoot, paint)

        // Right Boot
        val rightBoot = Path().apply {
            moveTo(rightFootX - bodyH * 0.04f, feetY - bodyH * 0.08f)
            lineTo(rightFootX - bodyH * 0.05f, feetY)
            lineTo(rightFootX + bodyH * 0.09f, feetY)
            lineTo(rightFootX + bodyH * 0.06f, feetY - bodyH * 0.08f)
            close()
        }
        canvas.drawPath(rightBoot, paint)

        // Boot Toe Accents (Yellow for Vegeta/Trunks, Red for Goku)
        paint.color = if (archetype == CharacterArchetype.SAIYAN_VEGETA) Color.rgb(255, 214, 0) else Color.rgb(213, 0, 0)
        canvas.drawRect(leftFootX - bodyH * 0.08f, feetY - 6f, leftFootX - bodyH * 0.02f, feetY, paint)
        canvas.drawRect(rightFootX + bodyH * 0.02f, feetY - 6f, rightFootX + bodyH * 0.08f, feetY, paint)

        // 4. Pelvis & Martial Arts Belt Sash
        paint.color = palette.secondaryAttire
        canvas.drawRoundRect(RectF(cx - bodyH * 0.12f, waistY - 6f, cx + bodyH * 0.12f, waistY + pelvisH * 0.6f), 8f, 8f, paint)

        // Dangling Belt Sash Ribbons (for Goku / Gi)
        val sashKnot = Path().apply {
            moveTo(cx - 10f, waistY + 10f)
            lineTo(cx - 24f, waistY + pelvisH * 1.5f)
            lineTo(cx - 14f, waistY + pelvisH * 1.5f)
            lineTo(cx, waistY + 10f)
            close()
        }
        canvas.drawPath(sashKnot, paint)

        // 5. Muscular Torso & Attire (V-taper build)
        val shoulderWidth = bodyH * 0.28f
        val waistWidth = bodyH * 0.16f

        paint.color = palette.primaryAttire
        val torsoPath = Path().apply {
            moveTo(cx - shoulderWidth * 0.5f, shouldersY)
            lineTo(cx - waistWidth * 0.5f, waistY)
            lineTo(cx + waistWidth * 0.5f, waistY)
            lineTo(cx + shoulderWidth * 0.5f, shouldersY)
            close()
        }
        canvas.drawPath(torsoPath, paint)

        // Attire Details: Saiyan Battle Armor / Orange Gi V-Neck / Piccolo Cape
        if (archetype == CharacterArchetype.SAIYAN_VEGETA) {
            // White armor chestplate with golden shoulder guards
            paint.color = Color.WHITE
            canvas.drawRoundRect(RectF(cx - shoulderWidth * 0.45f, shouldersY, cx + shoulderWidth * 0.45f, waistY), 6f, 6f, paint)

            paint.color = Color.rgb(255, 214, 0) // Gold shoulder flaps
            canvas.drawRect(cx - shoulderWidth * 0.58f, shouldersY - 6f, cx - shoulderWidth * 0.35f, shouldersY + 14f, paint)
            canvas.drawRect(cx + shoulderWidth * 0.35f, shouldersY - 6f, cx + shoulderWidth * 0.58f, shouldersY + 14f, paint)
        } else if (archetype == CharacterArchetype.NAMEKIAN_PICCOLO) {
            // Oversized weighted white shoulder cape
            paint.color = Color.WHITE
            val cape = Path().apply {
                moveTo(cx - shoulderWidth * 0.70f, shouldersY - 14f)
                lineTo(cx + shoulderWidth * 0.70f, shouldersY - 14f)
                lineTo(cx + shoulderWidth * 0.55f, waistY + 20f)
                lineTo(cx - shoulderWidth * 0.55f, waistY + 20f)
                close()
            }
            canvas.drawPath(cape, paint)
        } else {
            // Classic Blue V-Neck undershirt
            paint.color = palette.secondaryAttire
            val vNeck = Path().apply {
                moveTo(cx - shoulderWidth * 0.25f, shouldersY)
                lineTo(cx, shouldersY + torsoH * 0.45f)
                lineTo(cx + shoulderWidth * 0.25f, shouldersY)
                close()
            }
            canvas.drawPath(vNeck, paint)
        }

        // Pectoral and Abs muscle shadows
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.argb(80, 0, 0, 0)
        canvas.drawLine(cx, shouldersY + torsoH * 0.25f, cx, waistY, paint) // Linea alba
        canvas.drawLine(cx - 25f, shouldersY + torsoH * 0.40f, cx + 25f, shouldersY + torsoH * 0.40f, paint)
        canvas.drawLine(cx - 20f, shouldersY + torsoH * 0.65f, cx + 20f, shouldersY + torsoH * 0.65f, paint)

        // 6. Muscular Arms & Clenched Power Fists
        paint.style = Paint.Style.FILL
        paint.color = palette.skinColor

        val armSpan = bodyH * 0.26f
        // Left Arm (bent flexed power stance)
        val leftArm = Path().apply {
            moveTo(cx - shoulderWidth * 0.5f, shouldersY)
            lineTo(cx - shoulderWidth * 0.5f - armSpan * 0.6f, shouldersY + torsoH * 0.55f) // Elbow
            lineTo(cx - shoulderWidth * 0.5f - armSpan * 0.3f, shouldersY + torsoH * 0.95f) // Wrist
            lineTo(cx - shoulderWidth * 0.4f, shouldersY + torsoH * 0.45f)
            close()
        }
        canvas.drawPath(leftArm, paint)

        // Right Arm (bent flexed power stance)
        val rightArm = Path().apply {
            moveTo(cx + shoulderWidth * 0.5f, shouldersY)
            lineTo(cx + shoulderWidth * 0.5f + armSpan * 0.6f, shouldersY + torsoH * 0.55f) // Elbow
            lineTo(cx + shoulderWidth * 0.5f + armSpan * 0.3f, shouldersY + torsoH * 0.95f) // Wrist
            lineTo(cx + shoulderWidth * 0.4f, shouldersY + torsoH * 0.45f)
            close()
        }
        canvas.drawPath(rightArm, paint)

        // Wristbands / Gloves
        paint.color = if (archetype == CharacterArchetype.SAIYAN_VEGETA) Color.WHITE else palette.secondaryAttire
        canvas.drawCircle(cx - shoulderWidth * 0.5f - armSpan * 0.3f, shouldersY + torsoH * 0.95f, 14f, paint)
        canvas.drawCircle(cx + shoulderWidth * 0.5f + armSpan * 0.3f, shouldersY + torsoH * 0.95f, 14f, paint)

        // Clenched Fists with Ki flare
        paint.color = palette.skinColor
        canvas.drawCircle(cx - shoulderWidth * 0.5f - armSpan * 0.3f - 8f, shouldersY + torsoH * 0.95f + 10f, 12f, paint)
        canvas.drawCircle(cx + shoulderWidth * 0.5f + armSpan * 0.3f + 8f, shouldersY + torsoH * 0.95f + 10f, 12f, paint)

        // 7. Head & Face on whole body
        val bodyFaceR = headH * 0.45f
        drawCharacterFace(canvas, archetype, wp, cx, headCy, bodyFaceR, palette, paint, isCloseUp = false)
    }

    // =========================================================================
    // COMPOSITION HELPERS & GRAPHICS
    // =========================================================================

    private fun drawEnergyRiftDivider(
        canvas: Canvas,
        w: Int,
        h: Int,
        wp: Wallpaper,
        palette: CharacterPalette,
        primary: Int,
        accent: Int,
        paint: Paint
    ) {
        val riftY = h * 0.46f

        // Horizontal luminous energy line
        val riftShader = LinearGradient(
            0f, riftY, w.toFloat(), riftY,
            intArrayOf(Color.TRANSPARENT, palette.auraCore, Color.WHITE, palette.auraCore, Color.TRANSPARENT),
            floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.shader = riftShader
        canvas.drawLine(0f, riftY, w.toFloat(), riftY, paint)
        paint.strokeWidth = 14f
        paint.color = Color.argb(90, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow))
        canvas.drawLine(w * 0.1f, riftY, w * 0.9f, riftY, paint)
        paint.shader = null

        // Central Golden Kanji Seal Disk
        val sealR = w * 0.11f
        val sealShader = RadialGradient(
            w * 0.5f, riftY, sealR,
            intArrayOf(Color.rgb(255, 235, 59), Color.rgb(255, 145, 0), Color.rgb(180, 50, 0)),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = sealShader
        canvas.drawCircle(w * 0.5f, riftY, sealR, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.WHITE
        canvas.drawCircle(w * 0.5f, riftY, sealR, paint)

        // Kanji text in seal
        val kanji = if (wp.kanjiSymbol.isNotEmpty()) wp.kanjiSymbol else "氣"
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = sealR * 1.25f
        canvas.drawText(kanji, w * 0.5f, riftY + sealR * 0.45f, paint)

        paint.color = Color.WHITE
        paint.textSize = sealR * 1.20f
        canvas.drawText(kanji, w * 0.5f, riftY + sealR * 0.42f, paint)
    }

    private fun drawBannerFooter(canvas: Canvas, w: Int, h: Int, wp: Wallpaper, accent: Int, paint: Paint) {
        val bannerY = h * 0.92f

        // Semi-transparent dark pill container
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(200, 10, 12, 18)
        canvas.drawRoundRect(RectF(w * 0.08f, bannerY - 32f, w * 0.92f, bannerY + 38f), 20f, 20f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.argb(120, Color.red(accent), Color.green(accent), Color.blue(accent))
        canvas.drawRoundRect(RectF(w * 0.08f, bannerY - 32f, w * 0.92f, bannerY + 38f), 20f, 20f, paint)

        // Character Name & Transformation Form
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.045f
        paint.color = Color.WHITE
        canvas.drawText(wp.character.uppercase(), w * 0.5f, bannerY - 4f, paint)

        paint.textSize = w * 0.032f
        paint.color = Color.rgb(255, 214, 0)
        canvas.drawText("${wp.form} • ${wp.resolution}", w * 0.5f, bannerY + 24f, paint)
    }

    private fun drawLightningBolt(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, rand: Random, paint: Paint) {
        val path = Path()
        path.moveTo(x1, y1)
        var curX = x1
        var curY = y1
        val segments = 6
        val dx = (x2 - x1) / segments
        val dy = (y2 - y1) / segments

        for (s in 1 until segments) {
            curX += dx + (rand.nextFloat() - 0.5f) * 28f
            curY += dy
            path.lineTo(curX, curY)
        }
        path.lineTo(x2, y2)
        canvas.drawPath(path, paint)
    }
}
