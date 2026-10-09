package com.aditya.solarmanagement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.aditya.solarmanagement.dto.InvoiceSavingsDetails.TariffBand;
import com.aditya.solarmanagement.dto.InvoiceSavingsDetails.TariffBillDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

class InvoiceSavingsDetailsTest {
	@Test
	void roundTripsThroughJson() throws Exception {
		TariffBillDetails bill = new TariffBillDetails(new BigDecimal("446.0000"),
				List.of(new TariffBand(BigDecimal.ZERO, new BigDecimal("100"), new BigDecimal("100"),
						new BigDecimal("3.96"), new BigDecimal("0.15"), new BigDecimal("396.00"),
						new BigDecimal("15.00")),
						new TariffBand(new BigDecimal("100"), null, new BigDecimal("346"),
								new BigDecimal("10.80"), new BigDecimal("0.30"), new BigDecimal("3736.80"),
								new BigDecimal("103.80"))),
				new BigDecimal("140.00"), new BigDecimal("1.6000"), new BigDecimal("713.60"),
				new BigDecimal("118.80"),
				new BigDecimal("16.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
				new BigDecimal("6632.96"));
		InvoiceSavingsDetails details = new InvoiceSavingsDetails(new BigDecimal("120.0000"),
				new BigDecimal("326.0000"), new BigDecimal("446.0000"), BigDecimal.ZERO, bill, null,
				new BigDecimal("140.00"), true, new BigDecimal("446.0000"), new BigDecimal("5.0000"),
				new BigDecimal("2230.00"), new BigDecimal("16.00"), new BigDecimal("356.80"));

		ObjectMapper mapper = new ObjectMapper();
		InvoiceSavingsDetails copy = mapper.readValue(mapper.writeValueAsString(details), InvoiceSavingsDetails.class);

		assertEquals(0, details.totalUnits().compareTo(copy.totalUnits()));
		assertEquals(2, copy.withoutSolar().bands().size());
		assertNull(copy.withoutSolar().bands().get(1).upToUnits());
		assertEquals(0, new BigDecimal("15.00").compareTo(copy.withoutSolar().bands().get(0).adjustmentAmount()));
		assertEquals(0, new BigDecimal("118.80").compareTo(copy.withoutSolar().fuelAdjustmentAmount()));
		assertEquals(0, new BigDecimal("6632.96").compareTo(copy.withoutSolar().totalAmount()));
		assertNull(copy.withSolarGrid());
	}
}
