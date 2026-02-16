package org.delcom.helpers

import org.delcom.data.AppException

class ValidatorHelper(private val data: Map<String, Any?>) {
    private val errors = mutableListOf<String>()

    fun addError(field: String, error: String) {
        errors.add("$field: $error")
    }

    fun required(field: String, message: String? = null) {
        val value = data[field]
        if (value == null || (value is String && value.isBlank())) {
            addError(field, message ?: "Is required")
        }
    }

    fun validate() {
        if (errors.isNotEmpty()) {
            throw AppException(400, errors.joinToString("|"))
        }
    }
}