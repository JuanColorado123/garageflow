package com.subacol.msagenda.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenCerradaV1(String eventoId, String ordenId) {
}
