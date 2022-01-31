package gg.growly.randomizer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


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
        val unparsed = Files
            .lines(Paths.get("${"What act would you like to cover?".response()}-${"What scene would you like to cover?".response()}.txt"))
            .collect(Collectors.toList())

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

        for (entry in quoteMappings.entries)
        {
            println("${entry.key}:")

            entry.value
                .forEach { println(" $it") }
        }
    }

    private fun String.response(): String
    {
        val reader = BufferedReader(
            InputStreamReader(System.`in`)
        )

        println("$this ")
        return reader.readLine()
    }
}
