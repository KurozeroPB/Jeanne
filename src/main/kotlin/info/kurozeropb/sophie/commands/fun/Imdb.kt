package info.kurozeropb.sophie.commands.`fun`

import com.beust.klaxon.Klaxon
import info.kurozeropb.sophie.*
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.HttpException
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Imdb : Command(
        name = "imdb",
        category = Category.FUN,
        description = "Search for a movie on imdb",
        usage = "<movie|serie: string>",
        cooldown = 20,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        if (args.isEmpty())
            return e.reply("Which movie/serie do you want to search?")

        val baseUrl = "http://www.omdbapi.com/?apikey=${Sophie.config.tokens.imdb}"
        val headers = mutableMapOf("Accept" to "application/json")
        headers.putAll(Sophie.defaultHeaders)
        val request = Request.Builder()
                .headers(Headers.of(headers))
                .url("$baseUrl&t=${args.joinToString(" ")}")
                .build()

        Sophie.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in imdb command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in imdb command", e.channel) {
                    val parser = Klaxon()
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    val exceptionMessage = "Could not find a movie, serie or episode for **${args.joinToString(" ")}**"
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply(exceptionMessage)

                        val omdbType = parser.parse<OmdbTypeTest>(respstring)
                        if (omdbType != null) {
                            if (omdbType.Response == "False") {
                                val error = parser.parse<OmdbError>(respstring)
                                return e.reply(error?.Error ?: exceptionMessage)
                            }

                            val omdb = parser.parse<Omdb>(respstring)
                            if (omdb != null) {
                                val poster = if (Utils.urlPattern.matches(omdb.Poster)) omdb.Poster else null

                                e.reply(EmbedBuilder().setTitle(omdb.Title, "https://www.imdb.com/title/${omdb.imdbID}")
                                        .setDescription(omdb.Plot)
                                        .setThumbnail(poster)
                                        .addField("Rated", omdb.Rated, true)
                                        .addField("Runtime", omdb.Runtime, true)
                                        .addField("Languages", omdb.Language, true)
                                        .addField("Rating", omdb.imdbRating, true)
                                        .addField("Type", omdb.Type ?: "-", true)
                                        .addField("Genres", omdb.Genre, true)
                                        .addField("Awards", omdb.Awards, false)
                                        .addField("Released", omdb.Released, false))
                            } else {
                                e.reply(exceptionMessage)
                            }
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}
