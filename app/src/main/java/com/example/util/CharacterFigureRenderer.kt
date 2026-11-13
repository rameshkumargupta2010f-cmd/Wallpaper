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
 * Cinematic Realistic Anime Character Renderer for Dragon Ball Wallpapers.
 * Renders movie-quality anime visuals: volumetric 3D shading, dynamic rim lighting,
 * multi-layered realistic hair with gloss bands, hyper-detailed anime eyes,
 * anatomically sculpted muscular bodies, and atmospheric cinematic lighting.
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

    data class RealisticPalette(
        val hairBase: Int,
        val hairShadow: Int,
        val hairHighlight: Int,
        val rimLight: Int,
        val eyeCore: Int,
        val eyeVibrant: Int,
        val skinBase: Int,
        val skinShadow: Int,
        val skinHighlight: Int,
        val attireBase: Int,
        val attireShadow: Int,
        val accentAttire: Int,
        val accentShadow: Int,
        val auraCore: Int,
        val auraGlow: Int,
        val auraDeep: Int
    )

    fun resolvePalette(wp: Wallpaper, archetype: CharacterArchetype): RealisticPalette {
        val formLower = wp.form.lowercase()
        val titleLower = wp.title.lowercase()
        val p = (wp.primaryColor or 0xFF000000).toInt()
        val s = (wp.secondaryColor or 0xFF000000).toInt()

        val isSilver = formLower.contains("ultra instinct") || formLower.contains("beast") || formLower.contains("silver")
        val isBlue = formLower.contains("blue") || formLower.contains("ssgss")
        val isGod = formLower.contains("god") || formLower.contains("red")
        val isEgo = formLower.contains("ego") || formLower.contains("purple")
        val isRose = formLower.contains("rose") || formLower.contains("rosé")
        val isGold = formLower.contains("super saiyan") || formLower.contains("ssj") || formLower.contains("golden")
        val isGreen = formLower.contains("legendary") || formLower.contains("broly") || formLower.contains("lssj")

        // Hair Palette
        val (hairBase, hairShadow, hairHighlight, rimLight) = when {
            isSilver -> Quad(Color.rgb(235, 240, 250), Color.rgb(150, 165, 185), Color.rgb(255, 255, 255), Color.rgb(200, 230, 255))
            isBlue -> Quad(Color.rgb(0, 215, 255), Color.rgb(0, 110, 200), Color.rgb(200, 250, 255), Color.rgb(0, 250, 255))
            isGod -> Quad(Color.rgb(255, 30, 80), Color.rgb(180, 10, 40), Color.rgb(255, 180, 200), Color.rgb(255, 100, 140))
            isEgo -> Quad(Color.rgb(186, 104, 200), Color.rgb(110, 45, 130), Color.rgb(235, 190, 245), Color.rgb(220, 120, 240))
            isRose -> Quad(Color.rgb(255, 130, 175), Color.rgb(175, 50, 100), Color.rgb(255, 210, 230), Color.rgb(255, 170, 210))
            isGreen -> Quad(Color.rgb(120, 255, 10), Color.rgb(60, 160, 0), Color.rgb(220, 255, 160), Color.rgb(160, 255, 50))
            isGold -> Quad(Color.rgb(255, 220, 0), Color.rgb(195, 135, 0), Color.rgb(255, 250, 180), Color.rgb(255, 235, 80))
            archetype == CharacterArchetype.NAMEKIAN_PICCOLO -> Quad(Color.rgb(76, 175, 80), Color.rgb(46, 125, 50), Color.rgb(165, 214, 167), Color.rgb(129, 199, 132))
            archetype == CharacterArchetype.BIO_FRIEZA -> Quad(Color.rgb(240, 235, 250), Color.rgb(160, 145, 180), Color.rgb(255, 255, 255), Color.rgb(210, 180, 240))
            archetype == CharacterArchetype.BIO_CELL -> Quad(Color.rgb(35, 35, 40), Color.rgb(15, 15, 20), Color.rgb(100, 100, 110), Color.rgb(76, 175, 80))
            archetype == CharacterArchetype.MAJIN_BUU -> Quad(Color.rgb(244, 143, 177), Color.rgb(194, 24, 91), Color.rgb(252, 228, 236), Color.rgb(255, 180, 205))
            archetype == CharacterArchetype.DEITY_BEERUS -> Quad(Color.rgb(156, 110, 180), Color.rgb(95, 60, 120), Color.rgb(205, 170, 225), Color.rgb(0, 229, 255))
            archetype == CharacterArchetype.DEITY_WHIS -> Quad(Color.rgb(245, 245, 255), Color.rgb(170, 175, 195), Color.rgb(255, 255, 255), Color.rgb(129, 212, 250))
            archetype == CharacterArchetype.DRAGON_SHENRON -> Quad(Color.rgb(0, 200, 83), Color.rgb(0, 105, 40), Color.rgb(120, 255, 170), Color.rgb(255, 214, 0))
            else -> Quad(Color.rgb(25, 25, 30), Color.rgb(10, 10, 15), Color.rgb(80, 85, 100), Color.rgb(180, 200, 230))
        }

        // Realistic Eye Colors
        val (eyeCore, eyeVibrant) = when {
            isSilver -> Pair(Color.rgb(255, 255, 255), Color.rgb(200, 215, 235))
            isBlue -> Pair(Color.rgb(200, 250, 255), Color.rgb(0, 180, 255))
            isGod -> Pair(Color.rgb(255, 200, 215), Color.rgb(255, 23, 68))
            isEgo -> Pair(Color.rgb(245, 200, 255), Color.rgb(213, 0, 249))
            isRose -> Pair(Color.rgb(255, 210, 240), Color.rgb(170, 0, 255))
            isGreen -> Pair(Color.rgb(230, 255, 200), Color.rgb(140, 255, 40))
            isGold -> Pair(Color.rgb(210, 255, 220), Color.rgb(0, 230, 118))
            archetype == CharacterArchetype.BIO_FRIEZA -> Pair(Color.rgb(255, 190, 200), Color.rgb(230, 20, 60))
            archetype == CharacterArchetype.BIO_CELL -> Pair(Color.rgb(255, 245, 180), Color.rgb(255, 214, 0))
            archetype == CharacterArchetype.DEITY_BEERUS -> Pair(Color.rgb(255, 255, 200), Color.rgb(255, 235, 59))
            archetype == CharacterArchetype.DRAGON_SHENRON -> Pair(Color.rgb(255, 200, 200), Color.rgb(255, 20, 40))
            else -> Pair(Color.rgb(80, 80, 85), Color.rgb(25, 25, 30))
        }

        // Realistic Anime Skin (Warm base, rich crimson-amber shadow, soft specular highlight)
        val (skinBase, skinShadow, skinHighlight) = when (archetype) {
            CharacterArchetype.NAMEKIAN_PICCOLO -> Triple(Color.rgb(102, 187, 106), Color.rgb(46, 125, 50), Color.rgb(165, 214, 167))
            CharacterArchetype.BIO_FRIEZA -> Triple(Color.rgb(245, 240, 255), Color.rgb(179, 157, 219), Color.rgb(255, 255, 255))
            CharacterArchetype.MAJIN_BUU -> Triple(Color.rgb(244, 143, 177), Color.rgb(194, 24, 91), Color.rgb(252, 228, 236))
            CharacterArchetype.DEITY_BEERUS -> Triple(Color.rgb(179, 136, 200), Color.rgb(106, 65, 130), Color.rgb(215, 180, 235))
            CharacterArchetype.DEITY_WHIS -> Triple(Color.rgb(129, 212, 250), Color.rgb(79, 195, 247), Color.rgb(210, 240, 255))
            CharacterArchetype.DRAGON_SHENRON -> Triple(Color.rgb(0, 200, 83), Color.rgb(0, 105, 40), Color.rgb(110, 255, 165))
            else -> Triple(Color.rgb(255, 218, 185), Color.rgb(210, 140, 100), Color.rgb(255, 240, 225))
        }

        // Realistic Attire Palette with Cast Shadows
        val (attireBase, attireShadow, accentAttire, accentShadow) = when (archetype) {
            CharacterArchetype.SAIYAN_VEGETA -> Quad(Color.rgb(21, 80, 165), Color.rgb(10, 40, 95), Color.rgb(245, 245, 250), Color.rgb(160, 165, 180))
            CharacterArchetype.NAMEKIAN_PICCOLO -> Quad(Color.rgb(106, 27, 154), Color.rgb(60, 15, 90), Color.rgb(245, 245, 250), Color.rgb(170, 175, 190))
            CharacterArchetype.SAIYAN_TRUNKS -> Quad(Color.rgb(40, 53, 147), Color.rgb(20, 25, 85), Color.rgb(255, 214, 0), Color.rgb(180, 140, 0))
            CharacterArchetype.SAIYAN_FUSION -> if (titleLower.contains("gogeta")) {
                Quad(Color.rgb(25, 25, 30), Color.rgb(10, 10, 15), Color.rgb(255, 145, 0), Color.rgb(180, 90, 0))
            } else {
                Quad(Color.rgb(25, 100, 190), Color.rgb(12, 50, 110), Color.rgb(255, 110, 0), Color.rgb(170, 70, 0))
            }
            CharacterArchetype.BIO_FRIEZA -> Quad(Color.rgb(240, 240, 245), Color.rgb(160, 160, 170), Color.rgb(142, 36, 170), Color.rgb(85, 20, 105))
            CharacterArchetype.BIO_CELL -> Quad(Color.rgb(46, 125, 50), Color.rgb(25, 75, 30), Color.rgb(25, 25, 30), Color.rgb(10, 10, 15))
            CharacterArchetype.MAJIN_BUU -> Quad(Color.rgb(245, 245, 245), Color.rgb(170, 170, 180), Color.rgb(255, 214, 0), Color.rgb(180, 140, 0))
            else -> Quad(Color.rgb(245, 110, 0), Color.rgb(170, 60, 0), Color.rgb(25, 100, 190), Color.rgb(12, 50, 110))
        }

        return RealisticPalette(
            hairBase = hairBase,
            hairShadow = hairShadow,
            hairHighlight = hairHighlight,
            rimLight = rimLight,
            eyeCore = eyeCore,
            eyeVibrant = eyeVibrant,
            skinBase = skinBase,
            skinShadow = skinShadow,
            skinHighlight = skinHighlight,
            attireBase = attireBase,
            attireShadow = attireShadow,
            accentAttire = accentAttire,
            accentShadow = accentShadow,
            auraCore = if (isSilver) Color.rgb(240, 245, 255) else p,
            auraGlow = if (isBlue) Color.rgb(0, 229, 255) else s,
            auraDeep = Color.argb(40, Color.red(p), Color.green(p), Color.blue(p))
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

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

        // 1. Cinematic Volumetric Background
        drawCinematicBackground(canvas, w, h, wp, palette, bgDark, paint)

        // 2. UPPER HALF: Realistic Face Close-up with Volumetric Lighting & Glowing Eyes
        val faceCx = w * 0.5f
        val faceCy = h * 0.22f
        val faceR = w * 0.25f

        // Volumetric Head Aura Bloom
        val headAuraShader = RadialGradient(
            faceCx, faceCy, faceR * 2.2f,
            intArrayOf(
                Color.argb(140, Color.red(palette.auraCore), Color.green(palette.auraCore), Color.blue(palette.auraCore)),
                Color.argb(70, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = headAuraShader
        canvas.drawCircle(faceCx, faceCy, faceR * 2.2f, paint)
        paint.shader = null

        drawRealisticFace(canvas, archetype, wp, faceCx, faceCy, faceR, palette, paint, isMacro = true)

        // 3. MIDDLE DIVIDER: Radiant Energy Slash & Golden Kanji Seal
        drawEnergyRiftDivider(canvas, w, h, wp, palette, paint)

        // 4. LOWER HALF: Realistic Muscular Full Body in Action Stance with Aura & Ground Lighting
        val bodyCx = w * 0.5f
        val bodyCy = h * 0.74f
        val bodyH = h * 0.42f

        drawRealisticWholeBody(canvas, archetype, wp, bodyCx, bodyCy, bodyH, palette, paint)

        // 5. Cinematic Movie-Grade Footer Banner
        drawCinematicFooter(canvas, w, h, wp, palette, paint)
    }

    /**
     * Full Body Focus Wallpaper
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

        drawCinematicBackground(canvas, w, h, wp, palette, bgDark, paint)

        // Volumetric Central Ki Core
        val sphereShader = RadialGradient(
            w * 0.5f, h * 0.55f, w * 0.45f,
            intArrayOf(Color.WHITE, palette.auraCore, palette.auraGlow, Color.TRANSPARENT),
            floatArrayOf(0f, 0.2f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = sphereShader
        canvas.drawCircle(w * 0.5f, h * 0.55f, w * 0.45f, paint)
        paint.shader = null

        // Full Body with heightened scale
        drawRealisticWholeBody(canvas, archetype, wp, w * 0.5f, h * 0.58f, h * 0.62f, palette, paint)

        drawCinematicFooter(canvas, w, h, wp, palette, paint)
    }

    /**
     * Cinematic Face Close-Up Focus Wallpaper
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

        drawCinematicBackground(canvas, w, h, wp, palette, bgDark, paint)

        val faceCx = w * 0.5f
        val faceCy = h * 0.46f
        val faceR = w * 0.42f

        drawRealisticFace(canvas, archetype, wp, faceCx, faceCy, faceR, palette, paint, isMacro = true)

        drawCinematicFooter(canvas, w, h, wp, palette, paint)
    }

    fun drawCharacterFace(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        r: Float,
        palette: RealisticPalette,
        paint: Paint,
        isCloseUp: Boolean = true
    ) {
        drawRealisticFace(canvas, archetype, wp, cx, cy, r, palette, paint, isMacro = isCloseUp)
    }

    fun drawCharacterWholeBody(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        bodyH: Float,
        palette: RealisticPalette,
        paint: Paint
    ) {
        drawRealisticWholeBody(canvas, archetype, wp, cx, cy, bodyH, palette, paint)
    }

    // =========================================================================
    // CINEMATIC BACKGROUND & LIGHTING
    // =========================================================================

    private fun drawCinematicBackground(
        canvas: Canvas,
        w: Int,
        h: Int,
        wp: Wallpaper,
        palette: RealisticPalette,
        bgDark: Int,
        paint: Paint
    ) {
        // Deep multi-stop gradient background
        val bgShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                bgDark,
                Color.argb(120, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow)),
                bgDark
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Volumetric Crepuscular God Rays streaming down
        val rand = Random(wp.id.hashCode())
        paint.style = Paint.Style.FILL
        for (i in 0..6) {
            val rayTopX = w * (0.2f + rand.nextFloat() * 0.6f)
            val rayBottomX = rayTopX + (rand.nextFloat() - 0.5f) * w * 0.5f
            val rayWidth = w * (0.08f + rand.nextFloat() * 0.12f)

            val rayShader = LinearGradient(
                rayTopX, 0f, rayBottomX, h * 0.8f,
                intArrayOf(
                    Color.argb(60, Color.red(palette.rimLight), Color.green(palette.rimLight), Color.blue(palette.rimLight)),
                    Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = rayShader
            val rayPath = Path().apply {
                moveTo(rayTopX - rayWidth * 0.2f, 0f)
                lineTo(rayTopX + rayWidth * 0.2f, 0f)
                lineTo(rayBottomX + rayWidth, h * 0.8f)
                lineTo(rayBottomX - rayWidth, h * 0.8f)
                close()
            }
            canvas.drawPath(rayPath, paint)
        }
        paint.shader = null

        // Floating glowing ki particles
        for (i in 0..50) {
            val px = rand.nextFloat() * w
            val py = rand.nextFloat() * h
            val size = 1f + rand.nextFloat() * 4f
            val alpha = 30 + rand.nextInt(150)
            paint.color = Color.argb(alpha, Color.red(palette.auraCore), Color.green(palette.auraCore), Color.blue(palette.auraCore))
            canvas.drawCircle(px, py, size, paint)
        }
    }

    // =========================================================================
    // REALISTIC ANIME FACE RENDERING ENGINE
    // =========================================================================

    fun drawRealisticFace(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        r: Float,
        palette: RealisticPalette,
        paint: Paint,
        isMacro: Boolean
    ) {
        // 1. Muscular Neck with Dynamic Light & Ambient Occlusion
        val neckTopY = cy + r * 0.45f
        val neckBottomY = cy + r * 1.35f
        val neckShader = LinearGradient(
            cx, neckTopY, cx, neckBottomY,
            intArrayOf(palette.skinShadow, palette.skinBase, palette.skinShadow),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = neckShader
        val neckPath = Path().apply {
            moveTo(cx - r * 0.38f, neckTopY)
            lineTo(cx - r * 0.60f, neckBottomY)
            lineTo(cx + r * 0.60f, neckBottomY)
            lineTo(cx + r * 0.38f, neckTopY)
            close()
        }
        canvas.drawPath(neckPath, paint)
        paint.shader = null

        // Sternocleidomastoid muscle & collarbone shading lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.03f
        paint.color = palette.skinShadow
        canvas.drawLine(cx - r * 0.28f, neckTopY + r * 0.3f, cx - r * 0.50f, neckBottomY - r * 0.1f, paint)
        canvas.drawLine(cx + r * 0.28f, neckTopY + r * 0.3f, cx + r * 0.50f, neckBottomY - r * 0.1f, paint)

        // 2. Anatomically Sculpted Jawline with Volumetric 3D Gradient
        val jawShader = LinearGradient(
            cx, cy - r * 0.3f, cx, cy + r * 0.9f,
            intArrayOf(palette.skinHighlight, palette.skinBase, palette.skinShadow),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = jawShader
        val jawPath = Path().apply {
            moveTo(cx - r * 0.68f, cy - r * 0.15f) // Left cheek
            lineTo(cx - r * 0.52f, cy + r * 0.55f) // Left jaw
            lineTo(cx, cy + r * 0.88f)             // Sharp chin point
            lineTo(cx + r * 0.52f, cy + r * 0.55f) // Right jaw
            lineTo(cx + r * 0.68f, cy - r * 0.15f) // Right cheek
            quadTo(cx, cy - r * 0.85f, cx - r * 0.68f, cy - r * 0.15f)
            close()
        }
        canvas.drawPath(jawPath, paint)
        paint.shader = null

        // Subtle cheekbone contour shadows
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(45, Color.red(palette.skinShadow), Color.green(palette.skinShadow), Color.blue(palette.skinShadow))
        canvas.drawOval(RectF(cx - r * 0.58f, cy + r * 0.15f, cx - r * 0.35f, cy + r * 0.45f), paint)
        canvas.drawOval(RectF(cx + r * 0.35f, cy + r * 0.15f, cx + r * 0.58f, cy + r * 0.45f), paint)

        // Energetic Rim Light along the jaw edge
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.025f
        paint.color = palette.rimLight
        val rimJaw = Path().apply {
            moveTo(cx - r * 0.52f, cy + r * 0.55f)
            lineTo(cx, cy + r * 0.88f)
            lineTo(cx + r * 0.52f, cy + r * 0.55f)
        }
        canvas.drawPath(rimJaw, paint)

        // 3. Ears & Species Adornments
        drawRealisticEars(canvas, archetype, cx, cy, r, palette, paint)

        // 4. Hyper-Detailed Realistic Anime Eyes & Eyebrows
        drawRealisticEyesAndBrows(canvas, wp, archetype, cx, cy, r, palette, paint)

        // 5. Sculpted Anime Nose & Determined Mouth
        drawRealisticNoseAndMouth(canvas, cx, cy, r, palette, paint)

        // 6. Realistic Battle Scuffs & Markings
        drawRealisticBattleScuffs(canvas, wp, archetype, cx, cy, r, paint)

        // 7. Volumetric Multi-Layered Hairstyle with Specular Sheen Band
        drawRealisticHairstyle(canvas, archetype, wp, cx, cy, r, palette, paint)

        // 8. Capsule Scouter HUD
        if (wp.tags.any { it.lowercase().contains("scouter") } || wp.form.lowercase().contains("scouter")) {
            drawRealisticScouter(canvas, cx, cy, r, paint)
        }
    }

    private fun drawRealisticEars(
        canvas: Canvas,
        archetype: CharacterArchetype,
        cx: Float,
        cy: Float,
        r: Float,
        palette: RealisticPalette,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        if (archetype == CharacterArchetype.NAMEKIAN_PICCOLO) {
            paint.color = palette.skinBase
            val leftEar = Path().apply {
                moveTo(cx - r * 0.65f, cy + r * 0.05f)
                lineTo(cx - r * 1.10f, cy - r * 0.18f)
                lineTo(cx - r * 0.60f, cy + r * 0.40f)
                close()
            }
            canvas.drawPath(leftEar, paint)

            val rightEar = Path().apply {
                moveTo(cx + r * 0.65f, cy + r * 0.05f)
                lineTo(cx + r * 1.10f, cy - r * 0.18f)
                lineTo(cx + r * 0.60f, cy + r * 0.40f)
                close()
            }
            canvas.drawPath(rightEar, paint)

            // Inner ribbed pink ear patch
            paint.color = Color.rgb(216, 67, 105)
            canvas.drawOval(RectF(cx - r * 0.88f, cy - r * 0.05f, cx - r * 0.70f, cy + r * 0.20f), paint)
            canvas.drawOval(RectF(cx + r * 0.70f, cy - r * 0.05f, cx + r * 0.88f, cy + r * 0.20f), paint)
        } else {
            // Realistic Shaded Anime Ears
            paint.color = palette.skinBase
            canvas.drawOval(RectF(cx - r * 0.80f, cy - r * 0.05f, cx - r * 0.60f, cy + r * 0.40f), paint)
            canvas.drawOval(RectF(cx + r * 0.60f, cy - r * 0.05f, cx + r * 0.80f, cy + r * 0.40f), paint)

            // Inner Ear Cavity Shadows
            paint.style = Paint.Style.FILL
            paint.color = palette.skinShadow
            canvas.drawOval(RectF(cx - r * 0.75f, cy + r * 0.06f, cx - r * 0.64f, cy + r * 0.28f), paint)
            canvas.drawOval(RectF(cx + r * 0.64f, cy + r * 0.06f, cx + r * 0.75f, cy + r * 0.28f), paint)

            // Golden Potara Earring for Vegito
            if (archetype == CharacterArchetype.SAIYAN_FUSION && palette.attireBase == Color.rgb(25, 100, 190)) {
                paint.style = Paint.Style.FILL
                val potaraShader = RadialGradient(
                    cx - r * 0.70f, cy + r * 0.44f, r * 0.09f,
                    intArrayOf(Color.WHITE, Color.rgb(255, 235, 59), Color.rgb(245, 127, 23)),
                    floatArrayOf(0f, 0.4f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = potaraShader
                canvas.drawCircle(cx - r * 0.70f, cy + r * 0.44f, r * 0.08f, paint)
                paint.shader = null
            }
        }
    }

    private fun drawRealisticEyesAndBrows(
        canvas: Canvas,
        wp: Wallpaper,
        archetype: CharacterArchetype,
        cx: Float,
        cy: Float,
        r: Float,
        palette: RealisticPalette,
        paint: Paint
    ) {
        val browY = cy - r * 0.12f

        // 1. Angular Volumetric Furrowed Eyebrows with Hair Texture
        paint.style = Paint.Style.FILL
        val browShader = LinearGradient(
            cx, browY - r * 0.15f, cx, browY + r * 0.15f,
            intArrayOf(palette.hairHighlight, palette.hairBase, palette.hairShadow),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = browShader

        val leftBrow = Path().apply {
            moveTo(cx - r * 0.60f, browY - r * 0.14f)
            lineTo(cx - r * 0.12f, browY + r * 0.02f)
            lineTo(cx - r * 0.12f, browY + r * 0.14f)
            lineTo(cx - r * 0.56f, browY + r * 0.02f)
            close()
        }
        canvas.drawPath(leftBrow, paint)

        val rightBrow = Path().apply {
            moveTo(cx + r * 0.60f, browY - r * 0.14f)
            lineTo(cx + r * 0.12f, browY + r * 0.02f)
            lineTo(cx + r * 0.12f, browY + r * 0.14f)
            lineTo(cx + r * 0.56f, browY + r * 0.02f)
            close()
        }
        canvas.drawPath(rightBrow, paint)
        paint.shader = null

        // 2. Realistic Anime Eyes with Multi-Tone Radiant Irises
        val eyeTopY = browY + r * 0.06f
        val eyeBottomY = browY + r * 0.30f

        // Sclera with realistic upper eyelid shadow
        val leftEyePath = Path().apply {
            moveTo(cx - r * 0.54f, eyeTopY)
            lineTo(cx - r * 0.16f, eyeTopY + r * 0.04f)
            lineTo(cx - r * 0.18f, eyeBottomY)
            lineTo(cx - r * 0.50f, eyeBottomY - r * 0.04f)
            close()
        }
        val rightEyePath = Path().apply {
            moveTo(cx + r * 0.54f, eyeTopY)
            lineTo(cx + r * 0.16f, eyeTopY + r * 0.04f)
            lineTo(cx + r * 0.18f, eyeBottomY)
            lineTo(cx + r * 0.50f, eyeBottomY - r * 0.04f)
            close()
        }

        // White Sclera with soft top shadow
        val scleraShader = LinearGradient(
            cx, eyeTopY, cx, eyeBottomY,
            intArrayOf(Color.rgb(200, 205, 215), Color.rgb(255, 255, 255)),
            null,
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = scleraShader
        canvas.drawPath(leftEyePath, paint)
        canvas.drawPath(rightEyePath, paint)
        paint.shader = null

        // Radiant Multi-Layered Irises
        val leftIrisCx = cx - r * 0.33f
        val rightIrisCx = cx + r * 0.33f
        val irisCy = eyeTopY + (eyeBottomY - eyeTopY) * 0.5f
        val irisRx = r * 0.085f
        val irisRy = r * 0.105f

        val irisShader = RadialGradient(
            leftIrisCx, irisCy, irisRy,
            intArrayOf(palette.eyeCore, palette.eyeVibrant, Color.rgb(10, 10, 15)),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = irisShader
        canvas.drawOval(RectF(leftIrisCx - irisRx, irisCy - irisRy, leftIrisCx + irisRx, irisCy + irisRy), paint)

        val rightIrisShader = RadialGradient(
            rightIrisCx, irisCy, irisRy,
            intArrayOf(palette.eyeCore, palette.eyeVibrant, Color.rgb(10, 10, 15)),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = rightIrisShader
        canvas.drawOval(RectF(rightIrisCx - irisRx, irisCy - irisRy, rightIrisCx + irisRx, irisCy + irisRy), paint)
        paint.shader = null

        // Deep Black Pupils
        paint.color = Color.BLACK
        canvas.drawCircle(leftIrisCx, irisCy, r * 0.045f, paint)
        canvas.drawCircle(rightIrisCx, irisCy, r * 0.045f, paint)

        // Double Crystalline Specular Reflections
        paint.color = Color.WHITE
        canvas.drawCircle(leftIrisCx - r * 0.025f, irisCy - r * 0.035f, r * 0.025f, paint)
        canvas.drawCircle(leftIrisCx + r * 0.025f, irisCy + r * 0.035f, r * 0.015f, paint)
        canvas.drawCircle(rightIrisCx - r * 0.025f, irisCy - r * 0.035f, r * 0.025f, paint)
        canvas.drawCircle(rightIrisCx + r * 0.025f, irisCy + r * 0.035f, r * 0.015f, paint)

        // Bold Crisp Anime Eyeliner Outlines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.04f
        paint.color = Color.BLACK
        canvas.drawLine(cx - r * 0.56f, eyeTopY, cx - r * 0.14f, eyeTopY + r * 0.04f, paint)
        canvas.drawLine(cx + r * 0.56f, eyeTopY, cx + r * 0.14f, eyeTopY + r * 0.04f, paint)
        canvas.drawLine(cx - r * 0.50f, eyeBottomY - r * 0.04f, cx - r * 0.18f, eyeBottomY, paint)
        canvas.drawLine(cx + r * 0.50f, eyeBottomY - r * 0.04f, cx + r * 0.18f, eyeBottomY, paint)

        // Realistic Forehead wrinkles & battle tension lines
        paint.strokeWidth = r * 0.02f
        paint.color = Color.argb(160, Color.red(palette.skinShadow), Color.green(palette.skinShadow), Color.blue(palette.skinShadow))
        canvas.drawLine(cx - r * 0.05f, browY - r * 0.06f, cx - r * 0.05f, browY + r * 0.10f, paint)
        canvas.drawLine(cx + r * 0.05f, browY - r * 0.06f, cx + r * 0.05f, browY + r * 0.10f, paint)
        canvas.drawLine(cx - r * 0.46f, eyeBottomY + r * 0.08f, cx - r * 0.28f, eyeBottomY + r * 0.10f, paint)
        canvas.drawLine(cx + r * 0.46f, eyeBottomY + r * 0.08f, cx + r * 0.28f, eyeBottomY + r * 0.10f, paint)
    }

    private fun drawRealisticNoseAndMouth(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        palette: RealisticPalette,
        paint: Paint
    ) {
        // Sculpted 3D Anime Nose with Cast Shadow
        val nosePath = Path().apply {
            moveTo(cx, cy + r * 0.18f)
            lineTo(cx - r * 0.07f, cy + r * 0.35f)
            lineTo(cx, cy + r * 0.35f)
            close()
        }
        paint.style = Paint.Style.FILL
        paint.color = palette.skinShadow
        canvas.drawPath(nosePath, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.025f
        paint.color = Color.BLACK
        canvas.drawLine(cx, cy + r * 0.18f, cx - r * 0.07f, cy + r * 0.35f, paint)
        canvas.drawLine(cx - r * 0.07f, cy + r * 0.35f, cx + r * 0.04f, cy + r * 0.35f, paint)

        // Determined Battle Smirk / Clenched Teeth Mouth
        val mouthY = cy + r * 0.55f
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(cx - r * 0.22f, mouthY - r * 0.035f, cx + r * 0.22f, mouthY + r * 0.035f), 4f, 4f, paint)

        // Black Mouth Line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.035f
        paint.color = Color.BLACK
        canvas.drawLine(cx - r * 0.25f, mouthY, cx + r * 0.25f, mouthY, paint)

        // Subtle teeth notches
        paint.strokeWidth = r * 0.015f
        canvas.drawLine(cx - r * 0.09f, mouthY - r * 0.03f, cx - r * 0.09f, mouthY + r * 0.03f, paint)
        canvas.drawLine(cx, mouthY - r * 0.03f, cx, mouthY + r * 0.03f, paint)
        canvas.drawLine(cx + r * 0.09f, mouthY - r * 0.03f, cx + r * 0.09f, mouthY + r * 0.03f, paint)

        // Lower lip cast shadow
        paint.strokeWidth = r * 0.03f
        paint.color = palette.skinShadow
        canvas.drawLine(cx - r * 0.09f, mouthY + r * 0.08f, cx + r * 0.09f, mouthY + r * 0.08f, paint)
    }

    private fun drawRealisticBattleScuffs(
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

        // Majin M Emblem on forehead
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

        // Realistic Anime Cheek Battle Scratches
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.02f
        paint.color = Color.rgb(183, 28, 28) // Deep crimson scuff
        canvas.drawLine(cx - r * 0.45f, cy + r * 0.38f, cx - r * 0.30f, cy + r * 0.42f, paint)
        canvas.drawLine(cx - r * 0.48f, cy + r * 0.42f, cx - r * 0.34f, cy + r * 0.46f, paint)
        canvas.drawLine(cx + r * 0.32f, cy + r * 0.38f, cx + r * 0.46f, cy + r * 0.42f, paint)

        // Bardock's Cross Scar
        if (charLower.contains("bardock")) {
            paint.strokeWidth = r * 0.035f
            paint.color = Color.rgb(183, 28, 28)
            val scarX = cx - r * 0.42f
            val scarY = cy + r * 0.40f
            canvas.drawLine(scarX - r * 0.14f, scarY - r * 0.14f, scarX + r * 0.14f, scarY + r * 0.14f, paint)
            canvas.drawLine(scarX - r * 0.12f, scarY + r * 0.12f, scarX + r * 0.12f, scarY - r * 0.12f, paint)
        }
    }

    private fun drawRealisticHairstyle(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        r: Float,
        palette: RealisticPalette,
        paint: Paint
    ) {
        val hairShader = LinearGradient(
            cx, cy - r * 1.8f, cx, cy,
            intArrayOf(palette.hairHighlight, palette.hairBase, palette.hairShadow),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = hairShader

        val hairPath = Path()

        when (archetype) {
            CharacterArchetype.SAIYAN_VEGETA -> {
                // Majestic Upright Flame Hair
                hairPath.moveTo(cx, cy - r * 0.38f)
                hairPath.lineTo(cx - r * 0.32f, cy - r * 0.52f)
                hairPath.lineTo(cx - r * 0.68f, cy - r * 0.32f)
                hairPath.lineTo(cx - r * 0.90f, cy - r * 0.90f)
                hairPath.lineTo(cx - r * 0.60f, cy - r * 1.05f)
                hairPath.lineTo(cx - r * 0.48f, cy - r * 1.60f)
                hairPath.lineTo(cx - r * 0.16f, cy - r * 1.30f)
                hairPath.lineTo(cx, cy - r * 1.75f)            // Tallest flame spike
                hairPath.lineTo(cx + r * 0.16f, cy - r * 1.30f)
                hairPath.lineTo(cx + r * 0.48f, cy - r * 1.60f)
                hairPath.lineTo(cx + r * 0.60f, cy - r * 1.05f)
                hairPath.lineTo(cx + r * 0.90f, cy - r * 0.90f)
                hairPath.lineTo(cx + r * 0.68f, cy - r * 0.32f)
                hairPath.lineTo(cx + r * 0.32f, cy - r * 0.52f)
                hairPath.close()
                canvas.drawPath(hairPath, paint)
                paint.shader = null

                // Iconic Anime Sheen Band (gloss highlight curve)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = r * 0.06f
                paint.color = palette.hairHighlight
                val sheen = Path().apply {
                    moveTo(cx - r * 0.55f, cy - r * 0.85f)
                    quadTo(cx, cy - r * 1.15f, cx + r * 0.55f, cy - r * 0.85f)
                }
                canvas.drawPath(sheen, paint)

                // Energetic Rim Highlight
                paint.strokeWidth = r * 0.03f
                paint.color = palette.rimLight
                canvas.drawPath(hairPath, paint)
            }

            CharacterArchetype.SAIYAN_GOHAN -> {
                // Teen SSJ2 / Beast Gohan: Swept-back spikes with single dramatic front bang
                hairPath.moveTo(cx - r * 0.58f, cy - r * 0.32f)
                hairPath.lineTo(cx - r * 0.95f, cy - r * 0.95f)
                hairPath.lineTo(cx - r * 0.65f, cy - r * 1.15f)
                hairPath.lineTo(cx - r * 0.38f, cy - r * 1.75f) // Towering Beast spike
                hairPath.lineTo(cx + r * 0.16f, cy - r * 1.25f)
                hairPath.lineTo(cx + r * 0.58f, cy - r * 1.55f)
                hairPath.lineTo(cx + r * 0.85f, cy - r * 0.75f)
                hairPath.lineTo(cx + r * 0.58f, cy - r * 0.32f)
                hairPath.close()
                canvas.drawPath(hairPath, paint)
                paint.shader = null

                // Dramatic long front bang falling past brow
                paint.style = Paint.Style.FILL
                paint.color = palette.hairBase
                val bang = Path().apply {
                    moveTo(cx - r * 0.16f, cy - r * 0.48f)
                    lineTo(cx - r * 0.35f, cy + r * 0.18f)
                    lineTo(cx - r * 0.05f, cy - r * 0.22f)
                    close()
                }
                canvas.drawPath(bang, paint)

                // Rim light
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = r * 0.03f
                paint.color = palette.rimLight
                canvas.drawPath(hairPath, paint)
            }

            CharacterArchetype.BIO_FRIEZA -> {
                // Bio-helmet with glossy purple crystal gem
                paint.style = Paint.Style.FILL
                val domeShader = RadialGradient(
                    cx, cy - r * 0.5f, r * 0.6f,
                    intArrayOf(Color.WHITE, Color.rgb(240, 235, 250), Color.rgb(170, 160, 190)),
                    floatArrayOf(0f, 0.4f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = domeShader
                canvas.drawCircle(cx, cy - r * 0.48f, r * 0.58f, paint)
                paint.shader = null

                // Central Forehead Gem with Jewel Refraction
                val gemShader = RadialGradient(
                    cx - r * 0.08f, cy - r * 0.55f, r * 0.25f,
                    intArrayOf(Color.WHITE, Color.rgb(171, 71, 188), Color.rgb(74, 20, 140)),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gemShader
                canvas.drawOval(RectF(cx - r * 0.24f, cy - r * 0.68f, cx + r * 0.24f, cy - r * 0.32f), paint)
                paint.shader = null
            }

            CharacterArchetype.BIO_CELL -> {
                // Crown beetle horn winglets
                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(20, 20, 25)
                val leftHorn = Path().apply {
                    moveTo(cx - r * 0.38f, cy - r * 0.42f)
                    lineTo(cx - r * 1.20f, cy - r * 1.35f)
                    lineTo(cx - r * 0.52f, cy - r * 0.85f)
                    close()
                }
                canvas.drawPath(leftHorn, paint)

                val rightHorn = Path().apply {
                    moveTo(cx + r * 0.38f, cy - r * 0.42f)
                    lineTo(cx + r * 1.20f, cy - r * 1.35f)
                    lineTo(cx + r * 0.52f, cy - r * 0.85f)
                    close()
                }
                canvas.drawPath(rightHorn, paint)

                // Purple cheek exoskeleton plates
                paint.color = Color.rgb(123, 31, 162)
                val leftCheek = Path().apply {
                    moveTo(cx - r * 0.65f, cy + r * 0.10f)
                    lineTo(cx - r * 0.52f, cy + r * 0.58f)
                    lineTo(cx - r * 0.36f, cy + r * 0.36f)
                    close()
                }
                canvas.drawPath(leftCheek, paint)
                val rightCheek = Path().apply {
                    moveTo(cx + r * 0.65f, cy + r * 0.10f)
                    lineTo(cx + r * 0.52f, cy + r * 0.58f)
                    lineTo(cx + r * 0.36f, cy + r * 0.36f)
                    close()
                }
                canvas.drawPath(rightCheek, paint)
            }

            else -> {
                // Master Goku / Saiyan / Fusion Hair with Volumetric Depth & Rim Flare
                hairPath.moveTo(cx - r * 0.48f, cy - r * 0.32f)
                // Left Spikes
                hairPath.lineTo(cx - r * 1.25f, cy - r * 0.28f)
                hairPath.lineTo(cx - r * 0.80f, cy - r * 0.60f)
                hairPath.lineTo(cx - r * 1.40f, cy - r * 0.95f)
                hairPath.lineTo(cx - r * 0.75f, cy - r * 1.05f)
                hairPath.lineTo(cx - r * 1.05f, cy - r * 1.55f)
                hairPath.lineTo(cx - r * 0.45f, cy - r * 1.35f)
                // Crown Spikes
                hairPath.lineTo(cx - r * 0.12f, cy - r * 1.78f)
                hairPath.lineTo(cx + r * 0.28f, cy - r * 1.35f)
                // Right Spikes
                hairPath.lineTo(cx + r * 0.90f, cy - r * 1.45f)
                hairPath.lineTo(cx + r * 0.70f, cy - r * 0.90f)
                hairPath.lineTo(cx + r * 1.25f, cy - r * 0.55f)
                hairPath.lineTo(cx + r * 0.60f, cy - r * 0.28f)
                hairPath.close()
                canvas.drawPath(hairPath, paint)
                paint.shader = null

                // Specular Anime Gloss Sheen
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = r * 0.06f
                paint.color = palette.hairHighlight
                val sheen = Path().apply {
                    moveTo(cx - r * 0.75f, cy - r * 0.85f)
                    quadTo(cx, cy - r * 1.25f, cx + r * 0.70f, cy - r * 0.85f)
                }
                canvas.drawPath(sheen, paint)

                // 3 Signature Forehead Bangs
                paint.style = Paint.Style.FILL
                paint.color = palette.hairBase

                val bang1 = Path().apply {
                    moveTo(cx - r * 0.48f, cy - r * 0.42f)
                    lineTo(cx - r * 0.36f, cy - r * 0.05f)
                    lineTo(cx - r * 0.22f, cy - r * 0.32f)
                    close()
                }
                canvas.drawPath(bang1, paint)

                val bang2 = Path().apply {
                    moveTo(cx - r * 0.28f, cy - r * 0.42f)
                    lineTo(cx - r * 0.06f, cy + r * 0.04f)
                    lineTo(cx + r * 0.10f, cy - r * 0.32f)
                    close()
                }
                canvas.drawPath(bang2, paint)

                val bang3 = Path().apply {
                    moveTo(cx + r * 0.14f, cy - r * 0.38f)
                    lineTo(cx + r * 0.30f, cy - r * 0.06f)
                    lineTo(cx + r * 0.45f, cy - r * 0.32f)
                    close()
                }
                canvas.drawPath(bang3, paint)

                // Energetic Rim Highlight
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = r * 0.035f
                paint.color = palette.rimLight
                canvas.drawPath(hairPath, paint)
            }
        }
    }

    private fun drawRealisticScouter(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        val scouterX = cx - r * 0.33f
        val scouterY = cy + r * 0.06f

        // Glowing Translucent Green HUD with Glass Reflection
        val lensShader = LinearGradient(
            scouterX - r * 0.36f, scouterY - r * 0.25f, scouterX + r * 0.36f, scouterY + r * 0.25f,
            intArrayOf(Color.argb(190, 0, 255, 120), Color.argb(120, 0, 180, 80)),
            null,
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = lensShader
        canvas.drawRoundRect(RectF(scouterX - r * 0.36f, scouterY - r * 0.26f, scouterX + r * 0.36f, scouterY + r * 0.26f), 12f, 12f, paint)
        paint.shader = null

        // Metallic Frame & Ear Device
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.04f
        paint.color = Color.rgb(255, 235, 59)
        canvas.drawRoundRect(RectF(scouterX - r * 0.36f, scouterY - r * 0.26f, scouterX + r * 0.36f, scouterY + r * 0.26f), 12f, 12f, paint)

        paint.strokeWidth = r * 0.05f
        paint.color = Color.rgb(190, 200, 215)
        canvas.drawLine(scouterX - r * 0.36f, scouterY, cx - r * 0.74f, scouterY, paint)

        // Digital Target Reticle & Power Level Grid
        paint.strokeWidth = 2f
        paint.color = Color.rgb(255, 23, 68)
        canvas.drawCircle(scouterX, scouterY, r * 0.12f, paint)
        canvas.drawLine(scouterX - r * 0.18f, scouterY, scouterX + r * 0.18f, scouterY, paint)
        canvas.drawLine(scouterX, scouterY - r * 0.18f, scouterX, scouterY + r * 0.18f, paint)
    }

    // =========================================================================
    // REALISTIC ANIME WHOLE BODY ENGINE
    // =========================================================================

    fun drawRealisticWholeBody(
        canvas: Canvas,
        archetype: CharacterArchetype,
        wp: Wallpaper,
        cx: Float,
        cy: Float,
        bodyH: Float,
        palette: RealisticPalette,
        paint: Paint
    ) {
        val headH = bodyH * 0.17f
        val torsoH = bodyH * 0.33f
        val pelvisH = bodyH * 0.10f
        val legsH = bodyH * 0.40f

        val topY = cy - bodyH * 0.5f
        val headCy = topY + headH * 0.6f
        val shouldersY = topY + headH
        val waistY = shouldersY + torsoH
        val pelvisY = waistY + pelvisH
        val feetY = pelvisY + legsH

        // 1. Realistic Multi-Layered Ki Aura (Atmospheric Corona + Core Plasma + Lightning)
        val auraRadius = bodyH * 0.58f
        val auraShader = RadialGradient(
            cx, cy - bodyH * 0.05f, auraRadius,
            intArrayOf(
                Color.argb(180, Color.red(palette.auraCore), Color.green(palette.auraCore), Color.blue(palette.auraCore)),
                Color.argb(90, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = auraShader
        canvas.drawCircle(cx, cy - bodyH * 0.05f, auraRadius, paint)
        paint.shader = null

        // Upward turbulent ki flame tongues
        val rand = Random(wp.id.hashCode() + 33)
        paint.style = Paint.Style.STROKE
        for (i in 0..12) {
            val fx = cx + (rand.nextFloat() - 0.5f) * bodyH * 0.45f
            val flamePath = Path()
            flamePath.moveTo(fx, feetY)
            var curX = fx
            var curY = feetY
            val fLen = bodyH * (0.4f + rand.nextFloat() * 0.45f)
            val steps = 6
            val dy = fLen / steps
            for (s in 1..steps) {
                curY -= dy
                curX += (rand.nextFloat() - 0.5f) * 45f
                flamePath.lineTo(curX, curY)
            }
            paint.strokeWidth = 3f + rand.nextFloat() * 4f
            paint.color = Color.argb(120 + rand.nextInt(100), Color.red(palette.rimLight), Color.green(palette.rimLight), Color.blue(palette.rimLight))
            canvas.drawPath(flamePath, paint)
        }

        // High-Voltage Plasma Lightning Arcs
        paint.strokeWidth = 2.5f
        for (i in 0..5) {
            paint.color = Color.WHITE
            val lx1 = cx + (rand.nextFloat() - 0.5f) * bodyH * 0.5f
            val ly1 = cy + (rand.nextFloat() - 0.5f) * bodyH * 0.6f
            drawLightningBolt(canvas, lx1, ly1, lx1 + (rand.nextFloat() - 0.5f) * 90f, ly1 + 100f, rand, paint)
        }

        // Ground Impact Crater with Molten Rim & Uplight
        val craterW = bodyH * 0.40f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb(220, Color.red(palette.rimLight), Color.green(palette.rimLight), Color.blue(palette.rimLight))
        canvas.drawOval(RectF(cx - craterW, feetY - 24f, cx + craterW, feetY + 24f), paint)

        // 2. Muscular Legs in Wide Grounded Power Stance with Realistic Fabric Shading
        val leftFootX = cx - bodyH * 0.21f
        val rightFootX = cx + bodyH * 0.21f

        paint.style = Paint.Style.FILL
        val legShader = LinearGradient(
            cx, waistY, cx, feetY,
            intArrayOf(palette.attireBase, palette.attireShadow),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = legShader

        val leftLeg = Path().apply {
            moveTo(cx - bodyH * 0.08f, waistY + pelvisH * 0.5f)
            lineTo(leftFootX - bodyH * 0.07f, feetY - bodyH * 0.08f)
            lineTo(leftFootX + bodyH * 0.04f, feetY - bodyH * 0.08f)
            lineTo(cx, pelvisY)
            close()
        }
        canvas.drawPath(leftLeg, paint)

        val rightLeg = Path().apply {
            moveTo(cx + bodyH * 0.08f, waistY + pelvisH * 0.5f)
            lineTo(rightFootX + bodyH * 0.07f, feetY - bodyH * 0.08f)
            lineTo(rightFootX - bodyH * 0.04f, feetY - bodyH * 0.08f)
            lineTo(cx, pelvisY)
            close()
        }
        canvas.drawPath(rightLeg, paint)
        paint.shader = null

        // Realistic Fabric Folds & Muscle Grooves on Pants
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = palette.attireShadow
        canvas.drawLine(leftFootX - bodyH * 0.02f, pelvisY + bodyH * 0.08f, leftFootX - bodyH * 0.04f, feetY - bodyH * 0.12f, paint)
        canvas.drawLine(rightFootX + bodyH * 0.02f, pelvisY + bodyH * 0.08f, rightFootX + bodyH * 0.04f, feetY - bodyH * 0.12f, paint)

        // 3. Combat Boots with Dynamic Highlights
        paint.style = Paint.Style.FILL
        paint.color = if (archetype == CharacterArchetype.SAIYAN_VEGETA) Color.WHITE else Color.rgb(18, 35, 75)

        val leftBoot = Path().apply {
            moveTo(leftFootX - bodyH * 0.07f, feetY - bodyH * 0.08f)
            lineTo(leftFootX - bodyH * 0.10f, feetY)
            lineTo(leftFootX + bodyH * 0.05f, feetY)
            lineTo(leftFootX + bodyH * 0.04f, feetY - bodyH * 0.08f)
            close()
        }
        canvas.drawPath(leftBoot, paint)

        val rightBoot = Path().apply {
            moveTo(rightFootX - bodyH * 0.04f, feetY - bodyH * 0.08f)
            lineTo(rightFootX - bodyH * 0.05f, feetY)
            lineTo(rightFootX + bodyH * 0.10f, feetY)
            lineTo(rightFootX + bodyH * 0.07f, feetY - bodyH * 0.08f)
            close()
        }
        canvas.drawPath(rightBoot, paint)

        // Boot Toe Cap Accent with Gloss
        paint.color = if (archetype == CharacterArchetype.SAIYAN_VEGETA) Color.rgb(255, 214, 0) else Color.rgb(213, 0, 0)
        canvas.drawRect(leftFootX - bodyH * 0.09f, feetY - 8f, leftFootX - bodyH * 0.03f, feetY, paint)
        canvas.drawRect(rightFootX + bodyH * 0.03f, feetY - 8f, rightFootX + bodyH * 0.09f, feetY, paint)

        // 4. Waistband Sash / Armor Belt
        val beltShader = LinearGradient(
            cx - bodyH * 0.14f, waistY, cx + bodyH * 0.14f, waistY,
            intArrayOf(palette.accentShadow, palette.accentAttire, palette.accentShadow),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = beltShader
        canvas.drawRoundRect(RectF(cx - bodyH * 0.13f, waistY - 6f, cx + bodyH * 0.13f, waistY + pelvisH * 0.6f), 8f, 8f, paint)
        paint.shader = null

        // Trailing Knot Ribbons
        paint.color = palette.accentAttire
        val sashKnot = Path().apply {
            moveTo(cx - 10f, waistY + 10f)
            lineTo(cx - 26f, waistY + pelvisH * 1.6f)
            lineTo(cx - 14f, waistY + pelvisH * 1.6f)
            lineTo(cx, waistY + 10f)
            close()
        }
        canvas.drawPath(sashKnot, paint)

        // 5. Sculpted Muscular Torso (Broad Shoulders, Striated Pecs, 6-Pack Abs)
        val shoulderWidth = bodyH * 0.30f
        val waistWidth = bodyH * 0.17f

        val torsoShader = LinearGradient(
            cx, shouldersY, cx, waistY,
            intArrayOf(palette.attireBase, palette.attireShadow),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = torsoShader

        val torsoPath = Path().apply {
            moveTo(cx - shoulderWidth * 0.5f, shouldersY)
            lineTo(cx - waistWidth * 0.5f, waistY)
            lineTo(cx + waistWidth * 0.5f, waistY)
            lineTo(cx + shoulderWidth * 0.5f, shouldersY)
            close()
        }
        canvas.drawPath(torsoPath, paint)
        paint.shader = null

        // Specific Attire Anatomy: Saiyan Battle Armor / Orange Gi V-Neck / Piccolo Cape
        if (archetype == CharacterArchetype.SAIYAN_VEGETA) {
            // Metallic White Armor with Gold Shoulder Flaps
            val armorShader = LinearGradient(
                cx, shouldersY, cx, waistY,
                intArrayOf(Color.rgb(255, 255, 255), Color.rgb(210, 215, 225)),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = armorShader
            canvas.drawRoundRect(RectF(cx - shoulderWidth * 0.46f, shouldersY, cx + shoulderWidth * 0.46f, waistY), 8f, 8f, paint)
            paint.shader = null

            // Gold shoulder guards with metallic shine
            val goldShader = LinearGradient(
                cx, shouldersY, cx, shouldersY + 18f,
                intArrayOf(Color.rgb(255, 240, 160), Color.rgb(255, 195, 0)),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = goldShader
            canvas.drawRect(cx - shoulderWidth * 0.60f, shouldersY - 6f, cx - shoulderWidth * 0.36f, shouldersY + 16f, paint)
            canvas.drawRect(cx + shoulderWidth * 0.36f, shouldersY - 6f, cx + shoulderWidth * 0.60f, shouldersY + 16f, paint)
            paint.shader = null
        } else if (archetype == CharacterArchetype.NAMEKIAN_PICCOLO) {
            // Oversized Weighted White Cape
            paint.color = Color.WHITE
            val cape = Path().apply {
                moveTo(cx - shoulderWidth * 0.72f, shouldersY - 16f)
                lineTo(cx + shoulderWidth * 0.72f, shouldersY - 16f)
                lineTo(cx + shoulderWidth * 0.58f, waistY + 24f)
                lineTo(cx - shoulderWidth * 0.58f, waistY + 24f)
                close()
            }
            canvas.drawPath(cape, paint)
        } else {
            // Classic V-Neck Blue Undershirt with depth
            paint.color = palette.accentAttire
            val vNeck = Path().apply {
                moveTo(cx - shoulderWidth * 0.26f, shouldersY)
                lineTo(cx, shouldersY + torsoH * 0.45f)
                lineTo(cx + shoulderWidth * 0.26f, shouldersY)
                close()
            }
            canvas.drawPath(vNeck, paint)
        }

        // Pectorals & 6-Pack Abdominal 3D Contours
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.argb(100, 0, 0, 0)
        canvas.drawLine(cx, shouldersY + torsoH * 0.25f, cx, waistY, paint) // Linea alba
        canvas.drawLine(cx - 28f, shouldersY + torsoH * 0.40f, cx + 28f, shouldersY + torsoH * 0.40f, paint)
        canvas.drawLine(cx - 24f, shouldersY + torsoH * 0.65f, cx + 24f, shouldersY + torsoH * 0.65f, paint)

        // 6. Muscular Arms (Deltoids, Flexed Biceps, Forearm Gauntlets & Clenched Fists)
        paint.style = Paint.Style.FILL
        val armShader = LinearGradient(
            cx, shouldersY, cx, waistY + 30f,
            intArrayOf(palette.skinHighlight, palette.skinBase, palette.skinShadow),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = armShader

        val armSpan = bodyH * 0.27f
        val leftArm = Path().apply {
            moveTo(cx - shoulderWidth * 0.5f, shouldersY)
            lineTo(cx - shoulderWidth * 0.5f - armSpan * 0.62f, shouldersY + torsoH * 0.55f) // Elbow
            lineTo(cx - shoulderWidth * 0.5f - armSpan * 0.32f, shouldersY + torsoH * 0.95f) // Wrist
            lineTo(cx - shoulderWidth * 0.42f, shouldersY + torsoH * 0.45f)
            close()
        }
        canvas.drawPath(leftArm, paint)

        val rightArm = Path().apply {
            moveTo(cx + shoulderWidth * 0.5f, shouldersY)
            lineTo(cx + shoulderWidth * 0.5f + armSpan * 0.62f, shouldersY + torsoH * 0.55f)
            lineTo(cx + shoulderWidth * 0.5f + armSpan * 0.32f, shouldersY + torsoH * 0.95f)
            lineTo(cx + shoulderWidth * 0.42f, shouldersY + torsoH * 0.45f)
            close()
        }
        canvas.drawPath(rightArm, paint)
        paint.shader = null

        // Wristbands / Gauntlets
        paint.color = if (archetype == CharacterArchetype.SAIYAN_VEGETA) Color.WHITE else palette.accentAttire
        canvas.drawCircle(cx - shoulderWidth * 0.5f - armSpan * 0.32f, shouldersY + torsoH * 0.95f, 15f, paint)
        canvas.drawCircle(cx + shoulderWidth * 0.5f + armSpan * 0.32f, shouldersY + torsoH * 0.95f, 15f, paint)

        // Clenched Power Fists with Energy Glow
        paint.color = palette.skinBase
        canvas.drawCircle(cx - shoulderWidth * 0.5f - armSpan * 0.32f - 9f, shouldersY + torsoH * 0.95f + 12f, 13f, paint)
        canvas.drawCircle(cx + shoulderWidth * 0.5f + armSpan * 0.32f + 9f, shouldersY + torsoH * 0.95f + 12f, 13f, paint)

        // 7. Full-Body Head & Face
        val bodyFaceR = headH * 0.46f
        drawRealisticFace(canvas, archetype, wp, cx, headCy, bodyFaceR, palette, paint, isMacro = false)
    }

    // =========================================================================
    // ENERGY RIFT DIVIDER & CINEMATIC FOOTER
    // =========================================================================

    private fun drawEnergyRiftDivider(
        canvas: Canvas,
        w: Int,
        h: Int,
        wp: Wallpaper,
        palette: RealisticPalette,
        paint: Paint
    ) {
        val riftY = h * 0.46f

        // Volumetric Horizontal Plasma Rift
        val riftShader = LinearGradient(
            0f, riftY, w.toFloat(), riftY,
            intArrayOf(Color.TRANSPARENT, palette.rimLight, Color.WHITE, palette.rimLight, Color.TRANSPARENT),
            floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.shader = riftShader
        canvas.drawLine(0f, riftY, w.toFloat(), riftY, paint)
        paint.strokeWidth = 18f
        paint.color = Color.argb(80, Color.red(palette.auraGlow), Color.green(palette.auraGlow), Color.blue(palette.auraGlow))
        canvas.drawLine(w * 0.08f, riftY, w * 0.92f, riftY, paint)
        paint.shader = null

        // Metallic Golden Kanji Seal Disk
        val sealR = w * 0.12f
        val sealShader = RadialGradient(
            w * 0.5f, riftY, sealR,
            intArrayOf(Color.rgb(255, 245, 180), Color.rgb(255, 175, 0), Color.rgb(190, 60, 0)),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = sealShader
        canvas.drawCircle(w * 0.5f, riftY, sealR, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f
        paint.color = Color.WHITE
        canvas.drawCircle(w * 0.5f, riftY, sealR, paint)

        // Kanji symbol with depth
        val kanji = if (wp.kanjiSymbol.isNotEmpty()) wp.kanjiSymbol else "氣"
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = sealR * 1.30f
        canvas.drawText(kanji, w * 0.5f, riftY + sealR * 0.46f, paint)

        paint.color = Color.WHITE
        paint.textSize = sealR * 1.25f
        canvas.drawText(kanji, w * 0.5f, riftY + sealR * 0.42f, paint)
    }

    private fun drawCinematicFooter(
        canvas: Canvas,
        w: Int,
        h: Int,
        wp: Wallpaper,
        palette: RealisticPalette,
        paint: Paint
    ) {
        val bannerY = h * 0.925f

        // Glassmorphism Dark Container with Rim Glow
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(220, 8, 10, 16)
        canvas.drawRoundRect(RectF(w * 0.06f, bannerY - 34f, w * 0.94f, bannerY + 40f), 22f, 22f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.argb(160, Color.red(palette.rimLight), Color.green(palette.rimLight), Color.blue(palette.rimLight))
        canvas.drawRoundRect(RectF(w * 0.06f, bannerY - 34f, w * 0.94f, bannerY + 40f), 22f, 22f, paint)

        // Character Name & Transformation Form
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = w * 0.046f
        paint.color = Color.WHITE
        canvas.drawText(wp.character.uppercase(), w * 0.5f, bannerY - 4f, paint)

        paint.textSize = w * 0.033f
        paint.color = palette.rimLight
        canvas.drawText("${wp.form} • ${wp.resolution}", w * 0.5f, bannerY + 25f, paint)
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
