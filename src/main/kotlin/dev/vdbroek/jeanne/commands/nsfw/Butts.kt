package dev.vdbroek.jeanne.commands.nsfw

import com.beust.klaxon.Klaxon
import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.ONsfwData
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.HttpException
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.*
import java.io.IOException

class Butts : Command(
    name = "butts",
    aliases = listOf("ass", "butt"),
    category = Category.NSFW,
    nsfw = true,
    description = "Sends a random butt pic",
    cooldown = 10,
    isDonatorsOnly = true,
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        if (e.isFromType(ChannelType.PRIVATE).not() && e.textChannel.isNSFW.not()) {
            e.reply("This command can only be used in NSFW channels")
            return
        }

        val headers = mutableMapOf("Accept" to "application/json")
        headers.putAll(Jeanne.defaultHeaders)
        val request = Request.Builder()
            .headers(Headers.of(headers))
            .url("http://api.obutts.ru/butts/0/1/random")
            .build()

        Jeanne.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Utils.catchAll("Exception occured in butts command", e.channel) {
                    throw exception
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.catchAll("Exception occured in butts command", e.channel) {
                    val respstring = response.body()?.string()
                    val message = response.message()
                    val code = response.code()
                    response.close()

                    if (response.isSuccessful) {
                        if (respstring.isNullOrBlank())
                            return e.reply("Could not find a butt picture, please try again later")

                        val ass = Klaxon().parseArray<ONsfwData>(respstring)
                        if (ass != null && ass.isNotEmpty()) {
                            headers.replace("Accept", "application/json", "image/*")
                            val newRequest = Request.Builder()
                                .headers(Headers.of(headers))
                                .url("http://media.obutts.ru/${ass[0].preview.replace("butts_preview", "butts")}")
                                .build()

                            val newResponse = Jeanne.httpClient.newCall(newRequest).execute()
                            val byteStream = newResponse.body()?.byteStream()
                            val contentType = newResponse.body()?.contentType()
                            val newMessage = newResponse.message()
                            val newCode = newResponse.code()

                            if (newResponse.isSuccessful) {
                                if (byteStream != null)
                                    e.reply(byteStream, "obutt-${ass[0].id}.${contentType.toString().replace("image/", "")}")
                                else
                                    e.reply("Could not find a butt picture, please try again later")
                            } else {
                                throw HttpException(newCode, newMessage)
                            }
                        } else {
                            e.reply("Could not find a butt picture, please try again later")
                        }
                    } else {
                        throw HttpException(code, message)
                    }
                }
            }
        })
    }
}
