package cubyz.client;

import java.util.ArrayDeque;
import java.util.Deque;

import cubyz.world.ClientWorld;
import org.joml.Vector3f;

import cubyz.client.entity.ClientPlayer;
import cubyz.gui.UISystem;
import cubyz.rendering.Fog;
import cubyz.rendering.RenderOctTree;

/**
 * A simple data holder for all static data that is needed for basic game functionality.
 */
public final class Cubyz {
	// stuff for rendering:
	public static Fog fog = new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.025f);
	public static UISystem gameUI = new UISystem();
	public static Deque<Runnable> renderDeque = new ArrayDeque<>();
	public static RenderOctTree chunkTree = new RenderOctTree();
	
	// World related stuff:
	public static ClientWorld world;
	public static ClientPlayer player;
	
	// Other:
	public static Vector3f playerInc = new Vector3f();
	/**Selected slot in hotbar*/
	public static int inventorySelection = 0;
	public static Vector3f dir = new Vector3f();
	public static MeshSelectionDetector msd = new MeshSelectionDetector();
}
