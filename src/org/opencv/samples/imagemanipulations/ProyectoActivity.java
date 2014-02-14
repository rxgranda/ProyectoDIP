package org.opencv.samples.imagemanipulations;

import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.imagemanipulations.R;




import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class ProyectoActivity extends Activity implements CvCameraViewListener2, SensorEventListener, OnTouchListener  {
	private static final String  TAG                 = "OCVSample::Activity";

	private SensorManager mSensorManager;
	private Sensor mRotationVectorSensor;


	private final float[] mRotationMatrix = new float[16];
	private  float[] orientacion = new float[3];


	public static final int      VIEW_MODE_RGBA      = 0;

	public static final int      VIEW_MODE_CORRECCION     = 3;

	public static final int      VIEW_MODE_CORRECCION_FILTRO  = 6;

	private MenuItem             mItemPreviewRGBA;

	private MenuItem             mItemPreviewSepia;

	private MenuItem             mItemPreviewPixelize;

	private CameraBridgeViewBase mOpenCvCameraView;

	private Size                 mSize0;

	private Mat                  mIntermediateMat;
	private Mat                  mMat0;
	private MatOfInt             mChannels[];
	private MatOfInt             mHistSize;
	private int                  mHistSizeNum = 25;
	private MatOfFloat           mRanges;
	private Scalar               mColorsRGB[];
	private Scalar               mColorsHue[];
	private Scalar               mWhilte;
	private Point                mP1;
	private Point                mP2;
	private float                mBuff[];
	private Mat                  mfiltro;

	public static int           viewMode = VIEW_MODE_RGBA;

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public ProyectoActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.image_manipulations_surface_view);
		///////////////////////////////
		// Get an instance of the SensorManager
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mRotationVectorSensor = mSensorManager.getDefaultSensor(
				Sensor.TYPE_ROTATION_VECTOR);

		// initialize the rotation matrix to identity
		mRotationMatrix[ 0] = 1;
		mRotationMatrix[ 4] = 1;
		mRotationMatrix[ 8] = 1;
		mRotationMatrix[12] = 1;
		///////////////////////////////

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setOnTouchListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		mSensorManager.registerListener(this, mRotationVectorSensor, 10000);

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {     
		mItemPreviewRGBA  = menu.add("Preview RGBA");    
		mItemPreviewSepia = menu.add("Correccion");   
		mItemPreviewPixelize  = menu.add("Filtro");  
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item == mItemPreviewRGBA)
			viewMode = VIEW_MODE_RGBA;
		else if (item == mItemPreviewSepia)
			viewMode = VIEW_MODE_CORRECCION;       
		else if (item == mItemPreviewPixelize)
			viewMode = VIEW_MODE_CORRECCION_FILTRO;      
		return true;
	}

	public void onCameraViewStarted(int width, int height) {
		mIntermediateMat = new Mat();
		mSize0 = new Size();
		mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
		mBuff = new float[mHistSizeNum];
		mHistSize = new MatOfInt(mHistSizeNum);
		mRanges = new MatOfFloat(0f, 256f);
		mMat0  = new Mat();
		mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
		mColorsHue = new Scalar[] {
				new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
				new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
				new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
				new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
				new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
		};
		mWhilte = Scalar.all(255);
		mP1 = new Point();
		mP2 = new Point();

		// Fill kernel
		mfiltro = new Mat(4, 4, CvType.CV_32F);
		mfiltro.put(0, 0, /* R */0.11f, 0.11f, 0.11f, 0f);
		mfiltro.put(1, 0, /* G */0.11, 0.11f, 0.11f, 0f);
		mfiltro.put(2, 0, /* B */0.11f, 0.11f, 0.11f, 0f);
		mfiltro.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
	}

	public void onCameraViewStopped() {
		// Explicitly deallocate Mats
		if (mIntermediateMat != null)
			mIntermediateMat.release();

		mIntermediateMat = null;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat rgba = inputFrame.rgba();
		Size sizeRgba = rgba.size();

		Mat rgbaInnerWindow;

		int rows = (int) sizeRgba.height;
		int cols = (int) sizeRgba.width;

		int left = cols / 8;
		int top = rows / 8;
		int width = cols  *8/20 ;
		int height = rows * 8/ 10;


		switch (ProyectoActivity.viewMode) {
		case ProyectoActivity.VIEW_MODE_RGBA:
			break;
		case ProyectoActivity.VIEW_MODE_CORRECCION:
			rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);

			double radians = Math.toRadians(0);
			double sin = Math.abs(Math.sin(radians));
			double cos = Math.abs(Math.cos(radians));

			int newWidth = (int) (rgbaInnerWindow.width() * cos + rgbaInnerWindow.height() * sin);
			int newHeight = (int) (rgbaInnerWindow.width() * sin + rgbaInnerWindow.height() * cos);

			int[] newWidthHeight = {newWidth, newHeight};
			// create new sized box (newWidth/newHeight)

			int pivotX = newWidthHeight[0]/2; 
			int pivotY = newWidthHeight[1]/2;
			

			org.opencv.core.Point center = new org.opencv.core.Point(pivotX, pivotY);
			Size targetSize = new Size(newWidthHeight[0], newWidthHeight[1]);
			// now create another mat, so we can use it for mapping

			Mat rotImage;
			if((57.30f*orientacion[2] )<-2)
				rotImage = Imgproc.getRotationMatrix2D(center,57.30f*orientacion[1] +90  , 1.0);
			else
				rotImage = Imgproc.getRotationMatrix2D(center,-57.30f*orientacion[1]-90   , 1.0); 
		 
			Imgproc.warpAffine(rgbaInnerWindow, rgbaInnerWindow, rotImage, targetSize, Imgproc.INTER_LINEAR);//,  //  Imgproc.BORDER_CONSTANT, null);

			rgbaInnerWindow.release();
			break;

		case ProyectoActivity.VIEW_MODE_CORRECCION_FILTRO:
			rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);            
			Core.transform(rgbaInnerWindow, rgbaInnerWindow, mfiltro);  
			double radians2 = Math.toRadians(0);
			double sin1 = Math.abs(Math.sin(radians2));
			double cos1 = Math.abs(Math.cos(radians2));

			int newWidth1 = (int) (rgbaInnerWindow.width() * cos1 + rgbaInnerWindow.height() * sin1);
			int newHeight1 = (int) (rgbaInnerWindow.width() * sin1 + rgbaInnerWindow.height() * cos1);

			int[] newWidthHeight1 = {newWidth1, newHeight1};
			// Crear uevo box box (newWidth/newHeight)

			int pivotX1 = newWidthHeight1[0]/2; 
			int pivotY1 = newWidthHeight1[1]/2;
			
			org.opencv.core.Point center1 = new org.opencv.core.Point(pivotX1, pivotY1);
			Size targetSize1 = new Size(newWidthHeight1[0], newWidthHeight1[1]);
			// Crear otra matriz

			Mat rotImage1;
			if((57.30f*orientacion[2] )<-2)
				rotImage1 = Imgproc.getRotationMatrix2D(center1,57.30f*orientacion[1] +90  , 1.0);
			else
				rotImage1 = Imgproc.getRotationMatrix2D(center1,-57.30f*orientacion[1]-90   , 1.0); 
			
			Imgproc.warpAffine(rgbaInnerWindow, rgbaInnerWindow, rotImage1, targetSize1, Imgproc.INTER_LINEAR);//,  //  Imgproc.BORDER_CONSTANT, null);



			rgbaInnerWindow.release();
			break;
		}

		if(turno){
			Point a = new Point(x1,y1);
			Point b = new Point(x2,y2);
			Core.rectangle(rgba, a, b, new Scalar(0,0,0));
			Log.d("Rectangulo","Puntos"+x1+" "+ y1+" "+x2+" "+ y2);
			
		}
		
		
		
		/*Bitmap bmp = null;
		try {
		    //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
		   // Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
		    bmp = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
		    Utils.matToBitmap(rgba, bmp);
			((ProyectoView)findViewById(R.id.canvas)).update(bmp);
		}
		catch (CvException e){Log.d("Exception",e.getMessage());}*/
		return rgba;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

			SensorManager.getRotationMatrixFromVector(
					mRotationMatrix , event.values);
			SensorManager.getOrientation(mRotationMatrix, orientacion);
		}

	}
	boolean turno=true;
	float x1=0;
	float y1=0;
	float x2=0;
	float y2=0;
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		int maskedAction = arg1.getActionMasked();

	    switch (maskedAction) {

	    case MotionEvent.ACTION_DOWN:{
	    	if(turno){
				x1=arg1.getX();
				y1=arg1.getY();
			}else{
				x2=arg1.getX();
				y2=arg1.getY();
			}
			turno=!turno;
			Log.d("Rectangulo","Turno="+turno);
	    	break;
	    }
	   
	    }
	   

		
		return true;
	}
	
}
