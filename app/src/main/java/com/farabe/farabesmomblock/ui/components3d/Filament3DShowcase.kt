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
import kotlinx.coroutines.delay
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Playful, interactive Real-Time Google Filament 3D PBR Showcase!
 * Renders 4 candy-gem cubes + 2 orbiting star-gems that dance, bounce,
 * cycle funny formations on tap, and spin smoothly with drag momentum.
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
            if (kotlin.math.abs(yawVelocity) > 0.05f || kotlin.math.abs(pitchVelocity) > 0.05f) {
                userYawOffset += yawVelocity
                userPitchOffset = (userPitchOffset + pitchVelocity).coerceIn(-45f, 45f)
                yawVelocity *= 0.93f
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
                        "হিহি! সুড়সুড়ি লাগছে আম্মু! 😄 (+5🪙)",
                        "ওয়াও আম্মু! ড্যান্স টাইম! 💃 (+5🪙)",
                        "চলো ব্লক মিলাই আম্মু! 🧩 (+5🪙)",
                        "ভালোবাসা ও দোয়া আম্মু! ❤️ (+5🪙)"
                    )
                    onBlockPoked(pokeCount, reactions[(pokeCount - 1) % reactions.size])
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    userYawOffset += dragAmount.x * 0.55f
                    userPitchOffset = (userPitchOffset + dragAmount.y * 0.40f).coerceIn(-45f, 45f)
                    yawVelocity = dragAmount.x * 0.42f
                    pitchVelocity = dragAmount.y * 0.25f
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
                    // Fallback handled gracefully
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

    private var engine: Engine? = null
    private var renderer: Renderer? = null
    private var scene: Scene? = null
    private var view: View? = null
    private var camera: Camera? = null
    private var swapChain: SwapChain? = null
    private var uiHelper: UiHelper? = null

    private var pbrMaterial: Material? = null
    private val matInstances = mutableListOf<MaterialInstance>()
    private val renderableEntities = mutableListOf<Int>()
    private var vertexBuffer: VertexBuffer? = null
    private var indexBuffer: IndexBuffer? = null
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
        if (matInstances.size >= 4) {
            try {
                matInstances[0].setParameter("baseColor", Colors.RgbType.SRGB, accentRgb[0], accentRgb[1], accentRgb[2])
                matInstances[3].setParameter("baseColor", Colors.RgbType.SRGB, goldRgb[0], goldRgb[1], goldRgb[2])
            } catch (_: Throwable) {}
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
                lookAt(0.0, 0.4, 5.2, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
            }
            view?.camera = camera

            setupLighting(eng)
            setupPbrCubes(eng)

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
            // Safe fallback if headless or unsupported GL
            isRunning = false
        }
    }

    private fun setupLighting(eng: Engine) {
        // Warm golden sun key light
        sunLightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.95f, 0.85f)
            .intensity(95_000.0f)
            .direction(-0.45f, -0.75f, -0.55f)
            .castShadows(false)
            .build(eng, sunLightEntity)
        scene?.addEntity(sunLightEntity)

        // Twilight violet/pink fill light
        rimLightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.85f, 0.50f, 1.0f)
            .intensity(48_000.0f)
            .direction(0.65f, 0.4f, -0.4f)
            .build(eng, rimLightEntity)
        scene?.addEntity(rimLightEntity)

        // Spherical harmonics IBL for glossy clearcoat reflections
        val shBands = floatArrayOf(
            0.35f, 0.30f, 0.45f,
            0.22f, 0.20f, 0.25f,
            0.10f, 0.12f, 0.18f,
            -0.12f, -0.08f, -0.10f,
            0.05f, 0.05f, 0.08f,
            0.04f, 0.03f, 0.06f,
            0.08f, 0.07f, 0.05f,
            -0.04f, -0.04f, -0.02f,
            0.06f, 0.04f, 0.03f
        )
        indirectLight = IndirectLight.Builder()
            .irradiance(3, shBands)
            .intensity(32_000.0f)
            .build(eng)
        scene?.indirectLight = indirectLight
    }

    private fun setupPbrCubes(eng: Engine) {
        val payloadBytes = cachedMaterialPayload ?: run {
            MaterialBuilder.init()
            val pkg = MaterialBuilder()
                .platform(MaterialBuilder.Platform.MOBILE)
                .name("CandyGemPbr")
                .shading(MaterialBuilder.Shading.LIT)
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
                        material.clearCoatRoughness = 0.06;
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

        // Create crisp 3D cube geometry with exact quaternion tangents per face
        val (vb, ib) = buildCubeBuffers(eng)
        vertexBuffer = vb
        indexBuffer = ib

        // 4 main hero gem blocks + 2 orbiting golden/rose mini star-gems
        val gemSpecs = listOf(
            GemColorSpec(accentRgb[0], accentRgb[1], accentRgb[2], 0.12f, 0.15f), // Play emerald / accent
            GemColorSpec(0.95f, 0.25f, 0.58f, 0.15f, 0.14f),                     // Shapla magenta
            GemColorSpec(0.05f, 0.78f, 0.92f, 0.18f, 0.12f),                     // Jamdani cyan
            GemColorSpec(goldRgb[0], goldRgb[1], goldRgb[2], 0.16f, 0.14f),      // Gold glossy crystal gem
            GemColorSpec(1.0f, 0.85f, 0.35f, 0.25f, 0.10f),                      // Orbiting mini gold star
            GemColorSpec(1.0f, 0.45f, 0.75f, 0.20f, 0.12f)                       // Orbiting mini rose heart-gem
        )

        for (spec in gemSpecs) {
            val mi = mat.createInstance().apply {
                setParameter("baseColor", Colors.RgbType.SRGB, spec.r, spec.g, spec.b)
                setParameter("metallicVal", spec.metallic)
                setParameter("roughnessVal", spec.roughness)
            }
            matInstances.add(mi)

            val entity = EntityManager.get().create()
            RenderableManager.Builder(1)
                .boundingBox(FilamentBox(0f, 0f, 0f, 0.6f, 0.6f, 0.6f))
                .material(0, mi)
                .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vb, ib, 0, 36)
                .build(eng, entity)
            scene?.addEntity(entity)
            renderableEntities.add(entity)
        }
    }

    private fun renderFrame(frameTimeNanos: Long) {
        val eng = engine ?: return
        val r = renderer ?: return
        val v = view ?: return
        val sc = swapChain ?: return
        if (uiHelper?.isReadyToRender != true) return

        val elapsedSec = ((frameTimeNanos - startNanos) / 1_000_000_000.0).toFloat()
        val pokeAge = if (lastPokeNanos > 0L) ((frameTimeNanos - lastPokeNanos) / 1_000_000_000.0).toFloat().coerceAtLeast(0f) else 10f
        val pokeImpulse = if (pokeAge < 1.8f) exp(-2.8f * pokeAge) * sin(pokeAge * 14f) else 0f
        val pokeSpinBoost = if (pokeAge < 1.5f) (1f - pokeAge / 1.5f) * 320f else 0f

        val speedMul = if (boostSpin) 2.5f else 1.0f
        val baseAngle = elapsedSec * 42f * speedMul + userYaw + pokeSpinBoost
        val pitchAngle = 18f + sin(elapsedSec * 1.4f) * 8f + userPitch + pokeImpulse * 18f

        val tm = eng.transformManager
        val wavePhase = elapsedSec * 3.1f
        val offsets = when (formationMode) {
            1 -> arrayOf( // Samba Dance Wave
                floatArrayOf(-0.82f, 0.1f + sin(wavePhase) * 0.28f, 0.0f),
                floatArrayOf(-0.26f, 0.1f + sin(wavePhase + 1.0f) * 0.28f, 0.12f),
                floatArrayOf(0.28f, 0.1f + sin(wavePhase + 2.0f) * 0.28f, 0.0f),
                floatArrayOf(0.82f, 0.1f + sin(wavePhase + 3.0f) * 0.28f, 0.12f)
            )
            2 -> Array(4) { idx -> // Merry-Go-Round Carousel Orbit
                val a = elapsedSec * 1.8f + idx * (PI.toFloat() * 0.5f)
                floatArrayOf(cos(a) * 0.72f, 0.12f + sin(elapsedSec * 3f + idx) * 0.14f, sin(a) * 0.55f)
            }
            3 -> arrayOf( // Diamond / Heart Jump
                floatArrayOf(-0.62f, 0.16f, 0.0f),
                floatArrayOf(0.62f, 0.16f, 0.0f),
                floatArrayOf(0.0f, -0.45f, 0.1f),
                floatArrayOf(0.0f, 0.78f, 0.1f)
            )
            else -> arrayOf( // Playful Pyramid Stack
                floatArrayOf(-0.66f, 0.16f, 0.0f),
                floatArrayOf(0.0f, 0.16f, 0.0f),
                floatArrayOf(0.66f, 0.16f, 0.0f),
                floatArrayOf(0.0f, 0.84f, 0.1f)
            )
        }

        for (i in renderableEntities.indices) {
            val inst = tm.getInstance(renderableEntities[i])
            if (inst == 0) continue

            if (i < 4) {
                val bob = sin(elapsedSec * 2.4f + i * 0.9f) * 0.08f
                val jumpHop = kotlin.math.abs(pokeImpulse) * (0.28f + i * 0.06f)
                val pulseScale = (if (i == 3) 0.52f else 0.56f) * (1f + pokeImpulse * 0.16f)
                val localYaw = baseAngle + i * 18f + sin(elapsedSec * 2f + i) * 10f
                val m = buildTransformMatrix(
                    tx = offsets[i][0],
                    ty = offsets[i][1] + bob + jumpHop - 0.28f,
                    tz = offsets[i][2],
                    scale = pulseScale,
                    yawDeg = localYaw,
                    pitchDeg = pitchAngle + sin(elapsedSec * 2.2f + i) * 6f
                )
                tm.setTransform(inst, m)
            } else {
                // Orbiting mini companion star-gems
                val orbitIdx = i - 4
                val oa = elapsedSec * (2.6f + orbitIdx * 0.7f) + orbitIdx * PI.toFloat()
                val m = buildTransformMatrix(
                    tx = cos(oa) * 1.05f,
                    ty = sin(elapsedSec * 3.2f + orbitIdx) * 0.45f + 0.15f + kotlin.math.abs(pokeImpulse) * 0.35f,
                    tz = sin(oa) * 0.75f,
                    scale = 0.22f + kotlin.math.abs(pokeImpulse) * 0.08f,
                    yawDeg = baseAngle * 2.2f,
                    pitchDeg = pitchAngle * 1.4f
                )
                tm.setTransform(inst, m)
            }
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
            for (e in renderableEntities) {
                scene?.removeEntity(e)
                eng.destroyEntity(e)
                EntityManager.get().destroy(e)
            }
            renderableEntities.clear()
            matInstances.forEach { eng.destroyMaterialInstance(it) }
            matInstances.clear()
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
            // Safe teardown
        }
    }

    private data class GemColorSpec(val r: Float, val g: Float, val b: Float, val metallic: Float, val roughness: Float)

    private fun buildCubeBuffers(eng: Engine): Pair<VertexBuffer, IndexBuffer> {
        val s = 0.5f
        val q = (1.0 / sqrt(2.0)).toFloat()

        // 24 vertices (4 per face), each vertex: pos(3 floats) + tangent quaternion(4 floats)
        val faces = arrayOf(
            // +Z
            FaceSpec(floatArrayOf(-s, -s, s,  s, -s, s,  s, s, s,  -s, s, s), floatArrayOf(0f, 0f, 0f, 1f)),
            // -Z
            FaceSpec(floatArrayOf(s, -s, -s,  -s, -s, -s,  -s, s, -s,  s, s, -s), floatArrayOf(0f, 1f, 0f, 0f)),
            // +X
            FaceSpec(floatArrayOf(s, -s, s,  s, -s, -s,  s, s, -s,  s, s, s), floatArrayOf(0f, q, 0f, q)),
            // -X
            FaceSpec(floatArrayOf(-s, -s, -s,  -s, -s, s,  -s, s, s,  -s, s, -s), floatArrayOf(0f, -q, 0f, q)),
            // +Y
            FaceSpec(floatArrayOf(-s, s, s,  s, s, s,  s, s, -s,  -s, s, -s), floatArrayOf(-q, 0f, 0f, q)),
            // -Y
            FaceSpec(floatArrayOf(-s, -s, -s,  s, -s, -s,  s, -s, s,  -s, -s, s), floatArrayOf(q, 0f, 0f, q))
        )

        val floatsPerVertex = 7
        val vertexData = FloatArray(24 * floatsPerVertex)
        var offset = 0
        for (face in faces) {
            for (v in 0 until 4) {
                vertexData[offset++] = face.positions[v * 3]
                vertexData[offset++] = face.positions[v * 3 + 1]
                vertexData[offset++] = face.positions[v * 3 + 2]
                vertexData[offset++] = face.quat[0]
                vertexData[offset++] = face.quat[1]
                vertexData[offset++] = face.quat[2]
                vertexData[offset++] = face.quat[3]
            }
        }

        val indices = ShortArray(36)
        var idxOff = 0
        for (f in 0 until 6) {
            val base = (f * 4).toShort()
            indices[idxOff++] = base
            indices[idxOff++] = (base + 1).toShort()
            indices[idxOff++] = (base + 2).toShort()
            indices[idxOff++] = base
            indices[idxOff++] = (base + 2).toShort()
            indices[idxOff++] = (base + 3).toShort()
        }

        val strideBytes = floatsPerVertex * 4
        val vb = VertexBuffer.Builder()
            .vertexCount(24)
            .bufferCount(1)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, strideBytes)
            .attribute(VertexBuffer.VertexAttribute.TANGENTS, 0, VertexBuffer.AttributeType.FLOAT4, 12, strideBytes)
            .build(eng)

        val vBuf = ByteBuffer.allocateDirect(vertexData.size * 4).order(ByteOrder.nativeOrder())
        vBuf.asFloatBuffer().put(vertexData)
        vBuf.position(0)
        vb.setBufferAt(eng, 0, vBuf)

        val ib = IndexBuffer.Builder()
            .indexCount(indices.size)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(eng)

        val iBuf = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder())
        iBuf.asShortBuffer().put(indices)
        iBuf.position(0)
        ib.setBuffer(eng, iBuf)

        return Pair(vb, ib)
    }

    private class FaceSpec(val positions: FloatArray, val quat: FloatArray)

    private fun buildTransformMatrix(
        tx: Float,
        ty: Float,
        tz: Float,
        scale: Float,
        yawDeg: Float,
        pitchDeg: Float
    ): FloatArray {
        val ry = yawDeg * (PI / 180.0).toFloat()
        val rx = pitchDeg * (PI / 180.0).toFloat()
        val cy = cos(ry)
        val sy = sin(ry)
        val cx = cos(rx)
        val sx = sin(rx)

        // Column-major 4x4 matrix: Translate * RotY * RotX * Scale
        return floatArrayOf(
            cy * scale,
            0f * scale,
            -sy * scale,
            0f,

            sy * sx * scale,
            cx * scale,
            cy * sx * scale,
            0f,

            sy * cx * scale,
            -sx * scale,
            cy * cx * scale,
            0f,

            tx,
            ty,
            tz,
            1f
        )
    }
}
