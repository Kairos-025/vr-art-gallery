import org.jogamp.java3d.Node;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.BranchGroup;

public class GroupObjects {

    public static BranchGroup scene_Group(BranchGroup a_BG, BranchGroup s_BG) {
        BranchGroup sceneBG = new BranchGroup();

        a_BG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        a_BG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        s_BG.setCapability(BranchGroup.ALLOW_DETACH);      // Allow 'shapeBG' detached
        a_BG.addChild(s_BG);                               // Add 'shapeBG' to 'alterableBG'

        TransformGroup sceneTG = new TransformGroup();     // Introduce a TransformGroup for rotation
        sceneTG.addChild(a_BG);                            // Attach 'alterableBG' to 'sceneTG'
        sceneBG.addChild(Commons.rotate_Behavior(7500, sceneTG));
        Commons.control_Rotation(true);                 // Make 'alterableBG' rotating by default
        sceneBG.addChild(sceneTG);

        return sceneBG;                                    // Return the structured 'sceneBG'
    }

    protected BranchGroup shapeBG = new BranchGroup();

    /* A function to return 'shapeBG' as a BranchGroup */
    public BranchGroup get_ShapeBG() {
        return shapeBG;
    }

    public GroupObjects(Node p) {
        shapeBG.addChild(p);
    }
}
