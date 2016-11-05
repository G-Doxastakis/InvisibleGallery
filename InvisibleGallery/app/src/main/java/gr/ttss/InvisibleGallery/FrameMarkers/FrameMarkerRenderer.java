/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package gr.ttss.InvisibleGallery.FrameMarkers;

import java.nio.Buffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
import gr.ttss.InvisibleGallery.SampleApplication.SampleApplicationSession;
import gr.ttss.InvisibleGallery.SampleApplication.utils.Shaders;
import gr.ttss.InvisibleGallery.SampleApplication.utils.SampleUtils;


// The renderer class for the FrameMarkers sample. 
public class FrameMarkerRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "FrameMarkerRenderer";
    
    SampleApplicationSession vuforiaAppSession;
    FrameMarkers mActivity;
    
    public boolean mIsActive = false;
    
    // OpenGL ES 2.0 specific:
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int mvpMatrixHandle = 0;
    
    // Constants:
    static private float modelScale = 20.0f;
    static private float modelTranslate = 0.0f;

    private Cube cube = new Cube();
    
    
    public FrameMarkerRenderer(FrameMarkers activity,
        SampleApplicationSession session)
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

        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            Shaders.CUSTOM_MESH_VERTEX_SHADER,
            Shaders.CUSTOM_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
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
            //assert (trackableResult.getType() == MarkerTracker.getClassType());
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();

            float[] modelViewProjection = new float[16];
            
            Matrix.translateM(modelViewMatrix, 0, -modelTranslate, -modelTranslate, 20.f);
            Matrix.scaleM(modelViewMatrix, 0, modelScale, modelScale, modelScale);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);

            // Select which model to draw:
            Buffer vertices = null;
            Buffer colors = null;
            int numVertices = 0;

            switch (marker.getMarkerId())
            {
                case 0:
                    vertices = cube.getVertices();
                    numVertices = cube.getNumObjectVertex();
                    colors = cube.mColBuff;

                    break;
            }

            GLES20.glVertexAttribPointer(vertexHandle,3, GLES20.GL_FLOAT, false, 0, vertices);
            GLES20.glEnableVertexAttribArray(vertexHandle);


            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,numVertices);

            GLES20.glDisableVertexAttribArray(vertexHandle);
            SampleUtils.checkGLError("FrameMarkers render frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        Renderer.getInstance().end();

    }
    
}
