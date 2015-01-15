package at.bii.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class FFT_Display extends JFrame {
	Color defBackground = Color.PINK;
	Color specColorDef = Color.GREEN;
	final int maxSpectralValue = 150;

	JPanel myPaint = new JPanel();
	private int[] spectrum;

	public FFT_Display() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		myPaint.setBackground(defBackground);
		getContentPane().add(myPaint);
		setSize(new Dimension(800, 600));
		setVisible(true);
		myPaint.setDoubleBuffered(true);
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
		paintComponents(getGraphics());
	}

	@Override
	public void paintComponents(Graphics g) {
		super.paintComponents(g);
		paintSpectrum(g);
	}


	private void paintSpectrum(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		int middleY = height / 2;

		g.setColor(defBackground);
		g.drawRect(0, 0, width, height);

		int barWidth = width / spectrum.length;
		int maxValue = maxSpectralValue;

		for (int i = 0; i < spectrum.length; i++) {
			/*
			 * recalc it each time cause there might be rounding errors which
			 * prevent a proper fill of the frame
			 */
			int barPos = width * i / spectrum.length;
			int curHeight = (spectrum[i] - maxSpectralValue) * height
					/ maxValue;
			int percentage = Math.min(100, Math.abs(curHeight * 100 / height));
			g.setColor(getColor(percentage, 1));

			g.drawRect(barPos, middleY - curHeight / 2, barWidth, curHeight);

			g.fillRect(barPos, middleY - curHeight / 2, barWidth, curHeight);
		}
	}

	private final Color getColor(double power, int type) {
		if (type == 0) {
			double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart
									// below)
			double S = 0.9; // Saturation
			double B = 0.9; // Brightness
			return Color.getHSBColor((float) H, (float) S, (float) B);
		} else if (type == 1) {
			int red = (int) (255 * power / 100);
			int green = (int) ((255 * (100 - power)) / 100);
			int blue = 0;
			return new Color(red, green, blue);
		} else
			return specColorDef;

	}

}
