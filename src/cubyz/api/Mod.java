package cubyz.api;

import cubyz.world.entity.EntityType;
import cubyz.world.items.Item;


/**
 * All classes that implement this inteface will be automatically instantiated(using the DEFAULT constructor) and added to the modlist.
 */
public interface Mod {

	String id();

	String name();
	
	default void preInit() {}

	default void init() {}

	/**
	 * It is not recommended to use this directly. In most cases a json file put into assets/modName/blocks/ is enough!
	 * @param registry
	 */
	default void registerItems(Registry<Item> registry) {}

	default void registerEntities(Registry<EntityType> registry) {}

	default void postInit() {}

	/**
	 * Is called every time the player enters a world.
	 * @param registries Contains all assets, including world specific assets.
	 */
	default void postWorldGen(CurrentWorldRegistries registries) {}
}
