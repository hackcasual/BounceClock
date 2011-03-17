/*
Holds points for the simulation
*/

#include <stdlib.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "points.h"



const GLfloat baseVertices[] = {
		-1.0f, 1.0f, 0.0f,
		-1.0f, -1.0f, 0.0f,
		1.0f, 1.0f, 0.0f,
		
		
		1.0f, 1.0f, 0.0f,			
		-1.0f, -1.0f, 0.0f,		
		1.0f, -1.0f, 0.0f,

		
        };

//Process the points into the buffers
void loadGLBuffers() {
	for (int i = 0; i < numPoints; i++) {
		memcpy(&pointQuads[i * 18], baseVertices, 18 * sizeof(GLfloat));
		memcpy(&textureMap[i * 12], &glSpriteTextureCoords[point_buffer[i].texture_id * 12], 12 * sizeof(GLfloat));
		
		locationPoints[i * 18 + 0] = point_buffer[i].x;
		locationPoints[i * 18 + 1] = point_buffer[i].y;
		locationPoints[i * 18 + 3] = point_buffer[i].rot;
		
		memcpy(&locationPoints[i * 18 + 3], &locationPoints[i * 18], 3 * sizeof(GLfloat));
		memcpy(&locationPoints[i * 18 + 6], &locationPoints[i * 18], 6 * sizeof(GLfloat));		
		memcpy(&locationPoints[i * 18 + 12], &locationPoints[i * 18], 6 * sizeof(GLfloat));				
	}
}

void makeDemoPoint() {
	point p1, p2;
	p1.texture_id = 4;
	p1.x = 0.3f;
	p1.y = -0.7f;
	
	p2.texture_id = 6;
	p2.x = -0.3f;
	p2.y = -0.2f;
	
	point_buffer[0] = p1;
	point_buffer[1] = p2;
	
	numPoints = 2;
}