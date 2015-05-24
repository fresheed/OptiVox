package org.fresheed.theremin;

import android.graphics.Bitmap;

interface ImageProcessCallback {
	void setProcessedImage(Bitmap b);
	void setFrequency(int f);
}
