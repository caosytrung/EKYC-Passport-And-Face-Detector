package com.fast.ekyc.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fast.ekyc.base.di.viewmodel.ViewModelKey
import com.fast.ekyc.base.di.viewmodel.ViewModelProviderFactory
import com.fast.ekyc.ui.card.card_capture.CardCaptureViewModel
import com.fast.ekyc.ui.card.card_preview.CardPreviewViewModel
import com.fast.ekyc.ui.face.face_capture.FaceCaptureViewModel
import com.fast.ekyc.ui.face.face_preview.FacePreviewViewModel
import com.fast.ekyc.ui.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun provideViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardCaptureViewModel::class)
    internal abstract fun bindCardCaptureViewModel(viewModel: CardCaptureViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FaceCaptureViewModel::class)
    internal abstract fun bindFaceCaptureViewModel(viewModel: FaceCaptureViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardPreviewViewModel::class)
    internal abstract fun bindCardPreviewViewModel(viewModel: CardPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FacePreviewViewModel::class)
    internal abstract fun bindFacePreviewViewModel(viewModel: FacePreviewViewModel): ViewModel
}