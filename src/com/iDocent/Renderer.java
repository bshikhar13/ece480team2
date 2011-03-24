package com.iDocent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

class Renderer implements GLSurfaceView.Renderer {
	private Map map;
	
	private float posX = 75;
	private float posY = 100;
	private float xOffset = 0;
	private float yOffset = 0;
	private float zoom = -300;
	
	public Renderer(){
		map = new Map();
	}
	
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	// Set the background color to black ( rgba ).
		gl.glClearColor(255.0f, 255.0f, 255.0f, 0.5f);
		// Enable Smooth Shading, default not really needed.
		gl.glShadeModel(GL10.GL_SMOOTH);
		// Depth buffer setup.
		gl.glClearDepthf(1.0f);
		// Enables depth testing.
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// The type of depth testing to do.
		gl.glDepthFunc(GL10.GL_LEQUAL);
		// Really nice perspective calculations.
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
    	// Sets the current view port to the new size.
		gl.glViewport(0, 0, w, h);
		// Select the projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		// Reset the projection matrix
		gl.glLoadIdentity();
		// Calculate the aspect ratio of the window
		GLU.gluPerspective(gl, 45.0f, (float) w / (float) h, 0.1f,
				400.0f);
		// Select the modelview matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// Reset the modelview matrix
		gl.glLoadIdentity();
    }

    public void onDrawFrame(GL10 gl) {
    	// Clears the screen and depth buffer.
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// Replace the current matrix with the identity matrix
		gl.glLoadIdentity();
		gl.glTranslatef(-(posX+xOffset), -(posY+yOffset), zoom); 
        map.Draw(gl);
		
		// Replace the current matrix with the identity matrix
		gl.glLoadIdentity(); // OpenGL docs        
    }

	public void UpdateLocation(float x, float y) {
		posX = x;
		posY = y;
		map.UpdateLoction(posX, posY);
	}

	public void zoomOut() {
		zoom -= 50;
		if(zoom <= -350)
			zoom = -350;
	}

	public void zoomIn() {
		zoom += 50;
		if(zoom >= -50)
			zoom = -50;
	}

	public void MoveCamera(float x, float y) {
		xOffset = -x*zoom/-350f;
		yOffset = y*zoom/-350f;
	}
	
	public void CenterCamera()
	{
		xOffset = yOffset = 0;
	}
}
