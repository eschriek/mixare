package org.mixare.lib.gui;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;

/**
 * 
 * @author edwin schriek
 *
 */
public class Util {

	private static int[] textures = new int[1];

	public static Bitmap drawableToBitmap(Drawable d) {
		return ((BitmapDrawable) d).getBitmap();
	}

	public static float[] paintColorByteToFloat(Paint paint) {

		int red = Color.red(paint.getColor());
		int green = Color.green(paint.getColor());
		int blue = Color.blue(paint.getColor());
		int alpha = Color.alpha(paint.getColor());

		return new float[] { red / 255f, green / 255f, blue / 255f,
				alpha / 255f };
	}

	public static int[] loadGLTexture(GL10 gl, Bitmap bitmap) {

		// Bitmap clone = bitmap.copy(bitmap.getConfig(), true);

		gl.glDeleteTextures(1, textures, 0);
		gl.glGenTextures(1, textures, 0);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		// GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, bitmap,
		// GL10.GL_RGBA, GL10.GL_UNSIGNED_SHORT_4_4_4_4);

		int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			System.out.println("GL ERROR : " + error);
		}

		// Clean up
		// bitmap.recycle();

		return textures;
	}
}
