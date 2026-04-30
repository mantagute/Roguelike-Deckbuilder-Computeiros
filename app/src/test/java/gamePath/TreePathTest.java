package gamePath;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import events.Event;
import observer.Publisher;
import gameOrchestrator.GameFactory;

public class TreePathTest {

    @Test
    public void RootFromTreePathIsTheMiddleElement() { 
        Publisher publisher = new Publisher();
        TreePath treePath = GameFactory.createTreePath(publisher);
        Event event = treePath.getRoot().getEvents().get(1);
        String preview = event.getPreview();
        System.out.println(event.getPreview());
        assertTrue(preview.contains("Kojak"));
    }

    @Test
    public void RootHasSiblings() {
        Publisher publisher = new Publisher();
        TreePath treePath = GameFactory.createTreePath(publisher);
        assertNotNull(treePath.getRoot().getLeftNode());
        assertNotNull(treePath.getRoot().getRightNode());
    }

    @Test
    public void LeafNodesHaveNullChildren() {
        Publisher publisher = new Publisher();
        TreePath treePath = GameFactory.createTreePath(publisher);
        Node leaf = treePath.getRoot().getLeftNode().getLeftNode();
        assertNull(leaf.getLeftNode());
        assertNull(leaf.getRightNode());
    }
}
