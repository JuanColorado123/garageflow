package com.subacol.msagenda.adapter.out.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenRemotaV1(
        String id, String estado, String clienteNombre, String clienteTelefono) {
}
