package com.example.project1732.Helper

object ValidationUtils {
    @JvmStatic
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    @JvmStatic
    fun isValidCPF(cpf: String): Boolean {
        val cpfClean = cpf.replace(".", "").replace("-", "")
        if (cpfClean.length != 11) return false
        if (cpfClean.all { it == cpfClean[0] }) return false

        val dig1 = cpfClean.substring(0, 9).mapIndexed { index, c ->
            c.toString().toInt() * (10 - index)
        }.sum().let {
            val result = 11 - (it % 11)
            if (result >= 10) 0 else result
        }

        val dig2 = cpfClean.substring(0, 10).mapIndexed { index, c ->
            c.toString().toInt() * (11 - index)
        }.sum().let {
            val result = 11 - (it % 11)
            if (result >= 10) 0 else result
        }

        return cpfClean.endsWith("$dig1$dig2")
    }

    @JvmStatic
    fun isValidPhoneNumber(phone: String): Boolean {
        val phoneClean = phone.replace("[^\\d]".toRegex(), "")
        return phoneClean.length == 10 || phoneClean.length == 11
    }

    @JvmStatic
    fun isValidAddress(address: String): Boolean {
        return address.isNotBlank() && address.length >= 5
    }
}


