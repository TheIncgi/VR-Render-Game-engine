package com.theincgi.lwjglApp.render.text;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import javax.imageio.ImageIO;
import static java.lang.Math.max;
import static java.lang.Math.floor;
import static java.lang.Math.ceil;
import com.theincgi.lwjglApp.misc.AbsManager;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.misc.Settings;

public class FontTextures extends AbsManager<Pair<String, Integer>, FontTexture>{
	private FontTextures() {}
	
	public static FontTextures INSTANCE = new FontTextures();
	
	private static HashMap<String, ArrayList<Integer>> fontList = new HashMap<>();
	public static final File fontsFolder = new File(Settings.getRunFolder(), "fonts");

	public static void generate(String filePrefix, Font font, int utfStart, int utfStop) {
		generate(Optional.ofNullable(filePrefix), font, utfStart, utfStop);
	}
	public static void generate(Optional<String> filePrefix, Font font, int utfStart, int utfStop) {
		fontsFolder.mkdirs();

		Canvas tmp = new Canvas();
		FontMetrics fm = tmp.getFontMetrics(font);
		FontRenderContext frc = fm.getFontRenderContext();
		int maxW = 0; int h = fm.getMaxAscent() + fm.getMaxDescent();
		int decentUsed = fm.getMaxDescent();
		for(int codePoint=utfStart; codePoint <=utfStop; codePoint++) { //advance may not be known
			maxW = max(maxW, fm.charWidth(codePoint));
		}

		int charCount = utfStop - utfStart + 1;
		int imgSize = chooseImageSize(maxW, h, charCount, 16);

		ArrayList<Integer> charWidths = new ArrayList<>();
		BufferedImage imagePlain = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
		BufferedImage imageBold = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gPlain = imagePlain.createGraphics();
		Graphics2D gBold  = imageBold.createGraphics();
		gPlain.setPaint(java.awt.Color.white);
		gBold.setPaint(java.awt.Color.white);
		//g.drawLine(0, 0, imgSize, imgSize);
		int perRow = (int) floor(imgSize/(float)maxW);
		int rowCount = (int)ceil(charCount/(float)perRow);
		int codePoint = utfStart;
		gPlain.setFont(font);
		gBold.setFont(font.deriveFont(Font.BOLD));
		outer:
			for(int row = 0; row<rowCount; row++) {
				for(int col = 0; col<perRow; col++) {
					if(codePoint > utfStop) break outer;
					int px = col * maxW;
					int py = row * h + fm.getMaxAscent(); //ignoring leading
					int charWidth = fm.charWidth(codePoint);
					charWidths.add(charWidth);
					gPlain.drawString(Character.toString(codePoint), px, py);
					gBold .drawString(Character.toString(codePoint++), px, py);
				}
			}

		String fileNamePlain = String.format("%s%s_%d.",      filePrefix.orElseGet(()->{return "";}), font.getFontName(), font.getSize());
		String fileNameBold  = String.format("%s%s_Bold_%d.", filePrefix.orElseGet(()->{return "";}), font.getFontName(), font.getSize());
		File fontImgFilePlain  = new File(fontsFolder, fileNamePlain+"png");
		File fontImgFileBold  = new File(fontsFolder, fileNameBold+"png");
		File fontSizeFile = new File(fontsFolder, fileNamePlain+"chw");
		
		try (RandomAccessFile raf = new RandomAccessFile(fontSizeFile, "rw")){
			ImageIO.write(imagePlain, "png", fontImgFilePlain);
			ImageIO.write(imageBold,  "png", fontImgFileBold);
			raf.writeInt(maxW);
			raf.writeInt(h);
			raf.writeInt(perRow);
			raf.writeInt(fm.getMaxAscent());
			raf.writeInt(fm.getMaxDescent());
			raf.writeInt(fm.getLeading());
			raf.writeInt(utfStart);
			raf.writeInt(utfStop);
			for (int i : charWidths) {
				raf.writeInt(i);
			}
		} catch (IOException e) {
			Logger.preferedLogger.e("FontTextures#generate", e);
		}
	}
	private static int chooseImageSize(int cw, int ch, int charCount, int guess) {
		if(guess >= cw) {
			int perRow = (int) floor(guess/(float)cw);
			int rowCount = (int)ceil(charCount/(float)perRow);
			if(rowCount * ch <= guess) return guess;
		}
		return chooseImageSize(cw, ch, charCount, guess*2);
	}

	public static void updateFontList() {
		fontList.clear();
		for(File f : fontsFolder.listFiles()) {
			String name = f.getName();
			if(name.endsWith(".chw")) {
				name = name.substring(0, name.length()-4);
				int last = name.lastIndexOf("_");
				String fontName = name.substring(0, last);
				try {
					int fontSize = Integer.parseInt(name.substring(last+1));
					fontList.computeIfAbsent(fontName.toLowerCase(),k->new ArrayList<>()).add(fontSize);
				}catch (NumberFormatException nfe) {/*Thats ok*/}
			}
		}
		StringBuilder avail = new StringBuilder("< ");
		for (Entry<String, ArrayList<Integer>> e : fontList.entrySet()) {
			avail.append("[").append(e.getKey()).append("|");
			Collections.sort(e.getValue());
			for (int i = 0 ; i<e.getValue().size(); i++) {
				avail.append(e.getValue().get(i));
				if(i<e.getValue().size()-1)
					avail.append(",");
			}
			avail.append("] ");
		}
		avail.append(">");
		Logger.preferedLogger.i("Fonts available: ", avail.toString());
	}

	@Override
	protected FontTexture load(Pair<String, Integer> key) {
		boolean dirty = false;
		try {
			ArrayList<Integer> options = fontList.get(key.x.toLowerCase());
			if(options==null) return null;
			for(int i=options.size()-1; i>=0; i--) {
				if(options.get(i) > key.y && i>0)continue; //choose largest under
				try {
					return new FontTexture(key.x, options.get(i));
				}catch (FileNotFoundException e) {
					Logger.preferedLogger.e("FontTextures#load", "Couldn't load a font '"+key.x+" "+options.get(i)+"', file may have been deleted after startup", e);
					dirty = true;
				}catch (Exception e) {
					Logger.preferedLogger.e("FontTextures#load", "Couldn't load "+key.x+" "+options.get(i)+" due to an IOexception", e);
					dirty = true;
				}
			}
		}finally {
			if(dirty)
				updateFontList();
		}
		return null;
	}
	@Override
	protected void onUnload(FontTexture t) {
		//only ImgTexture needs cleaning but it is managed by TextureManager
	}



}
