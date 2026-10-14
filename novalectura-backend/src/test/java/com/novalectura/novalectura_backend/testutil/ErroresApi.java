package com.novalectura.novalectura_backend.testutil;

import com.novalectura.novalectura_backend.exception.ApiException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public final class ErroresApi {
    private ErroresApi() {
    }

    public static void assertApi(HttpStatus esperado, ThrowingCallable accion) {
        Throwable error = catchThrowable(accion);
        assertThat(error).as("se esperaba ApiException %s", esperado).isInstanceOf(ApiException.class);
        assertThat(((ApiException) error).getStatus()).isEqualTo(esperado);
    }
}
