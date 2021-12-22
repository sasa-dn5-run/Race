package run.dn5.Xmas

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
            return Location(world, block.x.toDouble(), block.y.toDouble(), block.z.toDouble())
        }
        fun Location(world: com.sk89q.worldedit.world.World, block: BlockVector3): Location{
            return Location(BukkitAdapter.adapt(world), block.x.toDouble(), block.y.toDouble(), block.z.toDouble())
        }
    }
}