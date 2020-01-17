package com.theincgi.lwjglApp.render;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Optional;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Color;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL45.*;

public class Model {

	//Location location = new Location();

	int VAO[]; //the settings n stuff
	int vbo[]; //the data
	int ibo[];
	int elementSizes[];
	String[] materials;
	String modelName;
	//int nVerts;
	//int nTex;

	//IntBuffer index;
	//Range[] ranges;
	public Optional<ShaderProgram> shader = Optional.empty();
	//public Optional<MaterialGroup> material = Optional.empty();
	private Optional<MaterialGroup> material;
	private boolean materialCloned = false;

	//load material if exists
	//for each material
	//  get vvvuunnn format interleaved data
	//  get index values
	//  create VAO, VBO, IBO
	public Model(File source) throws FileNotFoundException, IOException{
		File material = new File(source.getParentFile(), source.getName().substring(0, source.getName().lastIndexOf("."))+".mtl");
		if(material.exists())
			this.material = MaterialManager.INSTANCE.get(material);


		FloatBuffer data = null;
		IntBuffer   index = null;
		RandomAccessFile in = new RandomAccessFile(source, "rw");
		try{
			modelName = in.readUTF().trim();
			int materialGroups = in.readInt();
			VAO = new int[materialGroups];
			vbo = new int[materialGroups];
			ibo = new int[materialGroups];
			elementSizes = new int[materialGroups];
			materials = new String[materialGroups];
		//	System.out.println("Model: "+modelName);
		//	System.out.println("\tMGroups: "+materialGroups);
			for(int mg = 0; mg<materialGroups; mg++) {
				materials[mg] = in.readUTF().trim();
				int uniqueIndexCount = in.readInt();
				System.out.println("\tUInd: "+uniqueIndexCount);
				float[] rawData = new float[uniqueIndexCount * 8]; //3 vert, 2 uv, 3 norm per element
				for (int i = 0; i < rawData.length; i++) 
					rawData[i] = in.readFloat();

				int indexSize = in.readInt();
		//		System.out.println("\tElements: "+indexSize);
				elementSizes[mg] = indexSize;
				int[] rawIndex = new int[indexSize];
				for(int i = 0; i<indexSize; i++)
					rawIndex[i] = in.readInt();

				//System.out.println("\tData: "+Arrays.toString(rawData));
				//System.out.println("\tIndex: "+Arrays.toString(rawIndex));
				data  = Utils.toBuffer(rawData);
				index = Utils.toBuffer(rawIndex);

				makeVao(mg, rawData, rawIndex);


			}
//			System.out.println("");
//			System.out.println("\tVAO: "+Arrays.toString(VAO));
//			System.out.println("\tIBO: "+Arrays.toString(ibo));
//			System.out.println("\tVBO: "+Arrays.toString(vbo));
//			System.out.println("\tMaterials: "+Arrays.toString(materials));
		}finally {
			Utils.freeBuffer(data);
			Utils.freeBuffer(index);
			glBindVertexArray(0);
			in.close();
		}
		shader = ShaderManager.INSTANCE.get("full");
	}
	//	public Model(File source) throws FileNotFoundException, IOException {
	//		File material = new File(source.getParentFile(), source.getName().substring(0, source.getName().lastIndexOf("."))+".mtl");
	//		if(material.exists())
	//			this.material = MaterialManager.INSTANCE.get(material);

	//			index = Utils.toBuffer(indexData);
	//
	//			VAO = glGenVertexArrays();
	//			glBindVertexArray(VAO);
	//
	//			vbo = glGenBuffers();
	//			glBindBuffer(GL_ARRAY_BUFFER, vbo);
	//			glBufferData(GL_ARRAY_BUFFER, Float.BYTES*(vertCount + uvCount + normCount), GL_STATIC_DRAW);
	//			glBufferSubData(GL_ARRAY_BUFFER, 0, vert);
	//			glBufferSubData(GL_ARRAY_BUFFER, vertCount*Float.BYTES, uv);
	//			glBufferSubData(GL_ARRAY_BUFFER, (vertCount+uvCount)*Float.BYTES, normal);
	//
	//			ibo = glGenBuffers();
	//			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
	//			glBufferData(GL_ELEMENT_ARRAY_BUFFER, index, GL_STATIC_DRAW);
	//
	//			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); //vertex
	//			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, (vertCount)*Float.BYTES); //uv
	//			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, (vertCount + uvCount)*Float.BYTES); //normal
	//			//not mentioned in the javadoc, offset pointer is in bytes
	//
	//			glBindBuffer(GL_ARRAY_BUFFER, 0);
	//			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	//			glBindVertexArray(0);
	//		}finally {
	//			Utils.freeBuffer(vert);
	//			Utils.freeBuffer(uv);
	//			Utils.freeBuffer(normal);
	//			Utils.freeBuffer(index);
	//			glBindVertexArray(0);
	//			in.close();
	//		}
	//		shader = ShaderManager.INSTANCE.get("full");
	//	}

	private void makeVao(int mg, float[] rawData, int[] rawIndex) {
		FloatBuffer data = null;
		IntBuffer index = null;
		int stride = 8 * Float.BYTES;
		try {
			data  = Utils.toBuffer(rawData );
			index = Utils.toBuffer(rawIndex);

			VAO[mg] = glGenVertexArrays();
			glBindVertexArray(VAO[mg]);

			vbo[mg] = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo[mg]);
			glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

			ibo[mg] = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[mg]);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, index, GL_STATIC_DRAW);

			glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 5 * Float.BYTES);

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			glBindVertexArray(0);

		}finally {
			Utils.freeBuffer(data);
			Utils.freeBuffer(index);
		}
	}

	private void delete() {
		shader.ifPresent(s->{
			s.disableVertexAttribArray("vPosition");
			s.disableVertexAttribArray("texPosition");
			s.disableVertexAttribArray("normPosition");
		});
		///Utils.freeBuffer(index);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(ibo);
		glDeleteBuffers(vbo);
		glBindVertexArray(0);
		glDeleteVertexArrays(VAO);
	}

	public void drawAt(Location location) {
		try(MatrixStack stk = MatrixStack.modelViewStack.push(location)){
			drawAtOrigin();
		}
	}

	public void drawAtOrigin() {
		drawAtOrigin(this.material);
	}
	public void drawAtOrigin(Optional<MaterialGroup> material) {
		MatrixStack stk = MatrixStack.modelViewStack;
		shader.ifPresentOrElse(s->{
			s.bind();
			s.trySetMatrix("modelViewMatrix", stk.get());
		},()->glEnableVertexAttribArray(0));

		for(int mg = 0; mg < VAO.length; mg++) {
			glBindVertexArray(VAO[mg]);
			final int MG = mg;
			shader.ifPresentOrElse(s->{
				s.bind();
				s.tryEnableVertexAttribArray("vPosition");
				s.trySetMatrix("modelViewMatrix", stk.get());
				s.trySetMatrix("viewMatrix", MatrixStack.view.get());
				s.tryEnableVertexAttribArray("texPosition");
				s.tryEnableVertexAttribArray("normPosition");
				material.ifPresent(matGroup->{
					Material m = matGroup.materials.get(materials[MG]);
					if(m!=null) {
						s.trySetUniform(       "material_kd",      m.kd.vec() );
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

						
					}else {
						Logger.preferedLogger.w("Model#render", "Missing material '"+materials[MG]+"'");
					}
					
				});
			},()->glEnableVertexAttribArray(0));

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[mg]);
			
			glDrawElements(GL_TRIANGLES, elementSizes[mg], GL_UNSIGNED_INT, 0l);
			glBindVertexArray(0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

			shader.ifPresentOrElse(s->{
				s.disableVertexAttribArray("vPosition");
				s.disableVertexAttribArray("tPosition");
				s.disableVertexAttribArray("nPosition");
				//ShaderProgram.unbind(); 
			},()->glDisableVertexAttribArray(0));
			glBindVertexArray(0);
		}


	}
	
	public Optional<MaterialGroup> getMaterial() {
		return material;
	}
	public void makeMaterialUnique() {
		material.ifPresent(mat->{
			if(materialCloned) return; //already unique
			materialCloned  = true;
			this.material = Optional.of(mat.clone());
		});
	}
	public void setMaterial(MaterialGroup materialGroup) {
		this.material = Optional.ofNullable(materialGroup);
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


	/**Called by ObjManager only*/
	@Deprecated
	public void onDestroy() {
		delete();
	}

	public String getName() {
		return modelName;
	}
}



