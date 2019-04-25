package com.glookast.api.capture;

import com.glookast.commons.capture.ApiError;

public class ApiException extends Exception
{
    private final ApiError apiError;

    public ApiException(ApiError apiError)
    {
        super(apiError.getStatus() + " " + apiError.getError() + (apiError.getMessage() != null && !apiError.getMessage().isEmpty() ? " - " + apiError.getMessage() : ""));
        this.apiError = apiError;
    }

    public ApiError getApiError()
    {
        return apiError;
    }
}
