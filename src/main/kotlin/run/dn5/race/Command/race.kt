package run.dn5.race.Command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import run.dn5.race.Game.GameManager
import run.dn5.race.MessageUtil
import run.dn5.race.Main

class race: TabExecutor {

    private val plugin = Main.plugin

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty()){
            sender.sendMessage(MessageUtil.info("/game <start|stop>"))
            return false
        }
        when(args[0]){
            "start" -> {
                sender.sendMessage(MessageUtil.info("Starting game..."))
                this.plugin.gameManager.startGame(sender)
            }
            "stop" -> {
                sender.sendMessage(MessageUtil.info("Stopping game..."))
                this.plugin.gameManager.stopGame(sender)
            }
            "set" -> {
                if(args.size < 2) return false
                if(GameManager.REGION_NAMES.contains(args[1])){
                    this.plugin.gameManager.saveRegion(args[1], sender as Player)
                }
            }
            "reset" -> {
                if(args.size < 2) return false
                if(GameManager.REGION_NAMES.contains(args[1])){
                    this.plugin.gameManager.saveRegion(args[1])
                }
                when(args[1]){
                    "players" -> {
                        this.plugin.gameManager.resetPlayers()
                        sender.sendMessage(MessageUtil.info("プレイヤーをリセットしました。"))
                    }
                }
            }
            "create" -> {
                if(args.size < 2) return false
                when(args[1]){
                    "teleporter" -> {
                        if(args.size < 3) {
                            sender.sendMessage(MessageUtil.warn("IDを指定してください。"))
                            return false
                        }
                        this.plugin.gameManager.createTeleporter(sender as Player, args[2])
                    }
                }
            }
            "remove" -> {
                if(args.size < 2) return false
                when(args[1]){
                    "teleporter" -> {
                        if(args.size < 3) {
                            sender.sendMessage(MessageUtil.warn("IDを指定してください。"))
                            return false
                        }
                        this.plugin.gameManager.removeTeleporter(sender, args[2])
                    }
                }
            }
            "config" -> {
                if(args.size < 2) return false
                when(args[1]){
                    "startDirection" -> {
                        if(args.size < 3) {
                            sender.sendMessage(MessageUtil.warn("方向を指定してください。"))
                            return false
                        }
                        this.plugin.gameManager.setStartDirection(args[2].toInt())
                        sender.sendMessage(MessageUtil.info("設定を保存しました。"))
                    }
                }
            }
            else -> {
                sender.sendMessage(MessageUtil.info("/game <start|stop>"))
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when(args.size){
            1 -> {
                mutableListOf("start", "stop", "set", "reset", "create", "remove", "config")
            }
            2 -> {
                when(args[0]){
                    "set" -> {
                        GameManager.REGION_NAMES.toMutableList()
                    }
                    "reset" -> {
                        val result = GameManager.REGION_NAMES.toMutableList()
                        result.add("players")
                        return result
                    }
                    "create" -> {
                        mutableListOf("teleporter")
                    }
                    "remove" -> {
                        mutableListOf("teleporter")
                    }
                    else -> {
                        mutableListOf()
                    }
                }
            }
            3 -> {
                return when(args[1]){
                    "teleporter" -> {
                        this.plugin.gameManager.teleporters.keys.toMutableList()
                    }
                    else -> {
                        mutableListOf()
                    }
                }
            }
            else -> {
                mutableListOf()
            }
        }
    }
}