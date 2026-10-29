package com.limereport.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceParserTest {
    @Test fun fullSummary() {
        val r = VoiceParser.parse("Relocated 12 scooters and 5 bikes, deployed 20 scooters and 3 bikes, 4 damaged")
        assertEquals(VoiceParser.Result(12, 5, 20, 3, 4), r)
    }

    @Test fun numberWords() {
        val r = VoiceParser.parse("moved twenty-one scooters and seven bikes collected three damaged")
        assertEquals(VoiceParser.Result(relocateScooters = 21, relocateBikes = 7, damaged = 3), r)
    }

    @Test fun damagedWithVehicleTypesAreSummed() {
        val r = VoiceParser.parse("damaged 3 scooters and 2 bikes")
        assertEquals(VoiceParser.Result(damaged = 5), r)
    }

    @Test fun damagedFirstThenOtherTasks() {
        val r = VoiceParser.parse("4 damaged ones, then deploy 10 scooters")
        assertEquals(VoiceParser.Result(deployScooters = 10, damaged = 4), r)
    }

    @Test fun repeatedMentionsAdd() {
        val r = VoiceParser.parse("relocate 5 scooters, 3 scooters, 2 e-bikes")
        assertEquals(VoiceParser.Result(relocateScooters = 8, relocateBikes = 2), r)
    }

    @Test fun batterySwaps() {
        val r = VoiceParser.parse("relocated 12 scooters, 4 damaged, swapped fifteen batteries")
        assertEquals(VoiceParser.Result(relocateScooters = 12, damaged = 4, batterySwaps = 15), r)
        assertEquals(VoiceParser.Result(deployBikes = 3, batterySwaps = 20), VoiceParser.parse("deployed 3 bikes, 20 battery swaps"))
        assertEquals(VoiceParser.Result(batterySwaps = 9), VoiceParser.parse("battery swaps 9"))
    }

    @Test fun nothingRecognised() {
        assertTrue(VoiceParser.parse("hello there").isEmpty)
        assertTrue(VoiceParser.parse("12 scooters").isEmpty)
    }
}
