package run.dn5.race.Game

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import run.dn5.race.MessageUtil
import run.dn5.race.Main
import java.io.File
import java.nio.file.Files

class GameManager {

    companion object {
        val REGION_NAMES = arrayOf("start", "goal", "lobby", "prepare")
    }

    private val plugin: Main = Main.plugin

    private var currentGame: Game? = null

    val players = mutableListOf<Player>()
    val teleporters = HashMap<String, Teleporter>()

    init {
        val settingFile = File(this.plugin.dataFolder, "setting.yml")
        if(!settingFile.exists()){
            Files.copy(this.plugin.getResource("setting.yml")!!, settingFile.toPath())
        }
        val setting = this.getSetting()

        val teleporters = setting.getConfigurationSection("teleporters")
        if(teleporters != null){
            for(key in teleporters.getKeys(false)){
                this.teleporters[key] = Teleporter.createFromSection(teleporters.getConfigurationSection(key)!!)
            }
        }
    }

    /**
     * Initialize methods
     */

    fun getSetting(): YamlConfiguration {
        val settingFile = File(this.plugin.dataFolder, "setting.yml")
        return YamlConfiguration.loadConfiguration(settingFile)
    }
    private fun saveSetting(setting: YamlConfiguration){
        val settingFile = File(this.plugin.dataFolder, "setting.yml")
        setting.save(settingFile)
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
        val config = this.getSetting()

        val maximum = mapOf("x" to value.maximumPoint.x, "y" to value.maximumPoint.y, "z" to value.maximumPoint.z)
        val minimum = mapOf("x" to value.minimumPoint.x, "y" to value.minimumPoint.y, "z" to value.minimumPoint.z)

        config.set("regions.$key.max", maximum)
        config.set("regions.$key.min", minimum)
        config.set("regions.$key.world", value.world?.name)

        this.saveSetting(config)
    }

    fun createTeleporter(player: Player, id: String){
        val session = WorldEdit.getInstance().sessionManager.get(BukkitAdapter.adapt(player))
        if(!session.isSelectionDefined(BukkitAdapter.adapt(player.location.world))){
            player.sendMessage(MessageUtil.warn("リージョンが選択されていません。"))
            return
        }
        if(this.teleporters.contains(id)) this.teleporters[id]?.destroy()

        val selection = session.selection
        val region = CuboidRegion(selection.world, selection.maximumPoint, selection.minimumPoint)
        val teleporter = Teleporter(region, player.location)
        this.saveTeleporter(teleporter, id)
        this.teleporters[id] = teleporter
        player.sendMessage(MessageUtil.info("テレポーターを作成しました。"))
    }
    fun removeTeleporter(sender: CommandSender, id: String){
        this.removeTeleporter(id)
        sender.sendMessage(MessageUtil.info("テレポーターを削除しました。"))
    }
    private fun removeTeleporter(id: String){
        val setting = this.getSetting()
        val teleporter = this.teleporters[id] ?: return
        teleporter.destroy()
        this.teleporters.remove(id)
        setting.set("teleporters.$id", null)
        this.saveSetting(setting)
    }
    private fun saveTeleporter(teleporter: Teleporter, id: String){
        val setting = this.getSetting()
        setting.set("teleporters.$id", teleporter.toMap())
        this.saveSetting(setting)
    }

    fun addPlayer(player: Player){
        if(this.players.contains(player)) {
            player.sendMessage(MessageUtil.warn("既に参加済みです。次のゲームに参加できます。"))
            return
        }
        val max = this.getSetting().getInt("maxPlayer", 8)
        if(this.players.size >= max) {
            player.sendMessage(MessageUtil.warn("参加人数が上限に達しています。"))
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
    fun resetPlayers(){
        this.players.clear()
    }

    fun setStartDirection(direction: Int){
        val setting = this.getSetting()
        setting.set("config.start.direction", direction)
        this.saveSetting(setting)
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