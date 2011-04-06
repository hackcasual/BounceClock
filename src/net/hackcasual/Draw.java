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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.hackcasual.gl2.GL2JNILib;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class Draw {
	
	public final static int DOT_RADIUS = 10;
	public final static int DOT_SPACING = 5;
	public final static int DAY_COLOR = 0xFF265897;
	public final static int HOUR_COLOR = 0xFF13ACFA;
	public final static int MINUTE_COLOR = 0xFFC0000B;
	public final static int SECONDS_COLOR = 0xFF009A49;
	public final static int DIM_COLOR = 0xFFC9C9C9;
	public final static int SEPERATOR_COLOR = 0xFFB6B4B5;
	
	public final static String DAY_CAPTION = "Days";
	public final static String HOUR_CAPTION = "Hours";
	public final static String MINUTE_CAPTION = "Minutes";
	public final static String SECOND_CAPTION = "Seconds";	
	
	static Map<Integer, Float> fontSizeCache = new HashMap<Integer, Float>();
	
	static Paint spacerPaint = new Paint();

	
	static {
		spacerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		spacerPaint.setAntiAlias(true);
		spacerPaint.setColor(SEPERATOR_COLOR);
	}
	
	public static void renderTime(Canvas target, int x, int y, int w, int h, int days, int hours, int minutes, int seconds) {
		if (w > h)
			renderTimeHorizontal(target, x, y, w, h, days, hours, minutes, seconds);
		else
			renderTimeVertical(target, x, y, w, h, days, hours, minutes, seconds);
	}
	
	public static void renderTimeHorizontal(Canvas target, int x, int y, int w, int h, int days, int hours, int minutes, int seconds) {

		y *= 2.0f;
		float radius = w / 118.0f;
		
		GL2JNILib.setRadius(radius);
		
		float spacing = radius / 2.0f;

		float digitWidth = 8.0f * radius + 3.0f * spacing;
		float interDigitSpace = 0.3f * digitWidth;
		
		float spacerWidth = 6.0f * radius;
		float spacer1YPos = 5.0f * radius + 2.0f * spacing;
		float spacer2YPos = 9.0f * radius + 4.0f * spacing;		
		
		float cx = x + radius;
		float cy = y;
		
		int dayDigit0 = days / 100 % 10;
		int dayDigit1 = days / 10  % 10;
		int dayDigit2 = days % 10;

		int hourDigit0 = hours / 10  % 10;
		int hourDigit1 = hours % 10;

		int minuteDigit0 = minutes / 10  % 10;
		int minuteDigit1 = minutes % 10;
		
		int secondDigit0 = seconds / 10  % 10;
		int secondDigit1 = seconds % 10;
		
		
		renderDigit(target, dayDigit0, 0, 0, cx, cy, DAY_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, dayDigit1, 1, 0,cx, cy, DAY_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);		
		renderDigit(target, dayDigit2, 2, 0,cx, cy, DAY_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth);	
		
		cx += spacerWidth / 2.0f - radius;		
		target.drawCircle(cx, cy + spacer1YPos, radius, spacerPaint);
		target.drawCircle(cx, cy + spacer2YPos, radius, spacerPaint);
		cx += spacerWidth / 2.0f + radius;
		
		renderDigit(target, hourDigit0, 3, 1,cx, cy, HOUR_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, hourDigit1, 4, 1,cx, cy, HOUR_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth);

		cx += spacerWidth / 2.0f - radius;		
		target.drawCircle(cx, cy + spacer1YPos, radius, spacerPaint);
		target.drawCircle(cx, cy + spacer2YPos, radius, spacerPaint);
		cx += spacerWidth / 2.0f + radius;
		
		renderDigit(target, minuteDigit0, 5, 2,cx, cy, MINUTE_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, minuteDigit1, 6, 2,cx, cy, MINUTE_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth);
		
		cx += spacerWidth / 2.0f - radius;		
		target.drawCircle(cx, cy + spacer1YPos, radius, spacerPaint);
		target.drawCircle(cx, cy + spacer2YPos, radius, spacerPaint);
		cx += spacerWidth / 2.0f + radius;
		
		renderDigit(target, secondDigit0, 7, 3,cx, cy, SECONDS_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, secondDigit1, 8, 3,cx, cy, SECONDS_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth);
	}

	public static void renderTimeVertical(Canvas target, int x, int y, int w, int h, int days, int hours, int minutes, int seconds) {
	
		float radius = h / 74.0f;
		
		GL2JNILib.setRadius(radius);
		
		float spacing = radius / 2.0f;

		float digitHeight = 14.0f * radius + 6.0f * spacing;
		float digitWidth = 8.0f * radius + 3.0f * spacing;
		float interDigitSpace = 0.3f * digitWidth;
		
		float digitBottomPadding = 2.0f * radius;
		

		float maxDaySpace = w - (3 * digitWidth + 2 * interDigitSpace + 4 * radius);
		float maxTimeSpace = w - (2 * digitWidth + interDigitSpace + 4 * radius);		
		
		float fontSize = calculateFontSize(w, maxDaySpace, maxTimeSpace, DAY_CAPTION, HOUR_CAPTION, MINUTE_CAPTION, SECOND_CAPTION);
		Paint caption = new Paint();
		caption.setTextSize(fontSize);
		caption.setColor(0xFF333333);
		caption.setTypeface(Typeface.SANS_SERIF);
		caption.setAntiAlias(true);
		caption.setShadowLayer(2.0f, 1.0f, 1.0f, 0x88000000);		
		
		float cx = x + radius;
		float cy = y + radius;
		
		int dayDigit0 = days / 100 % 10;
		int dayDigit1 = days / 10  % 10;
		int dayDigit2 = days % 10;

		int hourDigit0 = hours / 10  % 10;
		int hourDigit1 = hours % 10;

		int minuteDigit0 = minutes / 10  % 10;
		int minuteDigit1 = minutes % 10;
		
		int secondDigit0 = seconds / 10  % 10;
		int secondDigit1 = seconds % 10;
		
		cx = x + w - (3 * digitWidth + 2 * interDigitSpace);
		renderDigit(target, dayDigit0, 0, 0, cx, cy, DAY_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, dayDigit1, 1, 0, cx, cy, DAY_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);		
		renderDigit(target, dayDigit2, 2, 0, cx, cy, DAY_COLOR, DIM_COLOR, radius, spacing);
		cx = x + w - (2 * digitWidth + interDigitSpace);
		cy += digitHeight - radius - 3.0f;
		target.drawText(DAY_CAPTION, x, cy, caption);
		cy += 3.0f;
		target.drawLine(x + 1.0f, cy, x + maxDaySpace + 2 * radius, cy, caption);
		cy += digitBottomPadding + radius;
				
		renderDigit(target, hourDigit0, 3, 1, cx, cy, HOUR_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, hourDigit1, 4, 1,cx, cy, HOUR_COLOR, DIM_COLOR, radius, spacing);
		cx = x + w - (2 * digitWidth + interDigitSpace);
		cy += digitHeight - radius - 3.0f;
		target.drawText(HOUR_CAPTION, x, cy, caption);
		cy += 3.0f;
		target.drawLine(x + 1.0f, cy, x + maxTimeSpace + 2 * radius, cy, caption);
		cy += digitBottomPadding + radius;


		renderDigit(target, minuteDigit0, 5, 2,cx, cy, MINUTE_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, minuteDigit1, 6, 2,cx, cy, MINUTE_COLOR, DIM_COLOR, radius, spacing);
		cx = x + w - (2 * digitWidth + interDigitSpace);
		cy += digitHeight - radius - 3.0f;
		target.drawText(MINUTE_CAPTION, x, cy, caption);
		cy += 3.0f;
		target.drawLine(x + 1.0f, cy, x + maxTimeSpace + 2 * radius, cy, caption);
		cy += digitBottomPadding + radius;



		renderDigit(target, secondDigit0, 7, 3,cx, cy, SECONDS_COLOR, DIM_COLOR, radius, spacing);
		cx += (digitWidth + interDigitSpace);
		renderDigit(target, secondDigit1, 8, 3,cx, cy, SECONDS_COLOR, DIM_COLOR, radius, spacing);
		cx = x + w - (2 * digitWidth + interDigitSpace);
		cy += digitHeight - radius - 3.0f;
		target.drawText(SECOND_CAPTION, x, cy, caption);
		cy += 3.0f;
		target.drawLine(x + 1.0f, cy, x + maxTimeSpace + 2 * radius, cy, caption);
	}	
	
	static PointI[][] digitPoints = new PointI[7][];
	
	static {
		for (int r = 0; r < 7; r++) {
			digitPoints[r] = new PointI[4];
		
			for (int c = 0; c < 4; c++) 
				digitPoints[r][c] = new PointI(c, r);
		}
	}
	
	static PointI getPoint(int row, int column) {
		return digitPoints[row][column];
	}

	static Map<Integer,Set<PointI>> previousPoints = new HashMap<Integer,Set<PointI>>();
	
	static void addPoint(float cx, float cy, int type) {
		GL2JNILib.addPoint(cx, cy, type);
	}
	
	static void renderDigit(Canvas target, int digit, int digitId, int digitClass, float x, float y, int activeColor, int deactiveColor, float radius, float spacing) {
		Set<PointI> activePoints = Digit.getPointsForNum(digit);
		Set<PointI> prev = previousPoints.get(digitId);
		previousPoints.put(digitId, activePoints);
		for (int r = 0; r < 7; r++)
			for (int c = 0; c < 4; c++) {
				PointI curPoint = getPoint(r,c);
				
				Paint pointPaint = new Paint();
				pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				pointPaint.setAntiAlias(true);

				float cx = c * (2 * radius + spacing) + x;
				float cy = r * (2 * radius + spacing) + y;
				
				
				if (activePoints.contains(curPoint))
					pointPaint.setColor(activeColor);
				else {
					pointPaint.setColor(deactiveColor);
					 if (prev != null && prev.contains(curPoint)) {
						 addPoint(cx,cy, digitClass);
					 }
				}
				
				
				target.drawCircle(cx, cy, radius, pointPaint);
				
			}
	}
	
	static float calculateFontSize(int width, float maxDayWidth, float maxTimeWidth, String dayCaption, String hourCaption, String minuteCaption, String secondCaption) {
		
		// Since this can be an expensive operation, memoize
		if (fontSizeCache.containsKey(width)) {
			return fontSizeCache.get(width);
		}
		
		float fontSize = 24.0f;
		
		Paint renderPaint = new Paint();
		renderPaint.setTextSize(fontSize);
		renderPaint.setTypeface(Typeface.SANS_SERIF);

		float dayWidth = renderPaint.measureText(dayCaption);
		float hourWidth = renderPaint.measureText(hourCaption);
		float minuteWidth = renderPaint.measureText(minuteCaption);
		float secondsWidth = renderPaint.measureText(secondCaption);
		
		float whichWidth = 0.0f;
		String whichString = "";
		
		float dayScore = maxDayWidth / dayWidth;
		float hourScore = maxTimeWidth / hourWidth;
		float minuteScore = maxTimeWidth / minuteWidth;
		float secondsScore = maxTimeWidth / secondsWidth;
		
		float bestScore = Math.min(Math.min(dayScore, hourScore), Math.min(minuteScore,secondsScore));
		
		if (dayScore == bestScore) {
			whichWidth = maxDayWidth;
			whichString = dayCaption;
		} else if (hourScore == bestScore) {
			whichWidth = maxTimeWidth;
			whichString = hourCaption;		
		} else if (minuteScore == bestScore) {
			whichWidth = maxTimeWidth;
			whichString = minuteCaption;			
		} else if (secondsScore == bestScore) {
			whichWidth = maxTimeWidth;
			whichString = secondCaption;			
		}
		
		float lastBestSize = -1;
		
		for (int rounds = 0; rounds < 30; rounds++) {
			float curWidth = renderPaint.measureText(whichString);
			if (curWidth < whichWidth) {
				if (fontSize > lastBestSize)
					lastBestSize = fontSize;
				if (whichWidth - curWidth < 3) {
					fontSizeCache.put(width, lastBestSize);
					return lastBestSize;
				}
				
				fontSize += 2.0f;
			} else {
				fontSize -= 2.0f;
			}
			
			fontSize *= whichWidth / curWidth;
			fontSize -= 0.5f;
			renderPaint.setTextSize(fontSize);
		}
		
		fontSizeCache.put(width, lastBestSize);
		
		return lastBestSize;
	}
}
