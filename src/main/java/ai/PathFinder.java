package ai;

import entity.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import main.GamePanel;
import tile_interactive.InteractiveTile;

/** Cardinal A* search. Terrain and destructible obstacles are sampled once per query. */
public class PathFinder {
    private final GamePanel gp;
    private Node[][] node;
    private final PriorityQueue<Node> openList = new PriorityQueue<>(
            Comparator.comparingInt((Node n) -> n.fCost)
                    .thenComparingInt(n -> n.gCost).thenComparingLong(n -> n.order));
    public final ArrayList<Node> pathList = new ArrayList<>();
    private Node startNode;
    private Node goalNode;
    private Node currentNode;
    private boolean validQuery;
    private boolean goalReached;
    private long nextOrder;

    public PathFinder(GamePanel gp) {
        this.gp = gp;
        intantiateNodes();
    }

    /** Original method name retained for compatibility with recovered sources. */
    public void intantiateNodes() {
        node = new Node[gp.maxWorldCol][gp.maxWorldRow];
        for (int col = 0; col < node.length; col++) {
            for (int row = 0; row < node[col].length; row++) node[col][row] = new Node(col, row);
        }
    }

    public void resetNodes() {
        for (Node[] column : node) {
            for (Node cell : column) {
                cell.parent = null;
                cell.open = cell.checked = cell.solid = false;
                cell.gCost = Integer.MAX_VALUE;
                cell.hCost = cell.fCost = 0;
            }
        }
        openList.clear();
        pathList.clear();
        validQuery = goalReached = false;
        startNode = goalNode = currentNode = null;
        nextOrder = 0;
    }

    public void setNodes(int startCol, int startRow, int goalCol, int goalRow, Entity entity) {
        resetNodes();
        if (!inside(startCol, startRow) || !inside(goalCol, goalRow)
                || !gp.tileM.isMapLoaded(gp.currentMap)) return;
        startNode = node[startCol][startRow];
        goalNode = node[goalCol][goalRow];
        // Callers consume pathList[0]; no movement is needed if already at the target.
        if (startNode == goalNode) return;
        for (Node[] column : node) {
            for (Node cell : column) {
                int tileNumber = gp.tileM.mapTileNum[gp.currentMap][cell.col][cell.row];
                cell.solid = gp.tileM.tile[tileNumber].collision;
                cell.hCost = Math.abs(cell.col - goalCol) + Math.abs(cell.row - goalRow);
            }
        }
        // Previously this scan ran once per cell: 2,500 scans on a 50x50 map.
        for (InteractiveTile obstacle : gp.iTile[gp.currentMap]) {
            if (obstacle == null || !obstacle.destructible) continue;
            int col = obstacle.worldX / gp.tileSize;
            int row = obstacle.worldY / gp.tileSize;
            if (inside(col, row)) node[col][row].solid = true;
        }
        if (goalNode.solid) return;
        startNode.gCost = 0;
        startNode.fCost = startNode.hCost;
        startNode.open = true;
        startNode.order = nextOrder++;
        openList.add(startNode);
        validQuery = true;
    }

    public boolean search() {
        if (!validQuery) return false;
        while (!openList.isEmpty()) {
            currentNode = openList.remove();
            currentNode.open = false;
            currentNode.checked = true;
            if (currentNode == goalNode) {
                goalReached = true;
                trackThePath();
                return true;
            }
            int col = currentNode.col;
            int row = currentNode.row;
            if (row > 0) openNode(node[col][row - 1]);
            if (col > 0) openNode(node[col - 1][row]);
            if (row + 1 < gp.maxWorldRow) openNode(node[col][row + 1]);
            if (col + 1 < gp.maxWorldCol) openNode(node[col + 1][row]);
        }
        return false;
    }

    public void openNode(Node candidate) {
        if (candidate.checked || candidate.solid) return;
        int cost = currentNode.gCost + 1;
        if (cost >= candidate.gCost) return;
        if (candidate.open) openList.remove(candidate);
        candidate.parent = currentNode;
        candidate.gCost = cost;
        getCost(candidate);
        candidate.open = true;
        candidate.order = nextOrder++;
        openList.add(candidate);
    }

    public void getCost(Node cell) {
        cell.hCost = Math.abs(cell.col - goalNode.col) + Math.abs(cell.row - goalNode.row);
        cell.fCost = cell.gCost + cell.hCost;
    }

    public void trackThePath() {
        pathList.clear();
        if (!goalReached) return;
        for (Node current = goalNode; current != startNode; current = current.parent) pathList.add(current);
        Collections.reverse(pathList);
    }

    private boolean inside(int col, int row) {
        return col >= 0 && row >= 0 && col < gp.maxWorldCol && row < gp.maxWorldRow;
    }
}
