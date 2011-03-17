/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// OpenGL ES 2.0 code

#include <jni.h>
#include <android/log.h>
#include <pthread.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>


#define MAX_POINTS 1000

#define  LOG_TAG    "libgl2jni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)



static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

static const char gVertexShader[] = 
    "attribute vec4 vPosition;\n"
    "attribute vec2 a_texCoord;   \n"
    "varying vec2 v_texCoord;     \n"
    "void main() {\n"
    "  gl_Position = vPosition;\n"
	"   v_texCoord = a_texCoord;  \n"
    "}\n";

static const char gFragmentShader[] = 
    "precision mediump float;\n"
	"varying vec2 v_texCoord;                            \n"
	"uniform sampler2D s_texture;                        \n"
    "void main() {\n"
	"  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
    "}\n";

static const char gStampVertexShader[] =
    "attribute vec4 vPosition;\n"
    "attribute vec2 a_texCoord;\n"
	"attribute vec3 a_globalPosition;\n"
	"uniform float u_xscale;\n"
	"uniform float u_yscale;\n"	
    "varying vec2 v_texCoord;     \n"
    "void main() {\n"
    "  gl_Position = vPosition;\n"
	"  float old_x = gl_Position.x;\n"
	"  gl_Position.x = gl_Position.x*(cos(a_globalPosition.z)) - gl_Position.y*(sin(a_globalPosition.z));\n"
	"  gl_Position.y = old_x*(sin(a_globalPosition.z)) + gl_Position.y*(cos(a_globalPosition.z));\n"
	"  gl_Position.x = (gl_Position.x / u_xscale) + a_globalPosition.x;\n"
	"  gl_Position.y = (gl_Position.y / u_yscale) + a_globalPosition.y;\n"
	"   v_texCoord = a_texCoord;  \n"
    "}\n";

const GLfloat glQuadVertices[] = {
		-1.0f, 1.0f, 0.0f,
		-1.0f, -1.0f, 0.0f,
		1.0f, 1.0f, 0.0f,
		
		
		1.0f, 1.0f, 0.0f,			
		-1.0f, -1.0f, 0.0f,		
		1.0f, -1.0f, 0.0f,

		
        };	
	
GLfloat glSpriteTextureCoords[12 * 8];


int screenWidth, screenHeight;


struct point {
	float x, y, vel_x, vel_y, rot, vel_rot;
	int texture_id;
};

pthread_mutex_t point_mutex = PTHREAD_MUTEX_INITIALIZER;

point point_buffer[MAX_POINTS];
int numPoints = 0;

GLfloat pointQuads[MAX_POINTS * 18]; // 3 floats for each vertex, 3 verticies per triangle, 2 triangles per point
GLfloat locationPoints[MAX_POINTS * 18]; // 3 floats for each vertex, 3 verticies per triangle, 2 triangles per point
GLfloat textureMap[MAX_POINTS * 12]; // 2 floats per vertex

//Process the points into the buffers
void loadGLBuffers() {
	for (int i = 0; i < numPoints; i++) {
		memcpy(&pointQuads[i * 18], glQuadVertices, 18 * sizeof(GLfloat));
		memcpy(&textureMap[i * 12], &glSpriteTextureCoords[point_buffer[i].texture_id * 12], 12 * sizeof(GLfloat));
		
		locationPoints[i * 18 + 0] = point_buffer[i].x / (screenWidth / 2.0f) - 1.0f;
		locationPoints[i * 18 + 1] = -(point_buffer[i].y / (screenHeight / 2.0f) - 1.0f);
		locationPoints[i * 18 + 2] = point_buffer[i].rot;
		
		memcpy(&locationPoints[i * 18 + 3], &locationPoints[i * 18], 3 * sizeof(GLfloat));
		memcpy(&locationPoints[i * 18 + 6], &locationPoints[i * 18], 6 * sizeof(GLfloat));		
		memcpy(&locationPoints[i * 18 + 12], &locationPoints[i * 18], 6 * sizeof(GLfloat));
	}
}

void makeDemoPoint() {
	point p1, p2;
	p1.texture_id = 4;
	p1.x = 100.0f;
	p1.vel_x = -0.4f;	
	p1.y = 400.0f;
	p1.vel_y = 0.2f;	
	p1.rot = 0.9f;
	p1.vel_rot = 0.01f;	
	
	p2.texture_id = 6;
	p2.x = 300.0f;
	p2.vel_x = 0.7f;
	p2.y = 700.0f;
	p2.vel_y = -1.2f;		
	p2.rot = -0.9f;
	p2.vel_rot = -0.03f;		
	
	point_buffer[0] = p1;
	point_buffer[1] = p2;
	
	numPoints = 2;
}

void deletePoint(int i) {
	pthread_mutex_lock(&point_mutex);
	
	point_buffer[i] = point_buffer[numPoints - 1];
	numPoints--;
	
	pthread_mutex_unlock(&point_mutex);
}

void addPoint(point p) {
	if (numPoints >= MAX_POINTS)
		return;

	pthread_mutex_lock(&point_mutex);
	
	point_buffer[numPoints++] = p;
	
	pthread_mutex_unlock(&point_mutex);
}



void simulatePoints(float grav_x, float grav_y) {
	int pointToDelete = -1;
	for (int i = 0; i < numPoints; i++) {
		point_buffer[i].x += point_buffer[i].vel_x;
		point_buffer[i].y += point_buffer[i].vel_y;
		point_buffer[i].rot += point_buffer[i].vel_rot;
		
		if (point_buffer[i].vel_x < 0.3f && point_buffer[i].vel_x > -0.3f) {
			point_buffer[i].vel_x = 1.0f;
		}
		
		
		point_buffer[i].vel_x += grav_x;
		point_buffer[i].vel_y += grav_y;
		
		if (point_buffer[i].y > screenHeight - 10 && point_buffer[i].vel_y > 0) {
			point_buffer[i].vel_y = -point_buffer[i].vel_y * 0.9;
			point_buffer[i].vel_rot = -point_buffer[i].vel_rot * 1.05;
		}
		
		if (point_buffer[i].x < -20 || point_buffer[i].y < -20 || point_buffer[i].x > screenWidth + 20) {
			pointToDelete = i;
		}
	}
	
	if (pointToDelete > -1)
		deletePoint(pointToDelete);
	else if (numPoints < 100) {
		point newPoint;
		
		newPoint.x = rand() %100 + (screenWidth / 2 - 50);
		newPoint.y = rand() %50 + 10;
		newPoint.rot = 3.14f / (rand() %100);
		newPoint.vel_y = -2.0f + (rand() % 600) / 100.0f;
		newPoint.vel_x = -3.0f + (rand() % 600) / 100.0f;
		newPoint.vel_rot = -0.1f + (rand() % 1000) / 5000.0f;
		newPoint.texture_id = rand() % 8;
		addPoint(newPoint);
	}
}

GLuint loadShader(GLenum shaderType, const char* pSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char* buf = (char*) malloc(infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d:\n%s\n",
                            shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint createProgram(const char* pVertexSource, const char* pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char* buf = (char*) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

GLuint gProgram;
GLuint gStampProgram;
GLuint gvPositionHandle;
GLuint gvTexCoordHandle;

GLuint gvStampPositionHandle;
GLuint gvStampTexCoordHandle;
GLuint gvStampGlobalPositionHandle;
GLuint gvStampXScaleHandle;
GLuint gvStampYScaleHandle;
GLuint gvStampRotationHandle;
GLuint gvStampTextureHandle;

GLuint gvSamplerHandle;
GLuint textureID;
GLuint spriteTextureID;

GLfloat bgWScale, bgHScale;

float scaleX, scaleY;

bool setupGraphics(int w, int h, int backgroundTextureID, int bgW, int bgH, int spriteTexID) {
	srand(time(NULL));
	screenWidth = w;
	screenHeight = h;
	scaleX = w / 20;
	scaleY = h / 20;

    printGLString("Version", GL_VERSION);
    printGLString("Vendor", GL_VENDOR);
    printGLString("Renderer", GL_RENDERER);
    printGLString("Extensions", GL_EXTENSIONS);

    LOGI("setupGraphics(%d, %d, %d, %d)", w, h, backgroundTextureID, spriteTexID);

    bgWScale = w * 1.0f / bgW;
    bgHScale = h * 1.0f / bgH;

    textureID = backgroundTextureID;
    spriteTextureID = spriteTexID;

    gProgram = createProgram(gVertexShader, gFragmentShader);
    if (!gProgram) {
        LOGE("Could not create program.");
        return false;
    }

    gStampProgram = createProgram(gStampVertexShader, gFragmentShader);
    if (!gProgram) {
        LOGE("Could not create program.");
        return false;
    }

    gvPositionHandle = glGetAttribLocation(gProgram, "vPosition");
    checkGlError("glGetAttribLocation");
    LOGI("glGetAttribLocation(\"vPosition\") = %d\n",
            gvPositionHandle);

    glViewport(0, 0, w, h);
    checkGlError("glViewport");

    gvTexCoordHandle = glGetAttribLocation(gProgram, "a_texCoord");
    checkGlError("glGetAttribLocation: texCoord");
    gvSamplerHandle = glGetUniformLocation(gProgram, "s_texture");
    checkGlError("glGetAttribLocation: texture");

    LOGI("glGetAttribLocation(\"v_texCoord\") = %d\n",
    		gvTexCoordHandle);

    LOGI("glGetAttribLocation(\"s_texture\") = %d\n",
    		gvSamplerHandle);


    gvStampPositionHandle = glGetAttribLocation(gStampProgram, "vPosition");
    gvStampTexCoordHandle = glGetAttribLocation(gStampProgram, "a_texCoord");
    gvStampGlobalPositionHandle = glGetAttribLocation(gStampProgram, "a_globalPosition");
    gvStampXScaleHandle = glGetUniformLocation(gStampProgram, "u_xscale");
    gvStampYScaleHandle = glGetUniformLocation(gStampProgram, "u_yscale");	
    gvStampTextureHandle = glGetUniformLocation(gStampProgram, "s_texture");
    for (int r = 0; r < 1; r++)
    	for (int c = 0; c < 8; c++) {
    		glSpriteTextureCoords[(r + c) * 12 + 0] = c / 8.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 2] = c / 8.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 4] = (c + 1) / 8.0f;
			
			
    		glSpriteTextureCoords[(r + c) * 12 + 6] = (c + 1) / 8.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 8] = c / 8.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 10] = (c + 1) / 8.0f;
			
    		glSpriteTextureCoords[(r + c) * 12 + 1] = r / 2.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 3] = (r + 1) / 2.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 5] = r / 2.0f;
			
			
    		glSpriteTextureCoords[(r + c) * 12 + 7] = r / 2.0f;
    		glSpriteTextureCoords[(r + c) * 12 + 9] = (r + 1) / 2.0f;					
    		glSpriteTextureCoords[(r + c) * 12 + 11] = (r + 1) / 2.0f;
	
    	}

	LOGI("Ptrs: %d %d %d %d %d %d %d", gvStampPositionHandle, gvStampTexCoordHandle, gvStampGlobalPositionHandle, gvStampXScaleHandle, gvStampYScaleHandle, gvStampRotationHandle, gvStampTextureHandle);

	glEnable(GL_BLEND);
	glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);	

	makeDemoPoint();
	loadGLBuffers();
    return true;
}



const GLfloat glTestTexCoords[] = {
		0.0f, 1.0f,
		0.0f, 0.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f,
		1.0f, 1.0f,
		};		
		
const GLfloat glTestPos[] = {
		0.7f, 0.7f, 0.3f,
        0.7f, 0.7f, 0.3f,
        0.7f, 0.7f, 0.3f,
        0.7f, 0.7f, 0.3f,
        0.7f, 0.7f, 0.3f,
        0.7f, 0.7f, 0.3f,
		};



void renderFrame() {
    static float grey;

	GLfloat vVertices[] = { -1.0f, 1.0f, 0.0f, // Position 0

	                        0.0f, 0.0f, // TexCoord 0

	                        -1.0f, -1.0f, 0.0f, // Position 1

	                        0.0f, bgHScale, // TexCoord 1

	                        1.0f, -1.0f, 0.0f, // Position 2

	                        bgWScale, bgHScale, // TexCoord 2

	                        1.0f, 1.0f, 0.0f, // Position 3

	                        bgWScale, 0.0f // TexCoord 3
	                        };
	GLushort indices[] = { 0, 1, 2, 0, 2, 3 };
	GLushort stampindices[] = { 0, 1, 2, 3 };
	GLsizei stride = 5 * sizeof(GLfloat); // 3 for position, 2 for texture

	simulatePoints(0.0f, 0.03f);
	loadGLBuffers();
    grey += 0.01f;
    if (grey > 1.0f) {
        grey = 0.0f;
    }
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    checkGlError("glClearColor");
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    checkGlError("glClear");
    glUseProgram(gProgram);
    checkGlError("glUseProgram");

    // Load the vertex position
    glVertexAttribPointer(gvPositionHandle, 3, GL_FLOAT, GL_FALSE, stride,
                    vVertices);
    checkGlError("glVertexAttribPointer 1");
    // Load the texture coordinate
    glVertexAttribPointer(gvTexCoordHandle, 2, GL_FLOAT, GL_FALSE, stride,
                    &vVertices[3]);
    checkGlError("glVertexAttribPointer 2");

    glEnableVertexAttribArray(gvPositionHandle);
    checkGlError("glEnableVertexAttribArray(gvPositionHandle)");
    glEnableVertexAttribArray(gvTexCoordHandle);
    checkGlError("glEnableVertexAttribArray(gvTexCoordHandle);");
    // Bind the texture
    glActiveTexture(GL_TEXTURE0);
    checkGlError("glActiveTexture(GL_TEXTURE0);");
    glBindTexture(GL_TEXTURE_2D, textureID);
    checkGlError("glBindTexture(GL_TEXTURE_2D, textureID);");
    // Set the sampler texture unit to 0
    glUniform1i(gvSamplerHandle, 0);
    checkGlError("glUniform1i(gvSamplerHandle, 0);");
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);
    checkGlError("    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);");


    glUseProgram(gStampProgram);
    checkGlError("glUseProgram - stamp");
/*
GLfloat pointQuads[MAX_POINTS * 18]; // 3 floats for each vertex, 3 verticies per triangle, 2 triangles per point
GLfloat locationPoints[MAX_POINTS * 18]; // 3 floats for each vertex, 3 verticies per triangle, 2 triangles per point
GLfloat textureMap[MAX_POINTS * 12]; // 2 floats per vertex
*/
    // Load the vertex position
    glVertexAttribPointer(gvStampPositionHandle, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat),
                    pointQuads);
    checkGlError("glVertexAttribPointer 1");
    // Load the texture coordinate
    glVertexAttribPointer(gvStampTexCoordHandle, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                    textureMap);
    checkGlError("glVertexAttribPointer 2");


    glVertexAttribPointer(gvStampGlobalPositionHandle, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat),
                    locationPoints);
    checkGlError("glVertexAttribPointer 3");

    /*glVertexAttribPointer(gvStampRotationHandle, 1, GL_FLOAT, GL_FALSE, 1 * sizeof(GLfloat),
                    glTestRotation);
    checkGlError("glVertexAttribPointer 4");	*/
	
    glEnableVertexAttribArray(gvStampPositionHandle);
    glEnableVertexAttribArray(gvStampTexCoordHandle);	
    glEnableVertexAttribArray(gvStampGlobalPositionHandle);
    //glEnableVertexAttribArray(gvStampRotationHandle);
    checkGlError("glEnableVertexAttribArray - all");
    // Bind the texture
    glActiveTexture(GL_TEXTURE0);
    checkGlError("glActiveTexture(GL_TEXTURE0);");
    glBindTexture(GL_TEXTURE_2D, spriteTextureID);
    checkGlError("glBindTexture(GL_TEXTURE_2D, textureID);");
    // Set the sampler texture unit to 0
    glUniform1i(gvStampTextureHandle, 0);
    checkGlError("glUniform1i(gvSamplerHandle, 0);");
	
	
    glUniform1f(gvStampXScaleHandle, scaleX);
    glUniform1f(gvStampYScaleHandle, scaleY);	
    checkGlError("glUniform1i(gvStampScaleHandle, 0);");	
    glDrawArrays(GL_TRIANGLES, 0, numPoints * 6);
    checkGlError("glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);");
}

extern "C" {
    JNIEXPORT void JNICALL Java_net_hackcasual_gl2_GL2JNILib_init(JNIEnv * env, jobject obj,  jint width, jint height, jint backgroundTextureID, jint backgroundTextureWidth, jint backgroundTextureHeight, jint spriteTexID);
    JNIEXPORT void JNICALL Java_net_hackcasual_gl2_GL2JNILib_step(JNIEnv * env, jobject obj);
};

JNIEXPORT void JNICALL Java_net_hackcasual_gl2_GL2JNILib_init(JNIEnv * env, jobject obj,  jint width, jint height, jint backgroundTextureID, jint backgroundTextureWidth, jint backgroundTextureHeight, jint spriteTexID)
{
    setupGraphics(width, height, backgroundTextureID, backgroundTextureWidth, backgroundTextureHeight, spriteTexID);
}

JNIEXPORT void JNICALL Java_net_hackcasual_gl2_GL2JNILib_step(JNIEnv * env, jobject obj)
{
    renderFrame();
}
