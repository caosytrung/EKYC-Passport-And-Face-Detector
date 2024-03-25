package com.fast.ekyc.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class KycNfcData(
    val dataVerifyObject: String?,
    val nfcPortrait: String?,
    val identityData: IdentityData? = null,
) : Parcelable {

    @Parcelize
    data class IdentityData(
        val mrz: String? = null,
        val idNumber: String? = null,
        val previousNumber: String? = null,
        val name: String? = null,
        val dob: String? = null,
        val gender: String? = null,
        val country: String? = null,
        val ethnic: String? = null,
        val religion: String? = null,
        val bornPlace: String? = null,
        val livePlace: String? = null,
        val feature: String? = null,
        val issueDate: String? = null,
        val expireDate: String? = null,
        val relativeNames: @RawValue RelativeName? = null,
        val com: String? = null,
        val sod: String? = null,
    ) : Parcelable

    @Parcelize
    data class RelativeName(
        val father: String? = null,
        val mother: String? = null,
        val spouse: String? = null,
    ): Parcelable
}

data class DataVerityObject(
    val raw: Raw? = null
)

data class Raw(
    val com: String? = null,
    val sod: String? = null,
    val dg1: String? = null,
    val dg2: String? = null,
    val dg13: String? = null,
    val dg14: String? = null,
    val dg15: String? = null,
)
