package cubyz.gui.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import cubyz.Constants;
import cubyz.api.CubyzRegistries;
import cubyz.api.Resource;
import cubyz.client.Cubyz;
import cubyz.client.GameLauncher;
import cubyz.gui.MenuGUI;
import cubyz.gui.components.Button;
import cubyz.gui.components.Component;
import cubyz.gui.components.TextInput;
import cubyz.multiplayer.UDPConnectionManager;
import cubyz.rendering.text.Fonts;
import cubyz.utils.Logger;
import cubyz.utils.Utils;
import cubyz.utils.translate.ContextualTextKey;
import cubyz.utils.translate.TextKey;
import cubyz.world.ClientWorld;
import cubyz.world.terrain.ClimateMapGenerator;
import cubyz.world.terrain.MapGenerator;
import pixelguys.json.JsonObject;
import cubyz.multiplayer.server.Server;

import static cubyz.client.ClientSettings.GUI_SCALE;

/**
 * GUI shown when creating a new world in a new world file.<br>
 * TODO: world seed and other related settings.
 */

public class SaveCreationGUI extends MenuGUI {
	
	private Button create;
	private Button cancel;
	private TextInput name;
	private Button mapGenerator;
	private int selectedMapGenerator;
	private Button climateGenerator;
	private int selectedClimateGenerator;
	private Resource[] mapGenerators;
	private Resource[] climateGenerators;
	
	@Override
	public void init() {
		name = new TextInput();
		create = new Button();
		cancel = new Button();
		mapGenerator = new Button();
		climateGenerator = new Button();
		
		int num = 1;
		while (new File("saves/" + Utils.escapeFolderName("Save "+num)).exists()) {
			num++;
		}
		name.setText("Save " + num);
		
		
		// TODO: Implement a "select" component for that.
		MapGenerator[] mapGen = CubyzRegistries.MAP_GENERATOR_REGISTRY.registered(new MapGenerator[0]);
		ArrayList<Resource> generatorsName = new ArrayList<>();
		selectedMapGenerator = 0;
		for (MapGenerator g : mapGen) {
			generatorsName.add(g.getRegistryID());
		}
		mapGenerators = generatorsName.toArray(new Resource[0]);
		
		mapGenerator.setText(new ContextualTextKey("gui.cubyz.saves.create.mapgen",
		                     "generator.map." + mapGen[selectedMapGenerator].getRegistryID().getMod() + "."
		                     + mapGen[selectedMapGenerator].getRegistryID().getID()).getTranslation());
		mapGenerator.setOnAction(() -> {
			selectedMapGenerator = (selectedMapGenerator + 1) % mapGenerators.length;
			Resource id = mapGenerators[selectedMapGenerator];
			mapGenerator.setText(new ContextualTextKey("gui.cubyz.saves.create.mapgen", "generator.map." + id.getMod() + "." + id.getID()).getTranslation());
		});

		ClimateMapGenerator[] climateGen = CubyzRegistries.CLIMATE_GENERATOR_REGISTRY.registered(new ClimateMapGenerator[0]);
		generatorsName.clear();
		selectedClimateGenerator = 0;
		for (int i = 0; i < climateGen.length; i++) {
			generatorsName.add(climateGen[i].getRegistryID());
			if(climateGen[i].getRegistryID().getID().equals("polar_circles"))
				selectedClimateGenerator = i; // Select polar circles as default.
		}
		climateGenerators = generatorsName.toArray(new Resource[0]);
		
		climateGenerator.setText(new ContextualTextKey("gui.cubyz.saves.create.climategen",
		                         "generator.climate." + climateGen[selectedClimateGenerator].getRegistryID().getMod() + "."
		                         + climateGen[selectedClimateGenerator].getRegistryID().getID()).getTranslation());
		climateGenerator.setOnAction(() -> {
			selectedClimateGenerator = (selectedClimateGenerator + 1) % climateGenerators.length;
			Resource id = climateGenerators[selectedClimateGenerator];
			climateGenerator.setText(new ContextualTextKey("gui.cubyz.saves.create.climategen", "generator.climate." + id.getMod() + "." + id.getID()).getTranslation());
		});

		create.setText(TextKey.createTextKey("gui.cubyz.saves.create"));
		create.setOnAction(() -> {
			String path = Utils.escapeFolderName(name.getText());
			generateSettings(path);
			new Thread(() -> Server.main(new String[]{path}), "Server Thread").start();

			Cubyz.gameUI.setMenu(null, false); // hide from UISystem.back()
			while(Server.world == null) {
				try {
					Thread.sleep(10);
				} catch(InterruptedException e) {}
			}
			try {
				GameLauncher.logic.loadWorld(new ClientWorld("127.0.0.1", new UDPConnectionManager(Constants.DEFAULT_PORT + 1, false))); // TODO: Don't go over the local network in singleplayer.
			} catch(InterruptedException e) {}
		});

		cancel.setText(TextKey.createTextKey("gui.cubyz.general.cancel"));
		cancel.setOnAction(() -> {
			Cubyz.gameUI.back();
		});

		updateGUIScale();
	}

	private void generateSettings(String name) {
		JsonObject settings = new JsonObject();
		JsonObject mapGeneratorJson = new JsonObject();
		mapGeneratorJson.put("id", mapGenerators[selectedMapGenerator].toString());
		settings.put("mapGenerator", mapGeneratorJson);
		JsonObject climateGeneratorJson = new JsonObject();
		climateGeneratorJson.put("id", climateGenerators[selectedClimateGenerator].toString());
		settings.put("climateGenerator", climateGeneratorJson);
		try {
			File file = new File("saves/" + name + "/generatorSettings.json");
			file.getParentFile().mkdirs();
			PrintWriter writer = new PrintWriter(new FileOutputStream(file), false, StandardCharsets.UTF_8);
			settings.writeObjectToStream(writer);
			writer.close();
		} catch(FileNotFoundException e) {
			Logger.error(e);
		}
	}

	@Override
	public void updateGUIScale() {
		name.setBounds(-125 * GUI_SCALE, 50 * GUI_SCALE, 250 * GUI_SCALE, 20 * GUI_SCALE, Component.ALIGN_TOP);
		name.setFont(Fonts.PIXEL_FONT, 16 * GUI_SCALE);
		
		mapGenerator.setBounds(-125 * GUI_SCALE, 80 * GUI_SCALE, 250 * GUI_SCALE, 20 * GUI_SCALE, Component.ALIGN_TOP);
		mapGenerator.setFontSize(16 * GUI_SCALE);
		
		climateGenerator.setBounds(-125 * GUI_SCALE, 110 * GUI_SCALE, 250 * GUI_SCALE, 20 * GUI_SCALE, Component.ALIGN_TOP);
		climateGenerator.setFontSize(16 * GUI_SCALE);

		create.setBounds(10 * GUI_SCALE, 30 * GUI_SCALE, 150 * GUI_SCALE, 20 * GUI_SCALE, Component.ALIGN_BOTTOM_LEFT);
		create.setFontSize(16 * GUI_SCALE);

		cancel.setBounds(60 * GUI_SCALE, 30 * GUI_SCALE, 50 * GUI_SCALE, 20 * GUI_SCALE, Component.ALIGN_BOTTOM_RIGHT);
		cancel.setFontSize(16 * GUI_SCALE);
	}

	@Override
	public void render() {
		name.render();
		mapGenerator.render();
		climateGenerator.render();
		create.render();
		cancel.render();
	}
	
	@Override
	public boolean ungrabsMouse() {
		return true;
	}

	@Override
	public boolean doesPauseGame() {
		return true;
	}

}
