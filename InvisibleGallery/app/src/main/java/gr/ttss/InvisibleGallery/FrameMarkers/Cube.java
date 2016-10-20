package gr.ttss.InvisibleGallery.FrameMarkers;

import gr.ttss.InvisibleGallery.SampleApplication.utils.MeshObject;

import java.nio.Buffer;

/**
 * Created by TTSS on 19/10/2016.
 */

public class Cube extends MeshObject {
    private static final double objectVertices[] ={
            // f  1//2  7//2  5//2
            -0.5, -0.5, -0.5,
            0.5, 0.5, -0.5,
            0.5, -0.5, -0.5,
            // f  1//2  3//2  7//2
            -0.5, -0.5, -0.5,
            -0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            // f  1//6  4//6  3//6
            -0.5, -0.5, -0.5,
            -0.5, 0.5, 0.5,
            -0.5, 0.5, -0.5,
            // f  1//6  2//6  4//6
            -0.5, -0.5, -0.5,
            -0.5, -0.5, 0.5,
            -0.5, 0.5, 0.5,
            // f  3//3  8//3  7//3
            -0.5, 0.5, -0.5,
            0.5, 0.5, 0.5,
            0.5, 0.5, -0.5,
            // f  3//3  4//3  8//3
            -0.5, 0.5, -0.5,
            -0.5, 0.5, 0.5,
            0.5, 0.5, 0.5,
            // f  5//5  7//5  8//5
            0.5, -0.5, -0.5,
            0.5, 0.5, -0.5,
            0.5, 0.5, 0.5,
            // f  5//5  8//5  6//5
            0.5, -0.5, -0.5,
            0.5, 0.5, 0.5,
            0.5, -0.5, 0.5,
            // f  1//4  5//4  6//4
            -0.5, -0.5, -0.5,
            0.5, -0.5, -0.5,
            0.5, -0.5, 0.5,
            // f  1//4  6//4  2//4
            -0.5, -0.5, -0.5,
            0.5, -0.5, 0.5,
            -0.5, -0.5, 0.5,
            // f  2//1  6//1  8//1
            -0.5, -0.5, 0.5,
            0.5, -0.5, 0.5,
            0.5, 0.5, 0.5,
            // f  2//1  8//1  4//1
            -0.5, -0.5, 0.5,
            0.5, 0.5, 0.5,
            -0.5, 0.5, 0.5,
    };

    private static final double objectNormals[] ={
            // f  1//2  7//2  5//2
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            // f  1//2  3//2  7//2
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            // f  1//6  4//6  3//6
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            // f  1//6  2//6  4//6
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            // f  3//3  8//3  7//3
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            // f  3//3  4//3  8//3
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            // f  5//5  7//5  8//5
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            // f  5//5  8//5  6//5
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            // f  1//4  5//4  6//4
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            // f  1//4  6//4  2//4
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            // f  2//1  6//1  8//1
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            // f  2//1  8//1  4//1
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
    };

    Buffer mVertBuff;
    Buffer mNormBuff;


    public Cube()
    {
        mVertBuff = fillBuffer(objectVertices);
        mNormBuff = fillBuffer(objectNormals);
    }


    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }


    @Override
    public int getNumObjectVertex()
    {
        return objectVertices.length / 3;
    }


    @Override
    public int getNumObjectIndex()
    {
        return 0;
    }
}
