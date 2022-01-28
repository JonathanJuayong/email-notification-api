package ffufm.shelly.api.external

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service


@Service
class EmailService(private val env: Environment) {
    private val apiKey = env.getProperty("mailjetkey")
    private val apiSecret = env.getProperty("mailjetsecret")
    private val senderEmail = env.getProperty("mailjetemail")

    fun sendEmail(
        emailRequest: MailjetRequest
    ) {
        val clientOptions = ClientOptions.builder()
            .apiKey(apiKey)
            .apiSecretKey(apiSecret)
            .build()

        val client = MailjetClient(clientOptions)
        val response = client.post(emailRequest)
    }

    fun emailRequest(
        senderName: String,
        recipientEmail: String,
        recipientName: String,
        emailSubject: String,
        emailTextPart: String,
        emailBody: String
    ) = MailjetRequest(Emailv31.resource)
        .property(
            Emailv31.MESSAGES, JSONArray()
                .put(
                    JSONObject()
                        .put(
                            Emailv31.Message.FROM, JSONObject()
                                .put("Email", senderEmail)
                                .put("Name", senderName)
                        )
                        .put(
                            Emailv31.Message.TO, JSONArray()
                                .put(
                                    JSONObject()
                                        .put("Email", recipientEmail)
                                        .put("Name", recipientName)
                                )
                        )
                        .put(Emailv31.Message.SUBJECT, emailSubject)
                        .put(Emailv31.Message.TEXTPART, emailTextPart)
                        .put(
                            Emailv31.Message.HTMLPART,
                            emailBody
                        )
                )
        )
}