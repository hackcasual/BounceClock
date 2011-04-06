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

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class BounceClock  extends Thread {
	
	static final long GoogleIOKickoff;
	static {
		Calendar c = Calendar.getInstance();
		c.set(2011, 4, 10, 9, 0, 0);
		GoogleIOKickoff = c.getTimeInMillis();
	}
	

    	
		boolean stop;
    	Bitmap frameBuffer;
    	int width, height;
    	Runnable updater;
    	
    	public BounceClock(Bitmap target, int width, int height, Runnable updater) {
    		this.frameBuffer = target;
    		this.width = width;
    		this.height = height;
    		this.updater = updater;
    		this.stop = false;
    	}
    	
    	@Override
    	public void run() {
    		long lastUpdate = 0;
    		
    		while (!stop) {
    			long currentTime = System.currentTimeMillis();
    			
    			if (currentTime / 1000 > lastUpdate / 1000) {
    				lastUpdate = currentTime;
    				
    				long timeDelta = GoogleIOKickoff - currentTime;
    				timeDelta /= 1000;
    				
    				int seconds = (int)(timeDelta % 60);
    				timeDelta /= 60;
    				int minutes = (int)(timeDelta % 60);
    				timeDelta /= 60;
    				int hours = (int)(timeDelta % 24);
    				timeDelta /= 24;
    				int days = (int)(timeDelta);
    				
    				Canvas renderSurface = new Canvas(frameBuffer);
    				
    				GradientDrawable gradient = new GradientDrawable(Orientation.TL_BR, new int[]{0xFFFFFFFF, 0xFF999999});
    				gradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
    				gradient.setGradientRadius(Math.max(renderSurface.getWidth(), renderSurface.getHeight()));
    				gradient.setDither(true);
    				gradient.setGradientCenter(0.5f, 0.3f);
    				
    				gradient.setBounds(new Rect(0,0,width, height));
    				gradient.draw(renderSurface);

    				int horizontalMargin = width / 10;
    				int verticalMargin = height / 10;    				
    				Draw.renderTime(renderSurface, horizontalMargin, verticalMargin, width - (2 * horizontalMargin), height - (2 * verticalMargin), days, hours, minutes, seconds);
    				
    				updater.run();
    			}
    			
    			
    			
    			try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    	public void shutdown() {
    		this.stop = true;
    	}    
}