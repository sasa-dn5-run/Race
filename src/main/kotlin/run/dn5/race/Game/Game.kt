package run.dn5.race.Game

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleDamageEvent
import org.bukkit.scheduler.BukkitRunnable
import org.spigotmc.event.entity.EntityDismountEvent
import run.dn5.race.MessageUtil
import run.dn5.race.WorldEditConverter
import run.dn5.race.Main

class Game(
    private val startRegion: Region,
    private val goalRegion: Region,
    private val lobbyRegion: Region,
    private val prepareRegion: Region,
    val players: MutableList<Player>,
    private val onEnd: (()->Unit)? = null
): Listener {

    private val plugin = Main.plugin
    private val prepareTimer = PrepareTimer(this) {
        this.start()
    }
    private var gameTimer = GameTimer(this)
    private var particleRunnable = object: BukkitRunnable(){
        override fun run() {
            val world = BukkitAdapter.adapt(startRegion.world)
            startRegion.forEach {
                val loc = WorldEditConverter.Location(world, it)
                world.spawnParticle(Particle.REDSTONE, loc, 25, 0.0, 0.0, 0.0, Particle.DustOptions(Color.RED, 0.5f))
            }
            goalRegion.forEach {
                val loc = WorldEditConverter.Location(world, it)
                world.spawnParticle(Particle.REDSTONE, loc, 25, 0.0, 0.0, 0.0, Particle.DustOptions(Color.AQUA, 0.5f))
            }
        }
    }

    private var started = false
    private var destroyed = false


    private val result = mutableListOf<PlayerData>()
    private class PlayerData(
        val player: Player,
        val time: Double,
        val min: Int,
        val sec: Int
    )

    init {
        this.plugin.server.pluginManager.registerEvents(this, this.plugin)
        this.particleRunnable.runTaskTimer(this.plugin, 0, 10)

        for (player in Bukkit.getOnlinePlayers()) {
            this.teleportToPrepare(player)
        }
    }

    private fun teleportToPrepare(player: Player){
        val setting = this.plugin.gameManager.getSetting()
        val direction = setting.getInt("config.start.direction", 0)

        val blocks = mutableListOf<BlockVector3>()
        this.prepareRegion.forEach { blocks.add(it) }
        val block = blocks.random()

        val loc = WorldEditConverter.Location(prepareRegion.world!!, block)
        loc.yaw = direction.toFloat()
        player.vehicle?.remove()
        player.teleport(loc)
        loc.world.spawnEntity(loc, EntityType.BOAT).addPassenger(player)
    }

    private fun start(){
        this.started = true
        this.gameTimer.start()

        val title = Title.title(
            Component.text("Game Start!!!"),
            Component.text("")
        )

        val blocks = mutableListOf<BlockVector3>()
        this.prepareRegion.forEach { blocks.add(it) }

        for (player in this.players) {
            player.gameMode = GameMode.ADVENTURE
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
            resultInfo.add("[${player.player.name}] ${"%0,2d".format(player.min)}:${"%0,2d".format(player.sec)}")
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
                    player.teleport(WorldEditConverter.Location(lobbyRegion.world!!, loc))
                }
            }
        }.runTaskLater(this.plugin, 20*5)

        this.onEnd?.invoke()
    }

    private fun goal(player: Player){
        if(this.result.find { it.player == player } != null) return
        val title = Title.title(Component.text("${ChatColor.AQUA}Goal!!!"), Component.text("No, 1"))
        player.showTitle(title)
        player.vehicle?.remove()

        val data = PlayerData(player, this.gameTimer.time, this.gameTimer.min, this.gameTimer.sec)
        this.result.add(data)

        if(this.result.size == this.players.size) {
            this.stop()
        }
    }

    /**
     * Event Listeners
     */

    /**
     * Detect start and goal
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent){
        if(this.startRegion.contains(WorldEditConverter.BlockVector3(event.to))) {
            if(!this.started) this.teleportToPrepare(event.player)
        }
        if(this.goalRegion.contains(WorldEditConverter.BlockVector3(event.to))){
            if(this.started) this.goal(event.player)
        }
    }
    @EventHandler
    fun onVehicleExit(event: EntityDismountEvent){
        if(this.players.contains(event.entity)){
            event.isCancelled = true
        }
    }
    @EventHandler
    fun onVehicleDamage(event: VehicleDamageEvent){
        event.isCancelled = true
    }
}