package gr.ttss.InvisibleGallery.FrameMarkers;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import gr.ttss.InvisibleGallery.VuforiaApplication.utils.MeshObject;
/**
 * Created by TTSS on 22/11/2016.
 */

public class FileMeshObject extends MeshObject {

    public boolean indexed=false;
    // Data for drawing the 3D plane as overlay
    int NumVertex=0;
    int NumIndex=0;

    Buffer mVertBuff;
    Buffer mTexCoordBuff;
    Buffer mNormBuff;
    Buffer mIndBuff;

    public float scale;
    public float translate;
    public int texture;

    public FileMeshObject(String VertFile,String TexCoordFile,String NormFile, String IndFile,AssetManager assets,float scl,float trl,int txt)
    {
        indexed=true;
        double Vertices[]=loadDoubleCSV(VertFile,assets);
        double Normals[]=loadDoubleCSV(NormFile,assets);
        double Texcoords[]=loadDoubleCSV(TexCoordFile,assets);
        short Indices[]=loadIntCSV(IndFile,assets);
        mVertBuff = fillBuffer(Vertices);
        mTexCoordBuff = fillBuffer(Texcoords);
        mNormBuff = fillBuffer(Normals);
        mIndBuff = fillBuffer(Indices);
        NumVertex = Vertices.length / 3;
        NumIndex = Indices.length;
        scale=scl;
        translate=trl;
        texture=txt;
    }
    public FileMeshObject(String VertFile,String TexCoordFile,String NormFile,AssetManager assets,float scl,float trl,int txt)
    {
        indexed=false;
        double Vertices[]=loadDoubleCSV(VertFile,assets);
        double Normals[]=loadDoubleCSV(NormFile,assets);
        double Texcoords[]=loadDoubleCSV(TexCoordFile,assets);
        mVertBuff = fillBuffer(Vertices);
        mTexCoordBuff = fillBuffer(Texcoords);
        mNormBuff = fillBuffer(Normals);
        NumVertex = Vertices.length / 3;
        scale=scl;
        translate=trl;
        texture=txt;
    }



    private double[] loadDoubleCSV(String fileName, AssetManager assets){
        InputStream inputStream = null;

        ArrayList<Double> data = new ArrayList<Double>();
        try
        {
            inputStream = assets.open(fileName);
            BufferedInputStream stream=new BufferedInputStream(inputStream);
            Scanner scanner = new Scanner(stream);
            while(scanner.hasNextDouble()){
                data.add(scanner.nextDouble());
            }
            Log.i("Model Loader", "Success "+fileName);

        } catch (IOException e)
        {
            Log.e("Model Loader", "Failed to open '" + fileName + "' from APK");
        }
        return convertDoubles(data);
    }
    private short[] loadIntCSV(String fileName, AssetManager assets){
        InputStream inputStream = null;
        ArrayList<Integer> data = new ArrayList<Integer>();
        try
        {
            inputStream = assets.open(fileName);
            BufferedInputStream stream=new BufferedInputStream(inputStream);
            Scanner scanner = new Scanner(stream);
            while(scanner.hasNextInt()){
                data.add(scanner.nextInt());
            }
            Log.i("Model Loader", "Success "+fileName);

        } catch (IOException e)
        {
            Log.e("Model Loader", "Failed to open '" + fileName + "' from APK");
        }
        return convertShorts(data);
    }

    private double[] convertDoubles(ArrayList<Double> doubles)
    {
        double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        int i = 0;
        while(iterator.hasNext())
        {
            ret[i] = iterator.next();
            i++;
        }
        return ret;
    }
    private short[] convertShorts(ArrayList<Integer> ints)
    {
        short[] ret = new short[ints.size()];
        Iterator<Integer> iterator = ints.iterator();
        int i = 0;
        while(iterator.hasNext())
        {
            ret[i] =  iterator.next().shortValue();
            i++;
        }
        return ret;
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
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
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
        return NumVertex;
    }


    @Override
    public int getNumObjectIndex()
    {
        return NumIndex;
    }

}
