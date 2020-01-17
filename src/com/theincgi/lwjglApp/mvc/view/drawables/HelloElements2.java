package com.theincgi.lwjglApp.mvc.view.drawables;

import static org.lwjgl.opengl.GL45.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Color;

public class HelloElements2 implements Drawable{
	Location location = new Location();
	private int vao;
	private int vbo;
	private int ibo;
	private Optional<ShaderProgram> shader;
	private IntBuffer indices;
	static float triangleCoords[] = {   // in counterclockwise order:
			-.5f,  0.622008459f, -5.0f, // top
			-0.5f, -0.311004243f, -5.0f, // bottom left
			0.5f, -0.311004243f, -5.0f  // bottom right
	};
	
	public HelloElements2() {
		FloatBuffer vertexBuffer = null;
		try {
			vertexBuffer = Utils.toBuffer(triangleCoords);

			vao = glGenVertexArrays();
			glBindVertexArray(vao);

			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			//glBufferData(GL_ARRAY_BUFFER, triangleCoords, GL_STATIC_DRAW);
			glBufferData(GL_ARRAY_BUFFER, 9*Float.BYTES, GL_STATIC_DRAW);
			glBufferSubData(GL_ARRAY_BUFFER, 0, triangleCoords);
			
			ibo = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[]{0,1,2}, GL_STATIC_DRAW);
			
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
		}finally{
			if(vertexBuffer!=null)
				Utils.freeBuffer(vertexBuffer);
		}

		shader = ShaderManager.INSTANCE.get("basic");
	}
	
	public void onDestroy() {
		shader.ifPresent(s->s.disableVertexAttribArray("vPosition"));
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vbo);
		glDeleteBuffers(ibo);
		glBindVertexArray(0);
		glDeleteVertexArrays(vao);
	}

	public void drawAsColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void draw() {
		try (MatrixStack stk = MatrixStack.modelViewStack.push(location)){
			glBindVertexArray(vao);

			shader.ifPresentOrElse(s->{
				s.bind();
				s.tryEnableVertexAttribArray("vPosition");
				s.trySetMatrix("modelViewMatrix", stk.get());
				s.bind();
			},()->glEnableVertexAttribArray(0));

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			glDrawRangeElementsBaseVertex(GL_TRIANGLES, 0, 2, 3, GL_UNSIGNED_INT, 0l, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

			shader.ifPresentOrElse(s->{
				s.disableVertexAttribArray("vPosition");
				ShaderProgram.unbind();
			},()->glDisableVertexAttribArray(0));
			glBindVertexArray(0);
			ShaderProgram.unbind();
		}
	}

	@Override
	public boolean isTransparent() {
		return false;
	}

	@Override
	public float[] getTransparentObjectPos() {
		return null;
	}
	@Override
	public Optional<Bounds> getBounds() {
		return Optional.empty();
	}
	@Override
	public boolean showBounds() {
		return false;
	}
	@Override
	public void setShowBounds(boolean show) {
	}
}
