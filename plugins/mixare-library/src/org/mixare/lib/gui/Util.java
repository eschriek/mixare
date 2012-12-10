package org.mixare.lib.gui;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLException;
import android.opengl.GLUtils;

/**
 * Utils
 * 
 * @author Edwin Schriek Nov 14, 2012 mixare-library
 * 
 */
public class Util {

	/**
	 * Convert the color from a Paint object to float
	 * 
	 * @param paint
	 *            Paint object that must be converted
	 * @return Returns a float array which contains the converted color, which
	 *         can be used by glColorf
	 */
	public static float[] paintColorByteToFloat(Paint paint) {

		int red = Color.red(paint.getColor());
		int green = Color.green(paint.getColor());
		int blue = Color.blue(paint.getColor());
		int alpha = Color.alpha(paint.getColor());

		return new float[] { red / 255f, green / 255f, blue / 255f,
				alpha / 255f };
	}

	/**
	 * Convert hex value (0xFFFFFF) to a value whhich glColor4f can understand.
	 * Does not calculate alpha, because blending takes care of this
	 * 
	 * @param hex
	 *            The color in hex thats need to be converted
	 * @return Returns useable rgb values (0-1)
	 */
	public static float[] hexToRGB(int hex) {
		float[] rgb = new float[3];
		rgb[0] = ((hex & 0xFF0000) >> 16) / 255f;
		rgb[1] = ((hex & 0xFF00) >> 8) / 255f;
		rgb[2] = ((hex & 0xFF)) / 255f;

		return rgb;
	}

	/**
	 * Generates a texture from a bitmap TODO: Optimise
	 * 
	 * @param gl
	 *            The gl object supplied by onDraw
	 * @param bitmap
	 *            The bitmap that should be converted
	 * @return The resulting texture
	 * @throws a
	 *             GLException if something went wrong
	 */
	public static int[] loadGLTexture(GL10 gl, Bitmap bitmap)
			throws GLException {

		int[] textures = new int[1];
		gl.glDeleteTextures(1, textures, 0);
		gl.glGenTextures(1, textures, 0);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
				GL10.GL_MODULATE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

//		GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 50, 50, bitmap,
//				GL10.GL_RGBA, GL10.GL_UNSIGNED_SHORT_4_4_4_4);

		int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			throw new GLException(error, "Foutje in texImage2D");
		}

		// Clean up
		// bitmap.recycle();

		return textures;
	}
}
