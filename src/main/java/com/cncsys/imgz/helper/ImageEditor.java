package com.cncsys.imgz.helper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageEditor {

	@Value("${image.preview.scale}")
	private int PREVIEW_SCALE;

	@Value("${image.thumbnail.scale}")
	private int THUMBNAIL_SCALE;

	@Value("${image.watermark.text}")
	private String WATERMARK_TEXT;

	public BufferedImage getPreview(BufferedImage source) throws IOException {
		int sWidth, sHeight;
		if (source.getWidth() > source.getHeight()) {
			sWidth = PREVIEW_SCALE;
			sHeight = (int) ((double) sWidth * source.getHeight() / source.getWidth());
		} else {
			sHeight = PREVIEW_SCALE;
			sWidth = (int) ((double) sHeight * source.getWidth() / source.getHeight());
		}

		BufferedImage marked = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = marked.createGraphics();
		double xScale = (double) sWidth / source.getWidth();
		double yScale = (double) sHeight / source.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(source, at);

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
		g2d.setColor(Color.DARK_GRAY);
		g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
		g2d.setTransform(AffineTransform.getRotateInstance(Math.atan2(sHeight, sWidth)));
		// 対角線上の距離 / 2
		int distance = (int) Math.sqrt(sWidth * sWidth + sHeight * sHeight) / 2;

		//Rectangle2D rect = g2d.getFontMetrics().getStringBounds(WATERMARK_TEXT, g2d);
		//int tHeight = (int) rect.getHeight();
		//int tWidth = (int) rect.getWidth();
		//int centerX = (distance - tWidth) / 2;
		//g2d.drawString(WATERMARK_TEXT, centerX, 0);

		String text1 = WATERMARK_TEXT + "          " + WATERMARK_TEXT + "          " + WATERMARK_TEXT + "          "
				+ WATERMARK_TEXT + "          ";
		String text2 = "           " + WATERMARK_TEXT + "          " + WATERMARK_TEXT + "          " + WATERMARK_TEXT
				+ "          " + WATERMARK_TEXT;

		int x = 0;
		int y = -distance;
		while (y < distance) {
			if (x % 2 == 0) {
				g2d.drawString(text1, 0, y);
			} else {
				g2d.drawString(text2, 0, y);
			}
			x++;
			y += 60;
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
}
