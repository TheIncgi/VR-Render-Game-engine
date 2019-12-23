package com.theincgi.lwjglApp.mvc.view.drawables;


import java.nio.FloatBuffer;
import java.util.Optional;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Color;

import static org.lwjgl.opengl.GL45.*;

public class HelloTriangle implements Drawable {
	private int vao, vbo;
	Optional<ShaderProgram> shader = Optional.empty();
	// number of coordinates per vertex in this array
	Location location = new Location();

	static final int COORDS_PER_VERTEX = 3;
	static float triangleCoords[] = {   // in counterclockwise order:
			0.0f,  0.622008459f, 0.0f, // top
			-0.5f, -0.311004243f, 0.0f, // bottom left
			0.5f, -0.311004243f, 0.0f  // bottom right
	};

	final String VERTEX_ATTRIB = "vPosition";


	public HelloTriangle() {
		FloatBuffer vertexBuffer = null;
		try {
			vertexBuffer = Utils.toBuffer(triangleCoords);

			vao = glGenVertexArrays();
			glBindVertexArray(vao);

			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		}finally{
			if(vertexBuffer!=null)
				Utils.freeBuffer(vertexBuffer);
		}

		shader = ShaderManager.INSTANCE.get("basic");

	}

	@Override
	public void onDestroy() {
		shader.ifPresent(s->s.disableVertexAttribArray(VERTEX_ATTRIB));
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vbo);
		glBindVertexArray(0);
		glDeleteVertexArrays(vao);
	}

	public Color color = new Color(0.63671875f, 0.76953125f, 0.22265625f);

	public Optional<ShaderProgram> getShader() {
		return shader;
	}

	@Override
	public void draw() {
		try (MatrixStack stk = MatrixStack.modelViewStack.pushTransform(location)){
			glBindVertexArray(vao);

			shader.ifPresentOrElse(s->{
				s.bind();
				s.tryEnableVertexAttribArray(VERTEX_ATTRIB);
				s.trySetMatrix("modelViewMatrix", stk.get());
			},()->glEnableVertexAttribArray(0));


			glDrawArrays(GL_TRIANGLES, 0, triangleCoords.length / COORDS_PER_VERTEX);

			shader.ifPresentOrElse(s->s.disableVertexAttribArray(VERTEX_ATTRIB),()->glDisableVertexAttribArray(0));
			glBindVertexArray(0);
			ShaderProgram.unbind();
		}
	}

	@Override
	public void drawAsColor(float[] mvpm, Color color) {
		// TODO Auto-generated method stub

	}

	@Override
	public Location getLocation() {
		return location;
	}




}
