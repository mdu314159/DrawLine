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

package com.jhlabs.composite;

import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;

public final class HardLightComposite extends RGBComposite {

  static class Context extends RGBCompositeContext {
    public Context(float alpha, ColorModel srcColorModel, ColorModel dstColorModel) {
      super(alpha, srcColorModel, dstColorModel);
    }

    @Override
    public void composeRGB(int[] src, int[] dst, float alpha) {
      final int w = src.length;

      for (int i = 0; i < w; i += 4) {
        final int sr = src[i];
        final int dir = dst[i];
        final int sg = src[i + 1];
        final int dig = dst[i + 1];
        final int sb = src[i + 2];
        final int dib = dst[i + 2];
        final int sa = src[i + 3];
        final int dia = dst[i + 3];
        int dor, dog, dob;

        if (sr > 127)
          dor = 255 - 2 * multiply255(255 - sr, 255 - dir);
        else
          dor = 2 * multiply255(sr, dir);
        if (sg > 127)
          dog = 255 - 2 * multiply255(255 - sg, 255 - dig);
        else
          dog = 2 * multiply255(sg, dig);
        if (sb > 127)
          dob = 255 - 2 * multiply255(255 - sb, 255 - dib);
        else
          dob = 2 * multiply255(sb, dib);

        final float a = alpha * sa / 255f;
        final float ac = 1 - a;

        dst[i] = (int) (a * dor + ac * dir);
        dst[i + 1] = (int) (a * dog + ac * dig);
        dst[i + 2] = (int) (a * dob + ac * dib);
        dst[i + 3] = (int) (sa * alpha + dia * ac);
      }
    }
  }

  public HardLightComposite(float alpha) {
    super(alpha);
  }

  @Override
  public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
    return new Context(extraAlpha, srcColorModel, dstColorModel);
  }

}