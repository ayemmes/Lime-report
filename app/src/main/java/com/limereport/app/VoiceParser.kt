package com.limereport.app

/**
 * Turns a spoken summary such as
 * "relocated 12 scooters and 5 bikes, deployed 20 scooters 3 bikes, 4 damaged"
 * into task counts. Only the task types that were mentioned are returned, so the
 * caller can leave the other fields as they were.
 */
object VoiceParser {

    enum class Task { RELOCATE, DEPLOY, DAMAGED }
    enum class Vehicle { SCOOTER, BIKE }

    data class Result(
        val relocateScooters: Int? = null,
        val relocateBikes: Int? = null,
        val deployScooters: Int? = null,
        val deployBikes: Int? = null,
        val damaged: Int? = null,
    ) {
        val isEmpty get() = listOf(relocateScooters, relocateBikes, deployScooters, deployBikes, damaged).all { it == null }
    }

    private val units = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10, "eleven" to 11,
        "twelve" to 12, "thirteen" to 13, "fourteen" to 14, "fifteen" to 15, "sixteen" to 16,
        "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
    )
    private val tens = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
        "sixty" to 60, "seventy" to 70, "eighty" to 80, "ninety" to 90,
    )

    private fun taskOf(word: String): Task? = when {
        word.startsWith("relocat") || word.startsWith("move") || word == "moved" ||
            word.startsWith("remote") || word.startsWith("rebalanc") || word.startsWith("transfer") -> Task.RELOCATE
        word.startsWith("deploy") || word.startsWith("load") -> Task.DEPLOY
        word.startsWith("damage") || word.startsWith("broken") || word.startsWith("repair") ||
            word.startsWith("collect") || word.startsWith("fix") -> Task.DAMAGED
        else -> null
    }

    private fun vehicleOf(word: String): Vehicle? = when {
        word.startsWith("scoot") -> Vehicle.SCOOTER
        word.startsWith("bike") || word.startsWith("bicycle") || word.startsWith("ebike") ||
            word == "e-bike" || word == "e-bikes" -> Vehicle.BIKE
        else -> null
    }

    /** Reads a number starting at [i]; returns the value and how many tokens it used. */
    private fun numberAt(tokens: List<String>, i: Int): Pair<Int, Int>? {
        val t = tokens[i]
        t.toIntOrNull()?.let { return it to 1 }
        units[t]?.let { return it to 1 }
        val ten = tens[t] ?: return null
        val next = tokens.getOrNull(i + 1)
        val unit = next?.let { units[it] }
        return if (unit != null && unit in 1..9) (ten + unit) to 2 else ten to 1
    }

    fun parse(text: String): Result {
        val tokens = text.lowercase()
            .replace(Regex("(\\d)([a-z])"), "$1 $2") // "12scooters" -> "12 scooters"
            .replace(Regex("([a-z])-([a-z])"), "$1 $2") // "twenty-one" -> "twenty one"
            .split(Regex("[^a-z0-9]+"))
            .filter { it.isNotBlank() }

        val counts = mutableMapOf<Pair<Task, Vehicle?>, Int>()
        var task: Task? = null
        var i = 0
        while (i < tokens.size) {
            val number = numberAt(tokens, i)
            if (number == null) {
                taskOf(tokens[i])?.let { task = it }
                i++
                continue
            }
            val (value, used) = number
            i += used
            // Look a few words ahead for what the number counts: "5 bikes", "4 damaged ones".
            var vehicle: Vehicle? = null
            var numberTask: Task? = null
            var j = i
            while (j < tokens.size && j < i + 3 && numberAt(tokens, j) == null) {
                vehicle = vehicle ?: vehicleOf(tokens[j])
                if (vehicle == null && numberTask == null) numberTask = taskOf(tokens[j])
                j++
                if (vehicle != null) break
            }
            if (numberTask == Task.DAMAGED) {
                task = Task.DAMAGED
                i = j
            } else if (vehicle != null) {
                i = j
            } else if (numberTask != null && task == null) {
                task = numberTask
            }
            val t = task ?: continue
            val key = if (t == Task.DAMAGED) t to null else t to (vehicle ?: continue)
            counts[key] = (counts[key] ?: 0) + value
        }

        return Result(
            relocateScooters = counts[Task.RELOCATE to Vehicle.SCOOTER],
            relocateBikes = counts[Task.RELOCATE to Vehicle.BIKE],
            deployScooters = counts[Task.DEPLOY to Vehicle.SCOOTER],
            deployBikes = counts[Task.DEPLOY to Vehicle.BIKE],
            damaged = counts[Task.DAMAGED to null],
        )
    }
}
