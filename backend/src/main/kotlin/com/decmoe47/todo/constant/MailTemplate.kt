package com.decmoe47.todo.constant

object MailTemplate {
    const val VERIFICATION_CODE_SUBJECT = "[Todo APP] Your verification code"
    val VERIFICATION_CODE_BODY = """
            |Your verification code is:
            |
            |{code}
            |
            |The verification code is valid for 5 minutes.
            |
            """.trimMargin()
    const val AT_MESSAGE_SUBJECT = "有人@了你"
    const val DIRECT_MESSAGE_SUBJECT = "你有一条新的私信"
}