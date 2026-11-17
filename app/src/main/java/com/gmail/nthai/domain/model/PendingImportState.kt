package com.gmail.nthai.domain.model

data class PendingImportState(
    val fileBytes: ByteArray,
    val headers: List<String>,
    val selectedHeader: String,
    val selectedImportMode: ImportMode = ImportMode.REPLACE_ALL
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PendingImportState

        if (!fileBytes.contentEquals(other.fileBytes)) return false
        if (headers != other.headers) return false
        if (selectedHeader != other.selectedHeader) return false
        if (selectedImportMode != other.selectedImportMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileBytes.contentHashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + selectedHeader.hashCode()
        result = 31 * result + selectedImportMode.hashCode()
        return result
    }
}
