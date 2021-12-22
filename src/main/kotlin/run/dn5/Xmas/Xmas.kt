package run.dn5.Xmas

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import run.dn5.Xmas.Command.xmas
import run.dn5.Xmas.Game.GameManager
import run.dn5.Xmas.Menu.MenuHandler

class Xmas: JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: Xmas
            private set
    }

    lateinit var gameManager: GameManager
    lateinit var menuHandler: MenuHandler

    override fun onEnable() {
        Xmas.plugin = this

        if(!this.dataFolder.exists()){
            this.dataFolder.mkdir()
        }

        this.server.getPluginCommand("xmas")?.setExecutor(xmas())

        this.server.pluginManager.registerEvents(this, this)

        this.gameManager = GameManager()
        this.menuHandler = MenuHandler()
    }
}