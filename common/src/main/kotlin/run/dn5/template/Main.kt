package run.dn5.template

import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    override fun onEnable() {
        this.logger.info("Enabled")
    }
}