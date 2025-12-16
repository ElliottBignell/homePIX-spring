package org.springframework.samples.homepix.portfolio.sales;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.sales.EmailService;
import org.springframework.stereotype.Service;

@Service
public class PaymentWorkflowService {

	@Autowired
    private final EmailService emailService;

    public PaymentWorkflowService(EmailService emailService) {
        this.emailService = emailService;
    }

	public void checkMail() {
		System.out.println("MAIL_USERNAME = " + System.getenv("MAIL_USERNAME"));
	}

    public void handleSuccessfulPayment(String buyerEmail,
                                        String productDescription,
                                        String downloadUrl) {

        // 1) Notify you
        emailService.sendOwnerPurchaseNotification(buyerEmail, productDescription, downloadUrl);

        // 2) Send download link to the buyer
        emailService.sendBuyerDownloadLink(buyerEmail, productDescription, downloadUrl);
    }
}
