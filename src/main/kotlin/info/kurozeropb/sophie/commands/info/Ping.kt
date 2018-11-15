package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.function.Consumer

class Ping : Command(
        name = "ping",
        category = Category.INFO,
        description = "Returns an estimated ping to Discord's servers",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in ping command", e.channel) {
            var time = System.currentTimeMillis()
            e.reply("Pinging...", Consumer {
                time = (System.currentTimeMillis() - time) / 2
                Utils.edit(it, "**Ping:** ${time}ms")
            })
        }
    }
}
