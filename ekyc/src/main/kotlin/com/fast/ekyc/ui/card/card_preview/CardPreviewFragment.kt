package com.fast.ekyc.ui.card.card_preview

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.fast.ekyc.BR
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseFragment
import com.fast.ekyc.base.ui.SingleHandlerEventObserver
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.databinding.KycFragmentCardPreviewBinding
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.tracking.EventAction
import com.fast.ekyc.tracking.EventName
import com.fast.ekyc.tracking.EventSrc
import com.fast.ekyc.tracking.ObjectType
import com.fast.ekyc.ui.card.card_guide_popup.CardGuideBottomSheet
import com.fast.ekyc.ui.result_popup.ImageErrorBottomSheet
import com.fast.ekyc.utils.BitmapUtils
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.extension.isPassportOnly
import com.fast.ekyc.utils.extension.setOnSingleClickListener
import javax.inject.Inject

internal class CardPreviewFragment :
    BaseFragment<KycFragmentCardPreviewBinding, CardPreviewViewModel>() {

    private val cardPreviewViewModel: CardPreviewViewModel by viewModels { viewModelFactory }

    override fun getViewModel() = cardPreviewViewModel

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.kyc_fragment_card_preview

    private val args: CardPreviewFragmentArgs by navArgs()

    @Inject
    internal lateinit var config: EkycConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardPreviewViewModel.setMainViewModel(mainViewModel)
        cardPreviewViewModel.setCardType(args.cardType)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FastEkycSDK.tracker?.createEventAndTrack(
            objectName = EventName.CARD_PREVIEW_SHOW,
            eventSrc = EventSrc.APP,
            objectType = ObjectType.POPUP,
            action = EventAction.SHOW,
            eventValue = mapOf(
                "type" to mainViewModel.getTrackingCardSide(),
            )
        )
    }

    private val cardBitmap = DataHolder.getCardBitmap()

    override fun initComponents() {
        viewDataBinding.apply {
            val cardBitmap = cardBitmap ?: return
            ivCard.setImageBitmap(cardBitmap)
            ivClose.setOnSingleClickListener {
                FastEkycSDK.tracker?.createEventAndTrack(
                    objectName = EventName.CARD_PREVIEW_CLICK_BUTTON_BACK,
                    eventSrc = EventSrc.USER,
                    objectType = ObjectType.BUTTON,
                    action = EventAction.TAP,
                    eventValue = mapOf(
                        "type" to mainViewModel.getTrackingCardSide(),
                    )
                )
                getMainActivity()?.onCancelled()
            }

            btnUseImage.setButtonTextColor(Color.WHITE)
            btnUseImage.setButtonBackgroundColor(
                ThemeHolder.buttonColor
            )
            btnUseImage.setOnSingleClickListener {
                FastEkycSDK.tracker?.createEventAndTrack(
                    objectName = EventName.CARD_PREVIEW_CLICK_BUTTON_CONFIRM,
                    eventSrc = EventSrc.USER,
                    objectType = ObjectType.BUTTON,
                    action = EventAction.TAP,
                    eventValue = mapOf(
                        "type" to mainViewModel.getTrackingCardSide(),
                    )
                )

                lnPreview.isGone = true
                cardPreviewViewModel.verifyImage(cropBitmap()!!)
            }

            btnRecapture.setOnSingleClickListener {
                FastEkycSDK.tracker?.createEventAndTrack(
                    objectName = EventName.CARD_PREVIEW_CLICK_BUTTON_TAKE_AGAIN,
                    eventSrc = EventSrc.USER,
                    objectType = ObjectType.BUTTON,
                    action = EventAction.TAP,
                    eventValue = mapOf(
                        "type" to mainViewModel.getTrackingCardSide(),
                    )
                )
            }

            ivGuide.setOnSingleClickListener {
                openGuidePopup()
            }

        }
        observerViewModel()
    }

    private fun openGuidePopup() {
        if (childFragmentManager.findFragmentByTag(CardGuideBottomSheet.TAG) == null) {
            CardGuideBottomSheet.getInstance(config.isPassportOnly())
                .show(childFragmentManager, CardGuideBottomSheet.TAG)
        }
    }

    private fun cropBitmap(): Bitmap? {
        viewDataBinding.apply {
            val windowSize = CustomSize(previewLayout.width, previewLayout.height)
            val hole = overlayView.getCroppingHole()
            val postBitmap = cardBitmap ?: return null
            return BitmapUtils.cropBitmap(
                windowSize,
                hole,
                postBitmap
            )
        }
    }

    private fun observerViewModel() {
        cardPreviewViewModel.apply {
            showMessageErrorDialogEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                showErrorDialog(getString(R.string.kyc_invalid_card_title), it)
            })

            showNoticeErrorDialogEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                showErrorDialog(it.first, it.second)
            })

            uiFlowDone.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                getMainActivity()?.onUIFlowDone()
            })
        }
    }

    private fun showErrorDialog(title: String, error: String, closeButtonVisible: Boolean = true) {
        if (childFragmentManager.findFragmentByTag(ImageErrorBottomSheet.TAG) == null) {
            val bottomSheet = ImageErrorBottomSheet.newInstance(
                title = title,
                content = error,
                context?.getString(R.string.kyc_recapture) ?: "",
                isCloseButtonVisible = closeButtonVisible
            )
            bottomSheet.show(childFragmentManager, ImageErrorBottomSheet.TAG)
        }
    }

    override fun isShowDefaultLoading(): Boolean {
        return false
    }
}