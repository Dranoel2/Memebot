package net.dranoel.memebot

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import javax.security.auth.login.LoginException


object Memebot : ListenerAdapter() {
    @Throws(LoginException::class)
    fun login() {
        val id = System.getenv()["BOT_ID"]
        val jda = JDABuilder.createDefault(id)
            .addEventListeners(this)
            .build()
        jda.awaitReady()
        jda.updateCommands().addCommands(commands).queue()
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        when(event.name) {
            "template" -> {
                event.deferReply()
                    .queue()
                val id = event.getOption("id")?.asString as String
                val text1 = event.getOption("text1")?.asString
                val text2 = event.getOption("text2")?.asString
                val url = genUrl(id, text1, text2, "png")
                runBlocking {
                    launch {
                        if(InfoGetter.getIds().contains(id)) {
                            event.hook.sendMessage(url).queue()
                        } else {
                            event.user.openPrivateChannel()
                                .queue {
                                        channel -> channel.sendMessage("I don't know what that memes (id not found).")
                                        .queue()
                                }
                            event.hook.deleteOriginal()
                                .queue()
                        }
                    }
                }
            } "list" -> {
                event.deferReply()
                    .setEphemeral(true)
                    .queue()
                runBlocking {
                    launch {
                        val ids = InfoGetter.getIds()
                        val message = ids.joinToString(", ","Templates found (${ids.size}): ", ".")
                        event.hook.sendMessage(message)
                            .queue()
                    }
                }
            } "help" -> {
                event.deferReply()
                    .setEphemeral(true)
                    .queue()
                runBlocking {
                    launch {
                        val id = event.getOption("id")!!.asString
                        if(InfoGetter.getIds().contains(id)) {
                            val template: MemeTemplateData = InfoGetter.getTemplateFromID(id)
                            val builder: EmbedBuilder = EmbedBuilder()
                                .setTitle(template.name, null)
                                .addField("Id", template.id, false)
                                .addField("Source", template.source, false)
                                .setImage("https://api.memegen.link/images/$id.png")
                            val embed = builder.build()
                            event.hook.sendMessageEmbeds(embed)
                                .queue()
                        } else {
                            event.hook.sendMessage("I don't know what that memes (id not found).")
                                .queue()
                        }
                    }
                }
            } "custom" -> {
                event.deferReply()
                    .queue()
                val imageUrl = event.getOption("url")?.asString as String
                val toptext = event.getOption("toptext")?.asString
                val bottomtext = event.getOption("bottomtext")?.asString
                val suffix = imageUrl.split(".").last()
                val url = (genUrl("custom", toptext, bottomtext, suffix) + "?background=$imageUrl")
                runBlocking {
                    launch {
                        if(InfoGetter.verifyUrl(imageUrl)) {
                            event.hook.sendMessage(url).queue()
                        } else {
                            event.user.openPrivateChannel()
                                .queue {
                                        channel -> channel.sendMessage("I don't know what that memes (url not valid).")
                                    .queue()
                                }
                            event.hook.deleteOriginal()
                                .queue()
                        }
                    }
                }
            }
        }
    }

    private val commands = mutableListOf(
        CommandData("template", "Does a meme using a template id")
            .addOption(OptionType.STRING, "id", "The id of the meme", true)
            .addOption(OptionType.STRING, "text1", "The first text of the meme (usually at the top)", false)
            .addOption(OptionType.STRING, "text2", "The second text of the meme (usually at the bottom)", false),
        CommandData("list", "Lists all the meme ids available"),
        CommandData("help", "Gives you info about a meme from its id")
            .addOption(OptionType.STRING, "id", "The Id of the meme", true),
        CommandData("custom", "Creates a meme using an image url")
            .addOption(OptionType.STRING, "url", "the url of the image", true)
            .addOption(OptionType.STRING, "toptext", "The text at the top of the meme", false)
            .addOption(OptionType.STRING, "bottomtext", "The text at the bottom of the meme", false),
    )

    private fun genUrl(id: String, text1: String?, text2: String?, suffix: String): String {
        val finalText1: String = if(text1 != null) {
            "/" + escapeURL(text1)
        } else {
            ""
        }
        val finalText2: String = if(text2 != null) {
            "/" + escapeURL(text2)
        } else {
            ""
        }
        return "https://api.memegen.link/images/$id$finalText1$finalText2.$suffix"
    }

    private fun escapeURL(url: String): String {
        return url
            .replace("-", "--")
            .replace("_", "__")
            .replace(" ", "_")
            .replace("?", "~q")
            .replace("&", "~a")
            .replace("%", "~p")
            .replace("#", "~h")
            .replace("/", "~s")
            .replace("\\", "~b")
            .replace("\"", "''")
    }

    private fun listIds() {

    }
}