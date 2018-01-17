package generator;

import java.util.List;
import java.util.Random;

import javax.swing.Timer;

import main.Maze;
import main.MazeGridPanel;
import util.Cell;

public class AldousBroderGen {
	
	private final List<Cell> grid;
	private Cell current;
	private final Random r = new Random();

	public AldousBroderGen(List<Cell> grid, MazeGridPanel panel) {
		this.grid = grid;
		current = grid.get(r.nextInt(grid.size() - 1));
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
		List<Cell> neighs = current.getAllNeighbours(grid);
		Cell next = neighs.get(r.nextInt(neighs.size()));
		if (!next.isVisited()) {
			current.removeWalls(next);
		}
		current = next;
	}
}