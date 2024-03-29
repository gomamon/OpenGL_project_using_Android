package com.anative.grmillet.hw5_code;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

public class GLES30Renderer implements GLSurfaceView.Renderer {

    private Context mContext;


    Camera mCamera;
    private Mario mMario;
    private Cow mCow;
    private IronMan mIronMan;
    private Bus mBus;

    public float ratio = 1.0f;
    public int headLightFlag = 1;
    public int lampLightFlag = 1;
    public int pointLightFlag = 1;
    public int cowLightFlag = 1;
    public int textureFlag = 1;

    public float[] mMVPMatrix = new float[16];
    public float[] mProjectionMatrix = new float[16];
    public float[] mModelViewMatrix = new float[16];
    public float[] mModelMatrix = new float[16];
    public float[] mViewMatrix = new float[16];
    public float[] mModelViewInvTrans = new float[16];


    final static int TEXTURE_ID_MARIO = 0;
    final static int TEXTURE_ID_COW = 1;
    final static int TEXTURE_ID_IRONMAN = 2;
    final static int TEXTURE_ID_BUS = 3;

    private ShadingProgram mShadingProgram;

    public GLES30Renderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.8f, 1.0f);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // 초기 뷰 매트릭스를 설정.
        mCamera = new Camera();

        //vertex 정보를 할당할 때 사용할 변수.
        int nBytesPerVertex = 8 * 4;        // 3 for vertex, 3 for normal, 2 for texcoord, 4 is sizeof(float)
        int nBytesPerTriangles = nBytesPerVertex * 3;

        /*
            우리가 만든 ShadingProgram을 실제로 생성하는 부분
         */
        mShadingProgram = new ShadingProgram(
            AssetReader.readFromFile("vertexshader.vert" , mContext),
            AssetReader.readFromFile("fragmentshader.frag" , mContext));
        mShadingProgram.prepare();
        mShadingProgram.initLightsAndMaterial();
        mShadingProgram.initFlags();
        mShadingProgram.set_up_scene_lights(mViewMatrix);

        /*
                우리가 만든 Object들을 로드.
         */

        /////////////load cow /////////
        mCow = new Cow();
        mCow.addGeometry(AssetReader.readGeometry("Cow_triangles_vn.geom", nBytesPerTriangles, mContext));
        mCow.prepare();
        mCow.setTexture(AssetReader.getBitmapFromFile("grass_tex.jpg", mContext), TEXTURE_ID_COW);

        //////////load tiger////////
        mIronMan = new IronMan();
        mIronMan.addGeometry(AssetReader.readGeometry("IronMan.geom", nBytesPerTriangles, mContext));
        mIronMan.prepare();
        mIronMan.setTexture(AssetReader.getBitmapFromFile("cocacola_tex.jpg", mContext), TEXTURE_ID_IRONMAN);
        //////////////load building////////////////////////
        mBus = new Bus();
        mBus.addGeometry(AssetReader.readGeometry("Bus.geom", nBytesPerTriangles, mContext));
        mBus.prepare();
        mBus.setTexture(AssetReader.getBitmapFromFile("cracker_tex.jpg", mContext), TEXTURE_ID_BUS);


        mMario = new Mario();
        mMario.addGeometry(AssetReader.readGeometry("Mario_Triangle.geom", nBytesPerTriangles, mContext));
        mMario.prepare();
        mMario.setTexture(AssetReader.getBitmapFromFile("mario.jpg", mContext), TEXTURE_ID_MARIO);

    }

    @Override
    public void onDrawFrame(GL10 gl){ // 그리기 함수 ( = display )
        int pid;
        int timestamp = getTimeStamp();

        /*
             실시간으로 바뀌는 ViewMatrix의 정보를 가져온다.
             MVP 중 V 매트릭스.
         */
        mViewMatrix = mCamera.GetViewMatrix();
        /*
             fovy 변화를 감지하기 위해 PerspectiveMatrix의 정보를 가져온다.
             MVP 중 P
             mat, offset, fovy, ratio, near, far
         */
        Matrix.perspectiveM(mProjectionMatrix, 0, mCamera.getFovy(), ratio, 0.1f, 2000.0f);

        /*
              행렬 계산을 위해 이제 M만 계산하면 된다.
         */

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        mShadingProgram.set_lights1();


        /*
         그리기 영역.
         */
        //mShadingProgram.initLightsAndMaterial();
        mShadingProgram.initFlags();
        mShadingProgram.set_up_scene_lights(mViewMatrix);
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.

        ////////////////////////////////////////////////////////////////
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, 8.0f, 8.0f, 8.0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);;

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mBus.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_BUS);
        mBus.draw();



        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 5.0f*timestamp, 0f, 1f, 0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 2.0f);
        Matrix.scaleM(mModelMatrix, 0, 5.0f, 5.0f, 5.0f);
        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mCow.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_COW);
        mCow.draw();
        ///////////////////////////////////
        float[] positionEC = new float[4];
        Matrix.multiplyMV(positionEC, 0, mModelViewMatrix, 0, mShadingProgram.light[2].position, 0);

        float[] spot_direction = new float[4];
        GLES30.glUniform4fv(mShadingProgram.locLight[2].position, 1, BufferConverter.floatArrayToBuffer(positionEC));
        spot_direction[0] = mShadingProgram.light[2].spot_direction[0];
        spot_direction[1] = mShadingProgram.light[2].spot_direction[1];
        spot_direction[2] = mShadingProgram.light[2].spot_direction[2];
        spot_direction[3] = 0.0f;

        float[] directionEC = new float[4];
        Matrix.multiplyMV(directionEC, 0, mModelViewMatrix, 0, spot_direction, 0);

        GLES30.glUniform3fv(mShadingProgram.locLight[2].spot_direction, 1, BufferConverter.floatArrayToBuffer(directionEC));
        //////////////////////////
        //계층적 모델링/////

       // Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.rotateM(mModelMatrix, 0, -20.0f*timestamp, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.scaleM(mModelMatrix, 0, 0.1f, 0.1f, 0.1f);
        Matrix.translateM(mModelMatrix, 0, 0.5f, 2.5f, 0.0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mMario.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_MARIO);

        mShadingProgram.setUpMaterialMario();
        mMario.draw();


        ////////////////////
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, -10.0f*timestamp, 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, 1.0f, 1.0f, 1.0f);
        Matrix.translateM(mModelMatrix, 0, -5.0f, 0.0f, 0.0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);


        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mIronMan.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRONMAN);
        mIronMan.draw();

        /////////////////////
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, 1.0f, 1.0f, 1.0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 0.0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mIronMan.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRONMAN);
        mIronMan.draw();
        ///////////////////////


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        GLES30.glViewport(0, 0, width, height);

        ratio = (float)width / height;

        Matrix.perspectiveM(mProjectionMatrix, 0, mCamera.getFovy(), ratio, 0.1f, 2000.0f);
    }

    static int prevTimeStamp = 0;
    static int currTimeStamp = 0;
    static int totalTimeStamp = 0;

    private int getTimeStamp(){
        Long tsLong = System.currentTimeMillis() / 100;

        currTimeStamp = tsLong.intValue();
        if(prevTimeStamp != 0){
            totalTimeStamp += (currTimeStamp - prevTimeStamp);
        }
        prevTimeStamp = currTimeStamp;

        return totalTimeStamp;
    }

    public void setLight1(){
        mShadingProgram.light[1].light_on = 1 - mShadingProgram.light[1].light_on;
    }

}