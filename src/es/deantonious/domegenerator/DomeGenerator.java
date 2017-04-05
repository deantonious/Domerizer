package es.deantonious.domegenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import es.deantonious.domegenerator.ParticleEffects.ColoreableParticle;

public class DomeGenerator extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("domerize")) {
			if (sender.hasPermission("domerizer.use")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (args.length == 2) {
						if (!isNumeric(args[0])) {
							player.sendMessage(ChatColor.RED + "Please, enter a valid ID...");
							return true;
						}
						if (!isNumeric(args[1])) {
							player.sendMessage(ChatColor.RED + "Radius must be a number...");
							return true;
						}
						int id = Integer.parseInt(args[0]);
						int radius = Integer.parseInt(args[1]);

						Material m = Material.getMaterial(id);
						if (m != null) {
							if (radius >= 1 && radius <= 500) {

								ItemStack iS = new ItemStack(Material.BEACON, 1);
								ItemMeta iM = iS.getItemMeta();
								iM.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Dome Generator");
								ArrayList<String> iL = new ArrayList<>();
								iL.add(ChatColor.GRAY + "Place this block to generate");
								iL.add(ChatColor.GRAY + "on Dome...");
								iL.add(id + "");
								iL.add(radius + "");
								iM.setLore(iL);
								iS.setItemMeta(iM);

								player.getInventory().addItem(iS);
								player.sendMessage(ChatColor.GOLD + "Place this Gome Generator to crate the dome...");
								return true;
							} else {
								player.sendMessage(ChatColor.RED + "The size must be a number between 1 and 500");
								return true;
							}
						} else {
							player.sendMessage(ChatColor.RED + "Please, enter a valid ID...");
							return true;
						}
					} else {
						player.sendMessage(ChatColor.RED + "Use: /domerizer <id> <size>");
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onPlayerClick(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInHand().getType() == Material.BEACON) {
			if (player.getItemInHand().hasItemMeta()) {
				if (player.getItemInHand().getItemMeta().getLore().get(0).equals(ChatColor.GRAY + "Place this block to generate")) {

					final Location centerLoc = event.getBlock().getLocation().clone();
					final Location centerPos = event.getBlock().getLocation().clone();
					event.getBlock().setType(Material.AIR);
					centerPos.setX(centerPos.getX()+0.5);
					centerPos.setY(centerPos.getY()+0.5);
					centerPos.setZ(centerPos.getZ()+0.5);
					final int id = Integer.parseInt(player.getItemInHand().getItemMeta().getLore().get(2));
					int radius = Integer.parseInt(player.getItemInHand().getItemMeta().getLore().get(3));

					player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
					player.updateInventory();

					final ArrayList<Block> blocks = new ArrayList<Block>();
					for (int i = -radius; i <= radius; i++) {
						for (int j = 0; j <= radius; j++) {
							for (int k = -radius; k <= radius; k++) {
								if ((int) Math.sqrt((i) * (i) + (j) * (j) + (k) * (k)) == radius) {
									blocks.add(centerLoc.getWorld().getBlockAt((int) (i + centerLoc.getX()), (int) (j + centerLoc.getY()), (int) (k + centerLoc.getZ())));
								}
							}
						}
					}

					int horas = (blocks.size() / 20) / 3600;
					int minutos = ((blocks.size() / 20) % 3600) / 60;

					int segundos = (blocks.size() / 20) % 60;
					player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Generating Dome...");
					player.sendMessage(ChatColor.YELLOW + "Radius: " + radius + "; " + ChatColor.RED + "Approx. Remaining Time: " + horas + "h - " + minutos + "min - " + segundos + "s.");

					new BukkitRunnable() {

						@Override
						public void run() {
							if (blocks.size() > 0) {
								blocks.get(0).setTypeId(id);
								try {
									drawLine(blocks.get(0).getLocation(), centerPos, 0.1);
								} catch (ClassNotFoundException
										| IllegalAccessException
										| IllegalArgumentException
										| InvocationTargetException
										| NoSuchMethodException
										| SecurityException
										| NoSuchFieldException
										| InstantiationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								blocks.remove(0);
							} else {
								cancel();
							}

						}
					}.runTaskTimer(this, 0, 0);
				}
			}
		}
	}

	public boolean isNumeric(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static void drawLine(Location loc1, Location loc2, double particlesPerBlock) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, InstantiationException{
        Vector v = new Vector(-(loc1.getX() - loc2.getX()), -(loc1.getY() - loc2.getY()), -(loc1.getZ() - loc2.getZ()));
        v.multiply(particlesPerBlock/(loc1.distance(loc2)/2));
        Location l = loc1.clone();
        while(l.distance(loc2) > particlesPerBlock){
        	ParticleEffects.sendToLocation("CRIT_MAGIC", l, 0, 0, 0, 0, 2, 0);
            l.add(v);
        }
    }

}
