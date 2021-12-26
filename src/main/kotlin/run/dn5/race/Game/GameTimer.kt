package run.dn5.race.Game

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.scheduler.BukkitRunnable
import run.dn5.race.Main

class GameTimer(
    private val game: Game
) {

    private val plugin = Main.plugin
    var time = 0.0
        private set

    var sec = 0
        private set
    var min = 0
        private set

    private val bossBar = BossBar.bossBar(
        Component.text("${"%0,2d".format(this.min)}:${"%0,2d".format(this.sec)}"),
        1f,
        BossBar.Color.BLUE,
        BossBar.Overlay.NOTCHED_12
    )

    private var started = false

    private val runnable: BukkitRunnable = object : BukkitRunnable() {
        override fun run() {
            time += 0.05
            sec = time.toInt() - min * 60
            min = (time / 60).toInt()

            bossBar.name(Component.text("${"%0,2d".format(min)}:${"%0,2d".format(sec)}"))
        }
    }

    fun start(){
        this.started = true
        this.runnable.runTaskTimer(this.plugin, 0, 1)
        for (player in this.game.players) {
            player.showBossBar(this.bossBar)
        }
    }

    fun destroy(){
        if(this.started) this.runnable.cancel()
        for (player in this.game.players) {
            player.hideBossBar(this.bossBar)
        }
    }
}