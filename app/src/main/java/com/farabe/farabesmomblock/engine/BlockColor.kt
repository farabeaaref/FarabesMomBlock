package com.farabe.farabesmomblock.engine

import androidx.compose.ui.graphics.Color

/**
 * Authentic nostalgic Bangladeshi heritage themes for each block type.
 * Inspired by traditional Bengali art: Nakshi Kantha, Rickshaw Art, Jamdani,
 * Shapla, Beli phool, Krishnachura, Terracotta, and Alpona.
 */
enum class BangladeshiMotif(val displayNameBengali: String, val englishName: String) {
    SHAPLA("লাল শাপলা", "Water Lily"),
    BELI("বেলি ফুল", "Jasmine Garland"),
    NAKSHI_KANTHA("নকশী কাঁথা", "Embroidered Star"),
    JAMDANI("জামদানি", "Royal Weave"),
    RICKSHAW_ART("রিকশা আর্ট", "Painted Folk Rose"),
    KRISHNACHURA("কৃষ্ণচূড়া", "Summer Blossom"),
    MATIR_PRODIP("মাটির প্রদীপ", "Terracotta Lamp"),
    ALPONA("আলপনা", "Festive Mandala"),
    DOEL("দোয়েল", "Magpie Robin")
}

enum class BlockColor(
    val displayName: String,
    val motif: BangladeshiMotif,
    val primaryHex: Long,
    val highlightHex: Long,
    val shadowHex: Long,
    val motifAccentHex: Long,
    val composeColor: Color
) {
    // 1. লাল শাপলা (Shapla - Iconic Vibrant Magenta/Rose Water Lily)
    SHAPLA_PINK(
        displayName = "Shapla Blossom",
        motif = BangladeshiMotif.SHAPLA,
        primaryHex = 0xFFEE2A7B,
        highlightHex = 0xFFFF6EAB,
        shadowHex = 0xFFB80053,
        motifAccentHex = 0xFFFFF0F5,
        composeColor = Color(0xFFEE2A7B)
    ),

    // 2. বেলি ফুল (Beli - Fresh Jasmine Leaf Lime Green)
    BELI_CREAM(
        displayName = "Beli Phool",
        motif = BangladeshiMotif.BELI,
        primaryHex = 0xFF52C41A,
        highlightHex = 0xFF8CE158,
        shadowHex = 0xFF2F8A07,
        motifAccentHex = 0xFFF6FFED,
        composeColor = Color(0xFF52C41A)
    ),

    // 3. নকশী কাঁথা (Nakshi Kantha - Embroidered Golden Sunshine Needlework)
    KANTHA_GOLD(
        displayName = "Nakshi Kantha",
        motif = BangladeshiMotif.NAKSHI_KANTHA,
        primaryHex = 0xFFFFBA08,
        highlightHex = 0xFFFFDE59,
        shadowHex = 0xFFC88200,
        motifAccentHex = 0xFFFFFBE6,
        composeColor = Color(0xFFFFBA08)
    ),

    // 4. জামদানি (Jamdani - Heritage Royal Vivid Cyan / Teal Weave)
    JAMDANI_TEAL(
        displayName = "Jamdani Weave",
        motif = BangladeshiMotif.JAMDANI,
        primaryHex = 0xFF00B4D8,
        highlightHex = 0xFF5DE0E6,
        shadowHex = 0xFF007799,
        motifAccentHex = 0xFFE6F7FF,
        composeColor = Color(0xFF00B4D8)
    ),

    // 5. রিকশা আর্ট (Rickshaw Art - Vivid Painted Folk Mango Orange)
    RICKSHAW_ROSE(
        displayName = "Rickshaw Art",
        motif = BangladeshiMotif.RICKSHAW_ART,
        primaryHex = 0xFFFF7700,
        highlightHex = 0xFFFFA834,
        shadowHex = 0xFFC44A00,
        motifAccentHex = 0xFFFFF7E6,
        composeColor = Color(0xFFFF7700)
    ),

    // 6. কৃষ্ণচূড়া (Krishnachura - Fiery Bengal Summer Candy Scarlet Red)
    KRISHNACHURA_RED(
        displayName = "Krishnachura",
        motif = BangladeshiMotif.KRISHNACHURA,
        primaryHex = 0xFFFF2E4C,
        highlightHex = 0xFFFF6E84,
        shadowHex = 0xFFC2001F,
        motifAccentHex = 0xFFFFF1F0,
        composeColor = Color(0xFFFF2E4C)
    ),

    // 7. মাটির প্রদীপ (Matir Prodip - Traditional Terracotta Clay & Flame)
    TERRACOTTA_CLAY(
        displayName = "Matir Prodip",
        motif = BangladeshiMotif.MATIR_PRODIP,
        primaryHex = 0xFFD8502B,
        highlightHex = 0xFFFF805E,
        shadowHex = 0xFF962808,
        motifAccentHex = 0xFFFFD54F,
        composeColor = Color(0xFFD8502B)
    ),

    // 8. আলপনা (Alpona - Festive Orchid & Purple Folk Mandala)
    ALPONA_PURPLE(
        displayName = "Alpona Mandala",
        motif = BangladeshiMotif.ALPONA,
        primaryHex = 0xFF9D4EDD,
        highlightHex = 0xFFC77DFF,
        shadowHex = 0xFF6A1B9A,
        motifAccentHex = 0xFFF9F0FF,
        composeColor = Color(0xFF9D4EDD)
    ),

    // 9. দোয়েল (Doel - Brilliant Electric Sapphire Blue)
    DOEL_SAPPHIRE(
        displayName = "Doel Feather",
        motif = BangladeshiMotif.DOEL,
        primaryHex = 0xFF1E60FF,
        highlightHex = 0xFF699BFF,
        shadowHex = 0xFF0B3BB8,
        motifAccentHex = 0xFFE6F7FF,
        composeColor = Color(0xFF1E60FF)
    );

    companion object {
        fun fromId(id: Int): BlockColor {
            val values = values()
            return values[Math.floorMod(id, values.size)]
        }

        // Backwards compatibility mappings for tests & existing saves
        val SOFT_PINK = SHAPLA_PINK
        val LAVENDER = ALPONA_PURPLE
        val SKY_BLUE = DOEL_SAPPHIRE
        val MINT = JAMDANI_TEAL
        val SUNSHINE_YELLOW = KANTHA_GOLD
        val CORAL = RICKSHAW_ROSE
        val WARM_CREAM = BELI_CREAM
        val ROSE_GOLD = KRISHNACHURA_RED
    }
}
