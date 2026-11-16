package com.farabe.farabesmomblock.ui.components3d

import android.view.Choreographer
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.Box as FilamentBox
import com.google.android.filament.Camera
import com.google.android.filament.Colors
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import com.google.android.filament.IndexBuffer
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.VertexBuffer
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.UiHelper
import com.google.android.filament.filamat.MaterialBuilder
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Playful 3D Tetromino Mascot & Toy Showcase powered by Google Filament PBR!
 * Instead of plain static cubes, renders chamfered/beveled 3D puzzle shapes:
 *  - A smiling Golden L-Tetromino Mascot with 3D eyes, rosy cheeks & crown
 *  - A dancing Emerald T-Tetromino companion
 *  - A Cyan 3-Bar / Corner puzzle toy
 *  - A floating 3D Ruby-Pink Heart sculpture assembled from mini blocks
 * Supports tap acrobatics (backflips, jelly bounce, puzzle snap) and drag spin inertia.
 */
@Composable
fun Filament3DShowcase(
    accentColor: Color,
    goldColor: Color,
    isCelebrationActive: Boolean = false,
    onBlockPoked: (pokeCount: Int, reactionText: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var userYawOffset by remember { mutableFloatStateOf(0f) }
    var userPitchOffset by remember { mutableFloatStateOf(0f) }
    var yawVelocity by remember { mutableFloatStateOf(0f) }
    var pitchVelocity by remember { mutableFloatStateOf(0f) }
    var pokeCount by remember { mutableIntStateOf(0) }

    val controller = remember {
        FilamentShowcaseController()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            if (abs(yawVelocity) > 0.05f || abs(pitchVelocity) > 0.05f) {
                userYawOffset += yawVelocity
                userPitchOffset = (userPitchOffset + pitchVelocity).coerceIn(-40f, 40f)
                yawVelocity *= 0.92f
                pitchVelocity *= 0.90f
            }
        }
    }

    controller.userYaw = userYawOffset
    controller.userPitch = userPitchOffset
    controller.boostSpin = isCelebrationActive
    controller.updateColors(accentColor, goldColor)

    DisposableEffect(Unit) {
        onDispose {
            controller.destroy()
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    pokeCount += 1
                    controller.triggerPlayfulPoke(pokeCount % 4)
                    val reactions = listOf(
                        "🤸 হিহি! ডিগবাজি দিচ্ছি আম্মু! (+5🪙)",
                        "😍 আম্মু তোমাকে অনেক ভালোবাসি! ❤️ (+5🪙)",
                        "💃 দেখো আমাদের টেট্রোমিনো নাচ! (+5🪙)",
                        "🏆 চলো নতুন চ্যালেঞ্জ জিতি আম্মু! (+5🪙)"
                    )
                    onBlockPoked(pokeCount, reactions[(pokeCount - 1) % reactions.size])
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    userYawOffset += dragAmount.x * 0.55f
                    userPitchOffset = (userPitchOffset + dragAmount.y * 0.38f).coerceIn(-40f, 40f)
                    yawVelocity = dragAmount.x * 0.40f
                    pitchVelocity = dragAmount.y * 0.22f
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    controller.attach(this)
                }
            }
        )
    }
}

private class FilamentShowcaseController {

    companion object {
        private var initialized = false
        private var cachedMaterialPayload: ByteArray? = null

        fun ensureInit() {
            if (!initialized) {
                try {
                    Filament.init()
                    initialized = true
                } catch (_: Throwable) {
                }
            }
        }
    }

    var userYaw: Float = 0f
    var userPitch: Float = 0f
    var boostSpin: Boolean = false
    private var formationMode: Int = 0
    private var lastPokeNanos: Long = 0L

    fun triggerPlayfulPoke(nextFormation: Int) {
        formationMode = nextFormation
        lastPokeNanos = System.nanoTime()
    }

    private data class SubBlockNode(
        val entity: Int,
        val matInstance: MaterialInstance,
        val groupId: Int,       // 0 = Golden L Mascot, 1 = Emerald T, 2 = Cyan L/Bar, 3 = Top 3D Heart
        val localX: Float,
        val localY: Float,
        val localZ: Float,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val scaleZ: Float = 1f,
        val colorRole: Int = 0  // 0=Gold, 1=Emerald, 2=Cyan, 3=RubyHeart, 4=EyeDark, 5=CheekPink
    )

    private var engine: Engine? = null
    private var renderer: Renderer? = null
    private var scene: Scene? = null
    private var view: View? = null
    private var camera: Camera? = null
    private var swapChain: SwapChain? = null
    private var uiHelper: UiHelper? = null

    private var pbrMaterial: Material? = null
    private val nodes = mutableListOf<SubBlockNode>()
    private var vertexBuffer: VertexBuffer? = null
    private var indexBuffer: IndexBuffer? = null
    private var indexCount: Int = 0
    private var sunLightEntity: Int = 0
    private var rimLightEntity: Int = 0
    private var indirectLight: IndirectLight? = null

    private var accentRgb = floatArrayOf(0.37f, 0.84f, 0.23f)
    private var goldRgb = floatArrayOf(1.0f, 0.82f, 0.25f)

    private var isRunning = false
    private val startNanos = System.nanoTime()
    private val choreographer = Choreographer.getInstance()

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return
            choreographer.postFrameCallback(this)
            renderFrame(frameTimeNanos)
        }
    }

    fun updateColors(accent: Color, gold: Color) {
        accentRgb = floatArrayOf(accent.red, accent.green, accent.blue)
        goldRgb = floatArrayOf(gold.red, gold.green, gold.blue)
        for (node in nodes) {
            try {
                when (node.colorRole) {
                    0 -> node.matInstance.setParameter("baseColor", Colors.RgbType.SRGB, goldRgb[0], goldRgb[1], goldRgb[2])
                    1 -> node.matInstance.setParameter("baseColor", Colors.RgbType.SRGB, accentRgb[0], accentRgb[1], accentRgb[2])
                }
            } catch (_: Throwable) {
            }
        }
    }

    fun attach(textureView: TextureView) {
        try {
            ensureInit()
            val eng = Engine.create()
            engine = eng
            renderer = eng.createRenderer().apply {
                clearOptions = Renderer.ClearOptions().apply {
                    clear = true
                    clearColor = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
                }
            }
            scene = eng.createScene()
            view = eng.createView().apply {
                this.scene = this@FilamentShowcaseController.scene
                this.blendMode = View.BlendMode.TRANSLUCENT
                this.isPostProcessingEnabled = true
            }
            val camEntity = EntityManager.get().create()
            camera = eng.createCamera(camEntity).apply {
                lookAt(0.0, 0.25, 5.5, 0.0, 0.05, 0.0, 0.0, 1.0, 0.0)
            }
            view?.camera = camera

            setupLighting(eng)
            setupPlayableTetrominoCharacters(eng)

            uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
                isOpaque = false
                renderCallback = object : UiHelper.RendererCallback {
                    override fun onNativeWindowChanged(surface: Surface) {
                        swapChain?.let { eng.destroySwapChain(it) }
                        swapChain = eng.createSwapChain(surface, com.google.android.filament.SwapChainFlags.CONFIG_TRANSPARENT)
                    }

                    override fun onDetachedFromSurface() {
                        swapChain?.let {
                            eng.destroySwapChain(it)
                            swapChain = null
                        }
                    }

                    override fun onResized(width: Int, height: Int) {
                        if (width <= 0 || height <= 0) return
                        view?.viewport = Viewport(0, 0, width, height)
                        val aspect = width.toDouble() / height.toDouble()
                        camera?.setProjection(38.0, aspect, 0.1, 30.0, Camera.Fov.VERTICAL)
                    }
                }
                attachTo(textureView)
            }

            isRunning = true
            choreographer.postFrameCallback(frameCallback)
        } catch (_: Throwable) {
            isRunning = false
        }
    }

    private fun setupLighting(eng: Engine) {
        sunLightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.97f, 0.90f)
            .intensity(115_000.0f)
            .direction(-0.45f, -0.75f, -0.55f)
            .castShadows(false)
            .build(eng, sunLightEntity)
        scene?.addEntity(sunLightEntity)

        rimLightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.55f, 0.88f)
            .intensity(58_000.0f)
            .direction(0.65f, 0.45f, 0.6f)
            .castShadows(false)
            .build(eng, rimLightEntity)
        scene?.addEntity(rimLightEntity)

        val shBands = floatArrayOf(
            0.78f, 0.72f, 0.88f,
            0.12f, 0.15f, 0.22f,
            0.22f, 0.18f, 0.28f,
            0.10f, 0.08f, 0.14f,
            0.05f, 0.05f, 0.08f,
            0.05f, 0.05f, 0.08f,
            0.04f, 0.04f, 0.06f,
            0.04f, 0.04f, 0.06f,
            0.04f, 0.04f, 0.06f
        )
        indirectLight = IndirectLight.Builder()
            .irradiance(3, shBands)
            .intensity(36_000.0f)
            .build(eng)
        scene?.indirectLight = indirectLight
    }

    private fun setupPlayableTetrominoCharacters(eng: Engine) {
        val payloadBytes = cachedMaterialPayload ?: run {
            MaterialBuilder.init()
            val pkg = MaterialBuilder()
                .platform(MaterialBuilder.Platform.MOBILE)
                .name("BeveledToyGemPbr")
                .shading(MaterialBuilder.Shading.LIT)
                .doubleSided(true)
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "metallicVal")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "roughnessVal")
                .material(
                    """
                    void material(inout MaterialInputs material) {
                        prepareMaterial(material);
                        material.baseColor.rgb = materialParams.baseColor;
                        material.metallic = materialParams.metallicVal;
                        material.roughness = materialParams.roughnessVal;
                        material.clearCoat = 1.0;
                        material.clearCoatRoughness = 0.04;
                    }
                    """.trimIndent()
                )
                .build()

            val bytes = if (pkg.isValid) {
                val buf = pkg.buffer
                ByteArray(buf.remaining()).also { arr ->
                    buf.get(arr)
                }
            } else {
                null
            }
            MaterialBuilder.shutdown()
            cachedMaterialPayload = bytes
            bytes
        }

        if (payloadBytes != null) {
            val directBuf = ByteBuffer.allocateDirect(payloadBytes.size).order(ByteOrder.nativeOrder())
            directBuf.put(payloadBytes)
            directBuf.flip()
            pbrMaterial = Material.Builder().payload(directBuf, directBuf.remaining()).build(eng)
        }

        val mat = pbrMaterial ?: return

        val (vb, ib, count) = buildBeveledToyBlockBuffers(eng)
        vertexBuffer = vb
        indexBuffer = ib
        indexCount = count

        fun addBlock(
            groupId: Int,
            lx: Float,
            ly: Float,
            lz: Float,
            sx: Float = 1f,
            sy: Float = 1f,
            sz: Float = 1f,
            role: Int,
            r: Float,
            g: Float,
            b: Float,
            metallic: Float = 0.14f,
            roughness: Float = 0.12f
        ) {
            val mi = mat.createInstance().apply {
                setParameter("baseColor", Colors.RgbType.SRGB, r, g, b)
                setParameter("metallicVal", metallic)
                setParameter("roughnessVal", roughness)
            }
            val entity = EntityManager.get().create()
            RenderableManager.Builder(1)
                .boundingBox(FilamentBox(0f, 0f, 0f, 0.7f, 0.7f, 0.7f))
                .material(0, mi)
                .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vb, ib, 0, count)
                .build(eng, entity)
            scene?.addEntity(entity)
            nodes.add(SubBlockNode(entity, mi, groupId, lx, ly, lz, sx, sy, sz, role))
        }

        val step = 0.34f

        // --- GROUP 0: Smiling Golden L-Tetromino Mascot (Center Front) ---
        // Compact 3-tall + 2-wide L-tetromino with cute eyes, rosy cheeks & smile centered on the corner block!
        val lCoords = arrayOf(
            floatArrayOf(-0.5f * step, 0.7f * step, 0f),
            floatArrayOf(-0.5f * step, -0.3f * step, 0f),
            floatArrayOf(0.5f * step, -0.3f * step, 0f)
        )
        for (c in lCoords) {
            addBlock(0, c[0], c[1], c[2], 0.35f, 0.35f, 0.35f, 0, goldRgb[0], goldRgb[1], goldRgb[2])
        }
        // Cute 3D Eyes, Rosy Cheeks & Smile centered across the bottom of the Golden L-Mascot
        addBlock(0, -0.14f, -0.06f, 0.19f, 0.075f, 0.095f, 0.05f, 4, 0.10f, 0.05f, 0.14f, 0.05f, 0.05f)
        addBlock(0, 0.14f, -0.06f, 0.19f, 0.075f, 0.095f, 0.05f, 4, 0.10f, 0.05f, 0.14f, 0.05f, 0.05f)
        addBlock(0, -0.24f, -0.15f, 0.18f, 0.075f, 0.045f, 0.04f, 5, 1.0f, 0.35f, 0.58f, 0.10f, 0.20f)
        addBlock(0, 0.24f, -0.15f, 0.18f, 0.075f, 0.045f, 0.04f, 5, 1.0f, 0.35f, 0.58f, 0.10f, 0.20f)
        addBlock(0, 0.0f, -0.17f, 0.19f, 0.12f, 0.038f, 0.04f, 4, 0.12f, 0.05f, 0.15f, 0.05f, 0.08f)

        // --- GROUP 1: Playful Emerald T-Tetromino (Left Side) ---
        val tCoords = arrayOf(
            floatArrayOf(-step, 0f, 0f),
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(step, 0f, 0f),
            floatArrayOf(0f, step, 0f)
        )
        for (c in tCoords) {
            addBlock(1, c[0], c[1], c[2], 0.34f, 0.34f, 0.34f, 1, accentRgb[0], accentRgb[1], accentRgb[2])
        }

        // --- GROUP 2: Cyan L-Tetromino (Right Side) ---
        val zCoords = arrayOf(
            floatArrayOf(-0.5f * step, 0.5f * step, 0f),
            floatArrayOf(0.5f * step, 0.5f * step, 0f),
            floatArrayOf(0.5f * step, -0.5f * step, 0f)
        )
        for (c in zCoords) {
            addBlock(2, c[0], c[1], c[2], 0.34f, 0.34f, 0.34f, 2, 0.06f, 0.78f, 0.96f)
        }

        // --- GROUP 3: Floating 3D Ruby-Pink Heart Sculpture (Top Center) ---
        val hStep = 0.20f
        val heartCoords = arrayOf(
            floatArrayOf(0f, -hStep, 0f),
            floatArrayOf(-hStep, 0f, 0f),
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(hStep, 0f, 0f),
            floatArrayOf(-0.68f * hStep, hStep * 0.88f, 0f),
            floatArrayOf(0.68f * hStep, hStep * 0.88f, 0f)
        )
        for (c in heartCoords) {
            addBlock(3, c[0], c[1], c[2], 0.22f, 0.22f, 0.22f, 3, 0.98f, 0.18f, 0.48f, 0.22f, 0.08f)
        }
    }

    private fun renderFrame(frameTimeNanos: Long) {
        val eng = engine ?: return
        val r = renderer ?: return
        val v = view ?: return
        val sc = swapChain ?: return
        if (uiHelper?.isReadyToRender != true) return

        val t = ((frameTimeNanos - startNanos) / 1_000_000_000.0).toFloat()
        val pokeAge = if (lastPokeNanos > 0L) ((frameTimeNanos - lastPokeNanos) / 1_000_000_000.0).toFloat().coerceAtLeast(0f) else 10f
        val pokeImpulse = if (pokeAge < 1.8f) exp(-2.6f * pokeAge) * sin(pokeAge * 15f) else 0f
        val flipAngle = if (pokeAge < 1.1f && formationMode == 1) (pokeAge / 1.1f) * 360f else 0f

        val speedMul = if (boostSpin) 2.0f else 1.0f
        val globalYaw = userYaw + sin(t * 0.85f * speedMul) * 18f
        val globalPitch = 10f + userPitch + cos(t * 1.1f) * 5f

        val groupCenters = when (formationMode) {
            1 -> arrayOf( // Backflip & High Jump!
                floatArrayOf(0.05f, -0.22f + abs(pokeImpulse) * 0.55f, 0.35f),
                floatArrayOf(-0.88f, -0.05f + sin(t * 4f) * 0.22f, -0.15f),
                floatArrayOf(0.88f, -0.05f + cos(t * 4f) * 0.22f, -0.15f),
                floatArrayOf(0.0f, 0.72f + abs(pokeImpulse) * 0.30f, 0.20f)
            )
            2 -> arrayOf( // Orbiting Carousel Dance
                floatArrayOf(sin(t * 2.2f) * 0.35f, -0.20f + sin(t * 4.4f) * 0.12f, cos(t * 2.2f) * 0.35f),
                floatArrayOf(-0.85f + cos(t * 2.5f) * 0.18f, 0.02f + sin(t * 3.5f) * 0.18f, -0.1f),
                floatArrayOf(0.85f - cos(t * 2.5f) * 0.18f, 0.02f - sin(t * 3.5f) * 0.18f, -0.1f),
                floatArrayOf(0.0f, 0.72f + sin(t * 3.8f) * 0.12f, 0.25f)
            )
            3 -> arrayOf( // Puzzle Snap-Together Castle Formation
                floatArrayOf(-0.18f, -0.24f, 0.22f),
                floatArrayOf(-0.62f, 0.10f, 0.05f),
                floatArrayOf(0.58f, 0.05f, 0.05f),
                floatArrayOf(0.0f, 0.68f, 0.25f)
            )
            else -> arrayOf( // Cheerful Interactive Family Stage
                floatArrayOf(0.04f, -0.22f + sin(t * 2.6f) * 0.09f, 0.32f),
                floatArrayOf(-0.86f, 0.02f + sin(t * 2.2f + 1.2f) * 0.11f, -0.12f),
                floatArrayOf(0.86f, 0.04f + sin(t * 2.4f + 2.4f) * 0.11f, -0.12f),
                floatArrayOf(0.0f, 0.70f + sin(t * 3.2f) * 0.10f, 0.22f)
            )
        }

        val tm = eng.transformManager
        for (node in nodes) {
            val inst = tm.getInstance(node.entity)
            if (inst == 0) continue

            val g = node.groupId
            val center = groupCenters[g]

            val groupYaw = when (g) {
                0 -> globalYaw + sin(t * 1.8f) * 12f
                1 -> globalYaw + 22f + sin(t * 2.1f + 1f) * 24f + t * 28f
                2 -> globalYaw - 22f + cos(t * 2.0f + 2f) * 24f - t * 28f
                else -> t * 55f + userYaw
            }
            val groupPitch = when (g) {
                0 -> globalPitch + flipAngle + pokeImpulse * 20f
                3 -> 12f + sin(t * 2.8f) * 10f
                else -> globalPitch + flipAngle * 0.5f
            }

            // Jelly squash-and-stretch factor when poked
            val jelly = 1f + pokeImpulse * 0.18f

            val m = buildChildTransformMatrix(
                groupTx = center[0],
                groupTy = center[1],
                groupTz = center[2],
                groupYawDeg = groupYaw,
                groupPitchDeg = groupPitch,
                localX = node.localX * jelly,
                localY = node.localY / jelly.coerceAtLeast(0.82f),
                localZ = node.localZ,
                scaleX = node.scaleX * jelly,
                scaleY = node.scaleY / jelly.coerceAtLeast(0.82f),
                scaleZ = node.scaleZ
            )
            tm.setTransform(inst, m)
        }

        if (r.beginFrame(sc, frameTimeNanos)) {
            r.render(v)
            r.endFrame()
        }
    }

    fun destroy() {
        isRunning = false
        choreographer.removeFrameCallback(frameCallback)
        try {
            uiHelper?.detach()
            val eng = engine ?: return
            for (n in nodes) {
                scene?.removeEntity(n.entity)
                eng.destroyEntity(n.entity)
                EntityManager.get().destroy(n.entity)
                eng.destroyMaterialInstance(n.matInstance)
            }
            nodes.clear()
            pbrMaterial?.let { eng.destroyMaterial(it) }
            vertexBuffer?.let { eng.destroyVertexBuffer(it) }
            indexBuffer?.let { eng.destroyIndexBuffer(it) }
            indirectLight?.let { eng.destroyIndirectLight(it) }
            if (sunLightEntity != 0) {
                scene?.removeEntity(sunLightEntity)
                eng.destroyEntity(sunLightEntity)
            }
            if (rimLightEntity != 0) {
                scene?.removeEntity(rimLightEntity)
                eng.destroyEntity(rimLightEntity)
            }
            camera?.let {
                val cEnt = it.entity
                eng.destroyCameraComponent(cEnt)
                EntityManager.get().destroy(cEnt)
            }
            swapChain?.let { eng.destroySwapChain(it) }
            renderer?.let { eng.destroyRenderer(it) }
            view?.let { eng.destroyView(it) }
            scene?.let { eng.destroyScene(it) }
            eng.destroy()
            engine = null
        } catch (_: Throwable) {
        }
    }

    /**
     * Builds a smooth 3D Chamfered / Rounded Toy Block mesh using a subdivided cube
     * morphed toward a superellipsoid (rounded bevel edges & corners) with exact surface tangents.
     */
    private fun buildBeveledToyBlockBuffers(eng: Engine): Triple<VertexBuffer, IndexBuffer, Int> {
        val grid = 4 // 4x4 quads per face -> smooth rounded chamfered edges!
        val quadsPerFace = grid * grid
        val totalQuads = 6 * quadsPerFace
        val totalVertices = totalQuads * 4
        val totalIndices = totalQuads * 6

        val floatsPerVertex = 7
        val vertexData = FloatArray(totalVertices * floatsPerVertex)
        val indices = ShortArray(totalIndices)

        var vOffset = 0
        var iOffset = 0
        var vCount = 0

        fun evalRoundedBlockPos(u: Float, v: Float, face: Int): FloatArray {
            val raw = when (face) {
                0 -> floatArrayOf(u, v, 1f)   // +Z
                1 -> floatArrayOf(-u, v, -1f) // -Z
                2 -> floatArrayOf(1f, v, -u)  // +X
                3 -> floatArrayOf(-1f, v, u)  // -X
                4 -> floatArrayOf(u, 1f, -v)  // +Y
                else -> floatArrayOf(u, -1f, v) // -Y
            }
            // Blend cube with rounded superellipsoid so edges & corners are smoothly beveled
            val len = sqrt(raw[0] * raw[0] + raw[1] * raw[1] + raw[2] * raw[2]).coerceAtLeast(1e-4f)
            val sx = raw[0] / len
            val sy = raw[1] / len
            val sz = raw[2] / len
            val bevelBlend = 0.26f
            val px = (raw[0] * (1f - bevelBlend) + sx * bevelBlend) * 0.5f
            val py = (raw[1] * (1f - bevelBlend) + sy * bevelBlend) * 0.5f
            val pz = (raw[2] * (1f - bevelBlend) + sz * bevelBlend) * 0.5f
            return floatArrayOf(px, py, pz)
        }

        val q = (1.0 / sqrt(2.0)).toFloat()
        val faceQuats = arrayOf(
            floatArrayOf(0f, 0f, 0f, 1f),
            floatArrayOf(0f, 1f, 0f, 0f),
            floatArrayOf(0f, q, 0f, q),
            floatArrayOf(0f, -q, 0f, q),
            floatArrayOf(-q, 0f, 0f, q),
            floatArrayOf(q, 0f, 0f, q)
        )

        for (face in 0 until 6) {
            val quat = faceQuats[face]
            for (gy in 0 until grid) {
                val v0 = -1f + (2f * gy) / grid
                val v1 = -1f + (2f * (gy + 1)) / grid
                for (gx in 0 until grid) {
                    val u0 = -1f + (2f * gx) / grid
                    val u1 = -1f + (2f * (gx + 1)) / grid

                    val p0 = evalRoundedBlockPos(u0, v0, face)
                    val p1 = evalRoundedBlockPos(u1, v0, face)
                    val p2 = evalRoundedBlockPos(u1, v1, face)
                    val p3 = evalRoundedBlockPos(u0, v1, face)

                    val corners = arrayOf(p0, p1, p2, p3)
                    val baseVertex = vCount.toShort()
                    for (c in corners) {
                        vertexData[vOffset++] = c[0]
                        vertexData[vOffset++] = c[1]
                        vertexData[vOffset++] = c[2]
                        vertexData[vOffset++] = quat[0]
                        vertexData[vOffset++] = quat[1]
                        vertexData[vOffset++] = quat[2]
                        vertexData[vOffset++] = quat[3]
                        vCount++
                    }

                    indices[iOffset++] = baseVertex
                    indices[iOffset++] = (baseVertex + 1).toShort()
                    indices[iOffset++] = (baseVertex + 2).toShort()
                    indices[iOffset++] = baseVertex
                    indices[iOffset++] = (baseVertex + 2).toShort()
                    indices[iOffset++] = (baseVertex + 3).toShort()
                }
            }
        }

        val strideBytes = floatsPerVertex * 4
        val vb = VertexBuffer.Builder()
            .vertexCount(totalVertices)
            .bufferCount(1)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, strideBytes)
            .attribute(VertexBuffer.VertexAttribute.TANGENTS, 0, VertexBuffer.AttributeType.FLOAT4, 12, strideBytes)
            .build(eng)

        val vBuf = ByteBuffer.allocateDirect(vertexData.size * 4).order(ByteOrder.nativeOrder())
        vBuf.asFloatBuffer().put(vertexData)
        vBuf.position(0)
        vb.setBufferAt(eng, 0, vBuf)

        val ib = IndexBuffer.Builder()
            .indexCount(totalIndices)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(eng)

        val iBuf = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder())
        iBuf.asShortBuffer().put(indices)
        iBuf.position(0)
        ib.setBuffer(eng, iBuf)

        return Triple(vb, ib, totalIndices)
    }

    private fun buildChildTransformMatrix(
        groupTx: Float,
        groupTy: Float,
        groupTz: Float,
        groupYawDeg: Float,
        groupPitchDeg: Float,
        localX: Float,
        localY: Float,
        localZ: Float,
        scaleX: Float,
        scaleY: Float,
        scaleZ: Float
    ): FloatArray {
        val ry = groupYawDeg * (PI / 180.0).toFloat()
        val rx = groupPitchDeg * (PI / 180.0).toFloat()
        val cy = cos(ry)
        val sy = sin(ry)
        val cx = cos(rx)
        val sx = sin(rx)

        // Rotation matrix R = RotY(ry) * RotX(rx) (column-major)
        val r00 = cy
        val r10 = 0f
        val r20 = -sy

        val r01 = sy * sx
        val r11 = cx
        val r21 = cy * sx

        val r02 = sy * cx
        val r12 = -sx
        val r22 = cy * cx

        // Rotate local offset by group rotation
        val wx = r00 * localX + r01 * localY + r02 * localZ + groupTx
        val wy = r10 * localX + r11 * localY + r12 * localZ + groupTy
        val wz = r20 * localX + r21 * localY + r22 * localZ + groupTz

        return floatArrayOf(
            r00 * scaleX, r10 * scaleX, r20 * scaleX, 0f,
            r01 * scaleY, r11 * scaleY, r21 * scaleY, 0f,
            r02 * scaleZ, r12 * scaleZ, r22 * scaleZ, 0f,
            wx, wy, wz, 1f
        )
    }
}
