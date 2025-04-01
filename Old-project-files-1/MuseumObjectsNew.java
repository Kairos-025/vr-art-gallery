
import java.util.Iterator;

import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;



public class MuseumObjectsNew {
	
    public static BranchGroup loadModel(String filePath) {
        ObjectFile loader = new ObjectFile(ObjectFile.RESIZE);
        Scene modelScene = null;

        try {
            modelScene = loader.load(filePath);
        } catch (Exception e) {
            System.err.println("Error loading model: " + e.getMessage());
            return null;
        }

        return modelScene.getSceneGroup();
    }

    public static BranchGroup createMuseumLayout() {
        BranchGroup layoutBG = new BranchGroup();
        // map.obj     
        
        
        // Test Box
		Box testBox = new Box(0.25f, 1f, 2f,  Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Lime));
		//testBox.ENABLE_COLLISION_REPORTING;
		
		
		Transform3D testBoxTF3D = new Transform3D();
		testBoxTF3D.setTranslation(new Vector3d(7f, 0f, 5f));
		TransformGroup testBoxTG = new TransformGroup(testBoxTF3D);
		testBoxTG.addChild(testBox);
		//layoutBG.addChild(testBoxTG);
        
		TransformGroup fbSuperTG = new TransformGroup();
		Transform3D fbTF[] = new Transform3D[18];
		TransformGroup fbTG[] = new TransformGroup[18];
		Box fbBox[] = new Box[18];
		
		
		// Yellow Wing
		int x=0;
		fbBox[0] = new Box(2.85f, 1.45f, 0.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[0] = new Transform3D();
		fbTF[0].setTranslation(new Vector3d(7.15f, 0f, -0.8f));
		fbTG[0] = new TransformGroup(fbTF[0]);
		fbTG[0].addChild(fbBox[0]);
		fbSuperTG.addChild(fbTG[0]);
		
		x = 1;
		fbBox[1] = new Box(2.85f, 1.45f, 0.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[1] = new Transform3D();
		fbTF[1].setTranslation(new Vector3d(7.15f, 0f, 10.8f));
		fbTG[1] = new TransformGroup(fbTF[1]);
		fbTG[1].addChild(fbBox[1]);
		fbSuperTG.addChild(fbTG[1]);
		
		x = 2;
		fbBox[x] = new Box(0.1f, 1.45f, 5.75f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(10.05f, 0f, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 3;
		fbBox[x] = new Box(0.1f, 1.45f, 2.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(4.2f, 0f, 1.2f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 4;
		fbBox[x] = new Box(0.1f, 1.45f, 2.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(4.2f, 0f, 8.8f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 5;
		fbBox[x] = new Box(2.875f, 0.2f, 5.75f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(7.15f, -1.62f, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 6;
		fbBox[x] = new Box(2.875f, 0.2f, 5.75f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Yellow));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(7.15f, 1.6, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		
		
		// Blue Wing
		x = 7;	// Floor
		fbBox[x] = new Box(2.875f, 0.2f, 5.75f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-7.15f, -1.62f, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 8;	// Ceiling
		fbBox[x] = new Box(2.875f, 0.2f, 5.75f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-7.15f, 1.6, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		
		x=9;
		fbBox[x] = new Box(2.85f, 1.45f, 0.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-7.15f, 0f, -0.8f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 10;
		fbBox[x] = new Box(2.85f, 1.45f, 0.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-7.15f, 0f, 10.8f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 11;
		fbBox[x] = new Box(0.1f, 1.45f, 5.75f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-10.05f, 0f, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 12;
		fbBox[x] = new Box(0.1f, 1.45f, 2.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-4.2f, 0f, 1.2f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 13;
		fbBox[x] = new Box(0.1f, 1.45f, 2.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Blue));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(-4.2f, 0f, 8.8f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		
		// Green Middle
		x = 14;
		fbBox[x] = new Box(4.2f, 1.45f, 0.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Green));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(0f, 0f, 3.2f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 15;
		fbBox[x] = new Box(4.2f, 1.45f, 0.1f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Green));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(0f, 0f, 6.8f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 16;
		fbBox[x] = new Box(4.3f, 0.2f, 1.9f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Green));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(0f, -1.62f, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		fbSuperTG.addChild(fbTG[x]);
		
		x = 17;
		fbBox[x] = new Box(4.3f, 0.2f, 1.9f, Primitive.GENERATE_NORMALS, Commons.set_Appearance(Commons.Green));		
		fbTF[x] = new Transform3D();
		fbTF[x].setTranslation(new Vector3d(0f, 1.6f, 5f));
		fbTG[x] = new TransformGroup(fbTF[x]);
		fbTG[x].addChild(fbBox[x]);
		
		
		
		//System.out.println(fbBox[0].getCollisionBounds());
		
		Bounds fbBounds[] = new Bounds[18];
		Bounds finalBounds = fbBox[0].getBounds();
		/*
		Appearance fbApp = new Appearance();
		TransparencyAttributes fbTA = new TransparencyAttributes();
		fbTA.setTransparency(0.5f);
		fbApp.setTransparencyAttributes(fbTA);
		*/
		for(int i=0; i<18;i++)
		{
			fbBox[i].getAppearance().setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 1f));
			//fbBox[i].setAppearance(fbApp);
			//fbBox[i].getAppearance().setTransparencyAttributes(fbTA);
			//System.out.println("fbBox["+i+"].getBounds(): "+fbBox[i].getBounds());
			finalBounds.combine(fbBox[i].getBounds());
		}
		
		fbSuperTG.addChild(fbTG[x]);
		layoutBG.addChild(fbSuperTG);
		
		
		
        BranchGroup mapModel = loadModel("images/map.obj");
        if (mapModel != null) {
            colorize(mapModel);

            mapModel.setCollidable(false);
            
            TransformGroup transformGroup = createTransformGroup(
                new Vector3f(0.0f, 0.0f, 5.0f), // Position
                10.0, // Scale
                0.0 // Rotation
            );
            // TODO: Override collision bounds 
            mapModel.setBoundsAutoCompute(false);
            //mapModel.setC
            //System.out.println("getCollisionBounds before: "+mapModel.getCollisionBounds());
            mapModel.setCollisionBounds(finalBounds);
            //System.out.println("getCollisionBounds after: "+mapModel.getCollisionBounds());
            
            transformGroup.addChild(mapModel);
            layoutBG.addChild(transformGroup);
        }
        
        
        //System.out.println("mapModel.numChildren(): "+mapModel.numChildren());
        //System.out.println("mapModel.getChild(0): "+mapModel.getChild(0));
        //System.out.println("mapModel.getChild(0).getClass: "+mapModel.getChild(0).getClass());
        

        

        // 01.png
        Shape3D texturedSquare01 = createTexturedSquare("images/01.png");
        TransformGroup squareTransformGroup01 = createTransformGroup(
            new Vector3f(4.3f, 0.5f, 1f), // Position
            0.8, // Scale
            Math.toRadians(90) // Rotation
        );
        squareTransformGroup01.addChild(texturedSquare01);
        layoutBG.addChild(squareTransformGroup01);

        // 02.png
        Shape3D texturedSquare02 = createTexturedSquare("images/02.png");
        TransformGroup squareTransformGroup02 = createTransformGroup(
            new Vector3f(7.2f, 0.4f, -0.7f), // Position
            0.9, // Scale
            Math.toRadians(0) // Rotation
        );
        squareTransformGroup02.addChild(texturedSquare02);
        layoutBG.addChild(squareTransformGroup02);

        // 03.png
        Shape3D texturedSquare03 = createTexturedSquare("images/03.png");
        TransformGroup squareTransformGroup03 = createTransformGroup(
            new Vector3f(9.95f, 0.4f, 1.8f), // Position
            0.8, // Scale
            Math.toRadians(-90) // Rotation
        );
        squareTransformGroup03.addChild(texturedSquare03);
        layoutBG.addChild(squareTransformGroup03);

        // 04.png
        Shape3D texturedSquare04 = createTexturedSquare("images/04.png");
        TransformGroup squareTransformGroup04 = createTransformGroup(
            new Vector3f(9.95f, 0.4f, 5.25f), // Position
            0.9, // Scale
            Math.toRadians(-90) // Rotation
        );
        squareTransformGroup04.addChild(texturedSquare04);
        layoutBG.addChild(squareTransformGroup04);

        // 05.png
        Shape3D texturedSquare05 = createTexturedSquare("images/05.png");
        TransformGroup squareTransformGroup05 = createTransformGroup(
            new Vector3f(9.95f, 0.4f, 8.45f), // Position
            0.9, // Scale
            Math.toRadians(-90) // Rotation
        );
        squareTransformGroup05.addChild(texturedSquare05);
        layoutBG.addChild(squareTransformGroup05);

        // 06.png
        Shape3D texturedSquare06 = createTexturedSquare("images/06.png");
        TransformGroup squareTransformGroup06 = createTransformGroup(
            new Vector3f(7.2f, 0.4f, 10.7f), // Position
            0.9, // Scale
            Math.toRadians(180) // Rotation 
        );
        squareTransformGroup06.addChild(texturedSquare06);
        layoutBG.addChild(squareTransformGroup06);

        // 07.png
        Shape3D texturedSquare07 = createTexturedSquare("images/07.png");
        TransformGroup squareTransformGroup07 = createTransformGroup(
            new Vector3f(4.3f, 0.5f, 8.6f), // Position
            0.8, // Scale
            Math.toRadians(90) // Rotation
        );
        squareTransformGroup07.addChild(texturedSquare07);
        layoutBG.addChild(squareTransformGroup07);

        // 08.png
        Shape3D texturedSquare08 = createTexturedSquare("images/08.png");
        TransformGroup squareTransformGroup08 = createTransformGroup(
            new Vector3f(-4.3f, 0.3f, 1.3f), // Position
            0.9, // Scale
            Math.toRadians(-90) // Rotation
        );
        squareTransformGroup08.addChild(texturedSquare08);
        layoutBG.addChild(squareTransformGroup08);

        // 09.png
        Shape3D texturedSquare09 = createTexturedSquare("images/09.png");
        TransformGroup squareTransformGroup09 = createTransformGroup(
            new Vector3f(-7.2f, 0.4f, -0.7f), // Position
            0.9, // Scale
            Math.toRadians(0) // Rotation
        );
        squareTransformGroup09.addChild(texturedSquare09);
        layoutBG.addChild(squareTransformGroup09);

        // 10.png
        Shape3D texturedSquare10 = createTexturedSquare("images/10.png");
        TransformGroup squareTransformGroup10 = createTransformGroup(
            new Vector3f(-9.95f, 0.4f, 1.8f), // Position
            0.8, // Scale
            Math.toRadians(90) // Rotation
        );
        squareTransformGroup10.addChild(texturedSquare10);
        layoutBG.addChild(squareTransformGroup10);

        return layoutBG;
    }

    
    
    // Create a TransformGroup with translation, scaling, and rotation
    public static TransformGroup createTransformGroup(Vector3f translation, double scale, double rotationY) {
        TransformGroup transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Transform3D transform3D = new Transform3D();
        transform3D.setTranslation(translation);
        transform3D.setScale(scale);

        // Apply rotation (around the Y-axis)
        if (rotationY != 0) {
            Transform3D rotation = new Transform3D();
            rotation.rotY(rotationY);
            transform3D.mul(rotation);
        }

        transformGroup.setTransform(transform3D);

        return transformGroup;
    }

    // traverse the scene graph and colorize all 3d model nodes
    public static void colorize(Node node) {
        if (node instanceof Group) {
            // If the node is a Group, traverse its children
            Group group = (Group) node;
            for (int i = 0; i < group.numChildren(); i++) {
                colorize(group.getChild(i));
            }
        } else if (node instanceof Shape3D) {
            // If the node is a Shape3D, apply material
            Shape3D shape = (Shape3D) node;
            Appearance appearance = shape.getAppearance();
            if (appearance == null) {
                appearance = new Appearance();
            }

            // Create material
            Material material = new Material();
            material.setDiffuseColor(new Color3f(0.7f, 0.9f, 0.7f)); // soft mint 
            material.setSpecularColor(new Color3f(0.5f, 0.7f, 0.5f)); // softer specular color
            material.setShininess(32.0f); // shininess

            material.setLightingEnable(true);
            appearance.setMaterial(material);
            shape.setAppearance(appearance);
        }
    }

    // Create a textured square
    public static Shape3D createTexturedSquare(String texturePath) {
        // Define the vertices of the square
        float halfSize = 1.0f;
        Point3f[] vertices = {
            new Point3f(-halfSize, -halfSize, 0),
            new Point3f(halfSize, -halfSize, 0),
            new Point3f(halfSize, halfSize, 0),
            new Point3f(-halfSize, halfSize, 0)
        };

        // Define the texture coordinates
        TexCoord2f[] texCoords = {
            new TexCoord2f(0, 0),
            new TexCoord2f(1, 0),
            new TexCoord2f(1, 1),
            new TexCoord2f(0, 1)
        };

        // create the QuadArray with vertices and texture coordinates
        QuadArray quadArray = new QuadArray(4, QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2);
        quadArray.setCoordinates(0, vertices);
        quadArray.setTextureCoordinates(0, 0, texCoords);

        // apply the texture
        TextureLoader textureLoader = new TextureLoader(texturePath, null);
        Texture texture = textureLoader.getTexture();
        Appearance appearance = new Appearance();
        appearance.setTexture(texture);
        Shape3D square = new Shape3D(quadArray, appearance);

        return square;
    }
}

/*
class CollisionDetector extends Behavior
{
	private boolean inCollision=false;
	private Box box;
	private ColoringAttributes shapeColoring;
	private Appearance shapeAppearance;
	private WakeupOnCollisionEntry wEnter;
	private WakeupOnCollisionExit wExit;
	
	public CollisionDetector(Box b)
	{
		box=b;	//save the original color of "shape"
		shapeAppearance=box.getAppearance();
		shapeColoring=shapeAppearance.getColoringAttributes();
		inCollision=false;
	}
	public void initialize()
	{
		wEnter=new WakeupOnCollisionEntry(box);
		wExit=new WakeupOnCollisionExit(box);
		wakeupOn(wEnter);	// initialize the behavior
	}
	
	public void processStimulus(Iterator<WakeupCriterion>criteria)
	{
		Color3f hilightClr=Commons.Green;
		ColoringAttributes highlight=new ColoringAttributes(hilightClr,
		ColoringAttributes.SHADE_GOURAUD);
		inCollision=!inCollision;	// collision has taken place
		
		if(inCollision) {	//change color to highlight'shape'
			shapeAppearance.setColoringAttributes(highlight);
			System.out.println("In collision");
			wakeupOn(wExit);// keep the color until no collision
		}
		else{	// change color back to its original
			shapeAppearance.setColoringAttributes(shapeColoring);
			System.out.println("not in collision");
			wakeupOn(wEnter);	// wait for collision happens
		} 
	} 
	
}
*/