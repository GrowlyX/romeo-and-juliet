package gg.growly.randomizer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * @author GrowlyX
 * @since 1/31/2022
 */
object Randomizer
{
    private val quoteMappings =
        mutableMapOf<String, MutableList<String>>()

    private val practiced = mutableListOf<String>()

    private var correct = 0
    private var totalQuotes = 0

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

    private fun stop()
    {
        println("${Color.GREEN}You're done! You got a ${(correct / totalQuotes) * 100}%.")
        exitProcess(0)
    }

    private fun pollInput()
    {
        val mapping = quoteMappings
            .entries.random()

        val speaker = mapping.key

        val quote = mapping.value
            .filter { !practiced.contains(it) }
            .randomOrNull() ?: stop()

        val randomSpeakers = quoteMappings.keys
            .filter { it != speaker }
            .shuffled().take(3)
            .toMutableList()

        randomSpeakers.add(speaker)
        randomSpeakers.shuffle()

        val description = mutableListOf<String>()
        description.add("")
        description.add("${Color.YELLOW}Who said this?")
        description.add("${Color.CYAN}$quote${Color.RESET}")
        description.add("")

        randomSpeakers
            .forEachIndexed { index, random ->
                description.add("#${index + 1}. $random")
            }

        description.add("")

        val response = description.response()

        if (response.toIntOrNull() != null)
        {
            try
            {
                if (randomSpeakers[response.toInt() - 1] == speaker)
                {
                    println("${Color.GREEN}✔ You got it correct!"); correct++
                }
                else
                {
                    println("${Color.RED}❌ Wrong! Correct answer: $speaker")
                }
            } catch (e: Exception)
            {
                println("${Color.RED}❌ Wrong! Correct answer: $speaker")
            }
        } else
        {
            if (response.equals(speaker, true))
            {
                println("${Color.GREEN}✔ You got it correct!"); correct++
            }
            else
            {
                println("${Color.RED}❌ Wrong! Correct answer: $speaker")
            }
        }

        practiced.add(quote.toString())

        println("${Color.YELLOW}You've practiced ${practiced.size}/${totalQuotes} quotes.")
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
                quoteMappings[value] ?: quoteMappings.put(value, mutableListOf())

                current = value
            } else
            {
                quoteMappings[current]?.add(value)
                totalQuotes++
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
