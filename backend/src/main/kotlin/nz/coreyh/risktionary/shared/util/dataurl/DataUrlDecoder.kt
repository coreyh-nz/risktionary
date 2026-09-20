package nz.coreyh.risktionary.shared.util.dataurl

import org.springframework.util.MimeTypeUtils
import kotlin.io.encoding.Base64

object DataUrlDecoder {
    fun decode(dataUrl: String): DecodedDataUrl {
        val mimeType =
            MimeTypeUtils.parseMimeType(
                dataUrl
                    .substringAfter("data:")
                    .substringBefore(";base64,"),
            )
        val bytes = Base64.decode(dataUrl.substringAfter("base64,"))
        return DecodedDataUrl(bytes, mimeType)
    }
}
