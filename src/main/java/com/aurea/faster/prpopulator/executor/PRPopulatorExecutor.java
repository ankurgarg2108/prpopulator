package com.aurea.faster.prpopulator.executor;

import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.utils.ToolRunMode;

public interface PRPopulatorExecutor {
	public void execute(ToolRunMode mode) throws BusinessException;
}
