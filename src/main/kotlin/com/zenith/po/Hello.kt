package com.zenith.po

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.jessecorbett.diskord.api.exception.DiscordBadPermissionsException
import com.jessecorbett.diskord.api.model.Permissions
import com.jessecorbett.diskord.api.model.UserStatus
import com.jessecorbett.diskord.api.rest.CreateGuildRole
import com.jessecorbett.diskord.api.websocket.model.ActivityType
import com.jessecorbett.diskord.api.websocket.model.UserStatusActivity
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.dsl.footer
import com.jessecorbett.diskord.util.authorId
import com.jessecorbett.diskord.util.words
import java.io.File

data class update(
    val version: String,
    val added: Array<String>,
    val deleted: Array<String>,
    val changed: Array<String>,
    val deprecated: Array<String>,
    val comingsoon: Array<String>,
    val etc: Array<String>
)

data class serverConf(
        @SerializedName("notice_channel")
        val noticeChannelId: String,
        val managerRoleName: String
)

val BOT_TOKEN: String = System.getenv("BOT_TOKEN")
val gson = GsonBuilder().setPrettyPrinting().create()
val updates = gson.fromJson(File("./update.json").readText(), update::class.java)
val admins = arrayOf("723354571115724805")
var isInspecting: Boolean = false
suspend fun main() {
    bot(BOT_TOKEN) {
        val bot = this
        this.started {
            setStatus(UserStatus.ONLINE, false, null, UserStatusActivity("동방천공장 OST", ActivityType.LISTENING))
        }
        commands("po!") {
            command("info") {
                delete()
                reply {
                    text = "Succeed"
                    title = "성공 - 봇 정보"
                    color = 0x00FF00
                    footer("Po210Bot | version = ${updates.version}")
                    description = "Powered by Kotlin(1.4.10), Diskord, GSON\nKotlin(1.4.10), Diskord, GSON에 기초함."
                }
            }
            command("setNoticeChannel"){
                var file:File
                try{
                    file = File("./conf/server/${this.guildId}.json")
                }catch(e: Exception){
                    file = File("./conf/server/${this.guildId}.json")
                    file.createNewFile()
                }
                val guildG = clientStore.guilds[this.guildId!!]
                var user = false
                guildG.getRoles().forEach {
                    if(this.rolesIdsMentioned.contains(it.id) && it.name == "Po210 Manager"){
                        user = true
                    }
                }
                val tmp = gson.fromJson(file.readText(), serverConf::class.java)
                var grolesname = arrayOf<String>()
                val guild = clientStore.guilds[this.guildId!!]
                val roles = guild.getRoles()
                roles.forEach {
                    grolesname = grolesname.plus(it.name)
                }
                if(this.words.size > 1 && user){
                    file.writeText(gson.toJson(serverConf(this.words[1], "Po210 Manager")))
                    delete()
                    reply{
                        text = "Succeed"
                        title = "성공 - 공지 채널 설정"
                        color = 0x00FF00
                        footer("Po210Bot | version = ${updates.version}")
                        description = "공지 채널을 성공적으로 생성함."
                    }
                }else if(grolesname.indexOf("Po210 Manager") == -1){
                    grolesname = arrayOf()
                    var isSucceed = false
                    try{
                        guild.createRole(CreateGuildRole("Po210 Manager", displayedSeparately = false, mentionable = true, permissions = Permissions.NONE))
                        isSucceed = true
                    }catch(e: DiscordBadPermissionsException){
                        isSucceed = false
                        delete()
                        reply{
                            text = "Failed"
                            title = "실패 - 공지 채널 설정"
                            color = 0xFF0000
                            footer("Po210Bot | version = ${updates.version}")
                            description = "'Po210 Manager' 라는 역할이 없어서 생성을 시도하였으나 권한 부족으로 실패함. 수동 생성 요망"
                        }
                    }
                    if(isSucceed){
                        reply{
                            text = "Failed"
                            title = "실패 - 공지 채널 설정"
                            color = 0xFF0000
                            footer("Po210Bot | version = ${updates.version}")
                            description = "'Po210 Manager' 라는 역할이 없어서 역할을 생성함. 수동 할당 요망"
                        }
                    }
                }else{
                    delete()
                    reply{
                        text = "Failed"
                        title = "실패 - 공지 채널 설정"
                        color = 0xFF0000
                        footer("Po210Bot | version = ${updates.version}")
                        description = "인자 값이 없음."
                    }
                }
            }
            command("setInspectMode"){
                delete()
                println(this.authorId)
                println(admins.indexOf(this.authorId))
                if(admins.indexOf(this.authorId) != -1){
                    when(this.content.split(" ")[1]){
                        "true" -> {
                            isInspecting = true
                            setStatus(UserStatus.ONLINE, false, null, UserStatusActivity("동방천공장 OST 듣다가 Ctrl+C 눌러서 빡쳐", ActivityType.GAME))
                            reply {
                                text = "Succeed"
                                title = "성공 - 점검 모드 설정"
                                color = 0x00FF00
                                footer("Po210Bot | version = ${updates.version}")
                                description = "점검 모드로 진입함."
                            }
                        }
                        "false" -> {
                            isInspecting = false
                            setStatus(UserStatus.ONLINE, false, null, UserStatusActivity("동방천공장 OST", ActivityType.LISTENING))
                            reply {
                                text = "Succeed"
                                title = "성공 - 점검 모드 해제"
                                color = 0x00FF00
                                footer("Po210Bot | version = ${updates.version}")
                                description = "점검 모드에서 탈출함."
                            }
                        }
                        else -> {
                            reply {
                                text = "Failed"
                                title = "실패 - 점검 모드 설정"
                                color = 0xFF0000
                                footer("Po210Bot | version = ${updates.version}")
                                description = "점검 모드 진입/해제에 실패함. 입력 값이 올바르지 않음."
                            }
                        }
                    }
                }
            }
        }
    }
}