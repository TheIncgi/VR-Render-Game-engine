package com.theincgi.lwjglApp.render;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Color;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL45.*;

public class Model {

	//Location location = new Location();
	
	int VAO; //the settings n stuff
	int vbo; //the data
	int ibo;
	int nVerts;
	int nTex;

	IntBuffer index;
	Range[] ranges;
	public Optional<ShaderProgram> shader = Optional.empty();

	public Model(File source) throws FileNotFoundException, IOException {
		FloatBuffer vert = null, uv = null, normal = null;
		RandomAccessFile in = new RandomAccessFile(source, "rw");
		try{
			String modelName = in.readUTF();

			int vertCount = nVerts = in.readInt();
			float[] vertexData = new float[vertCount];
			for(int i = 0; i<vertCount; i++)
				vertexData[i] = in.readFloat();

			int uvCount = nTex = in.readInt();
			float[] uvData = new float[uvCount];
			for(int i = 0; i<uvCount; i++)
				uvData[i] = in.readFloat();

			int normCount = in.readInt();
			float[] normData = new float[normCount];
			for(int i = 0; i<normCount; i++)
				normData[i] = in.readFloat();

			vert = Utils.toBuffer(vertexData);
			uv   = Utils.toBuffer(uvData);
			normal=Utils.toBuffer(normData);

//			vbo = glGenBuffers();

			int totalIndex = in.readInt();
			int nMaterialGroups = in.readInt();


			int[] indexData = new int[totalIndex];
			ranges = new Range[nMaterialGroups];
			for (int i = 0; i < nMaterialGroups; i++) {
				ranges[i] = new Range(in.readUTF(), in.readInt(), in.readInt(), in.readBoolean(), in.readBoolean(), in.readBoolean());

				for (int j = ranges[i].start; j <= ranges[i].end; j++) { // <=
					indexData[j] = in.readInt();
				}
			}

			index = Utils.toBuffer(indexData);

			VAO = glGenVertexArrays();
			glBindVertexArray(VAO);
			
			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, Float.BYTES*(vertCount + uvCount + normCount), GL_STATIC_DRAW);
			glBufferSubData(GL_ARRAY_BUFFER, 0, vert);
			glBufferSubData(GL_ARRAY_BUFFER, vertCount*Float.BYTES, uv);
			glBufferSubData(GL_ARRAY_BUFFER, (vertCount+uvCount)*Float.BYTES, normal);
			
			ibo = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, index, GL_STATIC_DRAW);
			
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); //vertex
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, (vertCount)*Float.BYTES); //uv
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, (vertCount + uvCount)*Float.BYTES); //normal
			//not mentioned in the javadoc, offset pointer is in bytes

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		}finally {
			Utils.freeBuffer(vert);
			Utils.freeBuffer(uv);
			Utils.freeBuffer(normal);
			Utils.freeBuffer(index);
			glBindVertexArray(0);
			in.close();
		}
		shader = ShaderManager.INSTANCE.get("basic");
	}

	private void delete() {
		shader.ifPresent(s->{
			s.disableVertexAttribArray("vPosition");
			s.disableVertexAttribArray("texPosition");
			s.disableVertexAttribArray("normPosition");
		});
		Utils.freeBuffer(index);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(ibo);
		glDeleteBuffers(vbo);
		glBindVertexArray(0);
		glDeleteVertexArrays(VAO);
	}

	public void drawAt(Location location) {
		
		try(MatrixStack stk = MatrixStack.modelViewStack.pushTransform(location)){
			glBindVertexArray(VAO);

			shader.ifPresentOrElse(s->{
				s.bind();
				s.tryEnableVertexAttribArray("vPosition");
				s.trySetMatrix("modelViewMatrix", stk.get());
				//TODO conditional based on range
				s.tryEnableVertexAttribArray("texPosition");
				s.tryEnableVertexAttribArray("normPosition");
				s.bind();
			}, ()->{glEnableVertexAttribArray(0);});

			
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			for(Range r : ranges) {
				glDrawRangeElements(GL_TRIANGLES, r.start, r.end, r.end-r.start+1, GL_UNSIGNED_INT, 0);
			}
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			
			shader.ifPresentOrElse(s->{
				s.disableVertexAttribArray("vPosition");
				s.disableVertexAttribArray("texPosition");
				s.disableVertexAttribArray("normPosition");
				ShaderProgram.unbind();
			},()->glDisableVertexAttribArray(0));

			glBindVertexArray(0);
		}
	}

//	public void drawAsColor(Color color, Location locaiton) {
//		try(MatrixStack stk = MatrixStack.modelViewStack.pushTransform(location)){
//			glBindVertexArray(VAO);
//			Optional<ShaderProgram> flat = ShaderManager.INSTANCE.get("flat");
//			flat.ifPresentOrElse(s->{
//				s.bind();
//				s.tryEnableVertexAttribArray("vPosition");
//				s.trySetMatrix("modelViewMatrix", stk.get());
//				s.trySetUniform("color", color.vec());
//			}, ()->{glEnableVertexAttribArray(0);});
//
//
//			glDrawRangeElements(GL_TRIANGLES, 0, ranges[ranges.length-1].end, index);
//
//			flat.ifPresentOrElse(s->{
//				s.disableVertexAttribArray("vPosition");
//			},()->glDisableVertexAttribArray(0));
//
//			glBindVertexArray(0);
//		}
//	}

	
	public void onDestroy() {
		delete();
	}

	

	private static class Range{
		String material;
		int start;
		int end;
		boolean smooth;
		boolean useUV;
		boolean useNormal;
		public Range(String material, int start, int end, boolean smooth, boolean useUV, boolean useNormal) {
			this.material = material;
			this.start = start;
			this.end = end;
			this.smooth = smooth;
			this.useUV = useUV;
			this.useNormal = useNormal;
		}



	}
}



