
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
    public Color getColor() { return color; }
    public Quadrant[] getChildren() { return children; }
    public boolean isLeaf() { return isLeaf; }

    public void setColor(Color color) { this.color = color; }
    public void setChildren(Quadrant[] children) { this.children = children; }
    public void setLeaf(boolean isLeaf) { this.isLeaf = isLeaf; }
}
