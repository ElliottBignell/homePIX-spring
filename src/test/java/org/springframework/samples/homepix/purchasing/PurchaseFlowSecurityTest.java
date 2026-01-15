package org.springframework.samples.homepix.purchasing;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.samples.homepix.*;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.sales.StripePaymentService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Import(com.example.test.config.TestConfig.class)
@ActiveProfiles("test")
class PurchaseFlowSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
	StripePaymentService paymentService;

    @Test
    void anonymousUserIsRedirectedToLoginWhenBuying() throws Exception {
        mockMvc.perform(post("/cart/addToCart/9705").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(header().string("Location", containsString("/prelogin")));
    }

	@Test
	@WithMockUser(username = "alice", roles = "USER")
	void loggedInUserCanAddToCart() throws Exception {

		mockMvc.perform(post("/cart/addToCart/9705")
				.with(csrf())
				.param("pictureID", "9705")
				.param("redirectTo", "/cart/addToCart/9705")
				.param("tier", "THUMBNAIL")
		)
		.andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/cart")));
	}

	@Test
	void staleCsrfTokenRedirectsGracefully() throws Exception {
		mockMvc.perform(post("/cart/addToCart/9705")
				// intentionally NO csrf()
				.param("pictureID", "9705")
				.param("tier", "THUMBNAIL")
			)
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Location", containsString("/prelogin")));
	}

	@Test
	void anonymousUserWithStaleCsrfIsRedirectedToLogin() throws Exception {
		mockMvc.perform(post("/cart/addToCart/9705"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(header().string("Location", containsString("/prelogin")));
	}

	@Test
	@WithMockUser(username = "alice", roles = "USER")
	void loggedInUserWithStaleCsrfIsRedirectedToCart() throws Exception {
		mockMvc.perform(post("/cart/addToCart/9705")
				.param("pictureID", "9705")
				.param("tier", "THUMBNAIL")
		)
		.andExpect(status().is3xxRedirection())
		.andExpect(header().string("Location", containsString("/checkout")));
	}

	@Test
	@WithMockUser(username = "alice", roles = "USER")
	void loggedInUserHitsBuy() throws Exception {

		mockMvc.perform(post("/cart/buy")
				.with(csrf())
			)
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Location", containsString("/cart")));
	}

	@Test
    @WithMockUser(username = "alice")
    void checkoutRedirectsToStripeOnSuccess() throws Exception {

        given(paymentService.createCheckoutSession("alice", "fred@fred.ch"))
            .willReturn("https://checkout.stripe.com/test-session");

        mockMvc.perform(post("/payments/stripe/session").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(header().string(
                   "Location",
                   containsString("checkout.stripe.com")
               ));
    }
}
