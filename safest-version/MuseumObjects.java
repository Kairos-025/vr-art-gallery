import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

public class MuseumObjects {

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
        BranchGroup mapModel = loadModel("images/map.obj");
        colorize(mapModel);
        TransformGroup transformGroup = createTransformGroup(
            new Vector3f(0.0f, 0.0f, 5.0f), // Position
            10.0, // Scale
            0.0, // Rotation
            Math.toRadians(0)
        );
        transformGroup.addChild(mapModel);
        layoutBG.addChild(transformGroup);

        // SculptureMan.obj
        BranchGroup sculptureManModel = loadModel("images/SculptureMan.obj");
        TransformGroup sculptureManTransform = createTransformGroup(
            new Vector3f(-5.2f, 0f, 9.75f), // Position
            1.4, // Scale
            Math.toRadians(-140), // Rotation
            Math.toRadians(0)
        );
        sculptureManTransform.addChild(sculptureManModel);
        layoutBG.addChild(sculptureManTransform);

        // tablet.obj
        BranchGroup tabletModel = loadModel("images/tablet.obj");
        TransformGroup tabletTransform = createTransformGroup(
            new Vector3f(0f, -0.7f, 6.4f), // Position
            0.8, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        tabletTransform.addChild(tabletModel);
        layoutBG.addChild(tabletTransform);

        // LightSwitchCover1 (Grey)
        Box lightSwitchCover1 = new Box(0.1f, 1f, 0.8f, createAppearance(new Color3f(0.5f, 0.5f, 0.5f))); // Grey
        TransformGroup coverTransform1 = createTransformGroup(
            new Vector3f(-2.0f, 0.7f, 6.70f), // Position
            0.15, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        coverTransform1.addChild(lightSwitchCover1);
        layoutBG.addChild(coverTransform1);

        // LightSwitchKey1 (Green)
        Box lightSwitchKey1 = new Box(0.2f, 0.2f, 0.2f, createAppearance(new Color3f(0, 1, 0))); // Green
        TransformGroup keyTransform1 = createTransformGroup(
            new Vector3f(-2.0f, 0.7f, 6.68f), // Position
            0.15, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        keyTransform1.addChild(lightSwitchKey1);
        layoutBG.addChild(keyTransform1);

        // LightSwitchCover2 (Grey)
        Box lightSwitchCover2 = new Box(0.1f, 1f, 0.8f, createAppearance(new Color3f(0.5f, 0.5f, 0.5f))); // Grey
        TransformGroup coverTransform2 = createTransformGroup(
            new Vector3f(-2.7f, 0.7f, 6.70f), // Position
            0.15, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        coverTransform2.addChild(lightSwitchCover2);
        layoutBG.addChild(coverTransform2);

        // LightSwitchKey2 (Green)
        Box lightSwitchKey2 = new Box(0.2f, 0.2f, 0.2f, createAppearance(new Color3f(0, 1, 0))); // Green
        TransformGroup keyTransform2 = createTransformGroup(
            new Vector3f(-2.7f, 0.7f, 6.68f), // Position
            0.15, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        keyTransform2.addChild(lightSwitchKey2);
        layoutBG.addChild(keyTransform2);

        // camera.obj
        BranchGroup cameraModel1 = loadModel("images/camera.obj");
        TransformGroup cameraTransform1 = createTransformGroup(
            new Vector3f(-9.6f, 1.2f, 10.4f), // Position
            0.5, // Scale
            Math.toRadians(-33), // Rotation
            Math.toRadians(0)
        );
        cameraTransform1.addChild(cameraModel1);
        layoutBG.addChild(cameraTransform1);

        // camera.obj
        BranchGroup cameraModel2 = loadModel("images/camera.obj");
        TransformGroup cameraTransform2 = createTransformGroup(
            new Vector3f(4.6f, 1.2f, 10.4f), // Position
            0.5, // Scale
            Math.toRadians(-33), // Rotation
            Math.toRadians(0)
        );
        cameraTransform2.addChild(cameraModel2);
        layoutBG.addChild(cameraTransform2);

        // egyptian_queen.obj
        BranchGroup egyptianQueenModel = loadModel("images/egyptian_queen.obj");
        TransformGroup egyptianQueenTransform = createTransformGroup(
            new Vector3f(-9.2f, 0.0f, 6.2f), // Position
            1.4, // Scale
            Math.toRadians(105), // Rotation
            Math.toRadians(-90)
        );
        egyptianQueenTransform.addChild(egyptianQueenModel);
        layoutBG.addChild(egyptianQueenTransform);

        // 01.png
        Shape3D texturedSquare01 = createTexturedSquare("images/01.png");
        TransformGroup squareTransformGroup01 = createTransformGroup(
            new Vector3f(4.3f, 0.5f, 1f), // Position
            0.8, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup01.addChild(texturedSquare01);
        layoutBG.addChild(squareTransformGroup01);

        // 02.png
        Shape3D texturedSquare02 = createTexturedSquare("images/02.png");
        TransformGroup squareTransformGroup02 = createTransformGroup(
            new Vector3f(7.2f, 0.4f, -0.7f), // Position
            0.9, // Scale
            Math.toRadians(0), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup02.addChild(texturedSquare02);
        layoutBG.addChild(squareTransformGroup02);

        // 03.png
        Shape3D texturedSquare03 = createTexturedSquare("images/03.png");
        TransformGroup squareTransformGroup03 = createTransformGroup(
            new Vector3f(9.95f, 0.4f, 1.8f), // Position
            0.8, // Scale
            Math.toRadians(-90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup03.addChild(texturedSquare03);
        layoutBG.addChild(squareTransformGroup03);

        // 04.png
        Shape3D texturedSquare04 = createTexturedSquare("images/04.png");
        TransformGroup squareTransformGroup04 = createTransformGroup(
            new Vector3f(9.95f, 0.4f, 5.25f), // Position
            0.9, // Scale
            Math.toRadians(-90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup04.addChild(texturedSquare04);
        layoutBG.addChild(squareTransformGroup04);

        // 05.png
        Shape3D texturedSquare05 = createTexturedSquare("images/05.png");
        TransformGroup squareTransformGroup05 = createTransformGroup(
            new Vector3f(9.95f, 0.4f, 8.45f), // Position
            0.9, // Scale
            Math.toRadians(-90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup05.addChild(texturedSquare05);
        layoutBG.addChild(squareTransformGroup05);

        // 06.png
        Shape3D texturedSquare06 = createTexturedSquare("images/06.png");
        TransformGroup squareTransformGroup06 = createTransformGroup(
            new Vector3f(7.2f, 0.4f, 10.7f), // Position
            0.9, // Scale
            Math.toRadians(180), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup06.addChild(texturedSquare06);
        layoutBG.addChild(squareTransformGroup06);

        // 07.png
        Shape3D texturedSquare07 = createTexturedSquare("images/07.png");
        TransformGroup squareTransformGroup07 = createTransformGroup(
            new Vector3f(4.3f, 0.5f, 8.6f), // Position
            0.8, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup07.addChild(texturedSquare07);
        layoutBG.addChild(squareTransformGroup07);

        // 08.png
        Shape3D texturedSquare08 = createTexturedSquare("images/08.png");
        TransformGroup squareTransformGroup08 = createTransformGroup(
            new Vector3f(-4.3f, 0.3f, 1.3f), // Position
            0.9, // Scale
            Math.toRadians(-90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup08.addChild(texturedSquare08);
        layoutBG.addChild(squareTransformGroup08);

        // 09.png
        Shape3D texturedSquare09 = createTexturedSquare("images/09.png");
        TransformGroup squareTransformGroup09 = createTransformGroup(
            new Vector3f(-7.2f, 0.4f, -0.7f), // Position
            0.9, // Scale
            Math.toRadians(0), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup09.addChild(texturedSquare09);
        layoutBG.addChild(squareTransformGroup09);

        // 10.png
        Shape3D texturedSquare10 = createTexturedSquare("images/10.png");
        TransformGroup squareTransformGroup10 = createTransformGroup(
            new Vector3f(-9.95f, 0.4f, 1.8f), // Position
            0.8, // Scale
            Math.toRadians(90), // Rotation
            Math.toRadians(0)
        );
        squareTransformGroup10.addChild(texturedSquare10);
        layoutBG.addChild(squareTransformGroup10);

        return layoutBG;
    }

    // Create a TransformGroup with translation, scaling, and rotation
    public static TransformGroup createTransformGroup(Vector3f translation, double scale, double rotationY, double rotationZ) {
        TransformGroup transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Transform3D transform3D = new Transform3D();
        transform3D.setTranslation(translation);
        transform3D.setScale(scale);

        // Apply rotation around the Y-axis
        if (rotationY != 0) {
            Transform3D rotationYAxis = new Transform3D();
            rotationYAxis.rotY(rotationY);
            transform3D.mul(rotationYAxis);
        }

        // Apply rotation around the X-axis
        if (rotationZ != 0) {
            Transform3D rotationZAxis = new Transform3D();
            rotationZAxis.rotZ(rotationZ);
            transform3D.mul(rotationZAxis);
        }

        transformGroup.setTransform(transform3D);

        return transformGroup;
    }

    // Traverse the scene graph and colorize all 3D model nodes
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
            material.setDiffuseColor(new Color3f(0.7f, 0.9f, 0.7f)); // soft mint green
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

        // Apply the texture
        TextureLoader textureLoader = new TextureLoader(texturePath, null);
        Texture texture = textureLoader.getTexture();
        Appearance appearance = new Appearance();
        appearance.setTexture(texture);
        Shape3D square = new Shape3D(quadArray, appearance);

        return square;
    }

    // create an appearance with a specific color
    private static Appearance createAppearance(Color3f color) {
        Appearance appearance = new Appearance();
        Material material = new Material();
        material.setDiffuseColor(color);
        material.setLightingEnable(true);
        appearance.setMaterial(material);
        return appearance;
    }
}
