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

public class HelloElements implements Drawable{
	Location location = new Location();
	private int vao;
	private int vbo;
	private int ibo;
	private Optional<ShaderProgram> shader;
	private IntBuffer indices;
	static float data[] = {   // in counterclockwise order:
			-.25f, -.25f, -0.0f,     0f, 1f,   0, 0, -1,
			 .25f,  .25f, -0.0f,     1f, 0f,   0, 0, -1,
			-.25f,  .25f, -0.0f,     1f, 1f,   0, 0, -1,
			 .25f, -.25f, -0.0f,     0f, 0f,   0, 0, -1
	};
	static int[] index = new int[]{0,1,2, 0,3,1};
	
	public HelloElements() {
		FloatBuffer vertexBuffer = null;
		IntBuffer indBuff = null;
		try {
			vertexBuffer = Utils.toBuffer(data);
			indBuff = Utils.toBuffer(index);

			vao = glGenVertexArrays();
			glBindVertexArray(vao);

			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

			
			ibo = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indBuff, GL_STATIC_DRAW);
			
			int stride = Float.BYTES * 8;
			glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 5 * Float.BYTES);
			
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
		}finally{
				Utils.freeBuffer(vertexBuffer);
				Utils.freeBuffer(indBuff);
		}

		shader = ShaderManager.INSTANCE.get("showUv");
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
		
	}

	

	@Override
	public void draw() {
		try (MatrixStack stk = MatrixStack.modelViewStack.pushTransform(location)){
			glBindVertexArray(vao);

			shader.ifPresentOrElse(s->{
				s.bind();
				s.tryEnableVertexAttribArray("vPosition");
				s.tryEnableVertexAttribArray("tPosition");
				s.tryEnableVertexAttribArray("nPosition");
				s.trySetMatrix("modelViewMatrix", stk.get());
				s.bind();
			},()->glEnableVertexAttribArray(0));

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			glDrawElements(GL_TRIANGLES, index.length, GL_UNSIGNED_INT, 0l);
			//glDrawRangeElementsBaseVertex(GL_TRIANGLES, 0, 2, 3, GL_UNSIGNED_INT, 0l, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

			shader.ifPresentOrElse(s->{
				s.disableVertexAttribArray("vPosition");
				s.disableVertexAttribArray("tPosition");
				s.disableVertexAttribArray("nPosition");
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
}
