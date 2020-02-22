// Copyright 2020 Enterkey
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package xyz.enterkey.mlkitdemo.vision.custommodel

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.common.FirebaseMLException
import xyz.enterkey.mlkitdemo.vision.common.VisionImageProcessor
import xyz.enterkey.mlkitdemo.vision.common.FrameMetadata
import xyz.enterkey.mlkitdemo.vision.common.GraphicOverlay
import xyz.enterkey.mlkitdemo.vision.common.BitmapUtils
import xyz.enterkey.mlkitdemo.vision.common.CameraImageGraphic
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/**
 * Custom Image Classifier Demo.
 */
class CustomImageClassifierProcessor
@Throws(FirebaseMLException::class)
constructor(activity: Activity, useQuantizedModel: Boolean) :
    VisionImageProcessor {
    private val classifier: CustomImageClassifier =
        CustomImageClassifier(
            activity,
            useQuantizedModel
        )
    private val activityRef = WeakReference(activity)

    @Throws(FirebaseMLException::class)
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        activityRef.get()?.let { activity ->
            classifier
                .classifyFrame(data, frameMetadata.width, frameMetadata.height)
                .addOnSuccessListener(
                    activity
                ) { result ->
                    val labelGraphic =
                        LabelGraphic(
                            graphicOverlay,
                            result
                        )
                    val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
                    val imageGraphic =
                        CameraImageGraphic(
                            graphicOverlay,
                            bitmap
                        )
                    graphicOverlay.clear()
                    graphicOverlay.add(imageGraphic)
                    graphicOverlay.add(labelGraphic)
                    graphicOverlay.postInvalidate()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Custom classifier failed: $e")
                    e.printStackTrace()
                }
        }
    }

    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) = Unit

    override fun stop() = Unit

    companion object {
        private const val TAG = "Custom"
    }
}
