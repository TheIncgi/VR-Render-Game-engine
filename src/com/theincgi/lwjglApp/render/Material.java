package com.theincgi.lwjglApp.render;

import java.util.Optional;

import com.theincgi.lwjglApp.ui.Color;

public class Material implements Cloneable{
	public final String materialName;
	/**Diffuse reflectivity - manditory*/
	public Color kd = Color.WHITE; //Kd r g b
	
	/**Ambient Reflectivity*/
	public Optional<Color> ka = Optional.empty(); //Ka r g b

	/**Specular reflectivity*/
	public Optional<Color> ks = Optional.empty();
	
	/**Emission color*/
	public Optional<Color> ke = Optional.empty();
	
	/**Transmission filter<br><br>Any light passing through the object is filtered by the transmission 
filter, which only allows the specifiec colors to pass through.  For 
example, Tf 0 1 0 allows all the green to pass through and filters out 
all the red and blue.*/
	public Optional<Color> tf = Optional.empty();
	
	/**Illumination model - manditory
	 * <table>
	 * <tr> <th>Mode</th><th>Desc</th> </tr>
	 * <tr><td>0</td><td>Color on and Ambient off</td></tr>
	 * <tr><td>1</td><td>Color on and Ambient on</td></tr>
	 * <tr><td>2</td><td>Highlight on</td></tr>
	 * <tr><td>3</td><td>Reflection on and Ray trace on</td></tr>
	 * <tr><td>4</td><td>Transparency: Glass on<br>
 						 Reflection: Ray trace on</td></tr>
	 * <tr><td>5</td><td>Reflection: Fresnel on and Ray trace on</td></tr>
	 * <tr><td>6</td><td>Transparency: Refraction on<br>
 						 Reflection: Fresnel off and Ray trace on</td></tr>
	 * <tr><td>7</td><td>Transparency: Refraction on<br>
 				 	  	 Reflection: Fresnel on and Ray trace on</td></tr>
	 * <tr><td>8</td><td>Reflection on and Ray trace off</td></tr>
	 * <tr><td>9</td><td>Transparency: Glass on<br>
 						 Reflection: Ray trace off</td></tr>
	 * <tr><td>10</td><td>Casts shadows onto invisible surfaces</td></tr>
	 * </table>
	 */
	public int illum = 1;
	
	/**<b>Dissolve factor</b> - Similar to Alpha/Opacity<br>
	 * <br>

	 Unlike a real transparent material, the dissolve does not depend upon 
	material thickness nor does it have any spectral character.<br>Dissolve 
	works on all illumination models.*/
	public Optional<Float> d = Optional.empty();
	/**Halo is related to {@link Material#d}<br>
	 * <blockquote>Specifies that a dissolve is dependent on the surface orientation 
relative to the viewer.  For example, a sphere with the following 
dissolve, d -halo 0.0, will be fully dissolved at its center and will 
appear gradually more opaque toward its edge.
 
 "factor" is the minimum amount of dissolve applied to the material.  
The amount of dissolve will vary between 1.0 (fully opaque) and the 
specified "factor".  <br>The formula is:
 <br>
 <code>dissolve = 1.0 - (N*v)(1.0-factor)</code></blockquote>
 (<a href="http://paulbourke.net/dataformats/mtl/">sited from here</a>)*/
	public Optional<Boolean> halo = Optional.empty();
	
	/**<b>Specular exponent</b> - is the value for the specular exponent.  A high exponent 
results in a tight, concentrated highlight.  Ns values normally range 
from 0 to 1000.*/
	public Optional<Float> ns = Optional.empty();
	
	/**Specifies the sharpness of the reflections from the local reflection 
map.  If a material does not have a local reflection map defined in its 
material definition, sharpness will apply to the global reflection map 
defined in PreView. A high 
value results in a clear reflection of objects in the reflection map.<br><br>Can be a number from 0 to 1000. <br>The default is 60<br><br><b>Tip:</b>	Sharpness values greater than 100 map introduce aliasing effects 
in flat surfaces that are viewed at a sharp angle*/
	public Optional<Float> sharpness = Optional.empty();
	
	/**Specifies the optical density for the surface.  This is also known as 
<b>index of refraction.</b>
 
 "optical_density" is the value for the optical density.  The values can 
range from 0.001 to 10.  A value of 1.0 means that light does not bend 
as it passes through an object.  Increasing the optical_density 
increases the amount of bending.  Glass has an index of refraction of 
about 1.5.  Values of less than 1.0 produce bizarre results and are not 
recommended.*/
	public Optional<Float> ni = Optional.empty();
	
	/**Ambient map - color*/
	public Optional<ImgTexture> map_ka = Optional.empty();
	/**Diffuse map - color*/
	public Optional<ImgTexture> map_kd = Optional.empty();
	/**Specular map - color*/
	public Optional<ImgTexture> map_ks = Optional.empty();
	/**Specular Exponent - scalar*/
	public Optional<ImgTexture> map_ns = Optional.empty();
	/**Disolve map*/
	public Optional<ImgTexture> map_d = Optional.empty();
	/**Displacement map*/
	public Optional<ImgTexture> map_disp = Optional.empty();
	/**Normal map*/
	public Optional<ImgTexture> map_bump = Optional.empty();

	public Material(String name){
		this.materialName = name;
	}
	
	@Override
	protected Material clone() {
		Material x = new Material(this.materialName);
		x.d 		= this.d;
		x.halo 		= this.halo;
		x.illum 	= this.illum;
		x.ka 		= this.ka;
		x.ke 		= this.ke;
		x.ks 		= this.ks;
		x.map_bump 	= this.map_bump;
		x.map_d 	= this.map_d;
		x.map_disp 	= this.map_disp;
		x.map_ka 	= this.map_ka;
		x.map_kd 	= this.map_kd;
		x.map_ks 	= this.map_ks;
		x.map_ns 	= this.map_ns;
		x.ni 		= this.ni;
		x.ns 		= this.ns;
		x.sharpness = this.sharpness;
		x.tf 		= this.tf;
		return x;
	}
}
