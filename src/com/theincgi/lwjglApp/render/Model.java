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
	public Optional<MaterialGroup> material = Optional.empty();

	public Model(File source) throws FileNotFoundException, IOException {
		File material = new File(source.getParentFile(), source.getName().substring(0, source.getName().lastIndexOf("."))+".mtl");
		if(material.exists())
			this.material = MaterialManager.INSTANCE.get(material);

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
				ranges[i] = new Range(in.readUTF().trim(), in.readInt(), in.readInt(), in.readBoolean(), in.readBoolean(), in.readBoolean());

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
		shader = ShaderManager.INSTANCE.get("full");
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
			drawAtOrigin();
		}
	}

	public void drawAtOrigin() {
		MatrixStack stk = MatrixStack.modelViewStack;
		glBindVertexArray(VAO);

		shader.ifPresentOrElse(s->{
			s.bind();
			s.tryEnableVertexAttribArray("vPosition");
			s.trySetMatrix("modelViewMatrix", stk.get());
			//TODO conditional based on range
			s.tryEnableVertexAttribArray("texPosition");
			s.tryEnableVertexAttribArray("normPosition");
		}, ()->{glEnableVertexAttribArray(0);});


		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		for(Range r : ranges) {
			if(shader.isPresent() && material.isPresent()) {
				ShaderProgram s = shader.get();
				MaterialGroup mg = material.get();
				Material m = mg.materials.get(r.material);
				if(m!=null) {
					s.trySetUniform(       "material_kd",      m.kd.vec());
					s.trySetUniform(       "material_illum",   m.illum);
					s.trySetUniform(       "material_ka", 		m.ka.isPresent()? m.ka.get().vec() : Color.WHITE.vec());
					s.trySetUniform(       "material_ks", 		m.ks.isPresent()? m.ks.get().vec() : Color.WHITE.vec());
					s.trySetUniform(       "material_ke", 		m.ke.isPresent()? m.ke.get().vec() : Color.BLACK.vec());
					s.trySetUniform(       "material_tf", 		m.tf.isPresent()? m.tf.get().vec() : Color.WHITE.vec());
					s.trySetUniform(       "material_d", 		m.d .isPresent()? m.d.get()        : 1);
					s.trySetUniform(       "material_halo", 	(m.halo.isPresent() && m.halo.get())?  1 : 0);
					s.trySetUniform(       "material_ns",      m.ns.isPresent()? m.ns.get() 	   : 1);
					s.trySetUniform(       "material_ni",      m.ni.isPresent()? m.ni.get()       : 1);
				    s.trySetUniformTexture("map_ka",           m.map_ka.isPresent()? m.map_ka.get().getHandle() : 0,         0);
				    s.trySetUniformTexture("map_kd",           m.map_kd.isPresent()? m.map_kd.get().getHandle() : 0,         1);
				    s.trySetUniformTexture("map_ks",           m.map_ks.isPresent()? m.map_ks.get().getHandle() : 0,         2);
				    s.trySetUniformTexture("map_ns",           m.map_ns.isPresent()? m.map_ns.get().getHandle() : 0,         3);
				    s.trySetUniformTexture("map_d",            m.map_d .isPresent()? m.map_d .get().getHandle() : 0,         4);
					s.trySetUniformTexture("map_disp",         m.map_disp.isPresent()? m.map_disp.get().getHandle() : 0,     5);
					s.trySetUniformTexture("map_bump",         m.map_bump.isPresent()? m.map_bump.get().getHandle() : 0,     6);
					s.trySetUniform("used_textures_flag", 
							(m.map_kd.isPresent()?  1 : 0)   |
							(m.map_ka.isPresent()?  2 : 0)   |
							(m.map_ks.isPresent()?  4 : 0)   |
							(m.map_ns.isPresent()?  8 : 0)   |
							(m.map_d .isPresent()? 16 : 0)   |
							(m.map_disp.isPresent()? 32 : 0) |
							(m.map_bump.isPresent()? 64 : 0));
					
				}

			}
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



