/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 
package dlg.visualization;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

// TODO: Auto-generated Javadoc
/**
 * The Class LabelEdgeRenderer.
 * 
 * @author santi
 */
public class LabelEdgeRenderer extends EdgeRenderer {

	/** The m_font. */
	protected Font m_font = null; // temp font holder

	/**
	 * Instantiates a new label edge renderer.
	 * 
	 * @param labelColor
	 *            the label color
	 */
	public LabelEdgeRenderer(int labelColor) {
		super();
	}

	/**
	 * Instantiates a new label edge renderer.
	 * 
	 * @param lt
	 *            the lt
	 * @param labelColor
	 *            the label color
	 */
	public LabelEdgeRenderer(int lt, int labelColor) {
		super(lt);
	}

	/**
	 * Instantiates a new label edge renderer.
	 * 
	 * @param lt
	 *            the lt
	 * @param at
	 *            the at
	 * @param labelColor
	 *            the label color
	 */
	public LabelEdgeRenderer(int lt, int at, int labelColor) {
		super(lt, at);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see prefuse.render.EdgeRenderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {
		// render the edge line
		super.render(g, item);
		
		// Add label:
		Shape s = getShape(item);
		if (s != null) {
			Rectangle2D r = s.getBounds2D();
			boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(), g.getTransform().getScaleY());
			if (m_font == null)
				m_font = item.getFont();
			// scale the font as needed
			FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
			if (item.canGetString("name")) {
				g.setFont(m_font);
				g.setPaint(ColorLib.getColor(item.getTextColor()));
				drawString(g, fm, item.getString("name"), useInt, r.getX(), r.getY(), r.getWidth(), r.getHeight());
			}

		}
	}

	/**
	 * Draw string.
	 * 
	 * @param g
	 *            the g
	 * @param fm
	 *            the fm
	 * @param text
	 *            the text
	 * @param useInt
	 *            the use int
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 */
	private final void drawString(Graphics2D g, FontMetrics fm, String text, boolean useInt, double x, double y, double w, double h) {
		// compute the x-coordinate
		double tx;
		double ty;
		

		tx = x + (w - fm.stringWidth(text))/2;
		ty = y + (h / 2) - fm.getHeight()/2;
		// use integer precision unless zoomed-in
		// results in more stable drawing

		if (useInt) {
			g.drawString(text, (int) tx, (int) ty);
		} else {
			g.drawString(text, (float) tx, (float) ty);
		}
		
	}

}
