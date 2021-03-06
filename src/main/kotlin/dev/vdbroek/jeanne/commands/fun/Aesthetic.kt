package dev.vdbroek.jeanne.commands.`fun`

import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Aesthetic : Command(
    name = "aesthetic",
    aliases = listOf("aes"),
    category = Category.FUN,
    description = "Convert text to aesthetic text",
    usage = "<text: string>",
    botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in aesthetic command", e.channel) {
            if (args.isEmpty())
                return e.reply("Insufficient argument count")

            var message = args.joinToString(" ")
            message = message.replace(Regex("[a-zA-Z0-9!?.'\";:\\]\\[}{)(@#\$%^&*\\-_=+`~><]")) { c -> c.value[0].plus(0xFEE0).toString() }
            message = message.replace(Regex(" "), "　")
            e.reply(message)
        }
    }
}