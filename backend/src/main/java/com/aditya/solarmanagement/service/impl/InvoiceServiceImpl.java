package com.aditya.solarmanagement.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.aditya.solarmanagement.dto.MsedclInvoiceRequest;
import com.aditya.solarmanagement.dto.MsedclInvoiceResponse;
import com.aditya.solarmanagement.dto.InvoiceOtherChargeRequest;
import com.aditya.solarmanagement.dto.InvoiceOtherChargeResponse;
import com.aditya.solarmanagement.dto.InvoiceReferralResponse;
import com.aditya.solarmanagement.dto.InvoiceSavingsDetails;
import com.aditya.solarmanagement.dto.ReferralIncentiveReportRow;
import com.aditya.solarmanagement.dto.InvoiceSavingsDetails.TariffBand;
import com.aditya.solarmanagement.dto.InvoiceSavingsDetails.TariffBillDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aditya.solarmanagement.dto.InvoicePaymentRequest;
import com.aditya.solarmanagement.dto.InvoicePaymentResponse;
import com.aditya.solarmanagement.models.Branch;
import com.aditya.solarmanagement.models.EffectiveRate;
import com.aditya.solarmanagement.models.EffectiveRateSlab;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.models.EmployeeRole;
import com.aditya.solarmanagement.models.InvoicePaymentEntryType;
import com.aditya.solarmanagement.models.InvoicePaymentStatus;
import com.aditya.solarmanagement.models.InvoiceStatus;
import com.aditya.solarmanagement.models.MsedclChargeType;
import com.aditya.solarmanagement.models.MsedclDetail;
import com.aditya.solarmanagement.models.MsedclInvoice;
import com.aditya.solarmanagement.models.MsedclInvoicePayment;
import com.aditya.solarmanagement.models.OtherChargeReason;
import com.aditya.solarmanagement.repo.EffectiveRateRepository;
import com.aditya.solarmanagement.repo.EmployeeRepository;
import com.aditya.solarmanagement.repo.MsedclDetailRepository;
import com.aditya.solarmanagement.repo.MsedclInvoiceRepository;
import com.aditya.solarmanagement.repo.MsedclInvoicePaymentRepository;
import com.aditya.solarmanagement.repo.OtherChargeReasonRepository;
import com.aditya.solarmanagement.service.InvoiceService;
import com.aditya.solarmanagement.service.InvoiceAttachmentStorageService;
import com.aditya.solarmanagement.repo.specification.InvoiceSpecifications;

@Service
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {
	private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);
	private static final ObjectMapper JSON = new ObjectMapper();
	private final EmployeeRepository employees;
	private final MsedclDetailRepository msedclDetails;
	private final MsedclInvoiceRepository invoices;
	private final MsedclInvoicePaymentRepository payments;
	private final EffectiveRateRepository effectiveRates;
	private final OtherChargeReasonRepository chargeReasons;
	private final InvoiceAttachmentStorageService attachmentStorage;

	public InvoiceServiceImpl(EmployeeRepository employees, MsedclDetailRepository msedclDetails,
			MsedclInvoiceRepository invoices, MsedclInvoicePaymentRepository payments, EffectiveRateRepository effectiveRates,
			OtherChargeReasonRepository chargeReasons, InvoiceAttachmentStorageService attachmentStorage) {
		this.employees = employees;
		this.msedclDetails = msedclDetails;
		this.invoices = invoices;
		this.payments = payments;
		this.effectiveRates = effectiveRates;
		this.chargeReasons = chargeReasons;
		this.attachmentStorage = attachmentStorage;
	}

	@Override
	public List<MsedclInvoiceResponse> invoices(String requestingEmail, String invoiceNo, String consumerName,
			String consumerNo, InvoicePaymentStatus paymentStatus, InvoiceStatus invoiceStatus,
			LocalDate invoiceDateFrom, LocalDate invoiceDateTo) {
		if (invoiceDateFrom != null && invoiceDateTo != null && invoiceDateFrom.isAfter(invoiceDateTo)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice date start must be on or before end date");
		}
		Employee requester = employeeByEmail(requestingEmail);
		var specification = InvoiceSpecifications.byInvoiceNo(invoiceNo)
				.and(InvoiceSpecifications.byConsumerName(consumerName))
				.and(InvoiceSpecifications.byConsumerNo(consumerNo))
				.and(InvoiceSpecifications.paymentStatus(paymentStatus))
				.and(InvoiceSpecifications.byStatus(invoiceStatus))
				.and(InvoiceSpecifications.invoiceDateFrom(invoiceDateFrom))
				.and(InvoiceSpecifications.invoiceDateTo(invoiceDateTo));
		specification = switch (requester.getRole()) {
			case ADMIN -> specification;
			case USER -> specification.and(InvoiceSpecifications.byBranchId(requester.getBranch().getId()));
			case CUSTOMER -> specification.and(InvoiceSpecifications.byCustomerId(requester.getId()));
		};
		List<MsedclInvoice> results = invoices.findAll(specification, Sort.by(Sort.Direction.DESC, "id"));
		Map<Long, BigDecimal> paidTotals = paymentTotals(results);
		boolean includeReferral = requester.getRole() != EmployeeRole.CUSTOMER;
		return results.stream().map(invoice -> invoiceResponse(invoice,
				paidTotals.getOrDefault(invoice.getId(), BigDecimal.ZERO), includeReferral)).toList();
	}

	@Override
	public List<ReferralIncentiveReportRow> referralIncentiveReport(String requestingEmail) {
		Employee requester = employeeByEmail(requestingEmail);
		List<MsedclInvoice> reportInvoices = switch (requester.getRole()) {
			case ADMIN -> invoices.findAllByStatusAndReferralIsNotNullOrderByBillingDateDescIdDesc(InvoiceStatus.OPEN);
			case USER -> invoices.findAllByStatusAndBranch_IdAndReferralIsNotNullOrderByBillingDateDescIdDesc(
					InvoiceStatus.OPEN, requester.getBranch().getId());
			case CUSTOMER -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Customers cannot access referral incentive reports");
		};
		Map<ReferralMonthKey, ReferralMonthTotals> totals = new HashMap<>();
		for (MsedclInvoice invoice : reportInvoices) {
			Employee referral = invoice.getReferral();
			String referralName = referral.getFirstName() + " " + referral.getLastName();
			ReferralMonthKey key = new ReferralMonthKey(referral.getId(), referralName,
					YearMonth.from(invoice.getBillingDate()));
			ReferralMonthTotals monthTotals = totals.computeIfAbsent(key, ignored -> new ReferralMonthTotals());
			monthTotals.invoiceCount++;
			monthTotals.solarAmount = monthTotals.solarAmount.add(invoice.getSolarAmount());
			monthTotals.incentiveAmount = monthTotals.incentiveAmount.add(invoice.getIncentiveAmount());
		}
		return totals.entrySet().stream()
				.sorted(Map.Entry.<ReferralMonthKey, ReferralMonthTotals>comparingByKey(
						Comparator.comparing(ReferralMonthKey::month).reversed()
								.thenComparing(ReferralMonthKey::referralName, String.CASE_INSENSITIVE_ORDER)))
				.map(entry -> new ReferralIncentiveReportRow(entry.getKey().referralId(),
						entry.getKey().referralName(), entry.getKey().month().toString(),
						entry.getValue().invoiceCount, entry.getValue().solarAmount,
						entry.getValue().incentiveAmount))
				.toList();
	}

	@Override
	public MsedclInvoiceResponse previewInvoice(String requestingEmail, MsedclInvoiceRequest request) {
		MsedclDetail detail = invoiceDetail(request.msedclDetailId(), requestingEmail);
		return invoiceResponse(buildInvoice(detail, request));
	}

	@Override
	@Transactional
	public MsedclInvoiceResponse createInvoice(String requestingEmail, MsedclInvoiceRequest request) {
		MsedclDetail detail = invoiceDetail(request.msedclDetailId(), requestingEmail);
		if (invoices.existsByMsedclDetail_IdAndBillingDateAndStatus(detail.getId(), request.billingDate(), InvoiceStatus.OPEN)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "An invoice already exists for this billing date");
		}
		MsedclInvoice saved = invoices.saveAndFlush(buildInvoice(detail, request));
		String invoiceNo = detail.getConsumerNo() + "/" + request.invoiceDate().format(DateTimeFormatter.BASIC_ISO_DATE)
				+ "/" + saved.getId();
		saved.assignInvoiceNo(invoiceNo);
		detail.updateLastInvoiceNo(invoiceNo);
		logger.info("Created MSEDCL invoice id={} detailId={} billingDate={}", saved.getId(), detail.getId(),
				saved.getBillingDate());
		return invoiceResponse(saved);
	}

	@Override
	@Transactional
	public void cancelInvoice(String requestingEmail, Long invoiceId) {
		Employee requester = employeeByEmail(requestingEmail);
		if (requester.getRole() == EmployeeRole.CUSTOMER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot cancel invoices");
		}
		MsedclInvoice invoice = invoices.findByIdForUpdate(invoiceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		assertInvoiceAccess(requester, invoice);
		if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice is already cancelled");
		}
		invoice.cancel();
		List<MsedclInvoicePayment> invoicePayments = payments.findAllByInvoice_IdOrderByPaymentDateDescIdDesc(invoiceId);
		if (!invoicePayments.isEmpty()) {
			LocalDate cancellationDate = LocalDate.now();
			MsedclInvoice reversal = invoices.saveAndFlush(invoice.createCancellationReversal(cancellationDate));
			reversal.assignInvoiceNo(invoice.getInvoiceNo() + "/CANCEL/" + reversal.getId());
			invoices.saveAndFlush(reversal);
			invoice.getMsedclDetail().updateLastInvoiceNo(reversal.getInvoiceNo());
			int paymentReversals = 0;
			for (MsedclInvoicePayment payment : invoicePayments) {
				if (payment.getEntryType() == InvoicePaymentEntryType.PAYMENT
						&& !payments.existsByReversalOfPayment_Id(payment.getId())) {
					payments.save(payment.createReversal(cancellationDate));
					paymentReversals++;
				}
			}
			payments.flush();
			logger.info("Cancelled invoice id={} with reversal id={} paymentReversals={}", invoiceId, reversal.getId(),
					paymentReversals);
		} else {
			logger.info("Cancelled unpaid invoice id={}", invoiceId);
		}
	}

	@Override
	@Transactional
	public MsedclInvoiceResponse uploadMsebBill(String requestingEmail, Long invoiceId, MultipartFile file) {
		MsedclInvoice invoice = authorizedInvoice(invoiceId, requestingEmail);
		if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled invoices cannot be changed");
		}
		if (invoice.getMsebBillStorageName() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "An MSEB bill is already attached to this invoice");
		}
		InvoiceAttachmentStorageService.StoredInvoiceAttachment attachment = attachmentStorage.store(file);
		invoice.attachMsebBill(attachment.storageName(), attachment.originalFileName(), attachment.contentType(),
				LocalDateTime.now());
		MsedclInvoice saved = invoices.saveAndFlush(invoice);
		logger.info("Uploaded MSEB bill for invoice id={}", invoiceId);
		return invoiceResponse(saved);
	}

	@Override
	public InvoiceAttachmentDownload downloadMsebBill(String requestingEmail, Long invoiceId) {
		MsedclInvoice invoice = authorizedInvoice(invoiceId, requestingEmail);
		if (invoice.getMsebBillStorageName() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No MSEB bill is attached to this invoice");
		}
		Resource resource = attachmentStorage.load(invoice.getMsebBillStorageName());
		return new InvoiceAttachmentDownload(resource, invoice.getMsebBillFileName(), invoice.getMsebBillContentType());
	}

	@Override
	public boolean hasInvoicesForDetail(Long detailId) {
		return invoices.existsByMsedclDetail_Id(detailId);
	}

	@Override
	public boolean hasInvoicesForReferral(Long referralId) {
		return invoices.existsByReferral_Id(referralId);
	}

	@Override
	public List<InvoicePaymentResponse> paymentHistory(String requestingEmail, Long invoiceId) {
		MsedclInvoice invoice = authorizedInvoice(invoiceId, requestingEmail);
		return payments.findAllByInvoice_IdOrderByPaymentDateDescIdDesc(invoice.getId()).stream()
				.map(this::paymentResponse).toList();
	}

	@Override
	public List<InvoicePaymentResponse> allPayments(String requestingEmail) {
		Employee requester = employeeByEmail(requestingEmail);
		List<MsedclInvoicePayment> results = switch (requester.getRole()) {
			case ADMIN -> payments.findAllByOrderByPaymentDateDescIdDesc();
			case USER -> payments.findAllByInvoice_Branch_IdOrderByPaymentDateDescIdDesc(requester.getBranch().getId());
			case CUSTOMER -> payments.findAllByInvoice_MsedclDetail_Customer_IdOrderByPaymentDateDescIdDesc(requester.getId());
		};
		return results.stream().map(this::paymentResponse).toList();
	}

	@Override
	@Transactional
	public List<InvoicePaymentResponse> addPayment(String requestingEmail, Long invoiceId,
			InvoicePaymentRequest request) {
		Employee requester = employeeByEmail(requestingEmail);
		if (requester.getRole() == EmployeeRole.CUSTOMER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot record invoice payments");
		}
		MsedclInvoice invoice = invoices.findByIdForUpdate(invoiceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		assertInvoiceAccess(requester, invoice);
		if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Payments cannot be added to a cancelled invoice");
		}
		BigDecimal paidAmount = payments.totalByInvoiceId(invoiceId);
		BigDecimal balance = invoice.getInvoiceAmount().subtract(paidAmount);
		if (request.amount().compareTo(balance) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment exceeds the invoice balance");
		}
		if (request.paymentType() == com.aditya.solarmanagement.models.InvoicePaymentType.UPI
				&& (request.transactionNo() == null || request.transactionNo().isBlank())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction number is required for UPI payments");
		}
		payments.saveAndFlush(new MsedclInvoicePayment(invoice, request.paymentDate(), request.amount(),
				request.paymentType(), request.transactionNo() == null ? null : request.transactionNo().trim(),
				request.note() == null ? null : request.note().trim()));
		return payments.findAllByInvoice_IdOrderByPaymentDateDescIdDesc(invoiceId).stream()
				.map(this::paymentResponse).toList();
	}

	@Override
	@Transactional
	public void cancelPayment(String requestingEmail, Long invoiceId, Long paymentId) {
		Employee requester = employeeByEmail(requestingEmail);
		if (requester.getRole() == EmployeeRole.CUSTOMER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot cancel payments");
		}
		MsedclInvoice invoice = invoices.findByIdForUpdate(invoiceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		assertInvoiceAccess(requester, invoice);
		if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Payments on cancelled invoices cannot be changed");
		}
		MsedclInvoicePayment payment = payments.findById(paymentId)
				.filter(item -> item.getInvoice().getId().equals(invoiceId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
		if (payment.getEntryType() == InvoicePaymentEntryType.REVERSAL
				|| payments.existsByReversalOfPayment_Id(paymentId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This payment already has a reversal entry");
		}
		payments.saveAndFlush(payment.createReversal(LocalDate.now()));
		logger.info("Added reversal for payment id={} invoiceId={}", paymentId, invoiceId);
	}

	private MsedclDetail invoiceDetail(Long detailId, String requestingEmail) {
		Employee requester = employeeByEmail(requestingEmail);
		MsedclDetail detail = msedclDetails.findById(detailId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MSEDCL detail not found"));
		Employee customer = detail.getCustomer();
		boolean allowed = switch (requester.getRole()) {
			case ADMIN -> true;
			case USER -> requester.getBranch().getId().equals(customer.getBranch().getId());
			case CUSTOMER -> requester.getId().equals(customer.getId());
		};
		if (!allowed) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot create an invoice for this consumer");
		}
		return detail;
	}

	private MsedclInvoice authorizedInvoice(Long invoiceId, String requestingEmail) {
		Employee requester = employeeByEmail(requestingEmail);
		MsedclInvoice invoice = invoices.findById(invoiceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		assertInvoiceAccess(requester, invoice);
		return invoice;
	}

	private void assertInvoiceAccess(Employee requester, MsedclInvoice invoice) {
		boolean allowed = switch (requester.getRole()) {
			case ADMIN -> true;
			case USER -> requester.getBranch().getId().equals(invoice.getBranch().getId());
			case CUSTOMER -> requester.getId().equals(invoice.getMsedclDetail().getCustomer().getId());
		};
		if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this invoice");
	}

	private MsedclInvoice buildInvoice(MsedclDetail detail, MsedclInvoiceRequest request) {
		BigDecimal importConsumption = consumption(request.importCurrent(), request.importPrevious(), "Import");
		BigDecimal exportConsumption = consumption(request.exportCurrent(), request.exportPrevious(), "Export");
		BigDecimal generationConsumption = consumption(request.generationCurrent(), request.generationPrevious(),
				"Generation");
		BigDecimal availableSolarUnits = exportConsumption.add(request.previousBankUnits());
		BigDecimal solarOffsetUnits = importConsumption.min(availableSolarUnits);
		BigDecimal bankSolarUnits = availableSolarUnits.subtract(importConsumption).max(BigDecimal.ZERO);
		BigDecimal solarBillUnits = generationConsumption.add(request.previousBankUnits()).subtract(bankSolarUnits);
		ResolvedRate rate = resolveRate(detail, request.billingDate());
		BigDecimal solarAmount = solarBillUnits.multiply(rate.ratePerUnit()).setScale(2, RoundingMode.HALF_UP);
		BigDecimal msebBillAmount = BigDecimal.ZERO.setScale(2);
		if (detail.getChargeType() == MsedclChargeType.SOLAR_PLUS_MSEB_BILL_AMOUNT) {
			if (request.msebBillAmount() == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MSEB bill amount is required for this charge type");
			}
			msebBillAmount = request.msebBillAmount().setScale(2, RoundingMode.HALF_UP);
		}
		BigDecimal otherChargesAmount = BigDecimal.ZERO.setScale(2);
		Set<Long> reasonIds = new HashSet<>();
		List<ChargeLine> chargeLines = new ArrayList<>();
		for (InvoiceOtherChargeRequest charge : request.otherCharges()) {
			if (!reasonIds.add(charge.reasonId())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each other charge reason can be added only once");
			}
			OtherChargeReason reason = chargeReasons.findById(charge.reasonId()).orElseThrow(() ->
					new ResponseStatusException(HttpStatus.BAD_REQUEST, "Other charge reason not found"));
			BigDecimal amount = charge.amount().setScale(2, RoundingMode.HALF_UP);
			chargeLines.add(new ChargeLine(reason, amount));
			otherChargesAmount = otherChargesAmount.add(amount);
		}
		BigDecimal invoiceAmount = solarAmount.add(msebBillAmount).add(otherChargesAmount)
				.setScale(2, RoundingMode.HALF_UP);
		Employee referral = detail.getReferral();
		BigDecimal referralPercentage = referral == null || referral.getReferralPercentage() == null
				? BigDecimal.ZERO : referral.getReferralPercentage();
		// Incentive is based on the solar amount only, not MSEB or other charges.
		BigDecimal incentiveAmount = solarAmount.multiply(referralPercentage)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		MsedclInvoice invoice = new MsedclInvoice(detail, request.invoiceDate(), request.billingDate(),
				request.invoiceDate().plusDays(detail.getDueDays()), request.importCurrent(),
				request.importPrevious(), importConsumption, request.exportCurrent(), request.exportPrevious(),
				exportConsumption, request.generationCurrent(), request.generationPrevious(), generationConsumption,
				request.previousBankUnits(), solarOffsetUnits, bankSolarUnits, solarBillUnits, rate.ratePerUnit(), rate.source(),
				solarAmount, msebBillAmount, invoiceAmount, referral, incentiveAmount, otherChargesAmount);
		for (ChargeLine line : chargeLines) {
			invoice.addOtherCharge(line.reason(), line.amount());
		}
		EffectiveRate tariff = resolveTariff(detail, request.billingDate());
		if (tariff != null) {
			// Direct solar use is billed with no solar (all units from the grid) and counted again in the solar units billed.
			BigDecimal directSolarUnits = generationConsumption.subtract(exportConsumption).max(BigDecimal.ZERO);
			BigDecimal totalUnits = directSolarUnits.add(importConsumption);
			BigDecimal netGridUnits = importConsumption.subtract(solarOffsetUnits);
			TariffBillDetails withoutSolar = tariffBill(tariff, totalUnits);
			boolean gridAmountEntered = detail.getChargeType() == MsedclChargeType.SOLAR_PLUS_MSEB_BILL_AMOUNT;
			TariffBillDetails withSolarGrid = gridAmountEntered ? null : tariffBill(tariff, netGridUnits);
			BigDecimal gridAmount = gridAmountEntered ? msebBillAmount : withSolarGrid.totalAmount();
			BigDecimal withSolar = gridAmount.add(solarAmount).setScale(2, RoundingMode.HALF_UP);
			BigDecimal savings = withoutSolar.totalAmount().subtract(withSolar);
			InvoiceSavingsDetails details = new InvoiceSavingsDetails(directSolarUnits, importConsumption, totalUnits,
					netGridUnits, withoutSolar, withSolarGrid, gridAmount, gridAmountEntered, solarBillUnits,
					rate.ratePerUnit(), solarAmount);
			invoice.applySavings(totalUnits, withoutSolar.totalAmount(), withSolar, savings, toJson(details));
		}
		return invoice;
	}

	private String toJson(InvoiceSavingsDetails details) {
		try {
			return JSON.writeValueAsString(details);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not store the savings calculation", exception);
		}
	}

	private InvoiceSavingsDetails savingsDetails(MsedclInvoice invoice) {
		if (invoice.getSavingsBreakdown() == null) return null;
		try {
			return JSON.readValue(invoice.getSavingsBreakdown(), InvoiceSavingsDetails.class);
		} catch (JsonProcessingException exception) {
			logger.warn("Unreadable savings breakdown for invoice id={}", invoice.getId());
			return null;
		}
	}

	private BigDecimal consumption(BigDecimal current, BigDecimal previous, String readingType) {
		if (current.compareTo(previous) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					readingType + " current reading cannot be less than previous reading");
		}
		return current.subtract(previous).setScale(4, RoundingMode.HALF_UP);
	}

	private EffectiveRate resolveTariff(MsedclDetail detail, LocalDate billingDate) {
		Branch branch = detail.getCustomer().getBranch();
		List<List<EffectiveRate>> candidates = List.of(
				effectiveRates.findAllByMsedclDetail_IdOrderByStartDateDescIdDesc(detail.getId()),
				effectiveRates.findAllByBranch_IdOrderByStartDateDescIdDesc(branch.getId()),
				effectiveRates.findAllByCompany_IdOrderByStartDateDescIdDesc(branch.getCompany().getId()));
		for (List<EffectiveRate> rates : candidates) {
			EffectiveRate current = rates.stream().filter(rate -> !rate.getStartDate().isAfter(billingDate))
					.findFirst().orElse(null);
			if (current != null && !current.getSlabs().isEmpty()) return current;
		}
		return null;
	}

	// Electricity duty is applied on energy, adjustment and wheeling charges, not on the fixed charge.
	private TariffBillDetails tariffBill(EffectiveRate tariff, BigDecimal units) {
		BigDecimal billedUnits = units.max(BigDecimal.ZERO);
		BigDecimal remaining = billedUnits;
		BigDecimal from = BigDecimal.ZERO;
		BigDecimal bandTotal = BigDecimal.ZERO;
		List<TariffBand> bands = new ArrayList<>();
		for (EffectiveRateSlab slab : tariff.getSlabs()) {
			if (remaining.signum() <= 0) break;
			BigDecimal band = slab.getUpToUnits() == null ? remaining : remaining.min(slab.getUpToUnits().subtract(from));
			BigDecimal amount = band.multiply(slab.getRatePerUnit().add(slab.getAdjustmentPerUnit()))
					.setScale(2, RoundingMode.HALF_UP);
			bands.add(new TariffBand(from, slab.getUpToUnits(), band, slab.getRatePerUnit(),
					slab.getAdjustmentPerUnit(), amount));
			bandTotal = bandTotal.add(amount);
			remaining = remaining.subtract(band);
			if (slab.getUpToUnits() != null) from = slab.getUpToUnits();
		}
		BigDecimal wheeling = billedUnits.multiply(tariff.getWheelingChargePerUnit()).setScale(2, RoundingMode.HALF_UP);
		BigDecimal dutyBase = bandTotal.add(wheeling);
		BigDecimal duty = dutyBase.multiply(tariff.getElectricityDutyPercent())
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal taxOnSale = billedUnits.multiply(tariff.getTaxOnSalePaisePerUnit())
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal fixed = tariff.getFixedCharge().setScale(2, RoundingMode.HALF_UP);
		BigDecimal total = fixed.add(dutyBase).add(duty).add(taxOnSale).setScale(2, RoundingMode.HALF_UP);
		return new TariffBillDetails(billedUnits, bands, fixed, tariff.getWheelingChargePerUnit(), wheeling,
				tariff.getElectricityDutyPercent(), duty, tariff.getTaxOnSalePaisePerUnit(), taxOnSale, total);
	}

	private ResolvedRate resolveRate(MsedclDetail detail, LocalDate billingDate) {
		EffectiveRate detailRate = effectiveRates.findAllByMsedclDetail_IdOrderByStartDateDescIdDesc(detail.getId())
				.stream().filter(rate -> !rate.getStartDate().isAfter(billingDate)).findFirst().orElse(null);
		if (detailRate != null) return new ResolvedRate(detailRate.getRatePerUnit(), "DETAIL_EFFECTIVE");
		if (detail.getRatePerUnit().compareTo(BigDecimal.ZERO) > 0) {
			return new ResolvedRate(detail.getRatePerUnit(), "DETAIL_BASE");
		}
		Branch branch = detail.getCustomer().getBranch();
		EffectiveRate branchRate = effectiveRates.findAllByBranch_IdOrderByStartDateDescIdDesc(branch.getId())
				.stream().filter(rate -> !rate.getStartDate().isAfter(billingDate)).findFirst().orElse(null);
		if (branchRate != null) return new ResolvedRate(branchRate.getRatePerUnit(), "BRANCH_EFFECTIVE");
		EffectiveRate companyRate = effectiveRates.findAllByCompany_IdOrderByStartDateDescIdDesc(branch.getCompany().getId())
				.stream().filter(rate -> !rate.getStartDate().isAfter(billingDate)).findFirst().orElse(null);
		if (companyRate != null) return new ResolvedRate(companyRate.getRatePerUnit(), "COMPANY_EFFECTIVE");
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"No effective rate is configured for this billing date");
	}

	private MsedclInvoiceResponse invoiceResponse(MsedclInvoice invoice) {
		return invoiceResponse(invoice, payments.totalByInvoiceId(invoice.getId()), true);
	}

	private MsedclInvoiceResponse invoiceResponse(MsedclInvoice invoice, BigDecimal paidAmount, boolean includeReferral) {
		if (invoice.getStatus() == InvoiceStatus.CANCELLED) paidAmount = BigDecimal.ZERO;
		BigDecimal balanceAmount = invoice.getStatus() == InvoiceStatus.CANCELLED
				? BigDecimal.ZERO : invoice.getInvoiceAmount().subtract(paidAmount).max(BigDecimal.ZERO);
		List<InvoiceOtherChargeResponse> otherCharges = invoice.getOtherCharges().stream()
				.map(charge -> new InvoiceOtherChargeResponse(charge.getReason().getId(), charge.getReasonName(),
						charge.getAmount()))
				.toList();
		InvoiceReferralResponse referral = includeReferral && invoice.getReferral() != null
				? new InvoiceReferralResponse(invoice.getReferral().getId(),
						invoice.getReferral().getFirstName() + " " + invoice.getReferral().getLastName(),
						invoice.getReferralPercentage(), invoice.getIncentiveAmount())
				: null;
		return new MsedclInvoiceResponse(invoice.getId(), invoice.getId(), invoice.getInvoiceNo(), invoice.getCompany().getId(),
				invoice.getBranch().getId(), invoice.getMsedclDetail().getId(), invoice.getConsumerNo(),
				invoice.getConsumerName(), invoice.getBillingUnit(), invoice.getChargeType(), invoice.getStatus(),
				invoice.getReversalOfInvoice() == null ? null : invoice.getReversalOfInvoice().getId(),
				invoice.getOriginalInvoiceNoSnapshot(), invoice.getInvoiceDate(),
				invoice.getBillingDate(), invoice.getDueDays(), invoice.getDueDate(), invoice.getImportCurrent(), invoice.getImportPrevious(), invoice.getImportConsumption(),
				invoice.getExportCurrent(), invoice.getExportPrevious(), invoice.getExportConsumption(),
				invoice.getGenerationCurrent(), invoice.getGenerationPrevious(), invoice.getGenerationConsumption(),
				invoice.getPreviousBankUnits(), invoice.getSolarOffsetUnits(), invoice.getBankSolarUnits(),
				invoice.getSolarBillUnits(), invoice.getRatePerUnit(), invoice.getRateSource(), invoice.getSolarAmount(), invoice.getMsebBillAmount(),
				invoice.getOtherChargesAmount(), otherCharges,
				invoice.getInvoiceAmount(), paidAmount, balanceAmount, invoice.getMsebBillFileName(),
				invoice.getMsebBillUploadedAt(), referral, invoice.getTotalConsumptionUnits(),
				invoice.getWithoutSolarBillAmount(), invoice.getWithSolarBillAmount(), invoice.getConsumerSavingsAmount(),
				savingsDetails(invoice));
	}

	private Map<Long, BigDecimal> paymentTotals(List<MsedclInvoice> invoiceList) {
		if (invoiceList.isEmpty()) return Map.of();
		List<Long> invoiceIds = invoiceList.stream().map(MsedclInvoice::getId).toList();
		Map<Long, BigDecimal> totals = new HashMap<>();
		for (Object[] row : payments.totalsByInvoiceIds(invoiceIds)) {
			totals.put((Long) row[0], (BigDecimal) row[1]);
		}
		return totals;
	}

	private InvoicePaymentResponse paymentResponse(MsedclInvoicePayment payment) {
		MsedclInvoice invoice = payment.getInvoice();
		return new InvoicePaymentResponse(payment.getId(), invoice.getId(), invoice.getInvoiceNo(), invoice.getStatus(),
				invoice.getConsumerName(), invoice.getConsumerNo(), payment.getPaymentDate(), payment.getAmount(),
				payment.getPaymentType(), payment.getTransactionNo(), payment.getNote(), payment.getEntryType(),
				payment.getReversalOfPayment() == null ? null : payment.getReversalOfPayment().getId(),
				payment.getEntryType() == InvoicePaymentEntryType.PAYMENT
						&& payments.existsByReversalOfPayment_Id(payment.getId()));
	}

	private Employee employeeByEmail(String emailAddress) {
		return employees.findByEmailAddressIgnoreCase(emailAddress)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
	}

	private record ResolvedRate(BigDecimal ratePerUnit, String source) {}

	private record ReferralMonthKey(Long referralId, String referralName, YearMonth month) {}

	private static final class ReferralMonthTotals {
		private long invoiceCount;
		private BigDecimal solarAmount = BigDecimal.ZERO;
		private BigDecimal incentiveAmount = BigDecimal.ZERO;
	}

	private record ChargeLine(OtherChargeReason reason, BigDecimal amount) {}
}