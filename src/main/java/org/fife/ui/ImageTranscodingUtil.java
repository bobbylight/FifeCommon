/*
 * Copyright (C) 2018 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Utility methods for converting SVGs to images.
 */
public final class ImageTranscodingUtil {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ImageTranscodingUtil() {
		// Do nothing (comment for Sonar)
	}

	/**
	 * Converts an SVG into an image.
	 *
	 * @param svgName The name of the SVG.  Used only in error messages.
	 * @param svg The SVG content.
	 * @param w The width of the desired image.
	 * @param h The height of the desired image.
	 * @return The created image.
	 * @throws IOException If an IO error occurs.
	 */
	public static BufferedImage rasterize(String svgName, InputStream svg, int w, int h)
			throws IOException {

		try (BufferedInputStream bin = new BufferedInputStream(svg)) {

			TranscoderInput input = new TranscoderInput(bin);

			BufferedImageGenerator t = new BufferedImageGenerator(w, h);
			t.transcode(input, null);
			return t.image;
		} catch (TranscoderException e) {
			throw new IOException("Couldn't convert " + svgName + " to an image", e);
		}
	}

	private static class BufferedImageGenerator extends ImageTranscoder {

		private BufferedImage image;
		private int width;
		private int height;

		BufferedImageGenerator(int width, int height) {
			this.width = width;
			this.height = height;
			setTranscodingHints(createTranscodingHints(width, height));
		}

		@Override
		public BufferedImage createImage(int w, int h) {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		private static TranscodingHints createTranscodingHints(int width, int height) {
			TranscodingHints hints = new TranscodingHints();
			hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
				SVGDOMImplementation.getDOMImplementation());
			hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
				SVGConstants.SVG_NAMESPACE_URI);
			hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
			hints.put(ImageTranscoder.KEY_WIDTH, (float)width);
			hints.put(ImageTranscoder.KEY_HEIGHT, (float)height);
			return hints;
		}

		@Override
		public void writeImage(BufferedImage image, TranscoderOutput out) {
			this.image = image;
		}
	}
}
