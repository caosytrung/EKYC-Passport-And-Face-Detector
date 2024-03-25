package com.fast.ekyc.di.module

import com.fast.ekyc.ui.card.card_capture.CardCaptureFragment
import com.fast.ekyc.ui.card.card_preview.CardPreviewFragment
import com.fast.ekyc.ui.face.face_capture.FaceCaptureFragment
import com.fast.ekyc.ui.face.face_preview.FacePreviewFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class FragmentModule {
    @ContributesAndroidInjector
    internal abstract fun provideCardCaptureFragment(): CardCaptureFragment

    @ContributesAndroidInjector
    internal abstract fun provideFaceCaptureFragment(): FaceCaptureFragment

    @ContributesAndroidInjector
    internal abstract fun provideCardPreviewFragment(): CardPreviewFragment

    @ContributesAndroidInjector
    internal abstract fun provideFacePreviewFragment(): FacePreviewFragment
}