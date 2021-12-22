package run.dn5.Xmas.Game

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import run.dn5.Xmas.MessageUtil
import run.dn5.Xmas.Xmas
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

class GameManager {

    companion object {
        val REGION_NAMES = arrayOf("start", "goal", "lobby", "prepare")
    }

    private val plugin: Xmas = Xmas.plugin

    private var currentGame: Game? = null

    private val players = mutableListOf<Player>()

    init {
        val settingFile = File(this.plugin.dataFolder, "setting.yml")
        if(!settingFile.exists()){
            Files.copy(this.plugin.getResource("setting.yml")!!, settingFile.toPath())
        }
    }

    /**
     * Initialize methods
     */

    fun getSetting(): YamlConfiguration {
        val settingFile = File(this.plugin.dataFolder, "setting.yml")
        return YamlConfiguration.loadConfiguration(settingFile)
    }
    private fun getRegion(key: String): CuboidRegion? {
        val setting = getSetting()
        val regions = setting.getConfigurationSection("regions")

        if(regions?.getString("$key.world") == null){
            return null
        }

        val bukkitWorld = Bukkit.getWorld(regions.getString("$key.world")!!)
        val world = BukkitAdapter.adapt(bukkitWorld)
        val pos1 =
            BlockVector3.at(regions.getInt("$key.max.x"), regions.getInt("$key.max.y"), regions.getInt("$key.max.z"))
        val pos2 =
            BlockVector3.at(regions.getInt("$key.min.x"), regions.getInt("$key.min.y"), regions.getInt("$key.min.z"))
        return CuboidRegion(world, pos1, pos2)
    }


    /**
     * Setting methods
     */
    fun saveRegion(key: String){
        val file = File(this.plugin.dataFolder, "setting.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        config.set("regions.$key", "{}")

        config.save(file)
    }
    fun saveRegion(key: String, player: Player){
        val session = WorldEdit.getInstance().sessionManager.get(BukkitAdapter.adapt(player))
        if(!session.isSelectionDefined(BukkitAdapter.adapt(player.location.world))){
            player.sendMessage(MessageUtil.warn("リージョンが選択されていません。"))
            return
        }
        val selection = session.selection
        this.saveRegion(key, CuboidRegion(selection.world, selection.maximumPoint, selection.minimumPoint))
        player.sendMessage(MessageUtil.info("リージョンを設定しました。"))
    }
    private fun saveRegion(key: String, value: Region) {
        val file = File(this.plugin.dataFolder, "setting.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        val maximum = mapOf("x" to value.maximumPoint.x, "y" to value.maximumPoint.y, "z" to value.maximumPoint.z)
        val minimum = mapOf("x" to value.minimumPoint.x, "y" to value.minimumPoint.y, "z" to value.minimumPoint.z)

        config.set("regions.$key.max", maximum)
        config.set("regions.$key.min", minimum)
        config.set("regions.$key.world", value.world?.name)

        config.save(file)
    }

    fun addPlayer(player: Player){
        if(this.players.contains(player)) {
            player.sendMessage(MessageUtil.warn("既に参加済みです。次のゲームに参加できます。"))
            return
        }
        this.players.add(player)
        player.sendMessage(MessageUtil.info("プレイヤーリストに追加されました。次のゲームに参加できます。"))
    }
    fun removePlayer(player: Player){
        if(!this.players.contains(player)){
            player.sendMessage(MessageUtil.warn("まだゲームに参加していません。"))
            return
        }
        this.players.remove(player)
        player.sendMessage(MessageUtil.info("ゲームの参加を取り消しました。"))
    }

    /**
     * CommonMethods
     */
    fun startGame(sender: CommandSender?){
        if(this.currentGame != null){
            sender?.sendMessage(MessageUtil.warn("ゲームが既に開始されています。"))
            return
        }
        val startRegion = this.getRegion("start")
        val goalRegion = this.getRegion("goal")
        val lobbyRegion = this.getRegion("lobby")
        val prepareRegion = this.getRegion("prepare")
        if(startRegion == null || goalRegion == null || lobbyRegion == null || prepareRegion == null){
            sender?.sendMessage(MessageUtil.warn("リージョンが設定されていません。"))
            return
        }
        this.currentGame = Game(
            startRegion,
            goalRegion,
            lobbyRegion,
            prepareRegion,
            this.players
        ) {
            this.players.clear()
            this.currentGame = null
        }
    }
    fun stopGame(sender: CommandSender?){
        if(this.currentGame == null){
            sender?.sendMessage(MessageUtil.warn("ゲームが開始されていません。"))
            return
        }

        this.currentGame!!.stop()
    }
}