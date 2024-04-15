package me.yourname.example.listeners;

import me.yourname.example.ExamplePlugin;
import me.yourname.example.utilities.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof Menu))
            return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null)
            return;

        Menu menu = (Menu) holder;
        menu.handleMenu(event);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();

        if (!(holder instanceof Menu))
            return;

        ExamplePlugin.getInstance().getMenuManager().remove(player);
    }

}