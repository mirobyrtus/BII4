package at.bii.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FFT_Display extends JFrame {
	JPanel myPaint = new JPanel();
	private int[] spectrum;
	private boolean isPaintingInProgress;

	public FFT_Display() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		myPaint.setBackground(Color.PINK);
		getContentPane().add(myPaint);
		setSize(new Dimension(800, 600));
		setVisible(true);
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (!b) {
			System.exit(0);
		}
	}

	public void displayData(int[] spectrum) {
		this.spectrum = spectrum;
		if (!isPaintingInProgress)
			paintComponents(getGraphics());
	}

	@Override
	public void paintComponents(Graphics g) {
		super.paintComponents(g);
//		 System.out.println("Time plotted: " + System.currentTimeMillis());

		isPaintingInProgress = true;
		int width = getWidth();
		int height = getHeight();

		int barWidth = width / spectrum.length;

		for (int i = 0; i < spectrum.length; i++) {
			int curHeight = spectrum[i] * height / 0xffffffff;
			g.drawRect(i * barWidth, 0, barWidth, curHeight);
			g.setColor(Color.green);
			g.fillRect(i * barWidth, 0, barWidth, curHeight);
		}
		isPaintingInProgress = false;
	}
}
