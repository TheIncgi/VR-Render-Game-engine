package com.theincgi.lwjglApp.render.shaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL45.*;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.render.ImgTexture;
import com.theincgi.lwjglApp.ui.Color;

public class ShaderProgram {
	static Logger log = Logger.preferedLogger;
	private static ShaderProgram activeShader = null;
	
	private ConcurrentHashMap<String, Integer> uniformLookup = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Integer> attribLookup = new ConcurrentHashMap<>();
	
	private int programHandle;
	private String label;
	
	File vertexSrc, fragmentSrc;
	long vertexLastModified, fragmentLastModified;
	
	//TODO cache handle names
	private boolean autoRefresh = false;
	
	public ShaderProgram(File vertex, File fragment) {
		this.vertexSrc = vertex;
		this.fragmentSrc = fragment;
		label = fragment.getName();
		init();
	}
	private void init() {
		int vShader = loadShader(GL_VERTEX_SHADER, vertexSrc);
		int fShader = loadShader(GL_FRAGMENT_SHADER, fragmentSrc);
		programHandle = glCreateProgram();

		vertexLastModified   = vertexSrc.lastModified();
		fragmentLastModified = fragmentSrc.lastModified();
		glAttachShader(programHandle, vShader);
		glAttachShader(programHandle, fShader);

		
		glLinkProgram(programHandle);
		
		try(MemoryStack stack = MemoryStack.stackPush()){
			IntBuffer link = stack.mallocInt(1);
			glGetProgramiv(programHandle, GL_LINK_STATUS, link);
			if(link.get(0) == GL_FALSE) {
				log.w("ShaderProgram#new", "Unable to create shader with source files: "+vertexSrc.getName()+" and "+fragmentSrc.getName()
				+" Reason: "+glGetProgramInfoLog(programHandle));
				glDeleteProgram(programHandle);
				programHandle = 0;
			}else {
				log.i("ShaderProgram#new", "Created shader with files: "+vertexSrc.getName()+" and "+fragmentSrc.getName());
			}
		}
		glDeleteShader(vShader);
		glDeleteShader(fShader);
	}
	
	/**Do from open GL thread only*/
	public void reload() {
		delete();
		uniformLookup.clear();
		attribLookup.clear();
		init();
	}
	public ShaderProgram autoRefresh(boolean doAutoRefresh) {
		autoRefresh = doAutoRefresh;
		return this;
	}
	
	
	private int _getAttribLocation(String name) {
		return glGetAttribLocation(programHandle, name);
	}
	public int getAttribLocation(String name) {
		return attribLookup.computeIfAbsent(name, this::_getAttribLocation);
	}
	
	private int _getUniformLocation(String name) {
			return glGetUniformLocation(programHandle, name);
	}
	public int getUniformLocation(String name) {
		return uniformLookup.computeIfAbsent(name, this::_getUniformLocation);
	}
	
	/**Used with VAO*/
	public boolean tryEnableVertexAttribArray(String key) {
		int posH = getAttribLocation(key);
		if(posH==-1) {
			//log.w("ShaderProgram#tryEnableVertexAttribArray", "could not enable attribVertArray for '"+key+"'"); 
		return false;}
		glEnableVertexAttribArray(posH);
		return true;
	}
	/**Used with VAO*/
	public boolean disableVertexAttribArray(String key) {
		int posH = getAttribLocation(key);
		if(posH==-1) {
			//log.w("ShaderProgram#tryDisableVertexAttribArray", "could not disable attribVertArray for '"+key+"'"); 
			return false;}
		glDisableVertexAttribArray(posH);
		return true;
	}
	public boolean trySetVertexAttribPointer(String key, int size, int type, int stride, long pointer) {
		int posH = getAttribLocation(key);
		if(posH==-1) {
			//log.w("ShaderProgram#tryEnableVertexAttribArray", "could not enable attribVertArray for '"+key+"'"); 
		return false;}
		glVertexAttribPointer(posH, size, type, false, stride, pointer);
		return true;
	}
	
	@Deprecated
	public boolean trySetVertexAttribArray(String key, FloatBuffer vCoords) {
		int posH = getAttribLocation(key);
		if(posH==-1) {
			//log.w("ShaderProgram#trySetVerexAttribArray", "could not set atrib. verted array for '"+key+"'");
			return false;}
		glEnableVertexAttribArray(posH);
		glVertexAttribPointer(posH, 3, GL_FLOAT, false, 3*Float.BYTES, vCoords);
		return true;
	}
	public boolean trySetUniform( String name, int i ){
        int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "integer"); return false;}
        glUniform1i( handle, i );
        return true;
    }
    public boolean trySetUniform( String name, float f) {
        int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "float"); return false;}
        glUniform1f( handle, f );
        return true;
    }
    public boolean trySetUniform( String name, float[] f){
        int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "vec"+f.length); return false;}
        switch (f.length){
            case 0:
                throw new RuntimeException("Empty float array!");
            case 1:
                glUniform1fv(handle, f);
                break;
            case 2:
                glUniform2fv(handle, f);
                break;
            case 3:
                glUniform3fv(handle, f);
                break;
            case 4:
                glUniform4fv(handle, f);
                break;
            default:
                throw new RuntimeException("Not a valid vector size ("+f.length+")");

        }
        return true;
    }
    public boolean trySetUniform( String name, Vector3f vec) {
    	return trySetUniform(name, new float[] {vec.x, vec.y, vec.z});
    }
    public boolean trySetUniform( String name, Vector2f vec) {
    	return trySetUniform(name, new float[] {vec.x, vec.y});
    }
    public boolean trySetUniform( String name, Vector4f vec) {
    	return trySetUniform(name, new float[] {vec.x, vec.y, vec.z, vec.w});
    }
    public boolean trySetUniform( String name, Color color ) {
    	return trySetUniform(name, color.vec());
    }
    public boolean trySetMatrix(String name, float[] m){
        int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "Matrix"); return false;}
        switch (m.length){
            case 4:
                glUniformMatrix2fv(handle, false, m);
                break;
            case 9:
                glUniformMatrix3fv(handle, false, m);
                break;
            case 16:
                glUniformMatrix4fv(handle, false, m);
                break;
            default:
                throw new RuntimeException("Not a valid matrix size");
        }
        return true;
    }
    public boolean trySetMatrix(String name, Matrix4f mat){
        int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "Matrix"); return false;}
        try (MemoryStack stack = MemoryStack.stackPush()){
        	FloatBuffer buf = stack.mallocFloat(16);
        	mat.store(buf);
        	buf.flip(); //TODO CHECKME, flip needed, right?
        	glUniformMatrix4fv(handle, false, buf);
        }
        return true;
    }
    
    /**
     * @param name Name of the sampler2D in GLSL shader
     * @param textureID id of the texture generated
     * @param textureTarget number 0 thru 31. Seems to be the max textures used is 32
     * */
    public boolean trySetUniformTexture( String name, int textureID, int textureTarget ){
    	if(textureTarget < 0 || 31 < textureTarget) throw new IndexOutOfBoundsException(textureTarget+" is outside the range 0 to 31");
    	int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "texture id"); return false;}
        glUniform1i(handle, textureTarget);
        glActiveTexture(GL_TEXTURE0 + textureTarget);
        glBindTexture(GL_TEXTURE_2D, textureID);
        return true;
    }
    /**
     * @param name Name of the sampler2D in GLSL shader
     * @param textureID id of the texture generated
     * @param textureTarget number 0 thru 31. Seems to be the max textures used is 32
     * */
    public boolean trySetUniformTexture( String name, ImgTexture textureID, int textureTarget ){
    	return trySetUniformTexture(name, textureID.getHandle(), textureTarget);
    }
    /**
     *  * @param textureTarget number 0 thru 31. Seems to be the max textures used is 32
     */
    public boolean disableTexture(String name, int textureTarget) {
    	if(textureTarget < 0 || 31 < textureTarget) throw new IndexOutOfBoundsException(textureTarget+" is outside the range 0 to 31");
    	int handle = getUniformLocation( name );
        if(handle == -1){warnMissingKey(name, "texture id"); return false;}
        glUniform1i(handle, 0);
        glActiveTexture(GL_TEXTURE0 + textureTarget);
        glBindTexture(GL_TEXTURE_2D, 0);
        return true;
    }

    private void warnMissingKey(String key, String type){
    	//if(!log.outputsToConsole())
        //log.sd("ShaderProgram", String.format("warnMissingKey: \"%s\" for type '%s' in shader [%s]",key, type,label));
    }

    /**
     * called on finalization if not by application<br>
     * don't rely on finalize please
     * */
    public void delete(){
    	if(programHandle!=0)
        glDeleteProgram(programHandle);
        programHandle = 0;
    }

    /**
     * Called when no object referes to this material anymore
     * */
    @Override
    protected void finalize() throws Throwable {
        if(programHandle!=0 && glIsProgram(programHandle)) {
            delete();
            log.w("ShaderProgram#finalize", "Program "+label+" was not deleted before all references were lost.");
        }
        super.finalize();
    }

	public int getProgram() {
		return programHandle;
	}
	
	public String getName() {
		return label;
	}
	
	
	long nextCheck = System.currentTimeMillis() +1000;
	public boolean isModified() {
		long time = System.currentTimeMillis();
		if(time > nextCheck) {
			nextCheck = time +1000;
			return vertexLastModified!=vertexSrc.lastModified() || fragmentLastModified!=fragmentSrc.lastModified();
		}
		return false;
	}
	
	public void bind() {
		if(autoRefresh && isModified()) {
			vertexLastModified   = vertexSrc.lastModified();
			fragmentLastModified = fragmentSrc.lastModified();
			log.i("ShaderProgram#bind", "Auto-reloading shader due to file changes");
			reload();
			glUseProgram(programHandle);
			activeShader = this;
			return;
		}
		if(activeShader!=this)
			glUseProgram(programHandle);
		activeShader = this;
	}
	public static void unbind() {
		glUseProgram(0);
		activeShader = null;
	}
	
	public static int loadShader( int type, File srcFile ) {
		try(FileInputStream fis = new FileInputStream(srcFile)){
			return loadShader(type, fis, srcFile.getName());
		} catch (FileNotFoundException e) {
			log.e("ShaderProgram#loadShader", srcFile.getName(),e);
		} catch (IOException e) {
			log.e("ShaderProgram#loadShader", srcFile.getName(), e);
		}
		return 0;
	}
	public static int loadShader( int type, InputStream in, String incaseOfErrorTheFileNameIs) {
		String src;
		try {
			src = new String(in.readAllBytes());
			return loadShader( type, src, incaseOfErrorTheFileNameIs );
		} catch (IOException e) {
			log.e("ShaderProgram#loadShader", "Unable to read all bytes", e);
			return 0;
		}
	}
	private static int loadShader(int type, String src, String incaseOfErrorTheFileNameIs) {
		int shader = glCreateShader(type);
		//log.checkGL();
		glShaderSource(shader, src);
		//log.checkGL();
		glCompileShader(shader);
		//log.checkGL();
		try(MemoryStack stack = MemoryStack.stackPush()){
			IntBuffer status = stack.mallocInt(1);
			glGetShaderiv(shader, GL_COMPILE_STATUS, status);
			if(status.get(0) == GL_FALSE) {
				log.d("ShaderProgram#loadShader", "src: "+src);
				log.w("ShaderProgram#loadShader", "Error compiling shader "+incaseOfErrorTheFileNameIs+" ("+status.get(0)+"): "+
				  glGetShaderInfoLog(shader));
				glDeleteShader(shader);
				return 0;
			}
		}
		return shader;
	}
	@Override
	public String toString() {
		return label;
	}
}
