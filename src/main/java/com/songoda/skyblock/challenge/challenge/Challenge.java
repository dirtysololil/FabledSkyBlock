package com.songoda.skyblock.challenge.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.songoda.core.compatibility.CompatibleMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;

public class Challenge {
	private ChallengeCategory category;
	private int id;
	private String name;
	private int maxTimes;
	private boolean showInChat;
	private List<Peer<Type, Object>> requires;
	private List<Peer<Type, Object>> rewards;
	private ItemChallenge item;

	public Challenge(ChallengeCategory category, int id, String name, int maxTimes, boolean showInChat,
			List<String> requires, List<String> rewards, ItemChallenge item) {
		this.category = category;
		this.id = id;
		this.name = name;
		this.maxTimes = maxTimes;
		this.showInChat = showInChat;
		this.item = item;
		item.setChallenge(this);
		loadChallenge(requires, rewards);
	}

	private void loadChallenge(List<String> requires, List<String> rewards) {
		this.requires = new ArrayList<>();
		this.rewards = new ArrayList<>();
		// Requires
		for (String str : requires) {
			int idx = str.indexOf(':');
			if (idx == -1)
				throw new IllegalArgumentException("Line \"" + str + "\" isn't a correct line");
			String arg0 = str.substring(0, idx);
			String arg1 = str.substring(idx + 1);
			try {
				Type t = Type.valueOf(arg0);
				this.requires.add(new Peer<Type, Object>(t, t.convert(arg1)));
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Invalid line : " + str + " : " + ex.getMessage());
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid line : " + str);
			}
		}
		// Rewards
		for (String str : rewards) {
			int idx = str.indexOf(':');
			if (idx == -1)
				throw new IllegalArgumentException("Line " + str + " isn't a correct line");
			String arg0 = str.substring(0, idx);
			String arg1 = str.substring(idx + 1);
			try {
				Type t = Type.valueOf(arg0);
				this.rewards.add(new Peer<Type, Object>(t, t.convert(arg1)));
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Invalid line : " + str + ": " + ex.getMessage());
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid line : " + str);
			}
		}
	}
	
	// GETTERS
	
	public ChallengeCategory getCategory() {
		return category;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getMaxTimes() {
		return maxTimes;
	}
	
	public boolean isShowInChat() {
		return showInChat;
	}
	
	public List<Peer<Type, Object>> getRequires() {
		return requires;
	}
	
	public List<Peer<Type, Object>> getRewards() {
		return rewards;
	}
	
	public ItemChallenge getItem() {
		return item;
	}

	public static enum Type {
		ITEM {
			// An item

			/**
			 * Convert the value to a useable ItemStack
			 */
			@Override
			public Object convert(String value) throws IllegalArgumentException {
				if (value == null || "".equalsIgnoreCase(value.trim()))
					throw new IllegalArgumentException("Value is empty or null");
				int index = value.indexOf(' ');
				// The id
				String id = index == -1 ? value : value.substring(0, index);
				// Check if it's a Minecraft item
				Material m = CompatibleMaterial.getMaterial(id).getMaterial();
				//Material m = Material.matchMaterial(id);
				if (m == null)
					throw new IllegalArgumentException(
							"\"" + id + "\" isn't a correct Minecraft Material (value = \"" + value + "\")");
				int amount = 1;
				if (index != -1) {
					String strAmount = value.substring(index + 1);
					try {
						amount = Integer.parseInt(strAmount);
					} catch (NumberFormatException ex) {
						throw new IllegalArgumentException(
								"\"" + strAmount + "\" isn't a correct number (value = \"" + value + "\")");
					}
				}
				return new ItemStack(m, amount);
			}

			@Override
			public boolean has(Player p, Object obj) {
				// Check if player has specific item in his inventory
				ItemStack is = (ItemStack) obj;
				return p.getInventory().contains(is.getType(), is.getAmount());
			}

			@Override
			public void executeRequire(Player p, Object obj) {
				// Remove specific item in player's inventory
				ItemStack is = (ItemStack) obj;
				p.getInventory().removeItem(new ItemStack(is.getType(), is.getAmount()));
				// TODO LOG
			}

			@Override
			public void executeReward(Player p, Object obj) {
				// Give specific item to player
				ItemStack is = (ItemStack) obj;
				HashMap<Integer, ItemStack> rest = p.getInventory()
						.addItem(new ItemStack(is.getType(), is.getAmount()));
				for (ItemStack restIs : rest.values())
					p.getWorld().dropItem(p.getLocation(), restIs);
				// TODO LOG
			}
		},
		CMD {
			// A command to execute

			@Override
			public Object convert(String value) throws IllegalArgumentException {
				// Here we don't have to convert the value because the value is the command
				if (value == null || "".equalsIgnoreCase(value))
					throw new IllegalArgumentException("Value is empty or null");
				return value;
			}

			@Override
			public boolean has(Player p, Object obj) {
				// CMD only works as a reward so we return false to prevent that
				return false;
			}

			@Override
			public void executeRequire(Player p, Object obj) {
				// Nothing to do here
			}

			@Override
			public void executeReward(Player p, Object obj) {
				// Obj is a String
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
						obj.toString().replaceAll("\\{player\\}", p.getName()));
			}
		},
		LEVEL {
			// The level of island of a player

			@Override
			public Object convert(String value) throws IllegalArgumentException {
				// Convert the value to an Integer representing the minimum level of island
				// required
				if (value == null || "".equalsIgnoreCase(value))
					throw new IllegalArgumentException("Value is empty or null");
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException(
							"\"" + value + "\" isn't a correct number (value = \"" + value + "\")");
				}
			}

			@Override
			public boolean has(Player p, Object obj) {
				// Check if the level of player's island is greater or equals to the required
				// level
				Island is = SkyBlockAPI.getIslandManager().getIsland(p);
				// Player doesn't have an island
				if (is == null)
					return false;
				return is.getLevel().getLevel() >= (Integer) obj;
			}

			@Override
			public void executeRequire(Player p, Object obj) {
				// Nothing to do here
			}

			@Override
			public void executeReward(Player p, Object obj) {
				// Nothing to do here
			}
		},
		NEAR {

			@Override
			public Object convert(String value) throws IllegalArgumentException {
				// We returns the entity type and the number of entity required
				if (value == null || "".equalsIgnoreCase(value))
					throw new IllegalArgumentException("Value is empty or null");
				int index = value.indexOf(' ');
				// The id
				String id = index == -1 ? value : value.substring(0, index);
				// Check if it's a Minecraft item
				EntityType et;
				try {
					et = EntityType.valueOf(id.toUpperCase());
				} catch (Exception ex) {
					throw new IllegalArgumentException(
							"\"" + id + "\" isn't a correct Minecraft EntityType (value = \"" + value + "\")");
				}
				if (et == null)
					throw new IllegalArgumentException(
							"\"" + id + "\" isn't a correct Minecraft EntityType (value = \"" + value + "\")");
				int amount = 1;
				if (index != -1) {
					String strAmount = value.substring(index + 1);
					try {
						amount = Integer.parseInt(strAmount);
					} catch (NumberFormatException ex) {
						throw new IllegalArgumentException(
								"\"" + strAmount + "\" isn't a correct number (value = \"" + value + "\")");
					}
				}
				return new Peer<EntityType, Integer>(et, amount);
			}

			@Override
			public boolean has(Player p, Object obj) {
				// Check if player is next to specific mob
				Peer<EntityType, Integer> peer = (Peer<EntityType, Integer>) obj;
				List<Entity> entities = p.getNearbyEntities(60, 60, 60);
				int count = 0;
				for (Entity e : entities) {
					if (e.getType() == peer.getKey())
						count++;
					if (count == peer.getValue())
						return true;
				}
				return false;
			}

			@Override
			public void executeRequire(Player p, Object obj) {
				// Nothing to do here
			}

			@Override
			public void executeReward(Player p, Object obj) {
				// Nothing to do here
			}
		},
		POTION {

			private Pattern space = Pattern.compile(" ");

			@Override
			public Object convert(String value) throws IllegalArgumentException {
				// We returns the potion required
				if (value == null || "".equalsIgnoreCase(value))
					throw new IllegalArgumentException("Value is empty or null");
				String[] split = space.split(value);
				if (split.length != 3)
					throw new IllegalArgumentException("Incorrect value : \"" + value + "\"");
				// The id
				// Check if it's a Minecraft item
				PotionType pt;
				try {
					pt = PotionType.valueOf(split[0].toUpperCase());
				} catch (Exception ex) {
					throw new IllegalArgumentException(
							"\"" + split[0] + "\" isn't a correct Minecraft PotionType (value = \"" + value + "\")");
				}
				if (pt == null)
					throw new IllegalArgumentException(
							"\"" + split[0] + "\" isn't a correct Minecraft PotionType (value = \"" + value + "\")");
				// The data
				int data;
				try {
					data = Integer.parseInt(split[1]);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException(
							"\"" + split[1] + "\" isn't a correct number (value = \"" + value + "\")");
				}
				if (data < 0 || data > 8)
					throw new IllegalArgumentException("Data must be between 0 and 8, but is \"" + split[1] + "\"");
				int amount;
				try {
					amount = Integer.parseInt(split[2]);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException(
							"\"" + split[2] + "\" isn't a correct number (value = \"" + value + "\")");
				}
				return new Peer<PotionType, Peer<Integer, Integer>>(pt, new Peer<>(data, amount));
			}

			@Override
			public boolean has(Player p, Object obj) {
				Peer<PotionType, Peer<Integer, Integer>> peer = (Peer<PotionType, Peer<Integer, Integer>>) obj;
				Inventory inv = p.getInventory();
				int count = 0;
				for (ItemStack is : inv) {
					if (is != null && is.getType() != Material.AIR
							&& isSame(is, peer.getKey(), peer.getValue().getKey())) {
						// Same potion
						count += is.getAmount();
						if (count >= peer.getValue().getValue())
							return true;
					}
				}
				return false;
			}

			@Override
			public void executeRequire(Player p, Object obj) {
				// Remove specific potions in player's inventory
				Peer<PotionType, Peer<Integer, Integer>> peer = (Peer<PotionType, Peer<Integer, Integer>>) obj;
				Inventory inv = p.getInventory();
				int rest = peer.getValue().getValue();
				for (ItemStack is : inv) {
					if (is != null && is.getType() != Material.AIR
							&& isSame(is, peer.getKey(), peer.getValue().getKey())) {
						// Same potion, remove it
						if (rest > is.getAmount()) {
							rest -= is.getAmount();
							is.setAmount(0);
						} else {
							is.setAmount(is.getAmount() - rest);
							p.updateInventory();
							return;
						}
					}
				}
			}

			@Override
			public void executeReward(Player p, Object obj) {
				// Give specific item to player
				Peer<PotionType, Peer<Integer, Integer>> peer = (Peer<PotionType, Peer<Integer, Integer>>) obj;
				ItemStack is = null;
				int data = peer.getValue().getKey();
				if (data <= 2)
					is = new ItemStack(Material.POTION, peer.getValue().getValue());
				else if (data <= 5)
					is = new ItemStack(Material.LINGERING_POTION, peer.getValue().getValue());
				else if (data <= 8)
					is = new ItemStack(Material.SPLASH_POTION, peer.getValue().getValue());
				PotionMeta pm = (PotionMeta) is.getItemMeta();
				pm.setBasePotionData(new PotionData(peer.getKey(), data == 1 || data == 4 || data == 7,
						data == 2 || data == 5 || data == 8));
				is.setItemMeta(pm);

				// Add item or drop if inventory is full
				HashMap<Integer, ItemStack> rest = p.getInventory().addItem(is);
				for (ItemStack restIs : rest.values())
					p.getWorld().dropItem(p.getLocation(), restIs);

				// TODO LOG
			}

			/**
			 * Check if:
			 * <ul>
			 * <li>Type of ItemStack is Potion or Lingering Potion or Splash Potion</li>
			 * <li>The potion is of type type</li>
			 * </ul>
			 * data must be:
			 * <ul>
			 * <li>0 = normal potion</li>
			 * <li>1 = extended</li>
			 * <li>2 = ++</li>
			 * <li>3 = splash</li>
			 * <li>4 = splash extended</li>
			 * <li>5 = splash ++</li>
			 * <li>6 = lingering</li>
			 * <li>7 = lingering extended</li>
			 * <li>8 = lingering ++</li>
			 * </ul>
			 * 
			 * @param is
			 * @param type
			 * @param data
			 * @return
			 */
			private boolean isSame(ItemStack is, PotionType type, int data) {
				if (data <= 2 && is.getType() != Material.POTION)
					return false;
				else if (data >= 3 && data <= 5 && is.getType() != Material.LINGERING_POTION)
					return false;
				else if (data >= 6 && data <= 8 && is.getType() != Material.SPLASH_POTION)
					return false;
				PotionMeta pm = (PotionMeta) is.getItemMeta();
				PotionData pd = pm.getBasePotionData();
				if (pd.getType() != type)
					return false;
				else if ((data == 0 || data == 3 || data == 6) && (pd.isExtended() || pd.isUpgraded()))
					return false;
				else if ((data == 1 || data == 4 || data == 7) && !pd.isExtended())
					return false;
				else if ((data == 2 || data == 5 || data == 8) && !pd.isUpgraded())
					return false;
				return true;
			}
		};

		/**
		 * Try to convert the value to a useable object used later
		 * 
		 * @param value
		 *                  The value to convert
		 * @return A useable object required
		 */
		public abstract Object convert(String value) throws IllegalArgumentException;

		/**
		 * Check if specific player has requirement for specific object
		 * 
		 * @param p
		 *                The player
		 * @param obj
		 *                The object
		 * @return true if specific player has requirement for this object
		 */
		public abstract boolean has(Player p, Object obj);

		/**
		 * Execute an action associated with specific object for specific player
		 * 
		 * @param p
		 *                The player
		 * @param obj
		 *                The object
		 */
		public abstract void executeRequire(Player p, Object obj);

		/**
		 * Give a reward to specific player for specific object
		 * 
		 * @param p
		 *                The player
		 * @param obj
		 *                The object
		 */
		public abstract void executeReward(Player p, Object obj);
	}
}
