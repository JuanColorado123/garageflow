package com.subacol.msordenes.adapter.in.web;

import jakarta.validation.constraints.Size;

public record ReabrirOrdenRequest(
    @Size(max = 500, message = "admite hasta 500 caracteres")
    String motivo
) {
}
