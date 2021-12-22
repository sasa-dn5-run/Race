package run.dn5.Xmas.Command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import run.dn5.Xmas.MessageUtil
import run.dn5.Xmas.Xmas

class xmas: TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val plugin = Xmas.plugin
        if(args.isEmpty()){
            sender.sendMessage(MessageUtil.info("/game <start|stop>"))
            return false
        }
        when(args[0]){
            "start" -> {
                sender.sendMessage(MessageUtil.info("Starting game..."))
                plugin.gameManager.startGame(sender)
            }
            "stop" -> {
                sender.sendMessage(MessageUtil.info("Stopping game..."))
                plugin.gameManager.stopGame(sender)
            }
            "set" -> {
                if(args.size < 2) return false
                when(args[1]){
                    "start" -> {
                        plugin.gameManager.saveRegion("start", sender as Player)
                    }
                    "goal" -> {
                        plugin.gameManager.saveRegion("goal", sender as Player)
                    }
                    "lobby" -> {
                        plugin.gameManager.saveRegion("lobby", sender as Player)
                    }
                }
            }
            "reset" -> {
                if(args.size < 2) return false
                when(args[1]){
                    "start" -> {
                        plugin.gameManager.saveRegion("start")
                    }
                    "goal" -> {
                        plugin.gameManager.saveRegion("goal")
                    }
                    "lobby" -> {
                        plugin.gameManager.saveRegion("lobby")
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
                mutableListOf("start", "stop", "set", "reset")
            }
            2 -> {
                when(args[0]){
                    "set" -> {
                        mutableListOf("start", "goal", "lobby")
                    }
                    "reset" -> {
                        mutableListOf("start", "goal", "lobby")
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