package at.bii.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class SplitFrame extends JFrame {

	public static void main(String[] args) {
		new SplitFrame(1);
	}

	ExecutorService pool = Executors.newFixedThreadPool(1);
	private int type;
	private int[] spectrum;
	private ArrayList<ArrayList<Integer>> lstGrids128;
	private ArrayList<Point> lstGridFineTotal;
	int gridSize = 3;
	private Color[] lstColors;

	public SplitFrame(int type) {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(900, 600);
		this.type = type;
		setVisible(true);
		new Thread() {
			public void run() {
				initFrame();
			};
		}.start();
	}

	private void initFrame() {
		long start = System.currentTimeMillis();

		System.out.println("Start to calculate the grid");
		Object[] grids = createGridList(gridSize, 128);

		System.out.printf("Finished splitup after %d ms \n",
				(System.currentTimeMillis() - start));

		lstGrids128 = (ArrayList<ArrayList<Integer>>) grids[0];
		lstGridFineTotal = (ArrayList<Point>) grids[1];

		if (type == 2) {
			paintRandomColors();
		}
		
		lstColors = new Color[lstGrids128.size() + 1];
	}

	private void paintRandomColors() {
		ArrayList<Color> gridCol = createGridColor(lstGrids128);
		paintGrid(gridSize, lstGrids128, lstGridFineTotal, gridCol);

		while (true) {
			int item = (int) (Math.random() * lstGrids128.size());

			ArrayList<Integer> lstGrids = lstGrids128.get(item);
			paintBinArea(gridSize, lstGridFineTotal, gridCol.get(item)
					.brighter(), lstGrids);

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			paintBinArea(gridSize, lstGridFineTotal, gridCol.get(item),
					lstGrids);

		}
	}

	private ArrayList<Color> createGridColor(ArrayList<ArrayList<Integer>> grids) {
		ArrayList<Color> colList = new ArrayList<Color>();
		for (int i = 0; i < grids.size(); i++) {
			colList.add(new Color((int) (Math.random() * 0x1000000)));
		}
		return colList;
	}

	private void paintGrid(int gridSize,
			ArrayList<ArrayList<Integer>> lstGridPoints,
			ArrayList<Point> lstTotPoint, ArrayList<Color> gridCol) {

		for (int grid = 0; grid < lstGridPoints.size(); grid++) {

			ArrayList<Integer> lstGrids = lstGridPoints.get(grid);
			paintBinArea(gridSize, lstTotPoint, gridCol.get(grid), lstGrids);
		}
	}

	/*
	 * paints a Bin with the specified color within a Thread cause otherwise the
	 * display update might be out of sync
	 */
	private void paintBinArea(final int gridSize,
			final ArrayList<Point> lstTotPoint, final Color gridCol,
			final ArrayList<Integer> lstGrids) {

		final Graphics2D g = (Graphics2D) getGraphics();
		pool.execute(new Runnable() {
			public void run() {

				for (Integer curGrid : lstGrids) {
					g.setPaint(gridCol);
					g.fillRect(lstTotPoint.get(curGrid).x,
							lstTotPoint.get(curGrid).y, gridSize, gridSize);
				}
			}
		});
	}

	public Object[] createGridList(final int gridWidth, final int totSquare) {
		ArrayList<ArrayList<Integer>> lstGridPoints = new ArrayList<ArrayList<Integer>>(
				totSquare);
		ArrayList<Point> lstTotPoint = new ArrayList<Point>();

		final int width = getWidth();
		final int height = getHeight();

		final float boxSizeX = (float) width / totSquare / gridWidth;
		final float boxSizeY = (float) height / totSquare / gridWidth;

		ArrayList<Point> cloneList = new ArrayList<Point>();
		for (int row = 0; row < totSquare * gridWidth; row++) {
			for (int col = 0; col < totSquare * gridWidth; col++) {
				Point pt = new Point((int) (row * boxSizeX),
						(int) (col * boxSizeY));
				lstTotPoint.add(pt);
				cloneList.add(pt);
			}
		}

		/* maintain a list to keep track, which square is already used */
		ArrayList<Integer> lstUsedInt = new ArrayList<Integer>();

		/* create the inital list */
		for (int i = 0; i < totSquare; i++) {
			ArrayList<Integer> lstInt = new ArrayList<Integer>();
			boolean pt2beAdd = true;
			while (pt2beAdd) {
				int useSquare = (int) (Math.random() * lstTotPoint.size());
				/* only add a point if not already be used */
				if (!lstUsedInt.contains(useSquare)) {
					lstUsedInt.add(useSquare);
					lstInt.add(useSquare);
					cloneList.set(useSquare, null);
					pt2beAdd = false;
				}
			}
			lstGridPoints.add(lstInt);
		}

		boolean lstContainsValues = true;

		int count = 0;
		while (lstContainsValues) {

			addPoint2Grid(totSquare, lstGridPoints, width, boxSizeX, cloneList);

			int checkI = 1;
			for (; checkI < cloneList.size() && cloneList.get(checkI) == null; checkI++) {
			}
			lstContainsValues = (checkI < cloneList.size());
			count++;
		}

		System.out.println("Loops: " + count);
		return new Object[] { lstGridPoints, lstTotPoint };

	}

	private void addPoint2Grid(final int totSquare,
			final ArrayList<ArrayList<Integer>> lstGridPoints, final int width,
			final float boxSizeX, final ArrayList<Point> cloneList) {
		for (int i = 0; i < totSquare; i++) {

			int index = (int) (totSquare * Math.random());
			ArrayList<Integer> lastPoint = lstGridPoints.get(index);

			int pt = checkPoint(lastPoint, cloneList,
					(int) (width / boxSizeX + 1));
			if (pt > 0) {
				lastPoint.add(pt);
				cloneList.set(pt, null);
			}

		}

	}

	/* we need to check each direction if a grid could be added */
	private int checkPoint(final ArrayList<Integer> lastPoint,
			final ArrayList<Point> cloneList, final int lineRow) {

		for (Integer curPoint : lastPoint) {
			if (curPoint < cloneList.size() - 1) {
				int checkX = curPoint + 1;
				if (cloneList.get(checkX) != null) {
					return checkX;
				}
				/* check next line, simply below */
				int checkY = curPoint + lineRow;
				if (checkY < cloneList.size() - 1
						&& cloneList.get(checkY) != null) {
					return checkY;
				}
				/* check next line, simply below plus 1 */
				checkY = curPoint + lineRow + 1;
				if (checkY < cloneList.size() - 1
						&& cloneList.get(checkY) != null) {
					return checkY;
				}

			}
			if (curPoint > 0) {
				int checkX = curPoint - 1;
				if (cloneList.get(checkX) != null) {
					return checkX;
				}
				/* check next line, simply above */
				int checkY = curPoint - lineRow;
				if (checkY > 0 && cloneList.get(checkY) != null) {
					return checkY;
				}
				/* check next line, simply above minus 1 */
				checkY = curPoint - lineRow - 1;
				if (checkY > 0 && cloneList.get(checkY) != null) {
					return checkY;
				}

			}
		}

		/* nothing found what could be added */
		return -1;
	}

	public void displayData(int[] spectrum) {
		this.spectrum = spectrum;
		paintComponents(getGraphics());
	}

	@Override
	public void paintComponents(Graphics g) {
		super.paintComponents(g);
		if (type==2)
		{
			/* since we display a random blinking no update on the display */
			return;
		}
		paintSpectrum(g);
	}

	private void paintSpectrum(Graphics g) {
		for (int i = 0; i < spectrum.length; i++) {
			Color color = FFT_Display.getColor(spectrum[i], type);
			if (lstColors!=null&& !color.equals(lstColors[i])) {
				paintBinArea(gridSize, lstGridFineTotal, color,
						lstGrids128.get(i));
				lstColors[i] = color;
			}

		}
	}
}
