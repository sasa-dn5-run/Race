package run.dn5.race.Game

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import run.dn5.race.WorldEditConverter
import run.dn5.race.Main
import kotlin.math.roundToInt

class Teleporter(
    private val from: Region,
    private val location: Location
): Listener {

    companion object {
        fun createFromSection(section: ConfigurationSection): Teleporter {
            val region = CuboidRegion(
                BukkitAdapter.adapt(Bukkit.getWorld(section.getString("region.world")!!)),
                BlockVector3.at(
                    section.getInt("region.min.x"),
                    section.getInt("region.min.y"),
                    section.getInt("region.min.z")
                ),
                BlockVector3.at(
                    section.getInt("region.max.x"),
                    section.getInt("region.max.y"),
                    section.getInt("region.max.z")
                )
            )
            val dest = Location(
                Bukkit.getWorld(section.getString("dest.world")!!),
                section.getDouble("dest.x"),
                section.getDouble("dest.y"),
                section.getDouble("dest.z")
            )
            return Teleporter(
                region,
                dest
            )
        }
    }

    private val plugin = Main.plugin
    private val particleRunnable = object: BukkitRunnable(){
        override fun run() {
//            val world = BukkitAdapter.adapt(from.world)
//            from.forEach {
//                val loc = WorldEditConverter.Location(world, it)
//                world.spawnParticle(Particle.REDSTONE, loc, 25, 0.0, 0.0, 0.0, Particle.DustOptions(Color.RED, 0.5f))
//            }
        }
    }

    init {
        this.plugin.server.pluginManager.registerEvents(this, this.plugin)
        this.particleRunnable.runTaskTimer(this.plugin, 0, 10)
    }

    fun toMap(): Map<String, Any> {
        val max = mapOf("x" to this.from.maximumPoint.x, "y" to this.from.maximumPoint.y, "z" to this.from.maximumPoint.z)
        val min = mapOf("x" to this.from.minimumPoint.x, "y" to this.from.minimumPoint.y, "z" to this.from.minimumPoint.z)
        val region = mapOf("max" to max, "min" to min,"world" to this.from.world?.name)
        val dest = mapOf(
            "x" to (this.location.x * 100.0).roundToInt() / 100.0,
            "y" to (this.location.y * 100.0).roundToInt() / 100.0,
            "z" to (this.location.z * 100.0).roundToInt() / 100.0,
            "world" to this.location.world.name
        )
        return mapOf("region" to region, "dest" to dest)
    }

    fun destroy(){
        HandlerList.unregisterAll(this)
        this.particleRunnable.cancel()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent){
        if(!this.from.contains(WorldEditConverter.BlockVector3(event.to))) return
        val player = event.player

        if(player.gameMode == GameMode.SPECTATOR) return
        player.vehicle?.remove()

        val boat = this.location.world.spawnEntity(this.location, EntityType.BOAT)
        player.teleport(this.location)
        boat.addPassenger(player)
    }

}