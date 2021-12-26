package run.dn5.race.Game

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.scheduler.BukkitRunnable
import run.dn5.race.Main

class PrepareTimer(
    private val game: Game,
    private val onEnd: (()->Unit)? = null
) {
    private val plugin = Main.plugin

    private val timerRunnable: BukkitRunnable
    private val bossBar: BossBar

    init {
        val setting = this.plugin.gameManager.getSetting()
        val prepareTime = setting.getInt("prepareTime", 60)
        var remain = prepareTime

        this.bossBar = BossBar.bossBar(
            Component.text("開始まで残り${remain}秒"),
            remain.toFloat() / prepareTime.toFloat(), BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
        for (player in this.game.players) {
            player.showBossBar(this.bossBar)
        }

        this.timerRunnable = object: BukkitRunnable(){
            override fun run() {
                bossBar.name(Component.text("開始まで残り${remain}秒"))
                bossBar.progress(remain.toFloat() / prepareTime.toFloat())
                remain--
                if(remain < 0){
                    end()
                }
            }
        }
        this.timerRunnable.runTaskTimer(this.plugin, 0, 20)
    }

    private fun end(){
        this.destroy()
        this.onEnd?.invoke()
    }

    fun destroy(){
        this.timerRunnable.cancel()
        for (player in this.game.players) {
            player.hideBossBar(this.bossBar)
        }
    }
}