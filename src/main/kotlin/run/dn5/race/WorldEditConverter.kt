package run.dn5.race

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Location

class WorldEditConverter {
    companion object {
        fun BlockVector3(loc: Location): BlockVector3 {
            val x = loc.x
            val y = loc.y
            val z = loc.z
            return BlockVector3.at(x, y, z)
        }

        fun Location(world: org.bukkit.World, block: BlockVector3): Location{
            return Location(world, block.x.toDouble() + 0.5, block.y.toDouble() + 0.5, block.z.toDouble() + 0.5)
        }
        fun Location(world: com.sk89q.worldedit.world.World, block: BlockVector3): Location{
            return this.Location(BukkitAdapter.adapt(world), block)
        }
    }
}