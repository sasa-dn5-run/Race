package run.dn5.race

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import run.dn5.race.Command.race
import run.dn5.race.Game.GameManager
import run.dn5.race.Menu.MenuHandler

class Main: JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: Main
            private set
    }

    lateinit var gameManager: GameManager
    lateinit var menuHandler: MenuHandler

    override fun onEnable() {
        Main.plugin = this

        if(!this.dataFolder.exists()){
            this.dataFolder.mkdir()
        }

        this.server.getPluginCommand("race")?.setExecutor(race())

        this.server.pluginManager.registerEvents(this, this)

        this.gameManager = GameManager()
        this.menuHandler = MenuHandler()
    }
}