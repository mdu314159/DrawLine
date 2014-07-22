package com.mdu.DrawLine;

import java.awt.Graphics2D;
import java.awt.Shape;

public abstract class DLRenderer {
  abstract void render(Graphics2D g);
  abstract void render(Graphics2D g, int i);
  abstract Shape makeGraphics();
}