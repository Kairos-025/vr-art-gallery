package Project;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

public class ProjectImages {

    /**
     * Creates a static image block (unchanged).
     */
    public static BranchGroup createImageBlock(String imageFile, float width, float height, float thickness,
                                                 Vector3f position, Vector3f rotation, boolean flip) {
        BranchGroup bg = new BranchGroup();
        
        // Calculate half extents.
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float halfThickness = thickness / 2.0f;
        
        // Create the Box.
        Box paintingBox = new Box(halfWidth, halfHeight, halfThickness, 
                Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, null);
        
        // Create Appearances.
        Appearance frontAppearance = new Appearance();
        TextureLoader loader = new TextureLoader(imageFile, null);
        Texture texture = loader.getTexture();
        if (texture != null) {
            frontAppearance.setTexture(texture);
        } else {
            System.err.println("Error loading image: " + imageFile);
        }
        frontAppearance.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                                                                      PolygonAttributes.CULL_NONE, 0));
        
        Appearance backAppearance = new Appearance();
        backAppearance.setMaterial(new Material(new Color3f(0.8f, 0.8f, 0.8f),
                                                  new Color3f(0.8f, 0.8f, 0.8f),
                                                  new Color3f(0.8f, 0.8f, 0.8f),
                                                  new Color3f(0.8f, 0.8f, 0.8f), 80f));
        backAppearance.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                                                                    PolygonAttributes.CULL_NONE, 0));
        
        // Assign appearances based on flip.
        if (flip) {
            paintingBox.getShape(Box.FRONT).setAppearance(backAppearance);
            paintingBox.getShape(Box.BACK).setAppearance(frontAppearance);
        } else {
            paintingBox.getShape(Box.FRONT).setAppearance(frontAppearance);
            paintingBox.getShape(Box.BACK).setAppearance(backAppearance);
        }
        paintingBox.getShape(Box.LEFT).setAppearance(backAppearance);
        paintingBox.getShape(Box.RIGHT).setAppearance(backAppearance);
        paintingBox.getShape(Box.TOP).setAppearance(backAppearance);
        paintingBox.getShape(Box.BOTTOM).setAppearance(backAppearance);
        
        // Transformation: Apply translation and rotation.
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(position));
        Transform3D rotationTransform = new Transform3D();
        rotationTransform.rotX(rotation.x);
        rotationTransform.rotY(rotation.y);
        rotationTransform.rotZ(rotation.z);
        transform.mul(rotationTransform);
        TransformGroup tg = new TransformGroup(transform);
        tg.addChild(paintingBox);
        bg.addChild(tg);
        bg.compile();
        
        return bg;
    }
    
    /**
     * Creates a gallery of morphing images by using ProjectAnimations.createMorphImageBlock
     * for each image. This replaces the static image block with a morphing version.
     */
    public static BranchGroup addGalleryImages() {
        BranchGroup galleryBG = new BranchGroup();
        
        float imageWidth = 30f, imageHeight = 20f, thickness = 0.5f;
        Vector3f noExtraRotation = new Vector3f(0f, 0f, 0f);
        
        // Back wall images (z = -50) with flip=false.
        float centerZ1 = -49 + thickness / 2f;
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageMonaLisa.jpg",
                imageWidth, imageHeight, thickness, 4, 64,
                new Vector3f(-40f, 0f, centerZ1), noExtraRotation, false));
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageStarryNight.jpg",
                imageWidth, imageHeight, thickness, 4, 64,
                new Vector3f(0f, 0f, centerZ1), noExtraRotation, false));
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageTsunami.jpg",
                imageWidth, imageHeight, thickness, 4, 64,
                new Vector3f(40f, 0f, centerZ1), noExtraRotation, false));
        
        
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageAbstract.jpg",
        		imageWidth, imageHeight, thickness, 4, 64,
        		new Vector3f(-99 + thickness / 2f, 0f, 0f), new Vector3f(0f, (float)Math.PI/2, 0f), false));
        
        // Front wall images (z = 50) with flip=true.
        float centerZ2 = 49 - thickness / 2f;
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageTwilight.jpg",
                imageWidth, imageHeight, thickness, 4, 64,
                new Vector3f(-40f, 0f, centerZ2), noExtraRotation, true));
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageShip.jpg",
                imageWidth, imageHeight, thickness, 4, 64,
                new Vector3f(0f, 0f, centerZ2), noExtraRotation, true));
        galleryBG.addChild(ProjectAnimations.createMorphImageBlock("Projects/ImageFlowers.jpg",
                imageWidth, imageHeight, thickness, 4, 64,
                new Vector3f(40f, 0f, centerZ2), noExtraRotation, true));
        
        galleryBG.compile();
        return galleryBG;
    }
}
