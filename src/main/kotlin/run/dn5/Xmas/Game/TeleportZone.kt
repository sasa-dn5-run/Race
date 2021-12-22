package run.dn5.Xmas.Game

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import run.dn5.Xmas.WorldEditConverter
import run.dn5.Xmas.Xmas

class TeleportZone(
    private val from: Region,
    private val to: Region
): Listener {

    private val plugin = Xmas.plugin

    init {
        this.plugin.server.pluginManager.registerEvents(this, this.plugin)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent){
        if(!this.from.contains(WorldEditConverter.BlockVector3(event.to))) return
        val player = event.player
        if(!player.isInsideVehicle) return

        player.vehicle!!.remove()

        val blocks = mutableListOf<BlockVector3>()
        this.to.forEach { blocks.add(it) }
        val world = BukkitAdapter.adapt(this.to.world)
        val loc = WorldEditConverter.Location(world, blocks.random())

        val boat = world.spawnEntity(loc, EntityType.BOAT)
        boat.passengers.add(player)
    }

}