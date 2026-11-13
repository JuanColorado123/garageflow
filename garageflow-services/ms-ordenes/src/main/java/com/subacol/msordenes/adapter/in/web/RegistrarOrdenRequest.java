package com.subacol.msordenes.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarOrdenRequest(
    @NotBlank(message = "es obligatorio")
    @Size(max = 120, message = "admite hasta 120 caracteres")
    String clienteNombre,

    @NotBlank(message = "es obligatorio")
    @Size(max = 30, message = "admite hasta 30 caracteres")
    String clienteTelefono,

    @NotBlank(message = "es obligatorio")
    @Size(max = 15, message = "admite hasta 15 caracteres")
    String vehiculoPlaca,

    @Size(max = 120, message = "admite hasta 120 caracteres")
    String vehiculoDescripcion,

    @Size(max = 120, message = "admite hasta 120 caracteres")
    String aseguradora,

    @NotBlank(message = "es obligatorio")
    @Size(max = 1000, message = "admite hasta 1000 caracteres")
    String trabajo
) {
}
