package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.farmledger.app.R
import com.farmledger.app.domain.usecase.FarmStageCapabilities
import com.farmledger.app.domain.usecase.FarmStageLogic
import kotlinx.coroutines.delay

/** Logical scene size matching docs/farm_scene_preview.png (5×3 of 32px tiles). */
private const val SceneW = 160f
private const val SceneH = 96f
private const val Tile = 32f
private const val HappyMs = 1_400L

/**
 * 單一農場場景層：依階段能力顯示荒地／草地邊緣／柵欄／小屋／寵物。
 * 純 UI — 不改動結算發獎規則。
 */
@Composable
fun FarmSceneLayer(
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    capabilities: FarmStageCapabilities = FarmStageLogic.capabilities(0),
    placedDecorIds: Set<String> = emptySet(),
    onPetTap: () -> Unit = {},
) {
    var happyUntilMs by remember { mutableLongStateOf(0L) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isHappy = nowMs < happyUntilMs

    LaunchedEffect(happyUntilMs) {
        if (happyUntilMs <= 0L) return@LaunchedEffect
        while (System.currentTimeMillis() < happyUntilMs) {
            nowMs = System.currentTimeMillis()
            delay(40)
        }
        nowMs = System.currentTimeMillis()
    }

    val grassBarren = ImageBitmap.imageResource(R.drawable.tile_grass_barren)
    val grassSprout = ImageBitmap.imageResource(R.drawable.tile_grass_sprout)
    val grassHome = ImageBitmap.imageResource(R.drawable.tile_grass_home)
    val grassThrive = ImageBitmap.imageResource(R.drawable.tile_grass_thrive)
    val dirt = ImageBitmap.imageResource(R.drawable.tile_dirt)
    val path = ImageBitmap.imageResource(R.drawable.tile_path)
    val edgeN = ImageBitmap.imageResource(R.drawable.tile_dirt_edge_n)
    val edgeW = ImageBitmap.imageResource(R.drawable.tile_dirt_edge_w)
    val edgeE = ImageBitmap.imageResource(R.drawable.tile_dirt_edge_e)
    val fenceHBmp = ImageBitmap.imageResource(R.drawable.fence_h)
    val fenceVBmp = ImageBitmap.imageResource(R.drawable.fence_v)
    val cornerNw = ImageBitmap.imageResource(R.drawable.fence_corner_nw)
    val cornerNe = ImageBitmap.imageResource(R.drawable.fence_corner_ne)
    val cornerSw = ImageBitmap.imageResource(R.drawable.fence_corner_sw)
    val cornerSe = ImageBitmap.imageResource(R.drawable.fence_corner_se)
    val hut = ImageBitmap.imageResource(R.drawable.building_hut)
    val hutRuin = ImageBitmap.imageResource(R.drawable.building_hut_ruin)
    val treeOak = ImageBitmap.imageResource(R.drawable.tree_oak)
    val treePine = ImageBitmap.imageResource(R.drawable.tree_pine)
    val treeShadow = ImageBitmap.imageResource(R.drawable.tree_shadow)
    val bush = ImageBitmap.imageResource(R.drawable.bush)
    val petIdle = ImageBitmap.imageResource(R.drawable.pet_idle)
    val petHappy = ImageBitmap.imageResource(R.drawable.pet_happy)
    val heart = ImageBitmap.imageResource(R.drawable.fx_heart)

    val stageGrass = when {
        capabilities.greenerDenser -> grassThrive
        capabilities.showHut -> grassHome
        capabilities.showGrassEdges -> grassSprout
        else -> grassBarren
    }

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .aspectRatio(SceneW / SceneH)
            .semantics { contentDescription = "農田場景・${capabilities.stage.nameZh}" }
    ) {
        val petSize = maxWidth * (Tile / SceneW)

        Canvas(Modifier.fillMaxSize()) {
            val sx = size.width / SceneW
            val sy = size.height / SceneH
            fun cell(tx: Int, ty: Int, bmp: ImageBitmap) {
                drawPixelTile(bmp, tx * Tile * sx, ty * Tile * sy, Tile * sx, Tile * sy)
            }
            fun decor(bmp: ImageBitmap, lx: Float, ly: Float, lw: Float, lh: Float) {
                drawPixelTile(bmp, lx * sx, ly * sy, lw * sx, lh * sy)
            }

            // Layer 0 — ground: stage grass differential (barren sparsest → thrive densest)
            if (capabilities.showGrassEdges || capabilities.greenerDenser) {
                for (ty in 0 until 3) {
                    for (tx in 0 until 5) {
                        cell(tx, ty, stageGrass)
                    }
                }
                if (capabilities.greenerDenser) {
                    // denser bushes feel
                    decor(bush, 4f, 4f, 20f, 16f)
                    decor(bush, 100f, 8f, 22f, 18f)
                }
            } else {
                // 荒地：dirt dominate + sparse barren grass corners
                for (ty in 0 until 3) {
                    for (tx in 0 until 5) {
                        val corner = (tx == 0 || tx == 4) && (ty == 0 || ty == 2)
                        cell(tx, ty, if (corner) stageGrass else dirt)
                    }
                }
            }
            // Path + dirt bed
            cell(2, 0, path)
            cell(2, 2, path)
            cell(1, 1, edgeW)
            cell(2, 1, dirt)
            cell(3, 1, edgeE)
            if (capabilities.showGrassEdges) {
                for (tx in 1..3) {
                    drawPixelTile(edgeN, tx * Tile * sx, 1 * Tile * sy, Tile * sx, Tile * sy)
                }
            }

            // Fence — 安家起
            if (capabilities.showFence) {
                drawPixelTile(cornerNw, 20f * sx, 18f * sy, Tile * sx, Tile * sy)
                drawPixelTile(fenceHBmp, 48f * sx, 18f * sy, Tile * sx, Tile * sy)
                drawPixelTile(fenceHBmp, 80f * sx, 18f * sy, Tile * sx, Tile * sy)
                drawPixelTile(cornerNe, 108f * sx, 18f * sy, Tile * sx, Tile * sy)
                drawPixelTile(fenceVBmp, 20f * sx, 34f * sy, Tile * sx, Tile * sy)
                drawPixelTile(fenceVBmp, 116f * sx, 34f * sy, Tile * sx, Tile * sy)
                drawPixelTile(cornerSw, 20f * sx, 50f * sy, Tile * sx, Tile * sy)
                drawPixelTile(fenceHBmp, 48f * sx, 54f * sy, Tile * sx, Tile * sy)
                drawPixelTile(fenceHBmp, 80f * sx, 54f * sy, Tile * sx, Tile * sy)
                drawPixelTile(cornerSe, 108f * sx, 50f * sy, Tile * sx, Tile * sy)
            }

            // Trees / hut — hut 安家起；旺場加樹木密度
            if (capabilities.smallExpansion || capabilities.showHut) {
                decor(treeShadow, -4f, 28f, 32f, 16f)
                decor(treePine, -4f, 0f, 32f, 40f)
            }
            if (capabilities.greenerDenser) {
                decor(treeShadow, 128f, 24f, 32f, 16f)
                decor(treeOak, 128f, -4f, 32f, 40f)
                decor(bush, 118f, 70f, 24f, 20f)
            }
            if (capabilities.showHut) {
                decor(hut, 2f, 48f, 48f, 48f)
            } else if (!capabilities.showGrassEdges) {
                // 荒地殘破屋 stub（純視覺，無獎勵）
                decor(hutRuin, 2f, 48f, 48f, 48f)
            }
            // Placed decor hint (scarecrow etc. as bush stand-in if placed)
            if (placedDecorIds.isNotEmpty() && capabilities.decorSlots > 0) {
                decor(bush, 70f, 68f, 24f, 20f)
            }
        }

        // Pets — 萌芽起 1 隻；21 日第二欄
        if (capabilities.animalSlots >= 1) {
            val petMod = Modifier
                .align(Alignment.TopStart)
                .offset(x = maxWidth * (64f / SceneW), y = maxHeight * (62f / SceneH))
                .size(petSize)
                .then(
                    if (interactive) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            happyUntilMs = System.currentTimeMillis() + HappyMs
                            nowMs = System.currentTimeMillis()
                            onPetTap()
                        }
                    } else Modifier
                )
            Image(
                bitmap = if (isHappy) petHappy else petIdle,
                contentDescription = "小芽",
                modifier = petMod,
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.None
            )
            if (isHappy) {
                Image(
                    bitmap = heart,
                    contentDescription = "心心",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = maxWidth * (92f / SceneW), y = maxHeight * (50f / SceneH))
                        .size(petSize),
                    contentScale = ContentScale.FillBounds,
                    filterQuality = FilterQuality.None
                )
            }
        }
        if (capabilities.animalSlots >= 2) {
            Image(
                bitmap = petIdle,
                contentDescription = "第二動物",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = maxWidth * (100f / SceneW), y = maxHeight * (66f / SceneH))
                    .size(petSize * 0.85f),
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.None
            )
        }
    }
}

private fun DrawScope.drawPixelTile(
    image: ImageBitmap,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
) {
    drawImage(
        image = image,
        dstOffset = IntOffset(x.toInt(), y.toInt()),
        dstSize = IntSize(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1)),
        filterQuality = FilterQuality.None
    )
}
