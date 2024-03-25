/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fast.ekyc.base.ui.camera;

import android.hardware.Camera;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the camera and allows UI updates on top of it (e.g. overlaying extra Graphics or
 * displaying extra information). This receives preview frames from the camera at a specified rate,
 * sending those frames to child classes' detectors / classifiers as fast as it is able to process.
 */
public class CameraSource {
    public static final int DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH = 1080;
    public static final int DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT = 1080;
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    public static SizePair selectSizePair(Camera camera) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);

        // The method for selecting the best size is to minimize the sum of the differences between
        // the desired values and the actual values for width and height.  This is certainly not the
        // only way to select the best size, but it provides a decent tradeoff between using the
        // closest aspect ratio vs. using the closest pixel area.
        SizePair selectedPair = null;
        int minDiff = Integer.MAX_VALUE;
        for (SizePair sizePair : validPreviewSizes) {
            CustomSize size = sizePair.preview;
            int diff =
                    Math.abs(size.getWidth() - DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH) + Math.abs(size.getHeight() - DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    public static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // By looping through the picture sizes in order, we favor the higher resolutions.
            // We choose the highest resolution in order to support taking the full resolution
            // picture later.
            for (Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all
        // of the preview sizes and hope that the camera can handle it.  Probably unlikely, but we
        // still account for it.
        if (validPreviewSizes.size() == 0) {
            for (Camera.Size previewSize : supportedPreviewSizes) {
                // The null picture size will let us know that we shouldn't set a picture size.
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    public static class SizePair {
        public final CustomSize preview;
        @Nullable
        public final CustomSize picture;

        SizePair(Camera.Size previewSize, @Nullable Camera.Size pictureSize) {
            preview = new CustomSize(previewSize.width, previewSize.height);
            picture = pictureSize != null ? new CustomSize(pictureSize.width, pictureSize.height) : null;
        }

        public SizePair(CustomSize previewSize, @Nullable CustomSize pictureSize) {
            preview = previewSize;
            picture = pictureSize;
        }
    }

}
