package run.dn5.Xmas.Menu

import de.themoep.inventorygui.GuiElement
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import run.dn5.Xmas.Menu.Item.MenuOpen
import run.dn5.Xmas.Xmas

class MenuHandler : Listener {

    private val plugin = Xmas.plugin

    init {
        this.plugin.server.pluginManager.registerEvents(this, this.plugin)
    }

    private fun createMenu(): InventoryGui {
        val guiSetup = arrayOf(
            "         ",
            "         ",
            "  d b    ",
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
        val player = event.player
        val inv = player.inventory
        if (!MenuOpen.equals(inv.itemInMainHand) && !MenuOpen.equals(inv.itemInOffHand)) return

        this.createMenu().show(player)
    }
}