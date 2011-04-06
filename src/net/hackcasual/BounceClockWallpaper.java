/*
 *    Copyright 2011 Charles Vaughn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package net.hackcasual;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class BounceClockWallpaper extends WallpaperService {


	
	@Override
	public Engine onCreateEngine() {
		// TODO Auto-generated method stub
		return new BounceClockEngine();
	}
	
	public class BounceClockEngine extends Engine {
		BounceClock updater;
		Bitmap renderTarget;
		Bitmap savedRenderTarget;
		float xOffset;
		float xScale;
		
		@Override
		public void onVisibilityChanged(boolean vis) {
			if (!vis) {
				updater.shutdown();
				updater = null;
			} else {

				if (updater == null) {
					updater = new BounceClock(renderTarget, renderTarget.getWidth(), renderTarget.getHeight(), new Runnable() {

						@Override
						public void run() {
				            final SurfaceHolder holder = getSurfaceHolder();

				            Canvas c = null;
				            try {
				                c = holder.lockCanvas();
				                if (c != null) {
				                	savedRenderTarget = renderTarget.copy(Config.RGB_565, false);
				                	c.drawBitmap(savedRenderTarget, new Rect((int)(renderTarget.getWidth() * xOffset* xScale), 0, (int)(renderTarget.getWidth() * xOffset* xScale + c.getWidth()), renderTarget.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()), new Paint());
				                }
				            } finally {
				                if (c != null) holder.unlockCanvasAndPost(c);
				            }
						}
						
					});
				}
				
				updater.start();
			}
		}
		
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            renderTarget = Bitmap.createBitmap((int)(width * 2.5f), height, Bitmap.Config.RGB_565);
            savedRenderTarget = renderTarget.copy(Config.RGB_565, false);
        }
        
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
        	this.xScale = 1.0f - (xStep * 1.5f);
        	this.xOffset = xOffset;
        	final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                	c.drawBitmap(savedRenderTarget, new Rect((int)(renderTarget.getWidth() * xOffset * xScale), 0, (int)(renderTarget.getWidth() * xOffset  * xScale + c.getWidth()), renderTarget.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()), new Paint());
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }        	
        }
	}

}
