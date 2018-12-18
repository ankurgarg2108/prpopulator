package com.aurea.faster.prpopulator.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aurea.faster.prpopulator.exception.BusinessException;
import com.aurea.faster.prpopulator.processors.Processor;
import com.aurea.faster.prpopulator.processors.ProcessorFactory;
import com.aurea.faster.prpopulator.utils.ToolRunMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PRPopulatorExecutorImpl implements PRPopulatorExecutor {

	@Autowired
	private ProcessorFactory processorFactory;

	public ProcessorFactory getProcessorFactory() {
		return processorFactory;
	}

	public void setProcessorFactory(ProcessorFactory processorFactory) {
		this.processorFactory = processorFactory;
	}

	@Override
	public void execute(ToolRunMode mode) throws BusinessException {
		Processor processor = processorFactory.getProcessor(mode);
		LOGGER.info("Processor being used {}", processor.getClass().getSimpleName());
		processor.process();

	}

}
