package com.aditya.solarmanagement.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aditya.solarmanagement.dto.OtherChargeReasonRequest;
import com.aditya.solarmanagement.dto.OtherChargeReasonResponse;
import com.aditya.solarmanagement.models.OtherChargeReason;
import com.aditya.solarmanagement.repo.MsedclInvoiceOtherChargeRepository;
import com.aditya.solarmanagement.repo.OtherChargeReasonRepository;

@Service
@Transactional(readOnly = true)
public class OtherChargeReasonService {
	private final OtherChargeReasonRepository reasons;
	private final MsedclInvoiceOtherChargeRepository usedCharges;

	public OtherChargeReasonService(OtherChargeReasonRepository reasons, MsedclInvoiceOtherChargeRepository usedCharges) {
		this.reasons = reasons;
		this.usedCharges = usedCharges;
	}

	public List<OtherChargeReasonResponse> list() {
		return reasons.findAllByOrderByNameAsc().stream().map(this::response).toList();
	}

	@Transactional
	public OtherChargeReasonResponse add(OtherChargeReasonRequest request) {
		String name = request.name().trim();
		if (reasons.existsByNameIgnoreCase(name)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This reason already exists");
		}
		return response(reasons.save(new OtherChargeReason(name)));
	}

	@Transactional
	public OtherChargeReasonResponse rename(Long id, OtherChargeReasonRequest request) {
		OtherChargeReason reason = reasons.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reason not found"));
		String name = request.name().trim();
		if (reasons.existsByNameIgnoreCaseAndIdNot(name, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This reason already exists");
		}
		reason.rename(name);
		return response(reason);
	}

	@Transactional
	public void delete(Long id) {
		OtherChargeReason reason = reasons.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reason not found"));
		if (usedCharges.existsByReason_Id(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Reasons used on invoices cannot be deleted");
		}
		reasons.delete(reason);
	}

	private OtherChargeReasonResponse response(OtherChargeReason reason) {
		return new OtherChargeReasonResponse(reason.getId(), reason.getName());
	}
}
