package com.subacol.msordenes.domain;

public record DatosOrden(
    String clienteNombre,
    String clienteTelefono,
    String vehiculoPlaca,
    String vehiculoDescripcion,
    String aseguradora,
    String trabajo
) {
    public DatosOrden {
        clienteNombre = trim(clienteNombre);
        clienteTelefono = trim(clienteTelefono);
        vehiculoPlaca = trim(vehiculoPlaca);
        vehiculoDescripcion = trimOrNull(vehiculoDescripcion);
        aseguradora = trimOrNull(aseguradora);
        trabajo = trim(trabajo);

        if (isBlank(clienteNombre)) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "DATOS_INVALIDOS",
                "Falta el dato obligatorio clienteNombre"
            );
        }
        if (isBlank(clienteTelefono)) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "DATOS_INVALIDOS",
                "Falta el dato obligatorio clienteTelefono"
            );
        }
        if (isBlank(vehiculoPlaca)) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "DATOS_INVALIDOS",
                "Falta el dato obligatorio vehiculoPlaca"
            );
        }
        if (isBlank(trabajo)) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "DATOS_INVALIDOS",
                "Falta el dato obligatorio trabajo"
            );
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }
}
