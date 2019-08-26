package com.wefeel.code.luminance;

import com.codename1.ui.EncodedImage;
import com.codename1.ui.Image;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;

public class ImageLuminanceSource extends LuminanceSource {
	private final EncodedImage img;
	private final LuminanceSource internal;

	public ImageLuminanceSource(Image img) {
		super(img.getWidth(), img.getHeight());
		internal = new RGBLuminanceSource(img.getWidth(), img.getHeight(), img.getRGB());
		this.img = img instanceof EncodedImage ? (EncodedImage) img : EncodedImage.createFromImage(img, true);
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		return internal.getRow(y, row);
	}

	@Override
	public byte[] getMatrix() {
		return internal.getMatrix();
	}

	@Override
	public boolean isCropSupported() {
		return true;
	}

	@Override
	public ImageLuminanceSource crop(int left, int top, int width, int height) {
		return new ImageLuminanceSource(
				EncodedImage.createFromImage(img.subImage(left, top, width, height, true), true));
	}

	@Override
	public boolean isRotateSupported() {
		return true;
	}

	@Override
	public ImageLuminanceSource rotateCounterClockwise() {
		return new ImageLuminanceSource(EncodedImage.createFromImage(img.rotate270Degrees(true), true));
	}

	public static ImageLuminanceSource createLuminanceSourceFromJpeg(byte[] jpeg) {
		EncodedImage img = EncodedImage.create(jpeg);
		return new ImageLuminanceSource(img);
	}

}
