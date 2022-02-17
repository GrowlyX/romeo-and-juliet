package gg.growly.randomizer

import gg.growly.katoot.question.Question
import gg.growly.katoot.question.QuestionChoice
import gg.growly.katoot.quiz.Quizzes
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import java.util.stream.Collectors
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
        println("\n${Color.GREEN_BOLD}Welcome to the Romeo & Juliet Quote Game!")
        println("${Color.YELLOW}We currently cover act 1, scene 1.\n")

        val act = "${Color.CYAN}What act would you like to cover?".response()
        val scene = "${Color.CYAN}What scene would you like to cover?".response()

        val useKahoot = try
        {
            UUID.fromString(
                "${Color.RED}Would you like to use kahoot? If yes, what's the game id?".response()
            )
        } catch (ignored: Exception) {
            null
        }

        val start = System.currentTimeMillis()
        println("${Color.YELLOW}Loading quotes for act $act scene $scene...")

        try
        {
            if (useKahoot == null)
            {
                val unparsed = Files
                    .lines(Paths.get("$act-$scene.txt"))
                    .collect(Collectors.toList())

                parseText(unparsed)
            } else
            {
                val quiz = Quizzes.byGameId(useKahoot)!!

                val mappings =
                    mutableMapOf<String, MutableList<Pair<Question, QuestionChoice>>>()

                for (question in quiz.questions)
                {
                    val choice = question
                        .getFirstCorrectChoice()

                    mappings.putIfAbsent(choice.answer, mutableListOf())

                    mappings[choice.answer]
                        ?.apply { add(Pair(question, choice)) }
                }

                for (mapping in mappings)
                {
                    quoteMappings.putIfAbsent(mapping.key, mutableListOf())

                    quoteMappings[mapping.key]?.apply {
                        for (pair in mapping.value)
                            add(pair.first.value)
                    }
                }
            }

            println("${Color.GREEN}Loaded quotes in ${System.currentTimeMillis() - start}ms!")
        } catch (exception: Exception)
        {
            println("${Color.RED}Sorry, we don't have quote mappings for this act-scene pair. :(")
            exitProcess(0)
        }

        // test doesn't have this
        quoteMappings.remove("Gregory")
        quoteMappings.remove("Sampson")

        while (true)
            pollInput()
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

        if (
            totalQuotes == practiced.size &&
            practiced.size != 0
        )
            stop()

        val speaker = mapping.key

        val quote = mapping.value
            .filter { !practiced.contains(it) }
            .randomOrNull()

        if (quote == null)
        {
            pollInput()
            return
        }

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
                if (value.first().isLowerCase())
                    continue

                if (value.length < 10)
                    continue

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
