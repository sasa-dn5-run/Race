package run.dn5.Xmas.Game

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import run.dn5.Xmas.MessageUtil
import run.dn5.Xmas.Xmas

class Game(
    private val startRegion: Region,
    private val goalRegion: Region,
    private val lobbyRegion: Region,
    val players: MutableList<Player>,
    val onEnd: (()->Unit)? = null
): Listener {

    private val plugin = Xmas.plugin
    private val prepareTimer = PrepareTimer(this) {
        this.start()
    }
    private var gameTimer = GameTimer(this)
    private var particleRunnable = object: BukkitRunnable(){
        override fun run() {
            val world = BukkitAdapter.adapt(startRegion.world)
            startRegion.forEach {
                val loc = Location(world, it.x.toDouble() + 0.5, it.y.toDouble() + 0.5, it.z.toDouble() + 0.5)
                world.spawnParticle(Particle.REDSTONE, loc, 25, 0.0, 0.0, 0.0, Particle.DustOptions(Color.RED, 0.5f))
            }
            goalRegion.forEach {
                val loc = Location(world, it.x.toDouble() + 0.5, it.y.toDouble() + 0.5, it.z.toDouble() + 0.5)
                world.spawnParticle(Particle.REDSTONE, loc, 25, 0.0, 0.0, 0.0, Particle.DustOptions(Color.AQUA, 0.5f))
            }
        }
    }

    private var started = false
    private var destroyed = false


    private val result = mutableListOf<PlayerData>()
    private class PlayerData(
        val player: Player,
        val time: Double
    )

    init {
        this.plugin.server.pluginManager.registerEvents(this, this.plugin)
        this.particleRunnable.runTaskTimer(this.plugin, 0, 10)
    }

    private fun start(){
        this.started = true
        this.gameTimer.start()

        val title = Title.title(
            Component.text("Game Start!!!"),
            Component.text("")
        )

        for (player in this.players) {
            player.showTitle(title)
            player.playSound(Sound.sound(Key.key("block.note_block.bell"), Sound.Source.BLOCK, 1f, 1f))
        }
    }

    fun stop(){
        this.destroyed = true

        this.prepareTimer.destroy()
        this.gameTimer.destroy()
        this.particleRunnable.cancel()
        HandlerList.unregisterAll(this)

        val title = Title.title(
            Component.text("${ChatColor.AQUA}GameSet!!"),
            Component.text("${ChatColor.GREEN}まもなくロビーに戻ります。")
        )

        val resultInfo = mutableListOf(
            MessageUtil.info("レースの結果")
        )
        this.result.sortedWith(compareBy { it.time })
        for(player in this.result){
            resultInfo.add("${player.player.name} : ${"%.2f".format(player.time)}")
        }

        for (player in Bukkit.getOnlinePlayers()) {
            player.showTitle(title)
            player.sendMessage(resultInfo.joinToString("\n"))
        }

        object: BukkitRunnable(){
            override fun run() {
                val blocks = mutableListOf<BlockVector3>()
                lobbyRegion.forEach { blocks.add(it) }

                for (player in Bukkit.getOnlinePlayers()) {
                    val loc = blocks.random()
                    player.teleport(Location(BukkitAdapter.adapt(lobbyRegion.world), loc.x.toDouble(), loc.y.toDouble(), loc.z.toDouble()))
                }
            }
        }.runTaskLater(this.plugin, 20*5)
    }

    /**
     * Event Listeners
     */

    /**
     * Detect start and goal
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent){
        if(!this.started) return
        if(this.startRegion.contains(BlockVector3.at(event.to.x, event.to.y, event.to.z))) {
        }
        if(this.goalRegion.contains(BlockVector3.at(event.to.x, event.to.y, event.to.z))){
            this.goal(event.player)
        }
    }

    private fun goal(player: Player){
        if(this.result.find { it.player == player } != null) return
        val title = Title.title(Component.text("${ChatColor.AQUA}Goal!!!"), Component.text("No, 1"))
        player.showTitle(title)

        val time = this.gameTimer.time
        val data = PlayerData(player, time)
        this.result.add(data)

        if(this.result.size == this.players.size) {
            this.stop()
        }
    }
}