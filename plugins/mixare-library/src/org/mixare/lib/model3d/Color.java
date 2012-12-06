package org.mixare.lib.model3d;

public class Color {
	public static final float ALPHA = .75f;
	static final int R = 0;
	static final int G = 1;
	static final int B = 2;
	static final int A = 3;
	
	public static final Color WHITE = new Color(1f, 1f, 1f, ALPHA);
	public static final Color GRAY = new Color(.6f, .6f, .6f, ALPHA);
	public static final Color RED = new Color(1f, 0f, 0f, ALPHA);
	public static final Color GREEN = new Color(0f, 1f, 0f, ALPHA);
	public static final Color BLUE = new Color(0f, 0f, 1f, ALPHA);
	public static final Color YELLOW = new Color(1f, 1f, 0f, ALPHA);
	public static final Color MAGENTA = new Color(1f, 0f, 1f, ALPHA);
	public static final Color CYAN = new Color(0f, 1f, 1f, ALPHA);
	
	public static final int QUESTION_RED = 0xEB2922;
	public static final int QUESTION_GREEN = 0x91ED19;
	public static final int QUESTION_BLUE = 0x4187DC;
	public static final int INFORMATION = 0x3191DE;
	public static final int TO_FAR = 0xF1D346;
	
	public float[] rgba = new float[4];

	public Color(float r, float g, float b, float a) {
		rgba[R] = r;
		rgba[G] = g;
		rgba[B] = b;
		rgba[A] = a;
	}
}
