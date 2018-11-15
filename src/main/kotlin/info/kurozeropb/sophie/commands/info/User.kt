package info.kurozeropb.sophie.commands.info

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class User : Command(
        name = "userinfo",
        aliases = listOf("memberinfo", "member", "user"),
        category = Category.INFO,
        description = "Get info about a guild member",
        usage = "[username: mention|string]",
        allowPrivate = false,
        botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {

    private val formatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendPattern("/")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendPattern("/")
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter()

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in user command", e.channel) {
            val member = Utils.convertMember(args.joinToString(" "), e)
                    ?: when {
                        e.message.mentionedMembers.isNotEmpty() -> e.message.mentionedMembers[0]
                        args.isEmpty() -> e.member
                        else -> return e.reply("No member was found with the username **${args.joinToString(" ")}**")
                    }

            val mutualGuildCount = Sophie.shardManager.getMutualGuilds(member.user).size
            val joinedDate = member.joinDate.format(formatter)
            val memberRoles = member.roles?.joinToString(", ") { it.name } ?: "-"
            val creationDate = member.user.creationTime.format(formatter)
            e.reply(EmbedBuilder()
                    .setTitle("User info of ${member.effectiveName}", member.user.effectiveAvatarUrl)
                    .setThumbnail(member.user.effectiveAvatarUrl)
                    .addField("Username", member.user.name, true)
                    .addField("Nickname", member.nickname ?: "-", true)
                    .addField("Bot", if (member.user.isBot) "Yes" else "No", true)
                    .addField("Mutual servers", mutualGuildCount.toString(), true)
                    .addField("Status", member.onlineStatus.key, true)
                    .addField("Playing", member.game?.name ?: "-", true)
                    .addField("Joined on", joinedDate, false)
                    .addField("Roles", memberRoles, false)
                    .setFooter("ID: ${member.user.id} | Created on: $creationDate", null))
        }
    }
}