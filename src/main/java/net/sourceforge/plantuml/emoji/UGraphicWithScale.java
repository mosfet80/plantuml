/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.emoji;

import java.awt.geom.AffineTransform;

import net.sourceforge.plantuml.klimt.UChange;
import net.sourceforge.plantuml.klimt.UShape;
import net.sourceforge.plantuml.klimt.color.HColor;
import net.sourceforge.plantuml.klimt.color.HColorSet;
import net.sourceforge.plantuml.klimt.color.HColorSimple;
import net.sourceforge.plantuml.klimt.color.HColors;
import net.sourceforge.plantuml.klimt.drawing.UGraphic;

public class UGraphicWithScale {

	final private UGraphic ug;
	final private AffineTransform at;
	final private double angle;
	final private double scale;
	final private HColor forcedColor;
	final private int minGray;
	final private int maxGray;

	public UGraphicWithScale(UGraphic ug, HColor forcedColor, double scale, int minGray, int maxGray) {
		this(updateColor(ug, forcedColor), forcedColor, AffineTransform.getScaleInstance(scale, scale), 0, scale,
				minGray, maxGray);
	}

	private static UGraphic updateColor(UGraphic ug, HColor forcedColor) {
		final HColor color = forcedColor == null ? HColors.BLACK : forcedColor;
		return ug.apply(color).apply(color.bg());
	}

	private UGraphicWithScale(UGraphic ug, HColor forcedColor, AffineTransform at, double angle, double scale,
			int minGray, int maxGray) {
		this.ug = ug;
		this.forcedColor = forcedColor;
		this.minGray = minGray;
		this.maxGray = maxGray;
		this.at = at;
		this.angle = angle;
		this.scale = scale;
	}

	public UGraphic getUg() {
		return ug;
	}

	public UGraphicWithScale apply(UChange change) {
		return new UGraphicWithScale(ug.apply(change), forcedColor, at, angle, scale, minGray, maxGray);
	}

	public HColor getTrueColor(String code) {
		final HColorSimple result = (HColorSimple) HColorSet.instance().getColorOrWhite(code);
		if (forcedColor == null)
			return result;
		final HColorSimple color = (HColorSimple) forcedColor;
		if (color.isGray())
			return result.asMonochrome();
		return result.asMonochrome(color, this.minGray, this.maxGray);
	}

	public UGraphicWithScale applyScale(double changex, double changey) {
		if (changex != changey)
			throw new IllegalArgumentException();
		final AffineTransform copy = new AffineTransform(at);
		copy.scale(changex, changey);
		return new UGraphicWithScale(ug, forcedColor, copy, angle, 1 * changex, minGray, maxGray);
	}

	public void draw(UShape shape) {
		ug.draw(shape);
	}

	public UGraphicWithScale applyRotate(double delta_angle, double x, double y) {
		final AffineTransform copy = new AffineTransform(at);
		copy.rotate(delta_angle * Math.PI / 180, x, y);
		return new UGraphicWithScale(ug, forcedColor, copy, this.angle + delta_angle, this.scale, minGray, maxGray);
	}

	public UGraphicWithScale applyTranslate(double x, double y) {
		final AffineTransform copy = new AffineTransform(at);
		copy.translate(x, y);
		return new UGraphicWithScale(ug, forcedColor, copy, angle, this.scale, minGray, maxGray);
	}

	public AffineTransform getAffineTransform() {
		return at;
	}

	public UGraphicWithScale applyMatrix(double v1, double v2, double v3, double v4, double v5, double v6) {
		final AffineTransform copy = new AffineTransform(at);
		copy.concatenate(new AffineTransform(new double[] { v1, v2, v3, v4, v5, v6 }));
		return new UGraphicWithScale(ug, forcedColor, copy, angle, this.scale, minGray, maxGray);
	}

	public final double getAngle() {
		return angle;
	}

	public double getScale() {
		return scale;
	}

}
