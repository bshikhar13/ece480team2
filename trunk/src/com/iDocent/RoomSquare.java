package com.iDocent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.*;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;
import android.widget.TextView;

public class RoomSquare extends GraphicsObject{	
	// The order we like to connect them.
	private short[] indices = { 0, 1, 3, 0, 3, 2 };
	
	// Our vertex buffer.
	private FloatBuffer vertexBuffer;

	// Our index buffer.
	private ShortBuffer indexBuffer;
	
	private float[] color = {0,0,0,0};
	private float[] selectedColor = {1,0,0,0.25f};
	private Line[] bound = new Line[4];
	
	private iDocent miD;
	
	private float[] vertices;
	
	private TexturedSquare TexSq;

	private boolean selected = false;
	
	public RoomSquare(float doorX, float doorY, String hallSide, String doorLocation, float width, 
			float height, String colorString, iDocent iD, int i) {
		
		miD = iD;
		color[3] = 0.25f;
		if(colorString.toLowerCase().equals("blue"))
		{
			color[2] = 1.0f;
		}
		else if(colorString.toLowerCase().equals("red"))
		{
			color[0] = 1.0f;
		}
		else if(colorString.toLowerCase().equals("green"))
		{
			color[1] = 1.0f;
		}
		else if(colorString.toLowerCase().equals("yellow"))
		{
			color[0] = color[1] = 1.0f;
		}
		else if(colorString.toLowerCase().equals("white"))
		{
			color[0] = color[1] = color[2] = 1;
		}
		else if(colorString.toLowerCase().equals("gray"))
		{
			color[0] = color[1] = color[2] = 0;
		}
		// Our vertices.
		float left=doorX-width, right = doorX, top = doorY+height/2f, bottom = doorY-height/2f;
		if(hallSide.toLowerCase().equals("right"))
		{
			left = doorX;
			right = doorX+width;
		}
		else if(hallSide.toLowerCase().equals("top"))
		{
			top = doorY+height;
			bottom = doorY;
		}
		else if(hallSide.toLowerCase().equals("bottom"))
		{
			top = doorY;
			bottom = doorY-height;
		}
		
		if(doorLocation.toLowerCase().equals("top"))
		{
			top = doorY;
			bottom = doorY-height;
		}
		else if(doorLocation.toLowerCase().equals("left"))
		{
			left = doorX;
			right = doorX+width;
		}
		else if(doorLocation.toLowerCase().equals("bottom"))
		{
			top = doorY+height;
			bottom = doorY;
		}
		vertices = new float[]{
			      left, bottom, 0.0f,  // 1, Bottom Left
			      right, bottom, 0.0f,  // 2, Bottom Right
			      left,  top, 0.0f,  // 0, Top Left
			      right, top, 0.0f,  // 3, Top Right
			};
		
		TexSq = new TexturedSquare(left, right, top, bottom);
		TexSq.setRoomNumber(i);
		
		bound[0] = new Line(left, top, left, bottom);//TL to BL
		bound[1] = new Line(left, bottom, right, bottom);//BL to BR
		bound[2] = new Line(right, bottom, right, top);//BR to TR
		bound[3] = new Line(right, top, left, top);//TR to TL
		
		// a float is 4 bytes, therefore we multiply the number if 
		// vertices with 4.
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		// short is 2 bytes, therefore we multiply the number if 
		// vertices with 2.
		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}
	
	/**
	 * This function draws our square on screen.
	 * @param gl
	 */
	public void Draw(GL10 gl) {	
		if(miD.ShowRoomNums() && TexSq.getTextureNum() != -1)
			TexSq.Draw(gl);
		gl.glEnable(GL10.GL_ALPHA_BITS);
		
		gl.glEnable(GL10.GL_BLEND);    
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glColor4f(color[0], color[1], color[2], color[3]);
		
		if(selected)
			gl.glColor4f(selectedColor[0], selectedColor[1], selectedColor[2], selectedColor[3]);

		// Counter-clockwise winding.
		gl.glFrontFace(GL10.GL_CCW);
		// Enable face culling.
		gl.glEnable(GL10.GL_CULL_FACE);
		// What faces to remove with the face culling.
		gl.glCullFace(GL10.GL_BACK);
		
		// Enabled the vertices buffer for writing and to be used during 
		// rendering.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// Specifies the location and data format of an array of vertex
		// coordinates to use when rendering.
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, 
				GL10.GL_UNSIGNED_SHORT, indexBuffer);
		
		// Disable the vertices buffer.
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		// Disable face culling.
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_ALPHA_BITS);
		gl.glDisable(GL10.GL_BLEND);   
		
		for(Line l : bound)
		{
			l.Draw(gl);
		}
	
		selected = false;
	}

	public void loadGLTexture(GL10 gl, iDocent miD) {
		if(TexSq.getTextureNum() != -1)
			TexSq.loadGLTexture(gl, miD);
	}

	public void setSelected() {
		selected  = true;
	}
}