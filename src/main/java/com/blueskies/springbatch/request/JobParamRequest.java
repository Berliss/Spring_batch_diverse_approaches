package com.blueskies.springbatch.request;

public record JobParamRequest(
        String paramKey,
        String paramValue
) {}
