package at.bii.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class FFT_Display extends JFrame {
	Color defBackground = Color.PINK;
	static Color specColorDef = Color.GREEN;
	final int maxSpectralValue = 150;

	JPanel myPaint = new JPanel();
	private int[] spectrum;
	private int type;

	public FFT_Display(int type) {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		myPaint.setBackground(defBackground);
		getContentPane().add(myPaint);
		setSize(new Dimension(800, 600));
		myPaint.setDoubleBuffered(true);
		this.type = type;
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

	ArrayList <Double> lstValues=new ArrayList<Double>();
	double epsilon=0.1;
	
	private void paintSpectrum(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		int middleY = height / 2;

		g.setColor(defBackground);

		Graphics2D g2 = (Graphics2D) g;

		g2.drawRect(0, 0, width, height);

		int barWidth = width / spectrum.length;
		int maxValue = maxSpectralValue;

		double average = 0;
		for (int i = 0; i < spectrum.length; i++) {
			/*
			 * recalc it each time cause there might be rounding errors which
			 * prevent a proper fill of the frame
			 */
			int barPos = width * i / spectrum.length;

			int curHeight = (spectrum[i] - maxSpectralValue) * height
					/ maxValue;
			if (i > 0)
				average += Math.abs(curHeight);

			if (type == 0) {
				GradientPaint green2red = new GradientPaint(barPos, middleY,
						Color.GREEN, barPos + barWidth, middleY / 2, Color.RED);
				g2.setPaint(green2red);
				g2.drawRect(barPos, middleY, barWidth, curHeight / 2);

				g2.drawRect(barPos, middleY, barWidth, -curHeight / 2);

			} else if (type == 1 || type == 2) {
				int percentage = Math.min(100,
						Math.abs(curHeight * 100 / height));
				g2.setColor(getColor(percentage, type));
				g2.drawRect(barPos, middleY - curHeight / 2, barWidth,
						curHeight);
			}
			g2.fillRect(barPos, middleY - curHeight / 2, barWidth, curHeight);
		}

		average=average/spectrum.length;
		if (lstValues.size()>=10) {
			lstValues.remove(0);
		}
		lstValues.add(average);
		int count = 1;
		for (Double checkVal:lstValues) {
			if (checkVal-epsilon< average && average<checkVal+epsilon) {
				count++;
			}
		}
		
		double percentage =  Math.min(100, Math.abs(average * 100 / height))/10d;
		Point2D center = new Point2D.Float((float) (width*percentage), (float) (height/2));
		
		
		float radius = (float) (50f*percentage*count);
		Point2D focus = new Point2D.Float(40, 40);
		float[] dist = { 0.0f, 0.2f, 1.0f };
		 Color[] colors = { Color.green, Color.WHITE, getColor(percentage,
		 type) };
//		Color[] colors = { Color.green, Color.WHITE, Color.BLUE };
		RadialGradientPaint p = new RadialGradientPaint(center, radius, focus,
				dist, colors, CycleMethod.NO_CYCLE);

		
		g2.setPaint(p);
		  g2.fillOval((int)(center.getX() - radius),(int)(center.getY() - radius),(int)(radius * 2),(int)(radius * 2));

//		System.out.printf("average: %.2f => percentage: %.2f (curHeight= %d) [%.2f,%.2f] \n",
//				average, percentage, height, center.getX(), center.getY());

	}

	public final static Color getColor(double power, int type) {
		if (type == 1) {
			double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart
									// below)
			double S = 0.9; // Saturation
			double B = 0.9; // Brightness
			return Color.getHSBColor((float) H, (float) S, (float) B);
		} else if (type == 2) {
			int red = (int) (255 * power / 100);
			int green = (int) ((255 * (100 - power)) / 100);
			int blue = 0;
			return new Color(red, green, blue);
		} else
			return specColorDef;

	}

}
