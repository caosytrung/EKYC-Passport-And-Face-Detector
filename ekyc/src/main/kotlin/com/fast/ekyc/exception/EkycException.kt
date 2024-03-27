package com.fast.ekyc.exception


sealed class InvalidConfigException(
    serverMessage: String,
) : Exception(serverMessage) {
    object MissingFrontCardIdException : InvalidConfigException("Missing Front Card Id")
    object MissingUserIdType : InvalidConfigException("Missing User Id Type")
    object MissingUserId : InvalidConfigException("Missing User Id")
    object WrongFlowType :
        InvalidConfigException("PASSPORT and BLX do not work on the Id Card Back Flow")

    object FaceFlowError :
        InvalidConfigException("Only one of these values is_face_search/is_face_save/is_face_update can be true")

    object InvalidCardType : InvalidConfigException("Invalid Card Type")

    object NfcNotAvailable : InvalidConfigException("Nfc is not available in this device")
    object MissingNfcUiConfig : InvalidConfigException("Missing NFC UI Config")
    object InvalidNfcUiConfig : InvalidConfigException("Invalid NFC UI Config")

}
