/**
 * 
 */
package org.mixare.lib.gui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.graphics.Paint;

/**
 * @author edwin schriek
 * 
 */
public class Square {

	private int crop[];
	private FloatBuffer vertexBuffer; // buffer holding the vertices
	private float vertices[] = { 0.0f, 0.0f, 0.0f, // V1 - bottom left
			0.0f, 1.0f, 0.0f, // V2 - top left
			1.0f, 0.0f, 0.0f, // V3 - bottom right
			1.0f, 1.0f, 0.0f // V4 - top right
	};

	private FloatBuffer textureBuffer; // buffer holding the texture coordinates
	private float texture[] = {
			// Mapping coordinates for the vertices
			0.0f, 1.0f, // top left (V2)
			0.0f, 0.0f, // bottom left (V1)
			1.0f, 1.0f, // top right (V4)
			1.0f, 0.0f // bottom right (V3)
	};

	private ByteBuffer mIndexBuffer;
	private byte indices[] = { 0, 2, 3, 1 };

	/** The texture pointer */
	private int[] textures = new int[1];
	private boolean filled;
	private float lineWidth;
	private float x, y;
	private float[] color;

	public Square(Paint paint, float x, float y, float width, float height) {

		this.color = Util.paintColorByteToFloat(paint);

		if (paint.getStyle() == Paint.Style.FILL) {
			this.filled = true;
		} else {
			this.filled = false;
		}

		this.lineWidth = paint.getStrokeWidth();
		this.x = x;
		this.y = y;

		vertices[0] *= width;
		vertices[3] *= width;
		vertices[6] *= width;
		vertices[9] *= width;

		vertices[1] *= height;
		vertices[4] *= height;
		vertices[7] *= height;
		vertices[10] *= height;

		// a float has 4 bytes so we allocate for each coordinate 4 bytes
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());

		// allocates the memory from the byte buffer
		vertexBuffer = byteBuffer.asFloatBuffer();

		// fill the vertexBuffer with the vertices
		vertexBuffer.put(vertices);

		// set the cursor position to the beginning of the buffer
		vertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}

	public void draw(GL10 gl, int[] texture) {
		this.textures = texture;
		this.draw(gl);
	}

	/** The draw method for the square with the GL context */
	public void draw(GL10 gl) {

		// bind the previously generated texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		if (GLParameters.DRAWTEX) {
			crop = new int[] { 0, GLParameters.HEIGHT, GLParameters.WIDTH, -GLParameters.HEIGHT };

			((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
					GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);

			((GL11Ext) gl).glDrawTexfOES(x, y, 0, GLParameters.WIDTH, GLParameters.HEIGHT);
		} else {

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			// Set the face rotation
			gl.glFrontFace(GL10.GL_CW);

			// Point to our vertex buffer
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

			gl.glPushMatrix();
			gl.glTranslatef(x, y, 0f);
			if (textures[0] == 0) {
				gl.glColor4f(color[0], color[1], color[2], color[3]);
			}

			if (!filled) {
				gl.glLineWidth(lineWidth);
				gl.glDrawElements(GL10.GL_LINE_LOOP, vertices.length / 3,
						GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
			} else {
				// Draw the vertices as triangle strip
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
			}

			gl.glPopMatrix();

			// Disable the client state before leaving
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
	}
}
