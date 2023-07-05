import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class DFS {
    public Room room;
    public static Tile[][] prev;

    public static ArrayList<Tile> compute(Room room, Agent agent, boolean shuffle) {
        Stack<Tile> stack = new Stack<>(); // Tạo một stack để sử dụng làm ngăn xếp
        Tile current = room.grid[agent.x][agent.y];
        prev = new Tile[room.getGridWidth()][room.getGridHeight()];
        stack.push(current); // Đưa ô hiện tại vào stack
        boolean[][] visited = new boolean[room.getGridWidth()][room.getGridHeight()];
        visited[current.getX()][current.getY()] = true;

        while (!stack.isEmpty()) { // Lặp cho đến khi stack rỗng
            current = stack.pop(); // Lấy phần tử hiện tại ra khỏi stack
            visited[current.getX()][current.getY()] = true; // Đánh dấu ô hiện tại đã thăm
            ArrayList<Tile> neighbors = (ArrayList<Tile>) room.getNeighbors(current);
            if (shuffle)
                Collections.shuffle(neighbors);
            for (Tile tile : neighbors) {
                if (!visited[tile.getX()][tile.getY()]) {
                    visited[tile.getX()][tile.getY()] = true;
                    stack.push(tile); // Đưa ô hàng xóm vào stack
                    prev[tile.getX()][tile.getY()] = current;
                    if (!tile.isClean())
                        return backtrack(tile);
                }
            }
        }
        return null;
    }

    public static ArrayList<Tile> backtrack(Tile end) {
        ArrayList<Tile> path = new ArrayList<>();
        path.add(end);
        Tile prevTile = prev[end.getX()][end.getY()];
        while (prevTile != null) {
            path.add(prevTile);
            prevTile = prev[prevTile.getX()][prevTile.getY()];
        }
        Collections.reverse(path);
        return path;
    }
}
