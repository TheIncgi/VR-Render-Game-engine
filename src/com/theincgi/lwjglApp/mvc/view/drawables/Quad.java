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
@Deprecated
public class Quad implements Drawable {
	private int vao, vbo;
	Optional<ShaderProgram> shader = Optional.empty();
	// number of coordinates per vertex in this array
	Location location = new Location();

	static final int COORDS_PER_VERTEX = 3;
	static float triangleCoords[] = {
		    -1.0f, -1.0f, 0.0f,
		    1.0f, -1.0f, 0.0f,
		    -1.0f,  1.0f, 0.0f,
		    -1.0f,  1.0f, 0.0f,
		    1.0f, -1.0f, 0.0f,
		    1.0f,  1.0f, 0.0f,
		};
	static float uv[] = {
		0, 1,
		1, 1,
		0, 0,
		0, 0,
		1, 1,
		1, 0
	};

	final String VERTEX_ATTRIB = "vPosition";

	@Deprecated
	public Quad() {
		FloatBuffer vertexBuffer = null;
		FloatBuffer uvBuffer = null;
		try {
			vertexBuffer = Utils.toBuffer(triangleCoords);
			uvBuffer = Utils.toBuffer(uv);

			vao = glGenVertexArrays();
			glBindVertexArray(vao);

			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, (6*5)*Float.BYTES, GL_STATIC_DRAW);
			glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
			glBufferSubData(GL_ARRAY_BUFFER, (triangleCoords.length*Float.BYTES), uvBuffer);
			//glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, (triangleCoords.length*Float.BYTES));

			glBindVertexArray(0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			
		}finally{
			if(vertexBuffer!=null)
				Utils.freeBuffer(vertexBuffer);
			if(uv!=null)
				Utils.freeBuffer(uvBuffer);
		}

		shader = ShaderManager.INSTANCE.get("basic");

	}

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

	public void drawAsColor(Color color) {
		// TODO Auto-generated method stub

	}

	public Location getLocation() {
		return location;
	}

	public void setShader(Optional<ShaderProgram> optional) {
		this.shader = optional;
	}
}
