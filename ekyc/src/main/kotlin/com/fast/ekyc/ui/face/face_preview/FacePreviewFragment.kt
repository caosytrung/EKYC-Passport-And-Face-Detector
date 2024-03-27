package com.fast.ekyc.ui.face.face_preview

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fast.ekyc.BR
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseFragment
import com.fast.ekyc.base.ui.SingleHandlerEventObserver
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.databinding.KycFragmentFacePreviewBinding
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.ui.face.normal_face_guide.FaceGuideBottomSheet
import com.fast.ekyc.ui.result_popup.ImageErrorBottomSheet
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.extension.isAdvancedMode
import com.fast.ekyc.utils.extension.setOnSingleClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class FacePreviewFragment :
    BaseFragment<KycFragmentFacePreviewBinding, FacePreviewViewModel>() {

    private val facePreviewViewModel: FacePreviewViewModel by viewModels { viewModelFactory }

    @Inject
    internal lateinit var config: EkycConfig
    private val faceBitmap by lazy { DataHolder.getFaceBitmap() }

    override fun getViewModel() = facePreviewViewModel

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.kyc_fragment_face_preview

    override fun isShowDefaultLoading() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        facePreviewViewModel.setMainViewModel(mainViewModel)
    }

    override fun initComponents() {
        viewDataBinding.apply {
            val title = if (!config.isAdvancedMode()) {
                R.string.kyc_capture_face
            } else {
                R.string.kyc_record_face
            }

            btnRecapture.setText(
                if (config.isAdvancedMode()) {
                    R.string.kyc_rerecord
                } else {
                    R.string.kyc_recapture
                }
            )

            tvTitle.setText(title)
            btnUseImage.setButtonTextColor(Color.WHITE)
            btnUseImage.setButtonBackgroundColor(ThemeHolder.buttonColor)

            btnUseImage.setOnSingleClickListener {
                lnPreview.isInvisible = true
                val windowSize = CustomSize(ivCard.width, ivCard.height)
                val hole = overlayView.getCroppingHole()
                facePreviewViewModel.setupForCroppingBitmap(windowSize, hole)
                verifyImage()
            }
            btnRecapture.setOnSingleClickListener {
                mainViewModel.decreaseFaceRetake()
                back()
            }
            ivClose.setOnSingleClickListener {
                getMainActivity()?.onCancelled()
            }
            ivGuide.setOnSingleClickListener { showHelp() }

            val bitmap = faceBitmap ?: return
            ivCard.setImageBitmap(bitmap)
        }

        observerViewModel()
    }

    private fun showHelp() {
        lifecycleScope.launch {
            if (childFragmentManager.findFragmentByTag(FaceGuideBottomSheet.TAG) == null) {
                FaceGuideBottomSheet.getInstance(config.isAdvancedMode())
                    .show(childFragmentManager, FaceGuideBottomSheet.TAG)
            }
        }
    }

    private fun verifyImage() {
        viewDataBinding.apply {
            facePreviewViewModel.verifyImage()
        }
    }

    private fun observerViewModel() {
        facePreviewViewModel.apply {
            uiFlowDoneEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                getMainActivity()?.onUIFlowDone()
            })

            showErrorDialogEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                val title =
                    getString(if (config.isAdvancedMode()) R.string.kyc_invalid_record_title else R.string.kyc_invalid_face_title)
                showErrorDialog(title, it)
            })

        }
    }

    private fun back() {
        findNavController().navigateUp()
    }

    private fun showErrorDialog(title: String, error: String) {
        if (childFragmentManager.findFragmentByTag(ImageErrorBottomSheet.TAG) == null) {
            val closeButton =
                if (config.isAdvancedMode()) R.string.kyc_rerecord else R.string.kyc_recapture
            val bottomSheet = ImageErrorBottomSheet.newInstance(
                title = title,
                content = error,
                closeButton = context?.getString(closeButton) ?: "",
            ) {
                back()
            }

            bottomSheet.show(childFragmentManager, ImageErrorBottomSheet.TAG)
        }
    }

}