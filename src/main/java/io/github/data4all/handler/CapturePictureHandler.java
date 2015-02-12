/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.handler;

import io.github.data4all.activity.ShowPictureActivity;
import io.github.data4all.logger.Log;
import io.github.data4all.model.DeviceOrientation;
import io.github.data4all.model.data.TransformationParamBean;
import io.github.data4all.util.Optimizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

/**
 * Create the file to the taken picture with additional data like GPS and save
 * this file.
 * 
 * @author sbollen
 *
 */
public class CapturePictureHandler implements PictureCallback {

	// Actual Activity for the context
	private final Context context;

	// The file into which the picture is saved
	private File photoFile;

	// The directory where the pictures are saved into
	private static final String DIRECTORY = "/Data4all";
	// The fileformat of the saved picture
	private static final String FILE_FORMAT = ".jpeg";
	// The name of the extra info for the filepath in the intent for the new
	// activity
	private static final String FILEPATH = "file_path";
	// Name and object of the DeviceOrientation to give to the next activity
	private static final String DEVICE_ORIENTATION = "current_orientation";
	private DeviceOrientation currentOrientation = null;
	// Name and object of the TransformationParamBean to give to the next
	// activity
	private static final String TRANSFORM_BEAN = "transform_bean";
	private TransformationParamBean transformBean;

	public CapturePictureHandler(Context context) {
		this.context = context;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[],
	 *      android.hardware.Camera)
	 */
    @Override
	public void onPictureTaken(byte[] raw, Camera camera) {
		Log.d(getClass().getSimpleName(), "Save the Picture");

		// get the current data which is necessary for creating an osm element
		final Camera.Parameters params = camera.getParameters();
		final double horizontalViewAngle = Math.toRadians(params
				.getHorizontalViewAngle());
		final double verticalViewAngle = Math.toRadians(params
				.getVerticalViewAngle());
		final Size pictureSize = params.getPictureSize();
		final Location currentLocation = Optimizer.currentBestLoc();
		transformBean = new TransformationParamBean(1.7, horizontalViewAngle,
				verticalViewAngle, pictureSize.width, pictureSize.height,
				currentLocation);
		currentOrientation = Optimizer.currentBestPos();

		// Start a thread to save the Raw Image in JPEG into SDCard
		new SavePhotoTask(params).execute(raw);
	}

	/*
	 * @Description: An inner Class for saving a picture in storage in a thread
	 */
	class SavePhotoTask extends AsyncTask<byte[], String, String> {
	    	Camera.Parameters params;
	    	public SavePhotoTask(Camera.Parameters params) {
	    	    this.params = params;
	    	}
	    	
		@Override
		protected String doInBackground(byte[]... photoData) {
			try {
				// Call the method where the file is created
				photoFile = createFile();

				Log.d(getClass().getSimpleName(), "Picturepath:" + photoFile);
				// Open file channel
				FileOutputStream fos = new FileOutputStream(photoFile.getPath());
                        new FileOutputStream(photoFile.getPath());
				fos.write(photoData[0]);
				fos.flush();
				fos.close();

			} catch (IOException ex) {
				Log.d(getClass().getSimpleName(), ex.getMessage());
				return ex.getMessage();
			}

			return "successful";
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("successful")) {
				Log.d(getClass().getSimpleName(), "Picture successfully saved");

				// Passes the filepath, location and device orientation to the
				// ShowPictureActivity
				Intent intent = new Intent();
				intent.setClass(context, ShowPictureActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(FILEPATH, photoFile);
				intent.putExtra(DEVICE_ORIENTATION, currentOrientation);
				intent.putExtra(TRANSFORM_BEAN, transformBean);
				
			    context.startActivity(intent);
			    

			} else {
				Toast.makeText(context, "Failed on taking picture",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
                

           
	/*
	 * Create a directory Data4all if necessary and create a file for the
	 * picture
	 */
	private File createFile() {
		// Create a File Reference of Photo File
		// Image Type is JPEG

		// Create a new folder on the internal storage named Data4all
        final File folder =
                new File(Environment.getExternalStorageDirectory() + DIRECTORY);
		if (!folder.exists() && folder.mkdirs()) {
			Toast.makeText(context, "New Folder Created", Toast.LENGTH_SHORT)
					.show();
		}

		// Save the picture to the folder in the internal storage
		return new File(Environment.getExternalStorageDirectory() + DIRECTORY,
				System.currentTimeMillis() + FILE_FORMAT);
	}
}
