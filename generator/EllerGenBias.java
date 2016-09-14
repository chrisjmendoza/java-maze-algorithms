package generator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

import main.Maze;
import main.MazeGridPanel;
import util.Cell;
import util.DisjointSets;

// Slightly modified version in that the algorithm implemented here focuses on columns rather than rows.

public class EllerGenBias {
	
	private final List<Cell> grid;
	private List<Cell> currentCol;
	
	private final DisjointSets disjointSet = new DisjointSets();
	
	private static final int COLS = Math.floorDiv(Maze.WIDTH, Maze.W);
	private int bias = 50; // default
	private int fromIndex, toIndex;
	
	private boolean genNextCol = true;

	public EllerGenBias(List<Cell> grid, MazeGridPanel panel) {
		this.grid = grid;
		
		fromIndex = 0;
		toIndex = COLS;
		
		setBiasValueSlider();
		
		for (int i = 0; i < grid.size(); i++) {
			grid.get(i).setId(i);
			disjointSet.create_set(grid.get(i).getId());
		}
		
		final Timer timer = new Timer(Maze.speed, null);
		timer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (genNextCol) {
					currentCol = grid.subList(fromIndex, toIndex);
					fromIndex = toIndex;
					toIndex += COLS;
					new ColumnGen(currentCol, panel);
				} else if (grid.parallelStream().allMatch(c -> c.isVisited())) {
					Maze.generated = true;
					timer.stop();
				}
			}
		});
		timer.start();
	}
	
	private class ColumnGen {
		
		private final Queue<Cell> carveDownQueue = new LinkedList<Cell>();
		private final Queue<Cell> carveRightQueue = new LinkedList<Cell>();
		private final List<Cell> col;
		private final Random r = new Random();
		private Cell current;
		
		private ColumnGen(List<Cell> col, MazeGridPanel panel) {
			genNextCol = false;
			this.col = col;
			carveDownQueue.addAll(col);
			carveRightQueue.addAll(col);
			
			final Timer timer = new Timer(Maze.speed, null);
			timer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!carveDownQueue.isEmpty()) {
						carveDown();
					} else if (!carveRightQueue.isEmpty()) {
						carveRight();
					} else {
						current = null;
						genNextCol = true;
						timer.stop();
					}
					panel.setCurrent(current);
					panel.repaint();
					timer.setDelay(Maze.speed);
				}
			});
			timer.start();
		}
		
		private void carveDown() {
			current = carveDownQueue.poll();
			current.setVisited(true);
			
			if (r.nextInt(101) < bias || col.contains(grid.get(grid.size() - 1))) { // or last column
				Cell bottom = current.getBottomNeighbour(grid);
				if (bottom != null) {
					if (disjointSet.find_set(current.getId()) != disjointSet.find_set(bottom.getId())) {
						current.removeWalls(bottom);
						disjointSet.union(current.getId(), bottom.getId());
					}
				}
			}
		}	
		
		private void carveRight() {
			Cell c = carveRightQueue.poll();
			
			List<Cell> cells = new ArrayList<Cell>();
			for (Cell c2 : col) {
				if (disjointSet.find_set(c.getId()) == disjointSet.find_set(c2.getId())) {
					cells.add(c2);
				}
			}
			Collections.shuffle(cells);
			Cell c3 = cells.get(0);
			Cell right = c3.getRightNeighbour(grid);
			if (right != null) {
				current = right;
				right.setVisited(true);
				c3.removeWalls(right);
				disjointSet.union(c3.getId(), right.getId());
			}
		}
	}
	
	private void setBiasValueSlider() {
		
		JPanel myPanel = new JPanel();
		
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
		
		JSlider slider = new JSlider(1, 99, 50); // 1-99 as we don't want user to select 0 or 100
		slider.setMajorTickSpacing(7);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		String msg = "This is the % amount the maze will carve down. "
				+ "(E.g. if 60 is selected then the maze will carve down 60% of the time)";
		
		myPanel.add(new JLabel(msg));
		myPanel.add(Box.createVerticalStrut(15));
		myPanel.add(slider);
		myPanel.add(Box.createVerticalStrut(15));

		int result = JOptionPane.showConfirmDialog(null, myPanel, "Select Bias Value", JOptionPane.DEFAULT_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			bias = slider.getValue();
		}
	}
}