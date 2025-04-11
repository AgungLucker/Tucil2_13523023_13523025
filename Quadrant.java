
import java.awt.*;

public class Quadrant {
    private int x, y, height, width;
    private Color color;
    private Quadrant[] children;
    private boolean isLeaf;

    public Quadrant(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isLeaf = false;
        this.children = null;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getNodesCount(Quadrant node) {
        if (node.isLeaf) return 1; 
        int count = 1; 
        if (node.getChildren() != null) {
            for (Quadrant child : node.getChildren()) {
                count += getNodesCount(child); 
            }
        }
        return count;
    }
    public int getMaxDepth(Quadrant node) {
        if (node.isLeaf) return 1; 
        int maxDepth = 0;
        if (node.getChildren() != null) {
            for (Quadrant child : node.getChildren()) {
                maxDepth = Math.max(maxDepth, getMaxDepth(child)); 
            }
        }
        return maxDepth + 1; 
    }
    public Color getColor() { return color; }
    public Quadrant[] getChildren() { return children; }
    public boolean isLeaf() { return isLeaf; }

    public void setColor(Color color) { this.color = color; }
    public void setChildren(Quadrant[] children) { this.children = children; }
    public void setLeaf(boolean isLeaf) { this.isLeaf = isLeaf; }
}
