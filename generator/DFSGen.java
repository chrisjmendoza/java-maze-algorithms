package generator;

import java.util.List;
import java.util.Stack;

import javax.swing.Timer;

import main.*;
import util.Cell;

public class DFSGen {

	private final Stack<Cell> stack = new Stack<>();
	private final List<Cell> grid;
	private Cell current;

	public DFSGen(List<Cell> grid, MazeGridPanel panel) {
		this.grid = grid;
		current = grid.get(0);
		final Timer timer = new Timer(Maze.speed, null);
		timer.addActionListener(e -> {
            if (!grid.parallelStream().allMatch(Cell::isVisited)) {
                carve();
            } else {
                current = null;
                Maze.generated = true;
                timer.stop();
            }
            panel.setCurrent(current);
            panel.repaint();
            timer.setDelay(Maze.speed);
        });
		timer.start();
	}

	private void carve() {
		current.setVisited(true);
		Cell next = current.getUnvisitedNeighbour(grid);
		if (next != null) {
			stack.push(current);
			current.removeWalls(next);
			current = next;
		} else if (!stack.isEmpty()) {
			try {
				current = stack.pop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}