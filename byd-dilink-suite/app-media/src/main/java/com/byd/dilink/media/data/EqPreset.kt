package com.byd.dilink.media.data

data class EqPreset(
    val name: String,
    val bands: List<Int> // millibel values (-1500 to +1500)
) {
    companion object {
        val BAND_FREQUENCIES = listOf(
            "60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz"
        )

        val FLAT = EqPreset("Flat", listOf(0, 0, 0, 0, 0))
        val ROCK = EqPreset("Rock", listOf(500, 300, -100, 300, 500))
        val POP = EqPreset("Pop", listOf(-100, 200, 500, 200, -100))
        val JAZZ = EqPreset("Jazz", listOf(300, 0, 100, 200, 400))
        val CLASSICAL = EqPreset("Classical", listOf(400, 200, 0, 200, 400))
        val BASS_BOOST = EqPreset("Bass+", listOf(700, 500, 0, 0, 0))
        val VOCAL = EqPreset("Vocal", listOf(-200, 0, 500, 300, 0))
        val CUSTOM = EqPreset("Custom", listOf(0, 0, 0, 0, 0))

        val ALL_PRESETS = listOf(FLAT, ROCK, POP, JAZZ, CLASSICAL, BASS_BOOST, VOCAL)
    }
}
