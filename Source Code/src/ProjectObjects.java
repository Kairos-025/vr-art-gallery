import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
	public static TransformGroup robotHeadRotationTG;
	public static RotationInterpolator robotHeadRotator;

	/**
     * Loads a 3D model from an OBJ file using the ObjectFile loader.
     * This version preprocesses the file content to remove lines starting with "o "
     * which can cause token errors.
     */
	public static BranchGroup loadModel(String filePath) {
        String objContent;
        try {
            objContent = readFileToString(filePath);
        } catch (IOException e) {
            System.err.println("Error reading OBJ file (" + filePath + "): " + e.getMessage());
            return null;
        }
        
        String processedContent = preprocessOBJ(objContent);
        ObjectFile loader = new ObjectFile(ObjectFile.TRIANGULATE | ObjectFile.RESIZE);
        
        // Set the base path so the loader can find the .mtl file and texture images.
        loader.setBasePath("ProjectFiles/models/");
        
        Scene modelScene = null;
        try {
            modelScene = loader.load(new StringReader(processedContent));
        } catch (Exception e) {
            System.err.println("Error loading model (" + filePath + "): " + e.getMessage());
            return null;
        }
        return modelScene.getSceneGroup();
    }
    
    /**
     * Reads a file's content into a String.
     */
    private static String readFileToString(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }
    
    /**
     * Preprocesses the OBJ content by replacing lines starting with "o " (object names)
     * with "g " (group names) so that the material definitions remain attached.
     */
    private static String preprocessOBJ(String content) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new StringReader(content));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("o ")) {
                    sb.append("g").append(line.substring(1)).append("\n");
                } else {
                    sb.append(line).append("\n");
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace(); // Should not occur with StringReader
        }
        return sb.toString();
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
			app.setMaterial(createLightingMaterial());
			
		} else {
			System.err.println("Error loading texture: " + texturePath);
		}
		return app;
	}
	
    
	/** Returns a custom material to allow objects to interact with lighting*/
	public static Material createLightingMaterial(Color3f ambientColor, Color3f diffuseColor, Color3f specularColor, float shininess) {
        Material material = new Material();
	    material.setAmbientColor(ambientColor);
	    material.setDiffuseColor(diffuseColor);
	    material.setSpecularColor(specularColor);
	    material.setShininess(shininess);
	    return material;
	}
	
	/** Returns a default material to allow objects to interact with lighting*/
	public static Material createLightingMaterial() {
		return createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f),new Color3f(1f, 1f, 1f), new Color3f(1f, 1f, 1f), 32f);
	}
    
	
    
	public static Appearance createFloorAppearance(String objectName) {
		Appearance app = createAppearance(objectName);  // Use existing texture-based appearance
	    
		// Make object interact with scene lighting (by giving it a material)		
	    Material materialLighting = createLightingMaterial(new Color3f(0.8f, 0.8f, 0.8f),new Color3f(0.8f, 0.8f, 0.8f),new Color3f(0.9f, 0.9f, 0.9f), 90f);
	    app.setMaterial(materialLighting);
	    
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(pa);
		app.setMaterial(createLightingMaterial());
	    
	    TextureAttributes texAttr = new TextureAttributes();
	    texAttr.setTextureMode(TextureAttributes.MODULATE);
	    app.setTextureAttributes(texAttr);
	    app.setPolygonAttributes(new PolygonAttributes());
	    
	    return app;
	}

	/** Recursively traverses the scene graph and sets the provided Appearance on all Shape3D nodes.	 */
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

	// Add (plant) pot at given position
	public static BranchGroup addPotAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup potModel = loadModel("ProjectFiles/models/pot.obj");
		if (potModel == null) {
			System.err.println("Failed to load pot model.");
			return bg;
		}
		
		// Set appearance
		Appearance potAppearance = createAppearance("pot");
		potAppearance.setMaterial(createLightingMaterial(new Color3f(0.2f, 0.25f, 0.2f),new Color3f(0.6f, 0.6f, 0.6f), new Color3f(0.8f, 0.8f, 0.8f), 32f));
		applyAppearance (potModel, potAppearance);

		// Position and scale object
		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(3.0f);
		
		TransformGroup tg = new TransformGroup(transform);
		tg.addChild(potModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}
	
	// Add plant at given position
	public static BranchGroup addPlantAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup plantModel = loadModel("ProjectFiles/models/plant.obj");
		if (plantModel == null) {
			System.err.println("Failed to load plant model.");
			return bg;
		}
		Appearance plantAppearance = createAppearance("plant");
		applyAppearance (plantModel, plantAppearance);

		// Position and scale object
		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(4.5f);
		
		TransformGroup tg = new TransformGroup(transform);
		tg.addChild(plantModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add camera at given position
	public static BranchGroup addCameraAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup cameraModel = loadModel("ProjectFiles/models/cctv.obj");
		if (cameraModel == null) {
			System.err.println("Failed to load camera model.");
			return bg;
		}
		Appearance cameraAppearance = createAppearance("cam");
	    applyAppearance(cameraModel, cameraAppearance);

	    // Position and scale object
		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(0.75);

		TransformGroup tg = new TransformGroup(transform);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setUserData(transform);	// Store the camera's transform in userData for later retrieval.
		tg.addChild(cameraModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	
	// Add T-Rex at given position
	public static BranchGroup addTrexAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup trexModel = loadModel("ProjectFiles/models/SculptureT-rex.obj");
		if (trexModel == null) {
			System.err.println("Failed to load T-rex model.");
			return bg;
		}
		Appearance trexAppearance = createAppearance("SculptureT-rex");

	    Material materialLighting = createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f),new Color3f(0.8f, 0.8f, 0.8f),new Color3f(0.6f, 0.6f, 0.6f), 20f);
	    trexAppearance.setMaterial(materialLighting);
	    applyAppearance(trexModel, trexAppearance);
		
	    // Position, rotate, and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D rotate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(30);
		rotate.rotY(Math.PI / 2);
		scale.mul(rotate);
		translate.setTranslation(pos);
		translate.mul(scale);
		
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(trexModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add wall at given position
	public static BranchGroup addWallAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup wallModel = loadModel("ProjectFiles/models/wall.obj");
		if (wallModel == null) {
			System.err.println("Failed to load wall model.");
			return bg;
		}

		
		// Make object interact with scene lighting (by giving it a material)	   
	    Material materialLighting = createLightingMaterial(new Color3f(0.6f, 0.6f, 0.6f),new Color3f(0.8f, 0.8f, 0.8f),new Color3f(0.3f, 0.3f, 0.3f), 70f);
	    
	    Appearance wallAppearance = new Appearance();
	    wallAppearance.setMaterial(materialLighting);
	    applyAppearance(wallModel, wallAppearance);
		
	    // Position, rotate, and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D rotate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(new Vector3d(11, 15, 11));
		rotate.rotY(rotationAngle);
		scale.mul(rotate);
		translate.setTranslation(pos);
		translate.mul(scale);

		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(wallModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add ceiling at given position and rotation
	public static BranchGroup addCeilingAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup ceilingModel = loadModel("ProjectFiles/models/ceiling.obj");
		if (ceilingModel == null) {
			System.err.println("Failed to load ceiling model.");
			return bg;
		}

		// Make object interact with scene lighting (by giving it a material)	
		Material materialLighting = createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f),new Color3f(0.9f, 0.9f, 0.9f),new Color3f(0.1f, 0.1f, 0.1f), 30f);
		Appearance ceilingAppearance = new Appearance();
	    ceilingAppearance.setMaterial(materialLighting);
	    applyAppearance(ceilingModel, ceilingAppearance);
		
	    
	    // Position, rotate, and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D rotate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(10);
		rotate.rotX(rotationAngle);
		scale.mul(rotate);
		translate.setTranslation(pos);
		translate.mul(scale);

		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(ceilingModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	
	// Add floor at given position
	public static BranchGroup addFloorAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup floorModel = loadModel("ProjectFiles/models/floor.obj");
		if (floorModel == null) {
			System.err.println("Failed to load floor model.");
			return bg;
		}
		
		// Set appearance
		Appearance floorAppearance = createFloorAppearance("floor");
		floorAppearance.getTextureAttributes().setTextureMode(TextureAttributes.MODULATE);
		applyAppearance(floorModel, floorAppearance);

		// Create a transform that flips geometry in the Y axis.
		Transform3D mirrorTransform = new Transform3D();		
		mirrorTransform.setScale(new Vector3d(1, -1, 1));
		
		// Position and scale object
		Transform3D transform = new Transform3D();
		transform.setTranslation(pos);
		transform.setScale(10);

		TransformGroup tg = new TransformGroup(transform);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.addChild(floorModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add table at given position
	public static BranchGroup addTableAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup tableModel = loadModel("ProjectFiles/models/table.obj");
		if (tableModel == null) {
			System.err.println("Failed to load table model.");
			return bg;
		}
		
		// Make object interact with scene lighting (by giving it a material)	
	    Material materialLighting = createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f),new Color3f(0.75f, 0.65f, 0.65f),new Color3f(0.65f, 0.65f, 0.65f), 32f);
		Appearance tableAppearance = createAppearance("table");
		tableAppearance.setMaterial(materialLighting);
		applyAppearance(tableModel, tableAppearance);

	    // Position and scale object
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
	// Add Woman (statue) at given position
	public static BranchGroup addWomanAt(Vector3d pos) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup womanModel = loadModel("ProjectFiles/models/woman.obj");
	    if (womanModel == null) {
	        System.err.println("Failed to load woman model.");
	        return bg;
	    }
	    
	    // Make object interact with scene lighting (by giving it a material)	    
	    Material materialLighting = createLightingMaterial(new Color3f(0.05f, 0.05f, 0.05f),new Color3f(0.9f, 0.9f, 0.85f), new Color3f(0.075f, 0.075f, 0.075f), 10f);
	    Appearance womanAppearance = createAppearance("woman_woman");
	    womanAppearance.setMaterial(materialLighting);
	    applyAppearance(womanModel, womanAppearance);

	    
	    // Rotate and scale object (static transform)
	    Transform3D staticTransform = new Transform3D();
	    Transform3D initRotate = new Transform3D();
	    staticTransform.setScale(5.0f);
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
	// Add Man (statue) at given position
	public static BranchGroup addManAt(Vector3d pos) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup manModel = loadModel("ProjectFiles/models/man.obj");
	    if (manModel == null) {
	        System.err.println("Failed to load man model.");
	        return bg;
	    }
	    
		// Make object interact with scene lighting (by giving it a material)	
	    Material materialLighting = createLightingMaterial(new Color3f(0.05f, 0.05f, 0.05f),new Color3f(0.9f, 0.9f, 0.75f), new Color3f(0.075f, 0.075f, 0.075f), 10f);
	    Appearance manAppearance = createAppearance("man_man");
	    manAppearance.setMaterial(materialLighting);
	    applyAppearance(manModel, manAppearance);


	    // Rotate and scale object (static transform)
	    Transform3D staticTransform = new Transform3D();
	    Transform3D initRotate = new Transform3D();
	    staticTransform.setScale(5.0f);
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

	// Add podium at given position
	public static BranchGroup addPodiumAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup podiumModel = loadModel("ProjectFiles/models/podium.obj");
		if (podiumModel == null) {
			System.err.println("Failed to load podium model.");
			return bg;
		}
		
		// Make object interact with scene lighting (by giving it a material)	
	    Material materialLighting = createLightingMaterial(new Color3f(0.4f, 0.4f, 0.4f),new Color3f(0.6f, 0.5f, 0.6f),new Color3f(0.7f, 0.6f, 0.7f), 80f);
	    Appearance podiumAppearance = createAppearance("podium");
	    podiumAppearance.setMaterial(materialLighting);
	    applyAppearance(podiumModel, podiumAppearance);
		
	    // Position and scale object
	    Transform3D translate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(25.0f);
		translate.setTranslation(pos);
		translate.mul(scale);
		
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(podiumModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add Light (object on ceiling) at given position
	public static BranchGroup addLightAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup lightModel = loadModel("ProjectFiles/models/light.obj");
		if (lightModel == null) {
			System.err.println("Failed to load light model.");
			return bg;
		}
		
		// Make object interact with scene lighting (by giving it a material)	   
	    Material materialLighting = createLightingMaterial(new Color3f(0.5f, 0.5f, 0.5f),new Color3f(0.5f, 0.5f, 0.5f),new Color3f(1f, 1f, 1f), 40f);
	    Appearance lightAppearance = new Appearance();
	    lightAppearance.setMaterial(materialLighting);
	    applyAppearance(lightModel, lightAppearance);
		
	    // Position and scale object
	    Transform3D translate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(5.0f);
		translate.setTranslation(pos);
		translate.mul(scale);
		
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(lightModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add speaker at given position
	public static BranchGroup addSpeakerAt(Vector3d pos) {
		BranchGroup bg = new BranchGroup();
		BranchGroup speakerModel = loadModel("ProjectFiles/models/speaker.obj");
		if (speakerModel == null) {
			System.err.println("Failed to load speaker model.");
			return bg;
		}
		
		// Make object interact with scene lighting (by giving it a material)	    
	    Material materialLighting = createLightingMaterial(new Color3f(0.8f, 0.8f, 0.8f),new Color3f(1f, 1f, 1f),new Color3f(1f, 1f, 1f), 32f);
	    Appearance speakerAppearance = createAppearance("speaker");
	    speakerAppearance.setMaterial(materialLighting);
	    applyAppearance(speakerModel, speakerAppearance);
		
	    // Position and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D scale = new Transform3D();
		scale.setScale(1.0f);
		translate.setTranslation(pos);
		translate.mul(scale);
		
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(speakerModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}

	// Add switch at given position and rotation
	public static BranchGroup addSwitchAt(Vector3d pos, double rotationAngle, int switchNumber) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup switchModel = loadModel("ProjectFiles/models/switch.obj");
	    if (switchModel == null) {
	        System.err.println("Failed to load switch model.");
	        return bg;
	    }
	    
		// Make object interact with scene lighting (by giving it a material)	   
	    Material materialLighting = createLightingMaterial(new Color3f(1f, 1f, 1f),new Color3f(1f, 1f, 1f),new Color3f(1.0f, 1.0f, 1.0f), 80f);
	    Appearance switchAppearance = createAppearance("switch");
	    switchAppearance.setMaterial(materialLighting);
	    applyAppearance(switchModel, switchAppearance);
	    
	    
	    // Position, rotate, and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D rotate = new Transform3D();
	    Transform3D scale = new Transform3D();
	    rotate.rotY(rotationAngle);
	    scale.setScale(1.0f);
	    scale.mul(rotate);
	    translate.setTranslation(pos);
	    translate.mul(scale);

	    TransformGroup tg = new TransformGroup(translate);
	    tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    tg.addChild(switchModel);

	    	    
	    // Colorful lights switch (starts "off")
	    if(switchNumber == 1) {
	        switch1TG = tg;
		    tg.setUserData("switch-off");	// Initially, mark switch as off
	        
	        // Correct switch's orientation to match on/off state
            Transform3D rotation180 = new Transform3D();
            rotation180.rotZ(Math.PI);	 	// Create a 180° rotation about Y.
            translate.mul(rotation180);	 	// Multiply the current transform by the new rotation.
            tg.setTransform(translate);	 	// Update transform
	    } 
	    
	    // Background music switch (starts "on")
	    else if(switchNumber == 2) {
	        switch2TG = tg;
		    tg.setUserData("switch-on");	// Initially, mark switch as on
	    }

	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}

	
	// Add indicator at given position
	public static BranchGroup addIndicatorAt(Vector3d pos, int indicatorNumber) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup indicatorModel = loadModel("ProjectFiles/models/indicator.obj");
	    if (indicatorModel == null) {
	        System.err.println("Failed to load indicator model.");
	        return bg;
	    }

	    // Position and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D scale = new Transform3D();
	    translate.setTranslation(pos);
	    scale.setScale(0.25f);
	    translate.mul(scale);
	    
	    
	    TransformGroup tg = new TransformGroup(translate);
	    tg.setUserData("indicator" + indicatorNumber);	// Tag the transform group so that later you know this is an indicator
	    tg.addChild(indicatorModel);

	    
	    // Create an appearance for this indicator
	    Material mat = new Material();
	    mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
	    
	    Appearance indicatorAppearance = new Appearance();
	    indicatorAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
	    
	    // Set starting appearance (material/color) of indicators
	    if(indicatorNumber==1) {									// Colorful lights (disabled at start)
	    	mat.setDiffuseColor(new Color3f(1f, 0f, 0f));	 		// Start red (off)
	    	mat.setEmissiveColor(new Color3f(1f, 0f, 0f));	 		// Start red (off)
	    	indicatorAppearance.setMaterial(mat);					// Set material
	    	indicator1TG = tg;	 									// Set indicator1TG TransformGroup to tg
	    }
	    else if(indicatorNumber==2) {								// Background music (enabled at start)
	    	mat.setDiffuseColor(new Color3f(0f, 1f, 0f));	 		// Start green (on)
	    	mat.setEmissiveColor(new Color3f(0f, 1f, 0f));	 		// Start green (on)
	    	indicatorAppearance.setMaterial(mat);					// Set material
	    	indicator2TG = tg;										// Set indicator2TG TransformGroup to tg
	    }
	    
	    // Apply the appearance only to the indicator's geometry
	    applyAppearance(indicatorModel, indicatorAppearance);
	    

	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}
	
	
	// Add stand at given position and rotation
	public static BranchGroup addStandAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup standModel = loadModel("ProjectFiles/models/stand.obj");
		if (standModel == null) {
			System.err.println("Failed to load stand model.");
			return bg;
		}
	    
		// Make object interact with scene lighting (by giving it a material)	
		Appearance standAppearance = createAppearance("stand");
		standAppearance.setMaterial(createLightingMaterial());
		applyAppearance(standModel, standAppearance);
	    
	    // Position, rotate, and scale object
		Transform3D translate = new Transform3D();
		Transform3D rotate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(7.5f);
		rotate.rotY(rotationAngle);
		scale.mul(rotate);
		translate.setTranslation(pos);
		translate.mul(scale);
		
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(standModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}
	
	
	// Add control button at given position and rotation
	public static BranchGroup addControlButtonAt(Vector3d pos, double rotationAngle, String controlType) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup buttonModel = loadModel("ProjectFiles/models/button.obj");
	    if (buttonModel == null) {
	        System.err.println("Failed to load button model.");
	        return bg;
	    }
	    
		// Make object interact with scene lighting (by giving it a material)	   	    
	   // Material materialLighting = createLightingMaterial(new Color3f(0.55f, 0.2f, 0.1f), new Color3f(0.55f, 0.2f, 0.1f), new Color3f(1.0f, 1.0f, 1.0f), 110f);
	    Material materialLighting = createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f), new Color3f(0.4f, 0.35f, 0.35f), new Color3f(1.0f, 1.0f, 1.0f), 110f);
	    Appearance buttonAppearance = createAppearance("podium");
	    buttonAppearance.setMaterial(materialLighting);
	    applyAppearance(buttonModel, buttonAppearance);
		
	    
	    // Position, rotate, and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D rotate = new Transform3D();
	    Transform3D scale = new Transform3D();
	    scale.setScale(0.5f);
	    rotate.rotZ(rotationAngle);
	    scale.mul(rotate);
	    translate.setTranslation(pos);
	    translate.mul(scale);
	    
	    TransformGroup tg = new TransformGroup(translate);
	    tg.addChild(buttonModel);
	    tg.setUserData(controlType);	// Tag this control button with its purpose
	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}
	
	// Add panel at given position and rotation
	public static BranchGroup addPanelAt(Vector3d pos, double rotationAngle) {
		BranchGroup bg = new BranchGroup();
		BranchGroup panelModel = loadModel("ProjectFiles/models/panel.obj");
		if (panelModel == null) {
			System.err.println("Failed to load panel model.");
			return bg;
		}
		
		// Make object interact with scene lighting (by giving it a material)	
	    Material materialLighting = createLightingMaterial(new Color3f(0.1f, 0.1f, 0.1f), new Color3f(0.3f, 0.31f, 0.32f), new Color3f(0.1f, 0.1f, 0.1f), 70f);
	    Appearance panelAppearance = createAppearance("stand");
	    panelAppearance.setMaterial(materialLighting);
	    applyAppearance(panelModel, panelAppearance);
	    
	    // Position, rotate, and scale object
	    Transform3D translate = new Transform3D();
	    Transform3D rotate = new Transform3D();
		Transform3D scale = new Transform3D();
		scale.setScale(10f);
		rotate.rotY(rotationAngle);
		scale.mul(rotate);
		translate.setTranslation(pos);
		translate.mul(scale);
		
		TransformGroup tg = new TransformGroup(translate);
		tg.addChild(panelModel);
		bg.addChild(tg);
		bg.compile();
		return bg;
	}
	
	
	// Add robot at given position and rotation
	public static BranchGroup addRobotAt(Vector3d pos, double rotationAngle) {
	    BranchGroup bg = new BranchGroup();
	    BranchGroup robotModel = loadModel("ProjectFiles/models/robot.obj");
	    if (robotModel == null) {
	        System.err.println("Failed to load robot model.");
	        return bg;
	    }

	    // Put all parts of robot into allParts
	    List<Shape3D> allParts = new ArrayList<>();
	    collectShape3DNodes(robotModel, allParts);
	    
	    // Parts for head rotation
	    Shape3D middleEye = (Shape3D) allParts.get(20).cloneNode(false);
	    Shape3D lowerEyes = (Shape3D) allParts.get(3).cloneNode(false);
	    Shape3D head = (Shape3D) allParts.get(16).cloneNode(false);
	    
	    // Set appearance of head
	    Appearance headApp = new Appearance();
	    headApp.setMaterial(createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f), new Color3f(0f, 0.45f, 0.45f), new Color3f(1f, 1f, 1f), 90f));
	    head.setAppearance(headApp);
	    
	    // Set appearance of eyes (middle and lower)
	    Appearance eyeApp = new Appearance();
	    eyeApp.setMaterial(createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f), new Color3f(1f, 0.7f, 0f), new Color3f(1f, 1f, 1f), 120f));
	    middleEye.setAppearance(eyeApp);
	    lowerEyes.setAppearance(eyeApp);

	    
	    // Create RotationInterpolator for robot's head
	    robotHeadRotationTG = new TransformGroup();
	    robotHeadRotationTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

	    int duration = 4000;
	    Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 0, 0, duration/2, 0, 0, duration/2, 0, 0);
	    float maxAngle = (float) Math.toRadians(50);
	    
	    robotHeadRotator = new RotationInterpolator(rotationAlpha, robotHeadRotationTG, new Transform3D(), -maxAngle, maxAngle);
	    robotHeadRotator.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));

	    // Add parts and RotationInterpolator to head rotation TransformGroup
	    robotHeadRotationTG.addChild(middleEye);
	    robotHeadRotationTG.addChild(lowerEyes);
	    robotHeadRotationTG.addChild(head);
	    robotHeadRotationTG.addChild(robotHeadRotator);
	    
	    // Add rotating head to robot
	    robotModel.addChild(robotHeadRotationTG);
	    
	    // Remove original parts
	    for(int i=0; i<allParts.size(); i++) {
	    	removeNodeFromParent(allParts.get(i));
	    }
	    
	    
	    // Set appearance of robot's main body (color + interaction with scene lighting)
	    Shape3D bodyMain[] = { (Shape3D) allParts.get(2).cloneNode(false),  (Shape3D) allParts.get(4).cloneNode(false), (Shape3D) allParts.get(6).cloneNode(false),  
	    					   (Shape3D) allParts.get(8).cloneNode(false),  (Shape3D) allParts.get(10).cloneNode(false), (Shape3D) allParts.get(11).cloneNode(false), 
	    					   (Shape3D) allParts.get(13).cloneNode(false), (Shape3D) allParts.get(18).cloneNode(false), (Shape3D) allParts.get(19).cloneNode(false), 
	    					   (Shape3D) allParts.get(22).cloneNode(false), (Shape3D) allParts.get(23).cloneNode(false), };
	    Appearance bodyMainApp = new Appearance();
	    bodyMainApp.setMaterial(createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f), new Color3f(0f, 0.35f, 0.35f), new Color3f(1f, 1f, 1f), 120f));	// color and reflectivity of parts
	    
	    // Set appearance each part
	    for(int i=0; i<bodyMain.length; i++) {
	    	bodyMain[i].setAppearance(bodyMainApp);	
		    robotModel.addChild(bodyMain[i]);
	    }
 
	    
	    // Set appearance of robot's wires (color + interaction with scene lighting)
	    Shape3D wires[] = { (Shape3D) allParts.get(5).cloneNode(false),  (Shape3D) allParts.get(7).cloneNode(false), (Shape3D) allParts.get(9).cloneNode(false),  
	    					(Shape3D) allParts.get(12).cloneNode(false), (Shape3D) allParts.get(15).cloneNode(false), (Shape3D) allParts.get(25).cloneNode(false), 
	    					(Shape3D) allParts.get(26).cloneNode(false), (Shape3D) allParts.get(27).cloneNode(false), (Shape3D) allParts.get(28).cloneNode(false), 
	    					(Shape3D) allParts.get(29).cloneNode(false), (Shape3D) allParts.get(30).cloneNode(false)  };
	    Appearance wiresApp = new Appearance();
	    wiresApp.setMaterial(createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f), new Color3f(1f, 0.5f, 0f), new Color3f(1f, 1f, 1f), 120f));	// color and reflectivity of wires
	    
	    // Set appearance each wire
	    for(int i=0; i<wires.length; i++) {
	    	wires[i].setAppearance(wiresApp);
		    robotModel.addChild(wires[i]);
	    }

	    
	    // Set appearance of robot's (accented) interior body (color + interaction with scene lighting)
	    Shape3D bodyAccents[] = { (Shape3D) allParts.get(1).cloneNode(false),  (Shape3D) allParts.get(14).cloneNode(false), (Shape3D) allParts.get(17).cloneNode(false), 
	    						  (Shape3D) allParts.get(0).cloneNode(false),  (Shape3D) allParts.get(24).cloneNode(false) };
	    Appearance bodyAccentsApp = new Appearance();
	    bodyAccentsApp.setMaterial(createLightingMaterial(new Color3f(0.2f, 0.2f, 0.2f), new Color3f(1f, 0.7f, 0f), new Color3f(1f, 1f, 1f), 120f));	// color and reflectivity of parts
	    
	    // Set appearance of each part
	    for(int i=0; i<bodyAccents.length; i++) {
	    	bodyAccents[i].setAppearance(bodyAccentsApp);
		    robotModel.addChild(bodyAccents[i]);
	    }
	    

	    // Position, rotate, and scale robot
	    Transform3D scale = new Transform3D();
	    Transform3D rotate = new Transform3D();
	    Transform3D transform = new Transform3D();
	    transform.setTranslation(pos);
	    scale.setScale(10f);
	    rotate.rotY(rotationAngle);
	    rotate.mul(scale);
	    transform.mul(rotate);
	    
	    
	    TransformGroup tg = new TransformGroup(transform);
	    tg.setUserData("robot");
	    tg.addChild(robotModel);
	    bg.addChild(tg);
	    bg.compile();
	    return bg;
	}

	
	// Remove child node from parent node
	private static void removeNodeFromParent(Node node) {
	    if (node.getParent() != null) {
	        ((Group)node.getParent()).removeChild(node);
	    }
	}

	// Collect Shape3D nodes
	private static void collectShape3DNodes(Group group, List<Shape3D> shapes) {
	    for (int i = 0; i < group.numChildren(); i++) {
	        Node child = group.getChild(i);
	        if (child instanceof Shape3D) {
	            shapes.add((Shape3D) child);
	        } else if (child instanceof Group) {
	            collectShape3DNodes((Group) child, shapes);
	        }
	    }
	}

	
	/** Combines multiple objects into a single BranchGroup. */
	public static BranchGroup addObjects() {
		BranchGroup objectsBG = new BranchGroup();
		objectsBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		objectsBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		// Position values
		double floorY = -10.0;
		double ceilingY = 14.5;
		double xLeft = -98.0, xRight = 98.0;
		double zNear = -48.0, zFar = 48.0;
		double j = 0.0;

		// Add plant pots in floor corners.
		objectsBG.addChild(addPotAt(new Vector3d(xLeft + 5, floorY - 1.5, zNear + 5)));
		objectsBG.addChild(addPotAt(new Vector3d(xLeft + 5, floorY - 1.5, zFar - 5)));
		objectsBG.addChild(addPotAt(new Vector3d(xRight - 5, floorY - 1.5, zNear + 5)));
		objectsBG.addChild(addPotAt(new Vector3d(xRight - 5, floorY - 1.5, zFar - 5)));
		
		// Add plants in floor corners.
		objectsBG.addChild(addPlantAt(new Vector3d(xLeft + 5, floorY + 1, zNear + 5)));
		objectsBG.addChild(addPlantAt(new Vector3d(xLeft + 5, floorY + 1, zFar - 5)));
		objectsBG.addChild(addPlantAt(new Vector3d(xRight - 5, floorY + 1, zNear + 5)));
		objectsBG.addChild(addPlantAt(new Vector3d(xRight - 5, floorY + 1, zFar - 5)));

		// Add cameras in ceiling corners.
		objectsBG.addChild(addCameraAt(new Vector3d(xLeft, ceilingY - 0.25, zNear)));
		objectsBG.addChild(addCameraAt(new Vector3d(xLeft, ceilingY - 0.25, zFar)));
		objectsBG.addChild(addCameraAt(new Vector3d(xRight, ceilingY - 0.25, zNear)));
		objectsBG.addChild(addCameraAt(new Vector3d(xRight, ceilingY - 0.25, zFar)));
		
		// Add T-Rex, T-Rex's podium, T-Rex's stand with button,  and robot 
		objectsBG.addChild(addTrexAt(new Vector3d(xRight - 20, floorY + 9, zNear + 48)));
		objectsBG.addChild(addPodiumAt(new Vector3d(xRight - 18, floorY - 2.5, zNear + 53)));
		objectsBG.addChild(addStandAt(new Vector3d(xRight - 40, floorY + 2.5, zNear + 43), 2 * Math.PI));
		objectsBG.addChild(addControlButtonAt(new Vector3d(xRight - 40.75, floorY + 8.5, zNear + 43), 0.22 * Math.PI, "trex-sound"));
		objectsBG.addChild(addRobotAt(new Vector3d(xRight - 28, floorY + 5, zNear + 68), 3 * Math.PI / 2));

		// Add walls
		for (int i = 0; i < 5; i++) {
			j = (i * 20) + 8;
			objectsBG.addChild(addWallAt(new Vector3d(xLeft - 2, floorY + 10, zNear + j), Math.PI / 2));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j, floorY + 10, zFar + 2), Math.PI));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j + 100, floorY + 10, zFar + 2), Math.PI));
			objectsBG.addChild(addWallAt(new Vector3d(xRight + 2, floorY + 10, zNear + j), 3 * Math.PI / 2));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j, floorY + 10, zNear - 2), 2 * Math.PI));
			objectsBG.addChild(addWallAt(new Vector3d(xLeft + j + 100, floorY + 10, zNear - 2), 2 * Math.PI));
		}

		// add Woman & Man statues, their tables, and their stands with buttons
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

		// Add ceiling
		for (int i = 0; i < 10; i++) {
			j = (i * 20) + 8;
			for (int k = 0; k < 5; k++) {
				double l = (k * 20) + 8;
				objectsBG.addChild(addCeilingAt(new Vector3d(xLeft + j, ceilingY + 0.5, zNear + l), Math.PI / 2));
			}
		}

		// Add ceiling lights (objects) and speakers
		objectsBG.addChild(addLightAt(new Vector3d(xLeft + 38, ceilingY, zNear + 25.5)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xLeft + 38, ceilingY, zNear + 45.5)));
		objectsBG.addChild(addLightAt(new Vector3d(xLeft + 38, ceilingY, zNear + 65.5)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xLeft + 38, ceilingY, zNear + 25.5)));
		objectsBG.addChild(addLightAt(new Vector3d(0, ceilingY, 0)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xRight - 38, ceilingY, zFar - 25.5)));
		objectsBG.addChild(addLightAt(new Vector3d(xRight - 38, ceilingY, zFar - 65.5)));
		objectsBG.addChild(addSpeakerAt(new Vector3d(xRight - 38, ceilingY, zFar - 45.5)));
		objectsBG.addChild(addLightAt(new Vector3d(xRight - 38, ceilingY, zFar - 25.5)));
		
		
		// Add panels behind the four video screens
		objectsBG.addChild(addPanelAt(new Vector3d(xLeft + 30, floorY + 5, zNear + 2.5), Math.PI));
		objectsBG.addChild(addPanelAt(new Vector3d(xLeft + 30, floorY + 5, zFar - 2.5), 2 * Math.PI));
		objectsBG.addChild(addPanelAt(new Vector3d(xRight - 30, floorY + 5, zNear + 2.5), Math.PI));
		objectsBG.addChild(addPanelAt(new Vector3d(xRight - 30, floorY + 5, zFar - 2.5), 2 * Math.PI));

		// Add wall switches and indicators (for colorful/white lights and background music)
		objectsBG.addChild(addSwitchAt(new Vector3d(xLeft - 1.25, floorY + 10, zNear + 13), Math.PI / 2, 1));	// Switch 1 - Colorful lights
		objectsBG.addChild(addSwitchAt(new Vector3d(xLeft - 1.25, floorY + 10, zNear + 83), Math.PI / 2, 2));	// Switch 2 - Background music
		objectsBG.addChild(addIndicatorAt(new Vector3d(xLeft - 1.45, floorY + 10.70, zNear + 13), 1));			// Indicator 1 - Colorful lights
		objectsBG.addChild(addIndicatorAt(new Vector3d(xLeft - 1.45, floorY + 10.70, zNear + 83), 2));			// Indicator 2 - Background music
		
		// Add floor
		 for (int i = 0; i < 10; i++) { 
			 j = (i * 20) + 8; 
			 for (int k = 0; k < 10; k++) { 
				 double l = (k * 10) + 3; 
				 objectsBG.addChild(addFloorAt(new Vector3d(xLeft + j, floorY - 5.75, zNear + l)));
			 } 
		 }

		// Compile
		objectsBG.compile();
		return objectsBG;
	}
}





// Robot parts
// allParts(1) = elbows   							// allParts(2) = pelvis
// allParts(4) = forearms							// allParts(5) = inside neck wire
// allParts(6) = back of shoulders/neck wire   		// allParts(7) = back of shoulders/neck wire
// allParts(8) = upper arms				    		// allParts(9) = other neck wire
// allParts(10) = toes/feet				    		// allParts(11) = middle fingers lower knuckle
// allParts(12) = inner body wire		    		// allParts(13) = outer chest ("vest")
// allParts(14) = kneecaps				    		// allParts(15) = inner body wire
// allParts(17) = inner chest (inside)		   		// allParts(18) = lower fingers lower knuckle
// allParts(19) = upper fingers lower knuckle   	// allParts(20) = central eye (DONT USE - ALREADY IN USE)
// allParts(21) = tiny rectangle kneecaps sides 	// allParts(22) = lower legs
// allParts(23) = upper legs + rest fingers+thumbs  // allParts(24) = palms
// allParts(25) = inside upper leg wire			    // allParts(26) = inside/back upper arm wire
// allParts(27) = inside lower arm wire				// allParts(28) = inside lower arm wire
// allParts(29) = inside upper leg wire			    // allParts(30) = wire on arms
