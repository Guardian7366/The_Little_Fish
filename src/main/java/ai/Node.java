package ai;

/** A reusable cell in the pathfinding grid. Costs are reset for each search. */
public class Node {
    Node parent;
    public final int col;
    public final int row;
    int gCost;
    int hCost;
    int fCost;
    boolean solid;
    boolean open;
    boolean checked;
    long order;

    public Node(int col, int row) {
        this.col = col;
        this.row = row;
    }
}
