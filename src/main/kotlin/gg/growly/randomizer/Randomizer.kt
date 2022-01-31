package gg.growly.randomizer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.concurrent.thread

/**
 * @author GrowlyX
 * @since 1/31/2022
 */
object Randomizer
{
    private val quoteMappings =
        mutableMapOf<String, MutableList<String>>()

    @JvmStatic
    fun main(args: Array<String>)
    {
        val act = "What act would you like to cover?".response()
        val scene = "What scene would you like to cover?".response()

        val unparsed = Files
            .lines(Paths.get("$act-$scene.txt"))
            .collect(Collectors.toList())

        parseText(unparsed)

        thread {
            while (true)
                pollInput()
        }
    }

    private fun pollInput()
    {
        val mapping = quoteMappings
            .entries.random()

        val speaker = mapping.key
        val quote = mapping.value.random()

        val randomSpeakers = quoteMappings.keys
            .filter { it != speaker }.take(3)
            .toMutableList().also {
                it.add(speaker)
                it.shuffle()
            }

        val description = mutableListOf<String>()
        description.add("")
        description.add("Who said this?")
        description.add(quote)
        description.add("")

        randomSpeakers.forEachIndexed { index, random ->
            description.add("#${index + 1}. $random")
        }

        description.add("")

        val response = description.response()

        if (response.equals(speaker, true))
        {
            println("✔ You got it correct!")
        } else
        {
            println("❌ Wrong! Correct answer: $speaker")
        }
    }

    private fun parseText(unparsed: List<String>)
    {
        var startIndex = 0
        var current = ""

        for ((index, value) in unparsed.withIndex())
        {
            if (index == 0)
            {
                current = value
                continue
            }

            if (value.isEmpty())
            {
                startIndex = index + 1
                continue
            }

            if (startIndex == index)
            {
                quoteMappings[value] ?:
                quoteMappings.put(value, mutableListOf())

                current = value
            } else
            {
                quoteMappings[current]?.add(value)
            }
        }
    }

    private val reader = BufferedReader(
        InputStreamReader(System.`in`)
    )

    private fun String.response(): String
    {
        println("$this ")
        return reader.readLine()
    }

    private fun List<String>.response(): String
    {
        forEach { println(it) }
        return reader.readLine()
    }
}
