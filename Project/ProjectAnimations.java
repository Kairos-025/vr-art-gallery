package Project;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;
import org.jdesktop.j3d.examples.morphing.MorphingBehavior;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class ProjectAnimations {

    // Maximum number of outer boundary points (all morph targets use this number)
    private static final int MAX_PTS = 64;

    /**
     * Creates a composite morphing image block that includes a front face,
     * a back face, and lateral sides to give thickness.
     * The morph targets are generated for three different edge counts (e.g., 4, 16, 64).
     * For the 4–sided case the outer boundary is computed via a parametric approach (yielding a rhombus-like shape).
     *
     * The "flip" flag swaps the front/back appearances as in your static block, while the lateral faces always use a white appearance.
     *
     * @param imageFile the texture file for the front face
     * @param width overall width of the image block
     * @param height overall height of the image block
     * @param thickness depth of the block (distance between front and back)
     * @param n0 first target edge count (e.g., 4)
     * @param n_mid second target edge count (e.g., 16)
     * @param n1 third target edge count (e.g., 64)
     * @param position scene position (translation only)
     * @param rotation scene rotation (applied to the morph block)
     * @param flip if true, swap the front and back appearances (as in the static image block)
     * @return a BranchGroup containing the composite morphing block
     */
 // Global list to keep track of all morphing Alpha objects.
    public static List<Alpha> morphAlphas = new ArrayList<>();

    /**
     * Creates a composite morphing image block...
     * (same Javadoc omitted for brevity)
     */
    public static BranchGroup createMorphImageBlock(String imageFile, float width, float height, float thickness,
                                                      int n0, int n1,
                                                      Vector3f position, Vector3f rotation, boolean flip) {
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float halfThickness = thickness / 2f;

        // Create appearances as in your static image block.
        Appearance appFront = new Appearance();
        TextureLoader loader = new TextureLoader(imageFile, null);
        Texture texture = loader.getTexture();
        if (texture != null) {
            appFront.setTexture(texture);
        } else {
            System.err.println("Error loading texture: " + imageFile);
        }
        appFront.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                                                             PolygonAttributes.CULL_NONE, 0));
        // appBack: white appearance.
        Appearance appBack = new Appearance();
        Material material = new Material(new Color3f(1f, 1f, 1f),
                                         new Color3f(1f, 1f, 1f),
                                         new Color3f(1f, 1f, 1f),
                                         new Color3f(1f, 1f, 1f), 80f);
        appBack.setMaterial(material);
        appBack.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                                                             PolygonAttributes.CULL_NONE, 0));
        // Lateral white appearance.
        Appearance appWhite = new Appearance();
        Material whiteMat = new Material(new Color3f(1f, 1f, 1f),
                                         new Color3f(1f, 1f, 1f),
                                         new Color3f(1f, 1f, 1f),
                                         new Color3f(1f, 1f, 1f), 80f);
        appWhite.setMaterial(whiteMat);
        appWhite.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                                                             PolygonAttributes.CULL_NONE, 0));
        if (flip) {
            Appearance temp = appFront;
            appFront = appBack;
            appBack = temp;
        }

        // Generate morph targets (front, back, lateral) for three steps.
        TriangleStripArray[] frontTargets = new TriangleStripArray[3];
        frontTargets[0] = createMorphTargetForFace(n0, halfWidth, halfHeight, halfThickness, true);
        frontTargets[1] = createMorphTargetForFace(n0, halfWidth, halfHeight, halfThickness, true);
        frontTargets[2] = createMorphTargetForFace(n1, halfWidth, halfHeight, halfThickness, true);

        TriangleStripArray[] backTargets = new TriangleStripArray[3];
        backTargets[0] = createMorphTargetForFace(n0, halfWidth, halfHeight, -halfThickness, false);
        backTargets[1] = createMorphTargetForFace(n0, halfWidth, halfHeight, -halfThickness, false);
        backTargets[2] = createMorphTargetForFace(n1, halfWidth, halfHeight, -halfThickness, false);

        TriangleStripArray[] lateralTargets = new TriangleStripArray[3];
        lateralTargets[0] = createLateralMorphTarget(n0, halfWidth, halfHeight, halfThickness, -halfThickness);
        lateralTargets[1] = createLateralMorphTarget(n0, halfWidth, halfHeight, halfThickness, -halfThickness);
        lateralTargets[2] = createLateralMorphTarget(n1, halfWidth, halfHeight, halfThickness, -halfThickness);

        Morph frontMorph = new Morph(frontTargets, appFront);
        frontMorph.setCapability(Morph.ALLOW_WEIGHTS_WRITE);
        Morph backMorph = new Morph(backTargets, appBack);
        backMorph.setCapability(Morph.ALLOW_WEIGHTS_WRITE);
        Morph lateralMorph = new Morph(lateralTargets, appWhite);
        lateralMorph.setCapability(Morph.ALLOW_WEIGHTS_WRITE);

        // Create a common Alpha for morphing.
        Alpha morphAlpha = new Alpha(-1,
                                      Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE,
                                      0, 0, 4000, 0, 1000, 4000, 0, 1000);
        // Store the alpha so we can toggle it later.
        morphAlphas.add(morphAlpha);

        MorphingBehavior behFront = new MorphingBehavior(morphAlpha, frontMorph);
        behFront.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0), 1000));
        MorphingBehavior behBack = new MorphingBehavior(morphAlpha, backMorph);
        behBack.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0), 1000));
        MorphingBehavior behLateral = new MorphingBehavior(morphAlpha, lateralMorph);
        behLateral.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0), 1000));

        TransformGroup tg = new TransformGroup();
        tg.addChild(frontMorph);
        tg.addChild(backMorph);
        tg.addChild(lateralMorph);
        tg.addChild(behFront);
        tg.addChild(behBack);
        tg.addChild(behLateral);

        // Apply overall translation and rotation.
        Transform3D trans = new Transform3D();
        trans.setTranslation(new Vector3d(position));
        Transform3D rX = new Transform3D(); rX.rotX(rotation.x);
        Transform3D rY = new Transform3D(); rY.rotY(rotation.y);
        Transform3D rZ = new Transform3D(); rZ.rotZ(rotation.z);
        rX.mul(rY); rX.mul(rZ);
        trans.mul(rX);
        TransformGroup finalTG = new TransformGroup(trans);
        finalTG.addChild(tg);

        BranchGroup bg = new BranchGroup();
        bg.setUserData(morphAlpha);  // Store the alpha so it can be toggled later.
        bg.addChild(finalTG);
        bg.compile();
        return bg;
    }

    /**
     * Creates a morph target for a face (front or back) as a TriangleStripArray.
     * The geometry is built by alternating a center point with points on the outer boundary.
     * For a front face, the normal is (0,0,1); for a back face it is (0,0,-1).
     *
     * @param targetSides number of edges for the morph target (e.g., 4, 16, 64)
     * @param halfWidth half the width of the face
     * @param halfHeight half the height of the face
     * @param z the z-coordinate (positive for front, negative for back)
     * @param front if true, creates a front face; otherwise, a back face.
     * @return a TriangleStripArray representing the face.
     */
    private static TriangleStripArray createMorphTargetForFace(int targetSides, float halfWidth, float halfHeight, float z, boolean front) {
        Point3f[] outer = new Point3f[MAX_PTS];
        if (targetSides == 4) {
            // For 4 sides, use a parametric approach that produces a rhombus-like shape.
            for (int i = 0; i < MAX_PTS; i++) {
                double theta = 2 * Math.PI * i / MAX_PTS;
                double cosTheta = Math.cos(theta);
                double sinTheta = Math.sin(theta);
                double scaleX = (Math.abs(cosTheta) < 1e-6) ? Double.POSITIVE_INFINITY : halfWidth / Math.abs(cosTheta);
                double scaleY = (Math.abs(sinTheta) < 1e-6) ? Double.POSITIVE_INFINITY : halfHeight / Math.abs(sinTheta);
                double r = Math.min(scaleX, scaleY);
                float x = (float)(r * cosTheta);
                float y = (float)(r * sinTheta);
                outer[i] = new Point3f(x, y, z);
            }
        } else {
            // For other targetSides, use standard polar mapping.
            for (int i = 0; i < MAX_PTS; i++) {
                int j = (int)((i * targetSides) / (float)MAX_PTS);
                double angle = 2 * Math.PI * j / targetSides;
                float x = halfWidth * (float)Math.cos(angle);
                float y = halfHeight * (float)Math.sin(angle);
                outer[i] = new Point3f(x, y, z);
            }
        }
        
        int vNum = (MAX_PTS + 1) * 2;
        Point3f[] coords = new Point3f[vNum];
        Vector3f[] normals = new Vector3f[vNum];
        float[] texCoords = new float[vNum * 2];
        Point3f center = new Point3f(0f, 0f, z);
        Vector3f norm = front ? new Vector3f(0f, 0f, 1f) : new Vector3f(0f, 0f, -1f);
        float centerU = 0.5f, centerV = 0.5f;
        for (int i = 0; i <= MAX_PTS; i++) {
            int idx = (i < MAX_PTS) ? i : 0;
            int index0 = i * 2;
            int index1 = i * 2 + 1;
            coords[index0] = new Point3f(center);
            normals[index0] = new Vector3f(norm);
            texCoords[index0 * 2] = centerU;
            texCoords[index0 * 2 + 1] = centerV;
            coords[index1] = new Point3f(outer[idx]);
            normals[index1] = new Vector3f(norm);
            float u = 0.5f + (outer[idx].x / (2 * halfWidth));
            float v = 0.5f + (outer[idx].y / (2 * halfHeight));
            texCoords[index1 * 2] = u;
            texCoords[index1 * 2 + 1] = v;
        }
        int[] stripCounts = { vNum };
        TriangleStripArray tsa = new TriangleStripArray(vNum,
            GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2,
            stripCounts);
        tsa.setCoordinates(0, coords);
        tsa.setNormals(0, normals);
        tsa.setTextureCoordinates(0, 0, texCoords);
        return tsa;
    }

    /**
     * Creates a morph target for the lateral (side) surfaces as a TriangleStripArray.
     * The geometry is built as a quad strip connecting the outer boundary points
     * of the front face (at z = frontZ) and the back face (at z = backZ).
     *
     * @param targetSides number of edges (e.g., 4, 16, 64)
     * @param halfWidth half the width of the face
     * @param halfHeight half the height of the face
     * @param frontZ z coordinate for the front face
     * @param backZ z coordinate for the back face
     * @return a TriangleStripArray representing the lateral surface.
     */
    private static TriangleStripArray createLateralMorphTarget(int targetSides, float halfWidth, float halfHeight, float frontZ, float backZ) {
        Point3f[] outerFront = new Point3f[MAX_PTS];
        Point3f[] outerBack = new Point3f[MAX_PTS];
        if (targetSides == 4) {
            for (int i = 0; i < MAX_PTS; i++) {
                double theta = 2 * Math.PI * i / MAX_PTS;
                double cosTheta = Math.cos(theta);
                double sinTheta = Math.sin(theta);
                double scaleX = (Math.abs(cosTheta) < 1e-6) ? Double.POSITIVE_INFINITY : halfWidth / Math.abs(cosTheta);
                double scaleY = (Math.abs(sinTheta) < 1e-6) ? Double.POSITIVE_INFINITY : halfHeight / Math.abs(sinTheta);
                double r = Math.min(scaleX, scaleY);
                float x = (float)(r * cosTheta);
                float y = (float)(r * sinTheta);
                outerFront[i] = new Point3f(x, y, frontZ);
                outerBack[i] = new Point3f(x, y, backZ);
            }
        } else {
            for (int i = 0; i < MAX_PTS; i++) {
                int j = (int)((i * targetSides) / (float)MAX_PTS);
                double angle = 2 * Math.PI * j / targetSides;
                float x = halfWidth * (float)Math.cos(angle);
                float y = halfHeight * (float)Math.sin(angle);
                outerFront[i] = new Point3f(x, y, frontZ);
                outerBack[i] = new Point3f(x, y, backZ);
            }
        }
        int vNum = (MAX_PTS + 1) * 2;
        Point3f[] coords = new Point3f[vNum];
        Vector3f[] normals = new Vector3f[vNum];
        float[] texCoords = new float[vNum * 2];
        for (int i = 0; i <= MAX_PTS; i++) {
            int idx = (i < MAX_PTS) ? i : 0;
            int index0 = i * 2;
            int index1 = i * 2 + 1;
            coords[index0] = new Point3f(outerFront[idx]);
            coords[index1] = new Point3f(outerBack[idx]);
            float nx = outerFront[idx].x;
            float ny = outerFront[idx].y;
            float len = (float)Math.sqrt(nx * nx + ny * ny);
            if (len == 0) len = 1;
            Vector3f n = new Vector3f(nx / len, ny / len, 0f);
            normals[index0] = n;
            normals[index1] = n;
            float u = (float)i / MAX_PTS;
            texCoords[index0 * 2] = u;
            texCoords[index0 * 2 + 1] = 0.0f;
            texCoords[index1 * 2] = u;
            texCoords[index1 * 2 + 1] = 1.0f;
        }
        int[] stripCounts = { vNum };
        TriangleStripArray tsa = new TriangleStripArray(vNum,
            GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2,
            stripCounts);
        tsa.setCoordinates(0, coords);
        tsa.setNormals(0, normals);
        tsa.setTextureCoordinates(0, 0, texCoords);
        return tsa;
    }
    
    // --- Animation Utilities ---

    public static TransformGroup createRotatingTG(Node node, long cycleTime) {
        TransformGroup rotateTG = new TransformGroup();
        rotateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rotateTG.addChild(node);
        Alpha rotationAlpha = new Alpha(-1, cycleTime);
        Transform3D identity = new Transform3D();
        RotationInterpolator rotator = new RotationInterpolator(
            rotationAlpha,
            rotateTG,
            identity,
            0.0f,
            (float)(2 * Math.PI)
        );
        rotator.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000.0));
        rotateTG.addChild(rotator);
        // Store the rotationAlpha for later toggling.
        rotateTG.setUserData(rotator);
        return rotateTG;
    }

    public static TransformGroup createLevitationTG(Node node, float minOffset, float maxOffset, long cycleTime) {
        TransformGroup levitateTG = new TransformGroup();
        levitateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        levitateTG.addChild(node);
        Levitation levitator = new Levitation(levitateTG, minOffset, maxOffset, cycleTime);
        levitator.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000.0));
        levitateTG.addChild(levitator);
        // Store the levitation behavior for later toggling.
        levitateTG.setUserData(levitator);
        return levitateTG;
    }


    public static class Levitation extends Behavior {
        private TransformGroup tg;
        private Transform3D transform = new Transform3D();
        private WakeupOnElapsedTime wakeup = new WakeupOnElapsedTime(30);
        private double y;
        private double deltaY;
        private double yMin;
        private double yMax;
        private int delayCounter = 0;
        private int delayCycles = 15;
        private boolean paused = false;


        public Levitation(TransformGroup tg, float minOffset, float maxOffset, long cycleTime) {
            this.tg = tg;
            this.yMin = minOffset;
            this.yMax = maxOffset;
            this.y = minOffset;
            int halfCycles = (int)((cycleTime / 2) / 30);
            this.deltaY = (maxOffset - minOffset) / (double) halfCycles;
            this.delayCycles = halfCycles / 3;
        }

        @Override
        public void initialize() {
            wakeupOn(wakeup);
        }

        public void togglePause() {
            paused = !paused;
        }

        @Override
        public void processStimulus(Iterator<WakeupCriterion> criteria) {
            if (paused) {
                wakeupOn(wakeup);
                return;
            }
            if (delayCounter > 0) {
                delayCounter--;
                wakeupOn(wakeup);
                return;
            }
            y += deltaY;
            if (y >= yMax) {
                y = yMax;
                delayCounter = delayCycles;
                deltaY = -Math.abs(deltaY);
            } else if (y <= yMin) {
                y = yMin;
                delayCounter = delayCycles;
                deltaY = Math.abs(deltaY);
            }
            transform.setTranslation(new Vector3d(0, y, 0));
            tg.setTransform(transform);
            wakeupOn(wakeup);
        }
    }
}
