package run.dn5.race.Menu

import de.themoep.inventorygui.GuiElement
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import run.dn5.race.Menu.Item.MenuOpen
import run.dn5.race.Main
import run.dn5.race.MessageUtil

class MenuHandler : Listener {

    private val plugin = Main.plugin

    init {
        this.plugin.server.pluginManager.registerEvents(this, this.plugin)
    }

    private fun createMenu(): InventoryGui {
        val guiSetup = arrayOf(
            "         ",
            "         ",
            "  d b p  ",
            "         ",
            "         "
        )
        val gui = InventoryGui(this.plugin, "ゲームメニュー", guiSetup)
        gui.addElement(StaticGuiElement(
            'b',
            ItemStack(Material.OAK_BOAT),
            GuiElement.Action {
                if(it.whoClicked is Player){
                    val player = it.whoClicked as Player
                    this.plugin.gameManager.addPlayer(player)
                    player.inventory.close()
                    player.playSound(Sound.sound(Key.key("block.note_block.bit"), Sound.Source.BLOCK, 1f, 1f))
                }
                return@Action true
            },
            "${ChatColor.AQUA}ゲームに参加する。"
        ))
        gui.addElement(StaticGuiElement(
            'd',
            ItemStack(Material.OAK_DOOR),
            GuiElement.Action {
                if(it.whoClicked is Player){
                    val player = it.whoClicked as Player
                    this.plugin.gameManager.removePlayer(player)
                    player.inventory.close()
                    player.playSound(Sound.sound(Key.key("block.note_block.bit"), Sound.Source.BLOCK, 1f, 1f))
                }
                return@Action true
            },
            "${ChatColor.AQUA}ゲームの参加をキャンセルする。"
        ))
        gui.addElement(StaticGuiElement(
            'p',
            ItemStack(Material.ARMOR_STAND),
            GuiElement.Action {
                if(it.whoClicked is Player){
                    val player = it.whoClicked as Player
                    player.inventory.close()
                    player.sendMessage(MessageUtil.info("現在${this.plugin.gameManager.players.size}人のプレイヤーが参加待機中です。"))
                }
                return@Action true
            },
            "${ChatColor.AQUA}現在の参加待機人数: ${this.plugin.gameManager.players.size}人"
        ))
        return gui
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val inventory = event.player.inventory
        val num = 4
        inventory.clear()
        inventory.setItem(num, MenuOpen())
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if(!(event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) return

        val player = event.player
        val inv = player.inventory
        if (!MenuOpen.equals(inv.itemInMainHand) && !MenuOpen.equals(inv.itemInOffHand)) return

        this.createMenu().show(player)
    }
}