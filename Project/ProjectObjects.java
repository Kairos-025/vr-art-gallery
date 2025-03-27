package Project;

import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

public class ProjectObjects {
	public static TransformGroup manRotationTG;
	public static TransformGroup manLevitationTG;
	public static TransformGroup womanRotationTG;
	public static TransformGroup womanLevitationTG;
	public static TransformGroup switch1TG;
	public static TransformGroup switch2TG;
	public static TransformGroup indicator1TG;
	public static TransformGroup indicator2TG;

	/**
	 * Loads a 3D model from an OBJ file using the ObjectFile loader.
	 */
	public static BranchGroup loadModel(String filePath) {
		ObjectFile loader = new ObjectFile(ObjectFile.RESIZE);
		Scene modelScene = null;
		try {
			modelScene = loader.load(filePath);
		} catch (Exception e) {
			System.err.println("Error loading model (" + filePath + "): " + e.getMessage());
			return null;
		}
		return modelScene.getSceneGroup();
	}

	/**
	 * Creates a custom Appearance for an object by loading a texture file that
	 * matches the object's name.
	 */
	static Appearance createAppearance(String objectName) {
		Appearance app = new Appearance();
		String texturePath = "ProjectFiles/models/" + objectName + ".jpg";
		TextureLoader loader = new TextureLoader(texturePath, null);
		Texture texture = loader.getTexture();
		if (texture != null) {
			texture.setBoundaryModeS(Texture.WRAP);
			texture.setBoundaryModeT(Texture.WRAP);
			texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
			texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);

			TextureAttributes texAttr = new TextureAttributes();
			texAttr.setTextureMode(TextureAttributes.MODULATE);
			app.setTexture(texture);
			app.setTextureAttributes(texAttr);

			PolygonAttributes pa = new PolygonAttributes();
			pa.setCullFace(PolygonAttributes.CULL_NONE);
			app.setPolygonAttributes(pa);
		} else {
			System.err.println("Error loading texture: " + texturePath);
		}
		return app;
	}
	
	public static Appearance createFloorAppearance(String objectName) {
	    Appearance app = createAppearance(objectName);  // Use existing texture-based appearance
	    Material material = new Material();
	    material.setAmbientColor(new Color3f(0.2f, 0.2f, 0.2f));
	    material.setDiffuseColor(new Color3f(0.8f, 0.8f, 0.8f));
	    material.setSpecularColor(new Color3f(0.0f, 0.0f, 0.0f)); // Remove specular highlights
	    material.setShininess(1.0f);  // Lower shininess
	    app.setMaterial(material);

	    TextureAttributes texAttr = new TextureAttributes();
	    texAttr.setTextureMode(TextureAttributes.REPLACE); // Ignore lighting effects
	    app.setTextureAttributes(texAttr);

	    return app;
	}

	/**
	 * Recursively traverses the scene graph and sets the provided Appearance on all
	 * Shape3D nodes.
	 */
	private static void applyAppearance(Node node, Appearance app) {
		if (node instanceof Shape3D) {
			((Shape3D) node).setAppearance(app);
		} else if (node instanceof Group) {
			Group group = (Group) node;
			for (int i = 0; i < group.numChildren(); i++) {
				applyAppearance(group.getChild(i), app);
			}
		}
	}

	public static BranchGroup addPlantAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup plantModel = loadModel("ProjectFiles/models/plant.obj");
		if (plantModel == null) {
			System.err.println("Failed to load plant model.");
			return bg;
		}
		Appearance plantAppearance = createAppearance("text");
		applyAppearance(plantModel, plantAppearance);

		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(5.0f);
		TransformGroup tg = new TransformGroup(transform);
		tg.addChild(plantModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addCameraAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup cameraModel = loadModel("ProjectFiles/models/cctv.obj");
		if (cameraModel == null) {
			System.err.println("Failed to load Camera model.");
			return bg;
		}
		Appearance cameraAppearance = createAppearance("cctv");
		applyAppearance(cameraModel, cameraAppearance);

		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(0.75);

		TransformGroup tg = new TransformGroup(transform);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// Store the camera's transform in userData for later retrieval.
		tg.setUserData(transform);

		tg.addChild(cameraModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addTrexAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup trexModel = loadModel("ProjectFiles/models/SculptureT-rex.obj");
		if (trexModel == null) {
			System.err.println("Failed to load T-rex model.");
			return bg;
		}
		Appearance trexAppearance = createAppearance("SculptureT-rex");
		applyAppearance(trexModel, trexAppearance);

		// Apply scaling and a fixed rotation.
		Transform3D scale = new Transform3D();
		scale.setScale(30);
		Transform3D rotate = new Transform3D();
		rotate.rotY(Math.PI / 2);
		scale.mul(rotate);

		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);

		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(trexModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addWallAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup wallModel = loadModel("ProjectFiles/models/wall.obj");
		if (wallModel == null) {
			System.err.println("Failed to load Wall model.");
			return bg;
		}

		Transform3D scale = new Transform3D();
		scale.setScale(new Vector3d(11, 15, 11));

		Transform3D rotate = new Transform3D();
		rotate.rotY(rotationAngle);

		scale.mul(rotate);

		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);

		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(wallModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addCeilingAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup ceilingModel = loadModel("ProjectFiles/models/ceiling.obj");
		if (ceilingModel == null) {
			System.err.println("Failed to load ceiling model.");
			return bg;
		}

		Transform3D scale = new Transform3D();
		scale.setScale(10);

		Transform3D rotate = new Transform3D();
		rotate.rotX(rotationAngle);

		scale.mul(rotate);

		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);

		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(ceilingModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addFloorAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup floorModel = loadModel("ProjectFiles/models/floor.obj");
		if (floorModel == null) {
			System.err.println("Failed to load floor model.");
			return bg;
		}
		Appearance floorAppearance = createFloorAppearance("floor");
		applyAppearance(floorModel, floorAppearance);

		// Create a transform that flips geometry in the Y axis.
		Transform3D mirrorTransform = new Transform3D();
		mirrorTransform.setScale(new Vector3d(1, -1, 1));
		
		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(10);

		TransformGroup tg = new TransformGroup(transform);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// Store the camera's transform in userData for later retrieval.
		tg.setUserData(transform);

		tg.addChild(floorModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addTableAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup tableModel = loadModel("ProjectFiles/models/table.obj");
		if (tableModel == null) {
			System.err.println("Failed to load table model.");
			return bg;
		}

		Appearance tableAppearance = createAppearance("table");
		applyAppearance(tableModel, tableAppearance);

		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(6);
		TransformGroup tg = new TransformGroup(transform);
		tg.addChild(tableModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	/**
	 * Updated addWomanAt creates a hierarchy so that the woman model is first
	 * scaled and oriented, then wrapped with a RotationAnimation for continuous
	 * 360° rotation (using ProjectAnimations), and finally translated into
	 * position.
	 */
	public static BranchGroup addWomanAt(Vector3d pos) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup womanModel = loadModel("ProjectFiles/models/woman.obj");
	    if (womanModel == null) {
	        System.err.println("Failed to load woman model.");
	        return bg;
	    }
	    Appearance womanAppearance = createAppearance("woman_ao");
	    applyAppearance(womanModel, womanAppearance);

	    // Static transform: scale and fixed rotation.
	    Transform3D staticTransform = new Transform3D();
	    staticTransform.setScale(5.0f);
	    Transform3D initRotate = new Transform3D();
	    initRotate.rotY(Math.PI / 2);
	    staticTransform.mul(initRotate);
	    TransformGroup staticTG = new TransformGroup(staticTransform);
	    staticTG.addChild(womanModel);

	    // Create rotation behavior.
	    TransformGroup rotatingTG = ProjectAnimations.createRotatingTG(staticTG, 8000);
	    womanRotationTG = rotatingTG; // save reference

	    // Create levitation behavior (applied on top of the rotation).
	    TransformGroup levitatingTG = ProjectAnimations.createLevitationTG(rotatingTG, 0.0f, 2.5f, 10000);
	    womanLevitationTG = levitatingTG; // save reference

	    // Translation into position.
	    Transform3D translate = new Transform3D();
	    translate.setTranslation(new Vector3d(pos.x, pos.y, pos.z));
	    TransformGroup translateTG = new TransformGroup(translate);
	    translateTG.addChild(levitatingTG);

	    bg.addChild(translateTG);
	    bg.compile();
	    return bg;
	}
	/**
	 * Updated addManAt creates a hierarchy so that the man model is first scaled
	 * and oriented, then wrapped with a RotationAnimation for continuous 360°
	 * rotation (using ProjectAnimations), and finally translated into position.
	 */
	public static BranchGroup addManAt(Vector3d pos) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup manModel = loadModel("ProjectFiles/models/man.obj");
	    if (manModel == null) {
	        System.err.println("Failed to load man model.");
	        return bg;
	    }
	    Appearance manAppearance = createAppearance("man_ao");
	    applyAppearance(manModel, manAppearance);

	    // Static transform: scale and fixed rotation.
	    Transform3D staticTransform = new Transform3D();
	    staticTransform.setScale(5.0f);
	    Transform3D initRotate = new Transform3D();
	    initRotate.rotY(Math.PI / 2);
	    staticTransform.mul(initRotate);
	    TransformGroup staticTG = new TransformGroup(staticTransform);
	    staticTG.addChild(manModel);

	    // Create rotation behavior.
	    TransformGroup rotatingTG = ProjectAnimations.createRotatingTG(staticTG, 8000);
	    manRotationTG = rotatingTG; // save reference

	    // Create levitation behavior (applied on top of the rotation).
	    TransformGroup levitatingTG = ProjectAnimations.createLevitationTG(rotatingTG, 0.0f, 2.5f, 10000);
	    manLevitationTG = levitatingTG; // save reference

	    // Translation into position.
	    Transform3D translate = new Transform3D();
	    translate.setTranslation(new Vector3d(pos.x, pos.y, pos.z));
	    TransformGroup translateTG = new TransformGroup(translate);
	    translateTG.addChild(levitatingTG);

	    bg.addChild(translateTG);
	    bg.compile();
	    return bg;
	}

	public static BranchGroup addPodiumAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup podiumModel = loadModel("ProjectFiles/models/podium.obj");
		if (podiumModel == null) {
			System.err.println("Failed to load podium model.");
			return bg;
		}
		Appearance podiumAppearance = createAppearance("podium");
		applyAppearance(podiumModel, podiumAppearance);
		Transform3D scale = new Transform3D();
		scale.setScale(25.0f);
		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(podiumModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addLightAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup lightModel = loadModel("ProjectFiles/models/light.obj");
		if (lightModel == null) {
			System.err.println("Failed to load light model.");
			return bg;
		}
		// Appearance lightAppearance = createAppearance("light");
		// applyAppearance(lightModel, lightAppearance);
		Transform3D scale = new Transform3D();
		scale.setScale(5.0f);
		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(lightModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addSpeakerAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup speakerModel = loadModel("ProjectFiles/models/speaker.obj");
		if (speakerModel == null) {
			System.err.println("Failed to load speaker model.");
			return bg;
		}
		Appearance speakerAppearance = createAppearance("speaker");
		applyAppearance(speakerModel, speakerAppearance);
		Transform3D scale = new Transform3D();
		scale.setScale(1.0f);
		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(speakerModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	public static BranchGroup addSwitchAt(Vector3d pos, double rotationAngle, int switchNumber) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup switchModel = loadModel("ProjectFiles/models/switch.obj");
	    if (switchModel == null) {
	        System.err.println("Failed to load switch model.");
	        return bg;
	    }
	    Appearance switchAppearance = createAppearance("switch");
	    applyAppearance(switchModel, switchAppearance);
	    Transform3D scale = new Transform3D();
	    scale.setScale(1.0f);

	    Transform3D rotate = new Transform3D();
	    rotate.rotY(rotationAngle);

	    scale.mul(rotate);

	    Transform3D translate = new Transform3D();
	    translate.setTranslation(pos);
	    translate.mul(scale);

	    TransformGroup tg = new TransformGroup(translate);
	    tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    // Initially, mark the switch as off
	    tg.setUserData("switch-off");
	    tg.addChild(switchModel);

	    // Store the correct reference based on the switch number
	    if(switchNumber == 1) {
	        switch1TG = tg;
	    } else if(switchNumber == 2) {
	        switch2TG = tg;
	    }

	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}

	
	public static BranchGroup addIndicatorAt(Vector3d pos, int indicatorNumber) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup indicatorModel = loadModel("ProjectFiles/models/indicator.obj");
	    if (indicatorModel == null) {
	        System.err.println("Failed to load indicator model.");
	        return bg;
	    }
	    // Create a fresh Appearance instance for this indicator
	    Appearance indicatorAppearance = new Appearance();
	    indicatorAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
	    
	    Material mat = new Material();
	    mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
	    // Start with red (off)
	    mat.setDiffuseColor(new Color3f(1f, 0f, 0f));
	    indicatorAppearance.setMaterial(mat);
	    
	    // Apply the appearance only to the indicator's geometry
	    applyAppearance(indicatorModel, indicatorAppearance);

	    Transform3D scale = new Transform3D();
	    scale.setScale(0.25f);
	    Transform3D translate = new Transform3D();
	    translate.setTranslation(pos);
	    translate.mul(scale);
	    
	    TransformGroup tg = new TransformGroup(translate);
	    // Tag the transform group so that later you know this is an indicator
	    tg.setUserData("indicator" + indicatorNumber);
	    tg.addChild(indicatorModel);

	    if(indicatorNumber == 1) {
	        indicator1TG = tg;
	    } else if(indicatorNumber == 2) {
	        indicator2TG = tg;
	    }

	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}



	public static BranchGroup addStandAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup standModel = loadModel("ProjectFiles/models/stand.obj");
		if (standModel == null) {
			System.err.println("Failed to load stand model.");
			return bg;
		}
		//Appearance standAppearance = createAppearance("stand");
		//applyAppearance(standModel, standAppearance);
		Transform3D scale = new Transform3D();
		scale.setScale(7.5f);

		Transform3D rotate = new Transform3D();
		rotate.rotY(rotationAngle);

		scale.mul(rotate);

		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(standModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}
	
	public static BranchGroup addControlButtonAt(Vector3d pos, double rotationAngle, String controlType) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup buttonModel = loadModel("ProjectFiles/models/button.obj");
	    if (buttonModel == null) {
	        System.err.println("Failed to load button model.");
	        return bg;
	    }
	    Transform3D scale = new Transform3D();
	    scale.setScale(0.5f);
	    Transform3D rotate = new Transform3D();
	    rotate.rotZ(rotationAngle);
	    scale.mul(rotate);
	    Transform3D translate = new Transform3D();
	    translate.setTranslation(pos);
	    translate.mul(scale);
	    TransformGroup tg = new TransformGroup(translate);
	    tg.addChild(buttonModel);
	    // Tag this control button with its purpose
	    tg.setUserData(controlType);
	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}

	
	public static BranchGroup addPanelAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup panelModel = loadModel("ProjectFiles/models/panel.obj");
		if (panelModel == null) {
			System.err.println("Failed to load panel model.");
			return bg;
		}
		//Appearance panelAppearance = createAppearance("panel");
		//applyAppearance(panelModel, panelAppearance);
		Transform3D scale = new Transform3D();
		scale.setScale(40f);

		Transform3D rotate = new Transform3D();
		rotate.rotY(rotationAngle);

		scale.mul(rotate);

		Transform3D translate = new Transform3D();
		translate.setTranslation(pos);
		translate.mul(scale);
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(panelModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	/**
	 * Combines multiple objects into a single BranchGroup.
	 */
	public static BranchGroup addObjects() {
		BranchGroup objectsBG = new BranchGroup();
		objectsBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		objectsBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		double floorY = -10.0;
		double ceilingY = 14.5;
		double xLeft = -98.0, xRight = 98.0;
		double zNear = -48.0, zFar = 48.0;
		double j = 0.0;

		// Plants at floor corners.
		objectsBG.addChild(addPlantAt(new Vector3d(xLeft + 5, floorY, zNear + 5)));
		objectsBG.addChild(addPlantAt(new Vector3d(xLeft + 5, floorY, zFar - 5)));
		objectsBG.addChild(addPlantAt(new Vector3d(xRight - 5, floorY, zNear + 5)));
		objectsBG.addChild(addPlantAt(new Vector3d(xRight - 5, floorY, zFar - 5)));

		// Cameras at ceiling corners.
		objectsBG.addChild(addCameraAt(new Vector3d(xLeft, ceilingY - 0.25, zNear)));
		objectsBG.addChild(addCameraAt(new Vector3d(xLeft, ceilingY - 0.25, zFar)));
		objectsBG.addChild(addCameraAt(new Vector3d(xRight, ceilingY - 0.25, zNear)));
		objectsBG.addChild(addCameraAt(new Vector3d(xRight, ceilingY - 0.25, zFar)));

		objectsBG.addChild(addTrexAt(new Vector3d(xRight - 20, floorY + 9, zNear + 48)));
		objectsBG.addChild(addPodiumAt(new Vector3d(xRight - 18, floorY - 2.5, zNear + 53)));
		objectsBG.addChild(addStandAt(new Vector3d(xRight - 40, floorY + 2.5, zNear + 43), 2 * Math.PI));
		objectsBG.addChild(addControlButtonAt(new Vector3d(xRight - 40.75, floorY + 8.5, zNear + 43), 0.22 * Math.PI, "trex-sound"));
		objectsBG.addChild(addPanelAt(new Vector3d(xRight + 28, floorY + 5, zNear + 57), Math.PI / 2));

		for (int i = 0; i < 5; i++) {
			j = (i * 20) + 8;
			objectsBG.addChild(addWallAt(new Vector3d(xLeft - 2, floorY + 10, zNear + j), Math.PI / 2));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j, floorY + 10, zFar + 2), Math.PI));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j + 100, floorY + 10, zFar + 2), Math.PI));
			objectsBG.addChild(addWallAt(new Vector3d(xRight + 2, floorY + 10, zNear + j), 3 * Math.PI / 2));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j, floorY + 10, zNear - 2), 2 * Math.PI));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j + 100, floorY + 10, zNear - 2), 2 * Math.PI));
		}

		objectsBG.addChild(addTableAt(new Vector3d(xLeft + 10, floorY, zNear + 28)));
		objectsBG.addChild(addStandAt(new Vector3d(xLeft + 22.5, floorY + 2.5, zNear + 28), Math.PI));
		objectsBG.addChild(addControlButtonAt(new Vector3d(xLeft + 23.25, floorY + 8.5, zNear + 29), 1.78 * Math.PI, "woman-rotation"));
		objectsBG.addChild(addControlButtonAt(new Vector3d(xLeft + 23.25, floorY + 8.5, zNear + 27), 1.78 * Math.PI, "woman-levitation"));
		objectsBG.addChild(addWomanAt(new Vector3d(xLeft + 10, floorY + 10, zNear + 28)));
		objectsBG.addChild(addTableAt(new Vector3d(xLeft + 10, floorY, zNear + 68)));
		objectsBG.addChild(addStandAt(new Vector3d(xLeft + 22.5, floorY + 2.5, zNear + 68), Math.PI));
		objectsBG.addChild(addControlButtonAt(new Vector3d(xLeft + 23.25, floorY + 8.5, zNear + 69), 1.78 * Math.PI, "man-rotation"));
		objectsBG.addChild(addControlButtonAt(new Vector3d(xLeft + 23.25, floorY + 8.5, zNear + 67), 1.78 * Math.PI, "man-levitation"));
		objectsBG.addChild(addManAt(new Vector3d(xLeft + 11, floorY + 10, zNear + 68)));

		for (int i = 0; i < 10; i++) {
			j = (i * 20) + 8;
			for (int k = 0; k < 5; k++) {
				double l = (k * 20) + 8;
				objectsBG.addChild(addCeilingAt(new Vector3d(xLeft + j, ceilingY + 0.5, zNear + l), Math.PI / 2));
			}
		}

		objectsBG.addChild(addLightAt(new Vector3d(xLeft + 28, ceilingY, zNear + 28)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xLeft + 28, ceilingY, zNear + 48)));
		objectsBG.addChild(addLightAt(new Vector3d(xLeft + 28, ceilingY, zNear + 68)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xLeft + 98, ceilingY, zNear + 28)));
		objectsBG.addChild(addLightAt(new Vector3d(0, ceilingY, 0)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xRight - 98, ceilingY, zFar - 28)));
		objectsBG.addChild(addLightAt(new Vector3d(xRight - 28, ceilingY, zFar - 68)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xRight - 28, ceilingY, zFar - 48)));
		objectsBG.addChild(addLightAt(new Vector3d(xRight - 28, ceilingY, zFar - 28)));

		objectsBG.addChild(addSwitchAt(new Vector3d(xLeft - 1.25, floorY + 10, zNear + 13), Math.PI / 2, 1));
		objectsBG.addChild(addIndicatorAt(new Vector3d(xLeft - 1.45, floorY + 10.70, zNear + 13), 1));
		objectsBG.addChild(addSwitchAt(new Vector3d(xLeft - 1.25, floorY + 10, zNear + 83), Math.PI / 2, 2));
		objectsBG.addChild(addIndicatorAt(new Vector3d(xLeft - 1.45, floorY + 10.70, zNear + 83), 2));

		
		 for (int i = 0; i < 10; i++) { 
			 j = (i * 20) + 8; 
			 for (int k = 0; k < 10; k++) { 
				 double l = (k * 10) + 3; 
				 objectsBG.addChild(addFloorAt(new Vector3d(xLeft + j, floorY - 5.75, zNear + l)));
			 } 
		 }

		objectsBG.compile();
		return objectsBG;
	}
}
