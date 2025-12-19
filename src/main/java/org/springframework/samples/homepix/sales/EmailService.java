package org.springframework.samples.homepix.sales;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Value("${homepix.owner.email:owner@homepix.ch}")
    private String ownerEmail;

	@Value("${homepix.url}")
	private String homePIXURL;

	public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

	@PostConstruct
	public void testInit() {
		System.out.println("MailSender = " + mailSender);
	}

	/**
     * Notify site owner about a purchase.
     */
    public void sendOwnerPurchaseNotification(String buyerEmail,
                                              String productDescription,
                                              String downloadUrl) {
        String subject = "[homePIX] New purchase";
        String text = """
                A new purchase has been completed.

                Buyer: %s
                Item: %s

                Download URL for buyer:
                %s%s
                """.formatted(buyerEmail, productDescription, homePIXURL, downloadUrl);

        sendEmail(ownerEmail, subject, text);
    }

    /**
     * Send the download link to the buyer.
     */
    public void sendBuyerDownloadLink(String buyerEmail,
                                      String productDescription,
                                      String downloadUrl) {
        String subject = "[homePIX] Your download is ready";
        String text = """
                Hi,

                Thank you for your purchase from homePIX.

                Item: %s

                You can download your photos here:
                %s%s

                This link may expire after a period of time, so please download and back up your files.

                Best regards,
                homePIX
                """.formatted(productDescription, homePIXURL, downloadUrl);

        sendEmail(buyerEmail, subject, text);
    }

    /**
     * Low-level helper.
     */
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);

        mailSender.send(msg);
    }
}
