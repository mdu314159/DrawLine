/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.jhlabs.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * A filter which draws a drop shadow based on the alpha channel of the image.
 */
public class ShadowFilter extends AbstractBufferedImageOp {

  private boolean addMargins = false;
  private float angle = (float) (Math.PI * 7. / 8.);
  private float distance = 5;
  private float opacity = 0.5f;
  private float radius = 5;
  private int shadowColor = 0xff000000;
  private boolean shadowOnly = false;

  /**
   * Construct a ShadowFilter.
   */
  public ShadowFilter() {
  }

  /**
   * Construct a ShadowFilter.
   *
   * @param radius
   *          the radius of the shadow
   * @param xOffset
   *          the X offset of the shadow
   * @param yOffset
   *          the Y offset of the shadow
   * @param opacity
   *          the opacity of the shadow
   */
  public ShadowFilter(float radius, float xOffset, float yOffset, float opacity) {
    this.radius = radius;
    this.angle = (float) Math.atan2(yOffset, xOffset);
    this.distance = (float) Math.sqrt(xOffset * xOffset + yOffset * yOffset);
    this.opacity = opacity;
  }

  @Override
  public BufferedImage filter(BufferedImage src, BufferedImage dst) {
    final int width = src.getWidth();
    final int height = src.getHeight();

    final float xOffset = distance * (float) Math.cos(angle);
    final float yOffset = -distance * (float) Math.sin(angle);

    if (dst == null)
      if (addMargins) {
        final ColorModel cm = src.getColorModel();
        dst = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth()
            + (int) (Math.abs(xOffset) + radius), src.getHeight() + (int) (Math.abs(yOffset) + radius)),
            cm.isAlphaPremultiplied(), null);
      } else
        dst = createCompatibleDestImage(src, null);

    final float shadowR = (shadowColor >> 16 & 0xff) / 255f;
    final float shadowG = (shadowColor >> 8 & 0xff) / 255f;
    final float shadowB = (shadowColor & 0xff) / 255f;

    // Make a black mask from the image's alpha channel
    final float[][] extractAlpha = { { 0, 0, 0, shadowR }, { 0, 0, 0, shadowG }, { 0, 0, 0, shadowB },
        { 0, 0, 0, opacity } };
    BufferedImage shadow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    new BandCombineOp(extractAlpha, null).filter(src.getRaster(), shadow.getRaster());
    shadow = new GaussianFilter(radius).filter(shadow, null);

    final Graphics2D g = dst.createGraphics();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    if (addMargins) {
      final float radius2 = radius / 2;
      final float topShadow = Math.max(0, radius2 - yOffset);
      final float leftShadow = Math.max(0, radius2 - xOffset);
      g.translate(leftShadow, topShadow);
    }
    g.drawRenderedImage(shadow, AffineTransform.getTranslateInstance(xOffset, yOffset));
    if (!shadowOnly) {
      g.setComposite(AlphaComposite.SrcOver);
      g.drawRenderedImage(src, null);
    }
    g.dispose();

    return dst;
  }

  /**
   * Get whether to increase the size of the output image to accomodate the
   * shadow.
   *
   * @return true to add margins.
   * @see #setAddMargins
   */
  public boolean getAddMargins() {
    return addMargins;
  }

  /**
   * Returns the angle of the shadow.
   *
   * @return the angle of the shadow.
   * @see #setAngle
   */
  public float getAngle() {
    return angle;
  }

  @Override
  public Rectangle2D getBounds2D(BufferedImage src) {
    final Rectangle r = new Rectangle(0, 0, src.getWidth(), src.getHeight());
    if (addMargins) {
      final float xOffset = distance * (float) Math.cos(angle);
      final float yOffset = -distance * (float) Math.sin(angle);
      r.width += (int) (Math.abs(xOffset) + 2 * radius);
      r.height += (int) (Math.abs(yOffset) + 2 * radius);
    }
    return r;
  }

  /**
   * Get the distance of the shadow.
   *
   * @return the distance.
   * @see #setDistance
   */
  public float getDistance() {
    return distance;
  }

  /**
   * Get the opacity of the shadow.
   *
   * @return the opacity.
   * @see #setOpacity
   */
  public float getOpacity() {
    return opacity;
  }

  @Override
  public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    if (dstPt == null)
      dstPt = new Point2D.Double();

    if (addMargins) {
      final float xOffset = distance * (float) Math.cos(angle);
      final float yOffset = -distance * (float) Math.sin(angle);
      final float topShadow = Math.max(0, radius - yOffset);
      final float leftShadow = Math.max(0, radius - xOffset);
      dstPt.setLocation(srcPt.getX() + leftShadow, srcPt.getY() + topShadow);
    } else
      dstPt.setLocation(srcPt.getX(), srcPt.getY());

    return dstPt;
  }

  /**
   * Get the radius of the kernel.
   *
   * @return the radius
   * @see #setRadius
   */
  public float getRadius() {
    return radius;
  }

  /**
   * Get the color of the shadow.
   *
   * @return the color.
   * @see #setShadowColor
   */
  public int getShadowColor() {
    return shadowColor;
  }

  /**
   * Get whether to only draw the shadow without the original image.
   *
   * @return true to only draw the shadow.
   * @see #setShadowOnly
   */
  public boolean getShadowOnly() {
    return shadowOnly;
  }

  /**
   * Set whether to increase the size of the output image to accomodate the
   * shadow.
   *
   * @param addMargins
   *          true to add margins.
   * @see #getAddMargins
   */
  public void setAddMargins(boolean addMargins) {
    this.addMargins = addMargins;
  }

  /**
   * Specifies the angle of the shadow.
   *
   * @param angle
   *          the angle of the shadow.
   * @angle
   * @see #getAngle
   */
  public void setAngle(float angle) {
    this.angle = angle;
  }

  /**
   * Set the distance of the shadow.
   *
   * @param distance
   *          the distance.
   * @see #getDistance
   */
  public void setDistance(float distance) {
    this.distance = distance;
  }

  /**
   * Set the opacity of the shadow.
   *
   * @param opacity
   *          the opacity.
   * @see #getOpacity
   */
  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }

  /**
   * Set the radius of the kernel, and hence the amount of blur. The bigger the
   * radius, the longer this filter will take.
   *
   * @param radius
   *          the radius of the blur in pixels.
   * @see #getRadius
   */
  public void setRadius(float radius) {
    this.radius = radius;
  }

  /**
   * Set the color of the shadow.
   *
   * @param shadowColor
   *          the color.
   * @see #getShadowColor
   */
  public void setShadowColor(int shadowColor) {
    this.shadowColor = shadowColor;
  }

  /**
   * Set whether to only draw the shadow without the original image.
   *
   * @param shadowOnly
   *          true to only draw the shadow.
   * @see #getShadowOnly
   */
  public void setShadowOnly(boolean shadowOnly) {
    this.shadowOnly = shadowOnly;
  }

  @Override
  public String toString() {
    return "Stylize/Drop Shadow...";
  }
}
