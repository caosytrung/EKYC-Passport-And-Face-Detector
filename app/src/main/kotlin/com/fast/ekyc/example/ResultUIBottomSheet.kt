package com.fast.ekyc.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64.DEFAULT
import android.util.Base64.decode
import androidx.core.view.isVisible
import com.fast.ekyc.base.ui.ApolloBottomSheetSize
import com.fast.ekyc.base.ui.BaseBottomSheet
import com.fast.ekyc.data.model.KycUIFlowResult
import com.fast.ekyc.data.model.ResultState
import com.fast.ekyc.example.databinding.BottomSheetUiResultBinding
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


internal class ResultUIBottomSheet : BaseBottomSheet<BottomSheetUiResultBinding>() {

    private var result: KycUIFlowResult? = null

    val file: File by lazy {
        val file = File(requireContext().filesDir, "kycImages")
        if (!file.exists()) {
            file.mkdir()
        }
        File(file, "test_image_size.jpg")
    }

    override fun initComponents() {
        isCancelable = false
        viewDataBinding.btnClose.setOnClickListener {
            dismiss()
        }

        val result = result ?: return

        val displayResultTitle = when (result.resultState) {
            ResultState.UserCancelled -> "UserCanceled"
            ResultState.Success -> "Success"
        }

        val displayResult = DisplayFlowResult(
            result.imageId,
            result.videoPath,
            result.advanceImageDataList?.map { TestCacheAdvanceImageData(it.imageId, it.labelPose) }
                ?: listOf()
        )

        val data = Gson().toJson(displayResult)
        var displayResultMessage = """
                 Result: $displayResultTitle
                 Information: 
                 $data
          """.trimIndent()


        val nfcResult = result.nfcData?.copy(
            dataVerifyObject = null,
            nfcPortrait = "",
            identityData = result.nfcData?.identityData
        )
        val displayNfc = nfcResult?.let { Gson().toJson(it) }

        if (!displayNfc.isNullOrEmpty()) {
            displayResultMessage += """
                
                NFC Data:
                $displayNfc
            """.trimIndent()
        }


        viewDataBinding.tvDisplayResult.text = displayResultMessage

        getNFCImage()?.let {
            viewDataBinding.ivNfcImage.setImageBitmap(it)
            viewDataBinding.ivNfcImage.isVisible = true
            viewDataBinding.tvNfcImage.isVisible = true
        }

        result.localFullImage?.let {
            viewDataBinding.ivFullImage.setImageBitmap(it)
            viewDataBinding.ivFullImage.isVisible = true
            viewDataBinding.tvFullImage.isVisible = true
        }

        result.localCroppedImage?.let {
            viewDataBinding.ivCropImage.setImageBitmap(it)
            viewDataBinding.ivCropImage.isVisible = true

            bitmapToFile(it, file)
            viewDataBinding.tvCropImage.text = "${file.length() / 1024} Kb"
            viewDataBinding.tvCropImage.isVisible = true
        }

        result.advanceImageDataList?.let {
            if (it.size == 2) {
                it[0].let { data ->
                    val bitmap = data.localImage ?: return
                    viewDataBinding.ivAdvanced1.setImageBitmap(bitmap)
                    viewDataBinding.ivAdvanced1.isVisible = true

                    bitmapToFile(bitmap, file)
                    viewDataBinding.tvAdvanced1.text = "${file.length() / 1024} Kb"
                    viewDataBinding.tvAdvanced1.isVisible = true

                    viewDataBinding.tvAdvancedSide1.isVisible = true
                    viewDataBinding.tvAdvancedSide1.text = data.labelPose


                }

                it[1].let { data ->
                    val bitmap = data.localImage ?: return

                    viewDataBinding.ivAdvanced2.setImageBitmap(bitmap)
                    viewDataBinding.ivAdvanced2.isVisible = true

                    bitmapToFile(bitmap, file)
                    viewDataBinding.tvAdvanced2.text = "${file.length() / 1024} Kb"
                    viewDataBinding.tvAdvanced2.isVisible = true

                    viewDataBinding.tvAdvancedSide2.isVisible = true
                    viewDataBinding.tvAdvancedSide2.text = data.labelPose
                }

            }
        }
    }

    private fun getNFCImage(): Bitmap? {
        val result = result ?: return null

        val image = result.nfcData?.nfcPortrait ?: return null
        val decodedString: ByteArray = decode(image, DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    override fun getBottomSheetSize() = ApolloBottomSheetSize.PERCENT_80

    override fun getLayoutId() = R.layout.bottom_sheet_ui_result

    override fun clearComponents() {
        result = null
    }

    fun bitmapToFile(bitmap: Bitmap, file: File) { // File name like "image.png"
        if (file.exists()) file.delete()

        return try {
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                bos
            ) // YOU can also save it in JPEG
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "ResultUIBottomSheet"

        fun newInstance(
            result: KycUIFlowResult,
        ) = ResultUIBottomSheet().apply {
            this.result = result
        }
    }
}

data class DisplayFlowResult(
    val imageId: String = "",
    var videoPath: String? = null,
    var advancedImage: List<TestCacheAdvanceImageData>? = null,
)

class TestCacheAdvanceImageData(
    val imageId: String? = null,
    val labelPose: String? = null,
)

fun bitmapToFile(bitmap: Bitmap, file: File) { // File name like "image.png"
    if (file.exists()) file.delete()

    return try {
        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            bos
        ) // YOU can also save it in JPEG
        val bitmapData = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}