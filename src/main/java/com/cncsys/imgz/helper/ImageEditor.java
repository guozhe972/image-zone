package com.cncsys.imgz.helper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

@Component
public class ImageEditor {

	@Value("${image.preview.scale}")
	private int PREVIEW_SCALE;

	@Value("${image.thumbnail.scale}")
	private int THUMBNAIL_SCALE;

	@Value("${image.watermark.text}")
	private String WATERMARK_TEXT;

	public BufferedImage getPreview(BufferedImage source, int price) throws IOException {
		int sWidth, sHeight;
		if (source.getWidth() > source.getHeight()) {
			sWidth = PREVIEW_SCALE;
			sHeight = (int) ((double) sWidth * source.getHeight() / source.getWidth());
		} else {
			sHeight = PREVIEW_SCALE;
			sWidth = (int) ((double) sHeight * source.getWidth() / source.getHeight());
		}

		BufferedImage marked = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
		double xScale = (double) sWidth / source.getWidth();
		double yScale = (double) sHeight / source.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		Graphics2D g2d = marked.createGraphics();
		g2d.drawRenderedImage(source, at);

		if (price > 0) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
			g2d.setColor(Color.DARK_GRAY);
			g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
			g2d.setTransform(AffineTransform.getRotateInstance(Math.atan2(sHeight, sWidth)));
			int tWidth = (int) Math.sqrt(sWidth * sWidth + sHeight * sHeight);
			int tHeight = sWidth * sHeight / tWidth;
			Rectangle2D rect = g2d.getFontMetrics().getStringBounds(WATERMARK_TEXT, g2d);
			int rHeight = (int) rect.getHeight();
			int rWidth = (int) rect.getWidth();

			//int centerX = (tWidth - rWidth) / 2;
			//g2d.drawString(WATERMARK_TEXT, centerX, 0);

			int row = 0, x = 0, y = -tHeight;
			while (y < tHeight) {
				x = 0;
				if (row % 2 == 1)
					x += rWidth;
				while (x < tWidth) {
					g2d.drawString(WATERMARK_TEXT, x, y);
					x += rWidth * 2;
				}
				y += rHeight * 2;
				row++;
			}
		}

		g2d.dispose();
		return marked;
	}

	public BufferedImage getThumbnail(BufferedImage source) throws IOException {
		int sWidth, sHeight;
		sHeight = THUMBNAIL_SCALE;
		sWidth = (int) ((double) sHeight * source.getWidth() / source.getHeight());

		BufferedImage scaled = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
		double xScale = (double) sWidth / source.getWidth();
		double yScale = (double) sHeight / source.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		//AffineTransformOp atOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		//atOp.filter(source, scaled);
		Graphics2D g2d = scaled.createGraphics();
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return scaled;
	}

	public BufferedImage rotateImage(File img) throws IOException, ImageProcessingException, MetadataException {
		BufferedImage srcImage = ImageIO.read(img);

		Metadata metadata = ImageMetadataReader.readMetadata(img);
		if (metadata != null) {
			if (metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
				Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
				int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

				int width = srcImage.getWidth();
				int height = srcImage.getHeight();
				int temp = 0;

				AffineTransform at = new AffineTransform();
				switch (orientation) {
				case 1: // Normal
					return srcImage;
				case 2: // Flip X
					at.scale(-1.0, 1.0);
					at.translate(-width, 0);
					break;
				case 3: // PI rotation
					at.translate(width, height);
					at.rotate(Math.PI);
					break;
				case 4: // Flip Y
					at.scale(1.0, -1.0);
					at.translate(0, -height);
					break;
				case 5: // - PI/2 and Flip X
					at.rotate(-Math.PI / 2);
					at.scale(-1.0, 1.0);
					temp = width;
					width = height;
					height = temp;
					break;
				case 6: // -PI/2 and -width
					at.translate(height, 0);
					at.rotate(Math.PI / 2);
					temp = width;
					width = height;
					height = temp;
					break;
				case 7: // PI/2 and Flip
					at.scale(-1.0, 1.0);
					at.rotate(Math.PI / 2);
					at.translate(width, height);
					at.rotate(Math.PI);
					temp = width;
					width = height;
					height = temp;
					break;
				case 8: // PI / 2
					at.translate(0, width);
					at.rotate(3 * Math.PI / 2);
					temp = width;
					width = height;
					height = temp;
					break;
				}

				AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
				BufferedImage dstImage = new BufferedImage(width, height, srcImage.getType());
				op.filter(srcImage, dstImage);
				return dstImage;
			}
		}

		return srcImage;
	}
}
