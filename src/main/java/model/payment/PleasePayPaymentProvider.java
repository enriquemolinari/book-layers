package model.payment;

import java.time.YearMonth;

import model.api.CreditCardPaymentProvider;

public class PleasePayPaymentProvider implements CreditCardPaymentProvider {

	@Override
	public void pay(String creditCardNumber, YearMonth expire,
			String securityCode, float totalAmount) {
		// always succeed
	}

}
