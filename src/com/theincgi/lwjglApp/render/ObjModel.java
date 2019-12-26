package com.theincgi.lwjglApp.render;

import java.io.File;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;

public class ObjModel {
	
	/**Should only be used by the ObjManager<br>
	 * Directories will be used as model frames using number suffix to determine order<br>
	 * the folder should contain no subdirectories or other files other than the .obj and .mtl*/
	public static void load(File file) {
		
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public float fps = 60;
	private Model[] frames;
	private ObjModel(int frames) {
		this.frames = new Model[frames];
	}
	
	public void delete() {
		for (Model model : frames) {
			model.delete();
		}
	}
	
	
	public class Model{
		int VAO; //the settings n stuff
		int VBO; //the data
		FloatBuffer data;
		int stride = 3;
		public Model() {
			VAO = glGenVertexArrays();
			glBindVertexArray(VAO);
			VBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, VBO);
			glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
			
			glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0); 
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		}
		
		private void delete() {
			
		}
		
	}
	
	
}
