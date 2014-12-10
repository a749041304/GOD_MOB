package org.jason.mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.spoony.JSONChatLib.JSONChatHoverEventType;
import me.spoony.chatlib.ChatPart;
import me.spoony.chatlib.MessageSender;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_7_R3.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jason.rpgitem.Items;

public class Main extends JavaPlugin implements Listener{
	
	public FileConfiguration config;
	
	private Random random = new Random();
	
	private List <Monster> monsters = new ArrayList<Monster>();
	
	private static Economy economy = null;
	
	private static Main instance;
	
	private List <Integer> dropHashCode = new ArrayList <Integer> ();
	
	public static Main getInstance () {
		return instance;
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = this.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public int getMoney(Player p) {
		int money = (int) economy.getBalance(p.getName());
		return money;
	}

	public boolean addMoney(Player p, int money) {
		economy.depositPlayer(p.getName(), money);
		return true;
	}

	public boolean payMoney(Player p, int money) {
		economy.withdrawPlayer(p.getName(), money);
		return true;
	}
	
	public void onLoad() {
		saveDefaultConfig();
		this.config = getConfig();
		loadMonsters();
		instance = this;
		System.out.println("[" + getName() + "]" + " " + getName() + "插件已加载");
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			
			@Override
			public void run() {
				World w = Bukkit.getWorld("fb");
				w.setTime(18000);
			}
		}, 0, 60 * 20);
		setupEconomy ();
		System.out.println("[" + getName() + "]" + " " + getName() + "插件已开启");
	}

	public void onDisable() {
		HandlerList.unregisterAll((Plugin)this);
		getServer().getScheduler().cancelTasks(this);
		System.out.println("[" + getName() + "]" + " " + getName() + "插件已关闭");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		String cmd = command.getName();
		Player p = (Player) sender;
		if (cmd.equalsIgnoreCase("mob")) {
			if (args[0].equalsIgnoreCase("save")) {
				if (args.length == 2) {
					Items.add(args[1], p.getItemInHand());
					sendMsg(p, getLang(3));
				}
			}
		}
		return false;
	}
	
	public void loadMonsters () {
		for (int i = 6;i >= 1; i--) {
			String name = this.config.getString("levels.level" + i + ".name").replace("&", "§");
			int range = this.config.getInt("levels.level" + i + ".range");
			int height = this.config.getInt("levels.level" + i + ".height");
			String health = this.config.getString("levels.level" + i + ".health");
			String damage = this.config.getString("levels.level" + i + ".damage");
			String exp = this.config.getString("levels.level" + i + ".exp");
			String money = this.config.getString("levels.level" + i + ".money");
			List <String> drops = this.config.getStringList("levels.level" + i + ".drops");
			List <String> skills = this.config.getStringList("levels.level" + i + ".skills");
			Monster monster = new Monster(name, range, height, health, damage, exp, money, drops, skills);
			monsters.add(monster);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn (CreatureSpawnEvent e) {
		if (e.getEntity() instanceof org.bukkit.entity.Monster) {
			LivingEntity le = e.getEntity();
			if (!le.getWorld().getName().equalsIgnoreCase("fb")) return;
			Location loc = le.getLocation();
			int range = this.random.nextInt(100);
			for (Monster monster : this.monsters) {
				if (range < monster.getRange() && loc.getY() > monster.getHeight()) {
					setEntity(le, monster);
					return;
				}
			}
			setEntity(le, this.monsters.get(this.monsters.size() - 1));
		}
	}
	
	@EventHandler
	public void onPlayerDamageByEntity (EntityDamageByEntityEvent e) {
		if (((e.getDamager() instanceof LivingEntity) || (e.getDamager() instanceof Arrow)) && e.getEntity() instanceof Player) {
			LivingEntity le = null;
			if (e.getDamager() instanceof Arrow) {
				le = ((Arrow) e.getDamager()).getShooter();
			} else {
				le = (LivingEntity) e.getDamager();
			}
			if (le.getCustomName() != null) {
				for (Monster monster : this.monsters) {
					if (le.getCustomName().indexOf(monster.getName()) != -1) {
						e.setDamage(random(monster.getDamage()));
					}
				}
			}
		}
	}
	
//	@EventHandler
//	public void onEntityDamageByPlayer (EntityDamageByEntityEvent e) {
//		if ((e.getDamager() instanceof Player) || (e.getDamager() instanceof Arrow) && e.getEntity() instanceof LivingEntity) {
//			LivingEntity le = (LivingEntity) e.getEntity();
//			Player p = null;
//			if (e.getDamager() instanceof Arrow) {
//				try {
//					p = (Player)((Arrow) e.getDamager()).getShooter();
//				} catch (Exception e2) {
//					return;
//				}
//			} else {
//				p = (Player) e.getDamager();
//			}
//			if (le.getCustomName() != null) {
//				for (Monster monster : this.monsters) {
//					if (le.getCustomName().indexOf(monster.getName()) != -1) {
//						checkMob(le, p);
//					}
//				}
//			}
//		}
//	}
	
//	@EventHandler
//	public void onEntityDamage (EntityDamageEvent e) {
//		if ((e.getEntity() instanceof LivingEntity) && e.getCause().equals(DamageCause.FALL)) {
//			LivingEntity le = (LivingEntity) e.getEntity();
//			if (le.getCustomName() != null) {
//				for (Monster monster : this.monsters) {
//					if (le.getCustomName().indexOf(monster.getName()) != -1) {
//						List <Entity> entitys = le.getNearbyEntities(le.getLocation().getX(), le.getLocation().getY(), le.getLocation().getZ());
//						for (Entity entity : entitys) {
//							if (entity instanceof Player) {
//								Player p = (Player) entity;
//								if (p.getLocation().distance(le.getLocation()) < 5) {
//									checkMob(le, p);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
	
	@EventHandler
	public void onEntityDeath (EntityDeathEvent e) {
		if (e.getEntity().getKiller() != null) {
			LivingEntity le = (LivingEntity) e.getEntity();
			Player p = le.getKiller();
			if (le.getCustomName() != null) {
				for (Monster monster : this.monsters) {
					if (le.getCustomName().indexOf(monster.getName()) != -1) {
						int exp = random(monster.getExp());
						int money = random(monster.getMoney());
						e.setDroppedExp(exp);
						addMoney(p, money);
						sendMsg(p, getLang(1).replace("{mob}", le.getCustomName()).replace("{money}", ""+money).replace("{exp}", ""+exp));
						if (!monster.getDrops().isEmpty()) {
//							for (String s : monster.getDrops())  {
//								int range = this.random.nextInt(100);
//								if (!s.equals("")) {
//									int r = Integer.parseInt(s.split(":")[0]);
//									if (range < r) {
//										ItemStack item = Items.get(s.split(":")[1]);
//										e.getDrops().add(item);
//										sendMsg(p, getLang(2).replace("{drop}", item.getItemMeta().getDisplayName()));
//										return;
//									}
//								}
//							}
							int range = this.random.nextInt(100);
							String s = monster.getDrops().get(this.random.nextInt(monster.getDrops().size()));
							if (!s.equals("")) {
								int r = Integer.parseInt(s.split(":")[0]);
								if (range < r) {
									ItemStack item = Items.get(s.split(":")[1]);
									e.getDrops().add(item);
									dropHashCode.add(item.hashCode());
									sendMsg(p, getLang(2).replace("{drop}", item.getItemMeta().getDisplayName()));
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem (PlayerPickupItemEvent e) {
		ItemStack item = e.getItem().getItemStack();
		Player p = e.getPlayer();
		for (ItemStack is : Items.getAll()) {
			if (item.hasItemMeta()) {
				String name = item.getItemMeta().getDisplayName();
				if (name != null) {
					if (name.equals(is.getItemMeta().getDisplayName())) {
						if (dropHashCode.contains(item.hashCode())) {
							sendItemMsg(p, is);
							dropHashCode.remove(new Integer(item.hashCode()));
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void setEntity (LivingEntity le, Monster monster) {
		le.setCustomNameVisible(true);
		le.setCustomName(monster.getName() + getMobName (le.getType().getTypeId()));
		le.setMaxHealth(random(monster.getHealth()));
		le.setHealth(le.getMaxHealth()); 
	}
	
//	@SuppressWarnings("deprecation")
//	public void checkMob (LivingEntity le, Player p) {
//		if (le.getTargetBlock(null, 2) != null) {
//			Location loc = le.getTargetBlock(null, 2).getLocation();
//			if (loc.getBlock() != null) {
//				if (!loc.getBlock().getType().equals(Material.AIR)) {
//					le.teleport(p.getLocation());
//				}
//			}
//		}
//	}
	
	public String getMobName (int id) {
		for (String s : this.config.getStringList("names")) {
			if (Integer.parseInt(s.split(":")[0]) == id) {
				return s.split(":")[1];
			}
		}
		return "";
	}
	
	public void sendMsg (Player p, String msg) {
		p.sendMessage(msg);
	}
	
	public String getLang (int id) {
		return this.config.getString("lang_" + id).replace("&", "§");
	}
	
	public int random (int max, int min) {
        return random.nextInt(max)%(max-min+1) + min;
	}
	public int random (String str) {
		int min = Integer.parseInt(str.split("-")[0]);
		int max = Integer.parseInt(str.split("-")[1]);
		return random.nextInt(max)%(max-min+1) + min;
	}
	
	public void sendItemMsg (Player p,ItemStack is) {
		net.minecraft.server.v1_7_R3.ItemStack nms = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = new NBTTagCompound();
        nms.save(tag);
        for (Player player : this.getServer().getOnlinePlayers()) {
        	MessageSender.sendMessage(player, new ChatPart("§a恭喜玩家§r[§d" + p.getName()+ "§r] §b获得了:§r["),new ChatPart(is.getItemMeta().getDisplayName()).setHoverEvent(JSONChatHoverEventType.SHOW_ITEM, tag.toString()),new ChatPart("§r]§e§l←←鼠标触摸可查看物品属性!"));
        }
	}
}