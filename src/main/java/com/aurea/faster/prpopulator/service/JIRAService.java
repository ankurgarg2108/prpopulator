package com.aurea.faster.prpopulator.service;

import java.util.List;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.exception.BusinessException;

public interface JIRAService {

	List<JIRATicketInfo> getAllTicketsWithSpecifiedJQL(String jql) throws BusinessException;

	void populateMergedPRInfo(JIRATicketInfo ticket) throws BusinessException;

}
