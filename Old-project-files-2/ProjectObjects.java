
import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

public class ProjectObjects {

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
     * Creates a custom Appearance for an object by loading a texture file that matches the object's name.
     */
    static Appearance createAppearance(String objectName) {
        Appearance app = new Appearance();
        String texturePath = "models/" + objectName + ".jpg";
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
    
    /**
     * Recursively traverses the scene graph and sets the provided Appearance on all Shape3D nodes.
     */
    private static void applyAppearance(Node node, Appearance app) {
        if (node instanceof Shape3D) {
            ((Shape3D) node).setAppearance(app);
            ((Shape3D) node).setCollidable(false); // custom boxes
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (int i = 0; i < group.numChildren(); i++) {
                applyAppearance(group.getChild(i), app);
            }
        }
    }
    
    public static BranchGroup addPlantAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        BranchGroup plantModel = loadModel("models/plant.obj");
        if (plantModel == null) {
            System.err.println("Failed to load plant model.");
            return bg;
        }
        //Appearance plantAppearance = createAppearance("plant");
        //applyAppearance(plantModel, plantAppearance);
        
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
        BranchGroup cameraModel = loadModel("models/Camera.obj");
        if (cameraModel == null) {
            System.err.println("Failed to load Camera model.");
            return bg;
        }
        Appearance cameraAppearance = createAppearance("Camera");
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


    // LOOKHERE: TRex method
    public static BranchGroup addTrexAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        BranchGroup trexModel = loadModel("models/SculptureT-rex.obj");
        if (trexModel == null) {
            System.err.println("Failed to load T-rex model.");
            return bg;
        }
        trexModel.setCollidable(false);
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
       
        // objectsBG.addChild(addTrexAt(new Vector3d(xRight-20, floorY+9, zNear+48))); // METHOD CALL
        // objectsBG.addChild(addTrexAt(new Vector3d(78, -1, 0)));		(what it is)
        
        // TODO: I ADDED CYL

        
        // Appearance of Cyl
//        Appearance app = new Appearance();
//		Material   mtl = new Material();
//		mtl.setShininess(32);
//		mtl.setAmbientColor  (new Color3f(1f, 1f, 1f));
//		mtl.setDiffuseColor  (new Color3f(0f, 1f, 1f));
//		mtl.setSpecularColor (new Color3f(0.35f, 0.35f, 0.35f));
//		mtl.setEmissiveColor (new Color3f(0f, 0f, 0f));
//		mtl.setLightingEnable(true);
//		app.setMaterial(mtl);
//		
//        app.setPolygonAttributes(new PolygonAttributes());
//	   	app.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		
        Appearance testapp = new Appearance();
		Material   mtl = new Material();
		mtl.setShininess(32);
		mtl.setAmbientColor  (new Color3f(0f, 0f, 0f));
		mtl.setDiffuseColor  (new Color3f(1f, 1f,1f));	//color
		mtl.setSpecularColor (new Color3f(0f, 0f, 0f));
		mtl.setEmissiveColor (new Color3f(0f, 0f, 0f));
		mtl.setLightingEnable(true);
        testapp.setMaterial(mtl);
        
        testapp.setPolygonAttributes(new PolygonAttributes());
        //testapp.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
        testapp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.5f));  
        
        Box testbox= new Box(12f, 10f, 12f, Box.GENERATE_NORMALS, new Appearance());
      
        
        Appearance testapp2 = new Appearance();
        
		Material   mtl2 = new Material();
		mtl2.setShininess(32);
		mtl2.setAmbientColor  (new Color3f(0f, 0f, 0f));
		mtl2.setDiffuseColor  (new Color3f(1f, 1f, 0f));
		mtl2.setSpecularColor (new Color3f(0f, 0f, 0f));
		mtl2.setEmissiveColor (new Color3f(0f, 0f, 0f));
		mtl2.setLightingEnable(true);
		testapp2.setMaterial(mtl2);
		testapp2.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0f));
		testapp2.setPolygonAttributes(new PolygonAttributes());
		//testapp2.getPolygonAttributes().setBackFaceNormalFlip(true);
	   	
	   	//TODO: Use BoundingPolytope??
		
		//BoundingSphere(Point3d center, double radius)
		

		
		// Create hitboxCylTRex (32 edges on "circle" base/top)
        //Cylinder hitboxCylTRex = new Cylinder(0.575f, 0.75f, 32, new Appearance());  
        //Box hitboxCylTRex = new Box(0.575f, 0.75f, 0.575f, Box.GENERATE_NORMALS, new Appearance());
        /*
        Box hitboxCylTRex = new Box(0.75f, 0.75f, 0.75f, Box.GENERATE_NORMALS, new Appearance());  
        hitboxCylTRex.setCapability(Box.ENABLE_COLLISION_REPORTING);
        hitboxCylTRex.setCapability(Node.ALLOW_BOUNDS_WRITE);
        hitboxCylTRex.setCapability(Node.ALLOW_BOUNDS_READ);
        hitboxCylTRex.setCollidable(true);
        hitboxCylTRex.setAlternateCollisionTarget(true);
        
        
        //System.out.println("\nhitboxCylTRex.getCollisionBounds(): "+hitboxCylTRex.getCollisionBounds());
        
        
        // Box.FRONT = 0    - Not visible, presumably side facing back wall
        // Box.BACK = 1		- Side facing "front"
        // Box.RIGHT = 2	- Side facing "left"
        // Box.LEFT = 3		- Side facing "right"
        // Box.TOP = 4		- presumably top
        // Box.BOTTOM = 5	- presumably bottom
        
        for(int i=0; i<6 && hitboxCylTRex.getShape(i)!=null; i++)
        {
        	if(i==3) { hitboxCylTRex.getShape(i).setAppearance(testapp2); }
        	else { hitboxCylTRex.getShape(i).setAppearance(testapp); }
        	
            GeometryArray geometry = (GeometryArray) hitboxCylTRex.getShape(i).getGeometry();
            for (int j = 0; j < geometry.getVertexCount(); j++) {
                Vector3f normal = new Vector3f();
                geometry.getNormal(j, normal);
                System.out.println("Normal at " + j + ": " + normal);
                
//                hitboxCylTRex.getShape(i).setBoundsAutoCompute(true);
//                hitboxCylTRex.getShape(i).setCollisionBounds(hitboxCylTRex.getShape(i).getBounds());
//                System.out.println("\t"+hitboxCylTRex.getShape(i).getBounds());
//                System.out.println("\t"+hitboxCylTRex.getShape(i).getCollisionBounds());
            }
        	
        }
*/
		
//		BoundingSphere bs = new BoundingSphere(new Point3d((0), (-0.2), (0.075)), 0.25);  //0.575f rad
//        hitboxCylTRex.setCollisionBounds(bs);
//        hitboxCylTRex.setBounds(hitboxCylTRex.getCollisionBounds());
//        
//        Point3d temp1 = new Point3d();
//        hitboxCylTRex.getCollisionBounds().getCenter(temp1);
//        System.out.println("hitboxCylTRex.getCollisionBounds(): "+hitboxCylTRex.getCollisionBounds());	// -> hitboxCylTRex.getCollisionBounds(): Center=(79.915, -5.2, -4.675)  Radius=17.200000762939453
//        System.out.println("hitboxCylTRex.getCollisionBounds().getCenter: "+temp1);	// -> hitboxCylTRex.getCollisionBounds(): Center=(79.915, -5.2, -4.675)  Radius=17.200000762939453
//
//        
//        Point3d temp2 = new Point3d();
//        hitboxCylTRex.getBounds().getCenter(temp2);
//        System.out.println("hitboxCylTRex.getBounds(): "+hitboxCylTRex.getBounds());					// -> hitboxCylTRex.getBounds(): Center=(-0.028050454661882218, -0.2260262420015656, -0.03417935995944508)  Radius=1.0378158270256108
//        System.out.println("hitboxCylTRex.getBounds().getCenter(): "+temp2);					// -> hitboxCylTRex.getBounds(): Center=(-0.028050454661882218, -0.2260262420015656, -0.03417935995944508)  Radius=1.0378158270256108
//        
        //hitboxCylTRex.getBounds().transform(translateHitboxCylTRex);

//        System.out.println("hitboxCylTRex.getCollisionBounds(): "+(hitboxCylTRex.getCollisionBounds()));
//        System.out.println("hitboxCylTRex.getBounds(): "+(hitboxCylTRex.getBounds()));
//        
//        //BoundingBox bb = new BoundingBox(new Point3d(-0.75f*translate.getScale(), -0.75f*translate.getScale(), -0.75f*translate.getScale()), new Point3d(0.75f*translate.getScale(), 0.75f*translate.getScale(), 0.75f*translate.getScale()));
//        //BoundingBox bb = new BoundingBox(new Point3d(-0.75f*scale.getScale(), -0.75f*scale.getScale(), -0.75f*scale.getScale()), new Point3d(0.75f*scale.getScale(), 0.75f*scale.getScale(), 0.75f*scale.getScale()));
//        BoundingBox bb = new BoundingBox(new Point3d(-0.75f, -0.75f, -0.75f), new Point3d(0.75f, 0.75f, 0.75f));
//        
//        hitboxCylTRex.setCollisionBounds(bb);
//        hitboxCylTRex.setBounds(bb);
//        System.out.println("hitboxCylTRex.getCollisionBounds(): "+(hitboxCylTRex.getCollisionBounds()));
//        System.out.println("hitboxCylTRex.getBounds(): "+(hitboxCylTRex.getBounds()));
//        
//        
//        Transform3D translateHitboxCylTRex = new Transform3D();
//        //translateHitboxCylTRex.setTranslation(new Vector3d((0.155), (-0.2), (0.075)));	// position cyl around TRex base
//        
//        translateHitboxCylTRex.setTranslation(new Vector3d((-0.65), (-0.2), (-0.6)));	// position cyl around TRex base
//        TransformGroup hitboxCylTRexTG = new TransformGroup(translateHitboxCylTRex);
//        
//        System.out.println("hitboxCylTRex.getCollisionBounds(): "+(hitboxCylTRex.getCollisionBounds()));
//        System.out.println("hitboxCylTRex.getBounds(): "+(hitboxCylTRex.getBounds()));
        
        /*
        Appearance wireframeAppearance = new Appearance();
        PolygonAttributes polyAttr = new PolygonAttributes();
        polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        polyAttr.setCullFace(PolygonAttributes.CULL_NONE);
        wireframeAppearance.setPolygonAttributes(polyAttr);
        ColoringAttributes colorAttr = new ColoringAttributes(new Color3f(1.0f, 0.0f, 0.0f), ColoringAttributes.SHADE_FLAT);
        wireframeAppearance.setColoringAttributes(colorAttr);

        Sphere boundingBoxVis = new Sphere(hitboxCylTRex.getRadius(), 16, wireframeAppearance);
        boundingBoxVis.setCollidable(false);
        */
        
//        Transform3D translateBoundingBoxVis = new Transform3D();
//        translateBoundingBoxVis.setTranslation(new Vector3d(temp2));	// position b around TRex base
//        TransformGroup boundingBoxVisTG = new TransformGroup(translateBoundingBoxVis);
       

        
       // hitboxCylTRexTG.addChild(boundingBoxVis);

//        	// Point3d temp2 = new Point3d();
//        	hitboxCylTRex.getBounds().transform(translateHitboxCylTRex);
//	        hitboxCylTRex.getBounds().getCenter(temp2);
//	        System.out.println("hitboxCylTRex.getBounds(): "+hitboxCylTRex.getBounds());					// -> hitboxCylTRex.getBounds(): Center=(-0.028050454661882218, -0.2260262420015656, -0.03417935995944508)  Radius=1.0378158270256108
//	        System.out.println("hitboxCylTRex.getBounds().getCenter(): "+temp2);					// -> hitboxCylTRex.getBounds(): Center=(-0.028050454661882218, -0.2260262420015656, -0.03417935995944508)  Radius=1.0378158270256108
	        

        
        
        
        
//        BoundingBox bb = new BoundingBox(new Point3d(-12, -10, -12), new Point3d(12,10,12));
//        hitboxCylTRex.setAlternateCollisionTarget(true);
//        hitboxCylTRex.setCollisionBounds(bb);
//        System.out.println(hitboxCylTRex.getCollisionBounds());
        
        /*
        Shape3D shape = hitboxCylTRex.getShape(Box.LEFT);
        GeometryArray geometry = (GeometryArray) shape.getGeometry();
        for (int i = 0; i < geometry.getVertexCount(); i++) {
            Vector3f normal = new Vector3f();
            geometry.getNormal(i, normal);
            System.out.println("Normal at " + i + ": " + normal);
        }
        */

        //testbox.getShape(3).setBackFaceNormalFlip(true);

        TransformGroup tg = new TransformGroup(translate);
        
       // hitboxCylTRex.getAppearance().setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.1f));
        //hitboxCylTRexTG.addChild(hitboxCylTRex);

        // Add to groups
        
        //TODO: RE-ENABLE TREX
        tg.addChild(trexModel);
        
        //tg.addChild(hitboxCylTRexTG);	// ADDED
        //tg.addChild(boundingBoxVisTG);	// ADDED
        bg.addChild(tg);
        bg.compile();
        return bg;
    }

    
    
    public static BranchGroup addWallAt(Vector3d pos, double rotationAngle) {
        BranchGroup bg = new BranchGroup();
        bg.setCollidable(false);	// allow custom collision
        BranchGroup wallModel = loadModel("models/wall.obj");
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
        bg.setCapability(BranchGroup.ALLOW_COLLIDABLE_WRITE);
        
        
        BranchGroup ceilingModel = loadModel("models/ceiling.obj");
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
    
    public static BranchGroup addTableAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        BranchGroup tableModel = loadModel("models/table.obj");
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
        bg.setCollidable(false);
        bg.compile();
       
        return bg;
    }

    /**
     * Updated addWomanAt creates a hierarchy so that the woman model is first scaled and oriented,
     * then wrapped with a RotationAnimation for continuous 360° rotation (using ProjectAnimations),
     * and finally translated into position.
     */
    public static BranchGroup addWomanAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        BranchGroup womanModel = loadModel("models/woman.obj");
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

        // Apply rotation behavior using ProjectAnimations.
        TransformGroup rotatingTG = ProjectAnimations.createRotatingTG(staticTG, 8000);
        // Apply levitation: oscillate between 0 and 5 units.
        TransformGroup levitatingTG = ProjectAnimations.createLevitationTG(rotatingTG, 0.0f, 2.5f, 10000);

        // Top-level translation: place the object at the given position.
        Transform3D translate = new Transform3D();
        translate.setTranslation(new Vector3d(pos.x, pos.y, pos.z));
        TransformGroup translateTG = new TransformGroup(translate);
        translateTG.addChild(levitatingTG);
        
        bg.addChild(translateTG);
        bg.compile();
        return bg;
    }

    /**
     * Updated addManAt creates a hierarchy so that the man model is first scaled and oriented,
     * then wrapped with a RotationAnimation for continuous 360° rotation (using ProjectAnimations),
     * and finally translated into position.
     */
    public static BranchGroup addManAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        bg.setCapability(BranchGroup.ALLOW_COLLIDABLE_WRITE);
        BranchGroup manModel = loadModel("models/man.obj");
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

        // Apply rotation behavior using ProjectAnimations.
        TransformGroup rotatingTG = ProjectAnimations.createRotatingTG(staticTG, 8000);
        // Apply levitation: oscillate between 0 and 5 units.
        TransformGroup levitatingTG = ProjectAnimations.createLevitationTG(rotatingTG, 0.0f, 2.5f, 10000);

        // Top-level translation: place the object at the given position.
        Transform3D translate = new Transform3D();
        translate.setTranslation(new Vector3d(pos.x, pos.y, pos.z));
        TransformGroup translateTG = new TransformGroup(translate);
        translateTG.addChild(levitatingTG);
        
        bg.addChild(translateTG);
        bg.setCollidable(false);
        bg.compile();
        
        return bg;
    }
    
    public static BranchGroup addPodiumAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        BranchGroup podiumModel = loadModel("models/podium.obj");
        if (podiumModel == null) {
            System.err.println("Failed to load podium model.");
            return bg;
        }
        podiumModel.setCollidable(false);
        Appearance podiumAppearance = createAppearance("podium");
        applyAppearance(podiumModel, podiumAppearance);
        Transform3D scale = new Transform3D();
        scale.setScale(25.0f);
        Transform3D translate = new Transform3D();
        translate.setTranslation(pos);
        translate.mul(scale);
        TransformGroup tg = new TransformGroup(translate);
        tg.addChild(podiumModel);
        //TODO: RE-ENABLE PODIUM
        bg.addChild(tg);
        bg.compile();
        return bg;
    }

    public static BranchGroup addLightAt(Vector3d pos) {
        BranchGroup bg = new BranchGroup();
        BranchGroup lightModel = loadModel("models/light.obj");
        if (lightModel == null) {
            System.err.println("Failed to load light model.");
            return bg;
        }
        //Appearance lightAppearance = createAppearance("light");
        //applyAppearance(lightModel, lightAppearance);
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
        BranchGroup speakerModel = loadModel("models/speaker.obj");
        if (speakerModel == null) {
            System.err.println("Failed to load speaker model.");
            return bg;
        }
        //Appearance lightAppearance = createAppearance("light");
        //applyAppearance(lightModel, lightAppearance);
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
    
	/*
	 * public static BranchGroup addCCTVAt(Vector3d pos) { BranchGroup bg = new
	 * BranchGroup(); // Load the model from the "models" folder. BranchGroup
	 * cctvModel = loadModel("models/cctv.obj"); if (cctvModel == null) {
	 * System.err.println("Failed to load CCTV model."); return bg; } // Optionally,
	 * create a custom Appearance if needed. // Here we use the default
	 * texture-based appearance. Appearance cctvAppearance =
	 * createAppearance("cctv"); // This will load "models/cctv.jpg" if it exists.
	 * applyAppearance(cctvModel, cctvAppearance);
	 * 
	 * // Create a TransformGroup for positioning (and scaling, if necessary).
	 * Transform3D transform = new Transform3D(); transform.setTranslation(pos); //
	 * If scaling is required (e.g., to adjust the model's size), set it here:
	 * transform.setScale(5.0); // Change 1.0 to the desired scale factor.
	 * TransformGroup tg = new TransformGroup(transform); tg.addChild(cctvModel);
	 * 
	 * bg.addChild(tg); bg.compile(); return bg; }
	 */

    
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
        
        // Plants at floor corners. TODO: Re-enable
        objectsBG.addChild(addPlantAt(new Vector3d(xLeft+5, floorY, zNear+5)));
        objectsBG.addChild(addPlantAt(new Vector3d(xLeft+5, floorY, zFar-5)));
        objectsBG.addChild(addPlantAt(new Vector3d(xRight-5, floorY, zNear+5)));
        objectsBG.addChild(addPlantAt(new Vector3d(xRight-5, floorY, zFar-5)));
        
        // Cameras at ceiling corners. TODO: Re-enable
        objectsBG.addChild(addCameraAt(new Vector3d(xLeft, ceilingY-0.25, zNear)));
        objectsBG.addChild(addCameraAt(new Vector3d(xLeft, ceilingY-0.25, zFar)));
        objectsBG.addChild(addCameraAt(new Vector3d(xRight, ceilingY-0.25, zNear)));
        objectsBG.addChild(addCameraAt(new Vector3d(xRight, ceilingY-0.25, zFar)));
        
        
   
        objectsBG.addChild(addTrexAt(new Vector3d(xRight-20, floorY+9, zNear+48)));
        objectsBG.addChild(addPodiumAt(new Vector3d(xRight-18, floorY-2.5, zNear+53)));

        for (int i = 0; i < 5; i++) {
            j = (i * 20) + 8;
            objectsBG.addChild(addWallAt(new Vector3d(xLeft-2, floorY+10, zNear+j), Math.PI / 2));
            objectsBG.addChild(addWallAt(new Vector3d(xLeft+j, floorY+10, zFar+2), Math.PI));
            objectsBG.addChild(addWallAt(new Vector3d(xLeft+j+100, floorY+10, zFar+2), Math.PI));
            objectsBG.addChild(addWallAt(new Vector3d(xRight+2, floorY+10, zNear+j), 3 * Math.PI / 2));
            objectsBG.addChild(addWallAt(new Vector3d(xLeft+j, floorY+10, zNear-2), 2 * Math.PI));
            objectsBG.addChild(addWallAt(new Vector3d(xLeft+j+100, floorY+10, zNear-2), 2 * Math.PI));
        }
        //TODO: Re-enable
        objectsBG.addChild(addTableAt(new Vector3d(xLeft+10, floorY, zNear+28)));
        objectsBG.addChild(addWomanAt(new Vector3d(xLeft+10, floorY+10, zNear+28)));
        objectsBG.addChild(addTableAt(new Vector3d(xLeft+10, floorY, zNear+68)));
        objectsBG.addChild(addManAt(new Vector3d(xLeft+11, floorY+10, zNear+68)));
        
        for (int i = 0; i < 10; i++) {
            j = (i * 20) + 8;
            for (int k = 0; k < 5; k++) {
                double l = (k * 20) + 8;
                objectsBG.addChild(addCeilingAt(new Vector3d(xLeft+j, ceilingY+0.5, zNear+l), Math.PI / 2));
            }
        }
        
        //TODO: Re-enable
        objectsBG.addChild(addLightAt(new Vector3d(xLeft+28, ceilingY, zNear+28)));
        objectsBG.addChild(addSpeakerAt(new Vector3d(xLeft+28, ceilingY, zNear+48)));
        objectsBG.addChild(addLightAt(new Vector3d(xLeft+28, ceilingY, zNear+68)));
        objectsBG.addChild(addSpeakerAt(new Vector3d(xLeft+98, ceilingY, zNear+28)));
        objectsBG.addChild(addLightAt(new Vector3d(0, ceilingY, 0)));
        objectsBG.addChild(addSpeakerAt(new Vector3d(xRight-98, ceilingY, zFar-28)));
        objectsBG.addChild(addLightAt(new Vector3d(xRight-28, ceilingY, zFar-68)));
        objectsBG.addChild(addSpeakerAt(new Vector3d(xRight-28, ceilingY, zFar-48)));
        objectsBG.addChild(addLightAt(new Vector3d(xRight-28, ceilingY, zFar-28)));
        
        // Add CCTV at a specific position (adjust the coordinates as needed)
        //objectsBG.addChild(ProjectObjects.addCCTVAt(new Vector3d(0, 10, -30)));

        
        objectsBG.compile();
        return objectsBG;
    }
}
