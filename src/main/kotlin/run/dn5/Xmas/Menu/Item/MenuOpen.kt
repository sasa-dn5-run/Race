package run.dn5.Xmas.Menu.Item

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class MenuOpen: ItemStack(Material.BOOK) {
    companion object {
        const val localizedName = "xmas:menuopen"
        fun equals(itemStack: ItemStack): Boolean{
            val meta = itemStack.itemMeta
            if(meta == null || !meta.hasLocalizedName()) return false
            return meta.localizedName == localizedName
        }
    }
    init {
        val meta = this.itemMeta
        meta.displayName(Component.text("${ChatColor.AQUA}メニューを開く"))
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addEnchant(Enchantment.KNOCKBACK, 0, true)
        meta.setLocalizedName(localizedName)
        this.itemMeta = meta
    }
}