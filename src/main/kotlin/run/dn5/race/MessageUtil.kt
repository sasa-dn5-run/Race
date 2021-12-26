package run.dn5.race

import org.bukkit.ChatColor

class MessageUtil {
    companion object {
        fun info(msg: String): String{
            return "${ChatColor.GREEN}[Xmas] ${ChatColor.AQUA} $msg"
        }
        fun warn(msg: String): String{
            return "${ChatColor.GREEN}[Xmas] ${ChatColor.RED} $msg"
        }
    }
}