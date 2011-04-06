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

public class PointI {
	public final int x;
	public final int y;
	
	public PointI(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PointI))
			return false;
		
		
		PointI other = (PointI)o;
		
		return other.x == x && other.y == y;
	}
	
	@Override
	public int hashCode() {
		return x + 23 * y;
	}
	
	@Override
	public String toString() {
		return String.format("[%dx%d]", x, y);
	}
}
