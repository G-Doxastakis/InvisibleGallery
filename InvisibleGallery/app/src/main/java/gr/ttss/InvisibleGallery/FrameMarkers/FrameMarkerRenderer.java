/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package gr.ttss.InvisibleGallery.FrameMarkers;

import java.nio.Buffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Marker;
import com.vuforia.MarkerResult;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;
import gr.ttss.InvisibleGallery.VuforiaApplication.VuforiaApplicationSession;
import gr.ttss.InvisibleGallery.VuforiaApplication.utils.CubeShaders;
import gr.ttss.InvisibleGallery.VuforiaApplication.utils.Texture;
import gr.ttss.InvisibleGallery.VuforiaApplication.utils.VuforiaUtils;


// The renderer class for the FrameMarkers sample. 
public class FrameMarkerRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "FrameMarkerRenderer";
    
    VuforiaApplicationSession vuforiaAppSession;
    FrameMarkers mActivity;
    
    public boolean mIsActive = false;

    private Vector<Texture> mTextures;
    
    // OpenGL ES 2.0 specific:
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int mvpMatrixHandle = 0;
    private int textureCoordHandle = 0;
    private int texSampler2DHandle = 0;

    private FileMeshObject Object;
    private FileMeshObject Model1;
    private FileMeshObject Model2;
    private FileMeshObject Model3;
    private AObject aObject = new AObject();
    
    
    public FrameMarkerRenderer(FrameMarkers activity,
        VuforiaApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();
    }
    
    
    void initRendering()
    {
        Log.d(LOGTAG, "initRendering");
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = VuforiaUtils.createProgramFromShaderSrc(
            CubeShaders.CUSTOM_MESH_VERTEX_SHADER,
            CubeShaders.CUSTOM_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");
    }
    
    
    void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        State state = Renderer.getInstance().begin();
        Renderer.getInstance().drawVideoBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        // Set the viewport
        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);


        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(
                trackableResult.getPose()).getData();
            
            // Check the type of the trackable:
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();


            // Select which model to draw:
            switch (marker.getMarkerId())
            {
                case 0:
                    Object=Model1;
                    break;
                case 1:
                    Object=Model2;
                    break;
                case 2:
                    Object=Model3;
                    break;
            }

            // Constants:
            float modelScale = Object.scale;;
            float modelTranslate = Object.translate;
            int textureIndex = Object.texture;
            boolean indexed = Object.indexed;
            Buffer vertices = Object.getVertices();
            int numVertices = Object.getNumObjectVertex();;
            Buffer normals = Object.getNormals();
            Buffer texCoords = Object.getTexCoords();
            Buffer indices = Object.getIndices();
            int numIndices = Object.getNumObjectIndex();

            Texture mTexture = mTextures.get(textureIndex);
            float[] modelViewProjection = new float[16];
            Matrix.translateM(modelViewMatrix, 0, -modelTranslate, -modelTranslate, 25.f);
            Matrix.scaleM(modelViewMatrix, 0, modelScale, modelScale, modelScale);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);

            GLES20.glVertexAttribPointer(vertexHandle,3, GLES20.GL_FLOAT, false, 0, vertices);
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoords);
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture.mTextureID[0]);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            if(indexed) GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indices);
            else GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,numVertices);


            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
            VuforiaUtils.checkGLError("FrameMarkers render frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        Renderer.getInstance().end();

    }

    public void setModelParam(Vector<Texture> textures,AssetManager assets)
    {
        mTextures = textures;
        Model1 = new FileMeshObject("FrameMarkers/EggHolder/Vertices.txt",
                "FrameMarkers/EggHolder/TexCoords.txt","FrameMarkers/EggHolder/Normals.txt",assets,
                30.0f,0.0f,2);
        Model2 = new FileMeshObject("FrameMarkers/Statue/Vertices.txt",
                "FrameMarkers/Statue/TexCoords.txt", "FrameMarkers/Statue/Normals.txt",assets,
                100.0f,0.0f,1);
        Model3 = new FileMeshObject("FrameMarkers/Banana/Vertices.txt",
                "FrameMarkers/Banana/TexCoords.txt","FrameMarkers/Banana/Normals.txt",assets,
                80.0f,0.0f,3);
    }
}
