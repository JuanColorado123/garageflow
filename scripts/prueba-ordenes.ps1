$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.Timeout = [TimeSpan]::FromSeconds(15)

$failureCount = 0
$baseUrl = 'http://localhost:8081/api/ordenes'

function Invoke-Api {
    param([string]$Method, [string]$Url, [hashtable]$Body, [string]$Usuario = 'asesor.ana', [string]$Rol = 'ASESOR')
    
    $request = New-Object System.Net.Http.HttpRequestMessage
    $request.Method = $Method
    $request.RequestUri = $Url
    
    if ($Body) {
        $json = $Body | ConvertTo-Json -Depth 5
        $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, 'application/json')
    }
    
    if ($Usuario) {
        $request.Headers.TryAddWithoutValidation('X-Usuario', $Usuario) | Out-Null
    }
    
    if ($Rol) {
        $request.Headers.TryAddWithoutValidation('X-Rol', $Rol) | Out-Null
    }
    
    try {
        $response = $httpClient.SendAsync($request).Result
        $content = $response.Content.ReadAsStringAsync().Result
        $cuerpo = $null
        if ($content) {
            try {
                $cuerpo = $content | ConvertFrom-Json
            } catch {
                $cuerpo = $content
            }
        }
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Body = $cuerpo }
    } catch {
        return [pscustomobject]@{ Status = 0; Body = $null }
    }
}

function Check {
    param([string]$Criterio, [bool]$Condicion, [string]$Detalle)
    
    if ($Condicion) {
        Write-Host "PASS $Criterio $Detalle"
    } else {
        Write-Host "FAIL $Criterio $Detalle"
        $script:failureCount++
    }
}

Write-Host "01 REQ-1.1: registrando orden con datos validos..."
$newOrder = @{
    clienteNombre = 'Carlos Ruiz'
    clienteTelefono = '+573001112233'
    vehiculoPlaca = 'ABC123'
    vehiculoDescripcion = 'Mazda 3 2019'
    aseguradora = ''
    trabajo = 'Cambio de pastillas de freno'
}
$resp = Invoke-Api -Method POST -Url $baseUrl -Body $newOrder -Usuario 'asesor.ana'
Check "01 REQ-1.1 Registrar orden status 201" ($resp.Status -eq 201) "creation successful"
Check "01 REQ-1.1 Estado REGISTRADA" ($resp.Body.estado -eq 'REGISTRADA') "initial state"
Check "01 REQ-1.1 Origen ASESOR" ($resp.Body.origen -eq 'ASESOR') "advisor origin"
Check "01 REQ-1.1 SiguienteEstado EN_DIAGNOSTICO" ($resp.Body.siguienteEstado -eq 'EN_DIAGNOSTICO') "next state"
Check "01 REQ-1.1 Acciones AVANZAR" ($resp.Body.accionesPermitidas -contains 'AVANZAR' -and $resp.Body.accionesPermitidas.Count -eq 1) "only advance action"
$ordenId = $resp.Body.id

Write-Host "01 REQ-1.3: verificando que aseguradora vacia queda null..."
Check "01 REQ-1.3 Aseguradora null" ($null -eq $resp.Body.aseguradora) "empty insurance becomes null"

Write-Host "01 REQ-1.2: registrando orden sin trabajo..."
$noWorkOrder = @{
    clienteNombre = 'Test'
    clienteTelefono = '+57300'
    vehiculoPlaca = 'XYZ999'
    trabajo = ''
}
$resp = Invoke-Api -Method POST -Url $baseUrl -Body $noWorkOrder -Usuario 'asesor.ana'
Check "01 REQ-1.2 Sin trabajo status 400" ($resp.Status -eq 400) "validation error"
Check "01 REQ-1.2 Codigo DATOS_INVALIDOS" ($resp.Body.codigo -eq 'DATOS_INVALIDOS') "error code"
Check "01 REQ-1.2 Error en trabajo" ($resp.Body.errores.trabajo -ne $null) "trabajo error present"

Write-Host "01 REQ-4.1: registrando sin X-Usuario..."
$resp = Invoke-Api -Method POST -Url $baseUrl -Body $newOrder -Usuario $null
Check "01 REQ-4.1 Sin usuario status 400" ($resp.Status -eq 400) "missing header error"
Check "01 REQ-4.1 Codigo USUARIO_REQUERIDO" ($resp.Body.codigo -eq 'USUARIO_REQUERIDO') "error code"

Write-Host "01 REQ-5.1: listando ordenes..."
$resp = Invoke-Api -Method GET -Url $baseUrl
Check "01 REQ-5.1 List status 200" ($resp.Status -eq 200) "list retrieved"
Check "01 REQ-5.1 Primera orden es la nueva" ($resp.Body[0].id -eq $ordenId) "newest order is first"

Write-Host "01 REQ-2.1, REQ-5.2: avanzando orden hasta CERRADA..."
$estados = @('EN_DIAGNOSTICO', 'EN_REPARACION', 'REPARADA', 'CERRADA')
$estadoActual = 'REGISTRADA'

foreach ($estadoSiguiente in $estados) {
    $resp = Invoke-Api -Method POST -Url "$baseUrl/$ordenId/avanzar" -Usuario 'asesor.ana'
    Check "01 REQ-2.1 Avanzar status 200" ($resp.Status -eq 200) "advance step $estadoActual -> $estadoSiguiente"
    Check "01 REQ-5.2 Nuevo estado" ($resp.Body.estado -eq $estadoSiguiente) "state is $estadoSiguiente"
    
    if ($estadoSiguiente -ne 'CERRADA') {
        Check "01 REQ-5.2 SiguienteEstado presente" ($resp.Body.siguienteEstado -ne $null) "next state present"
        Check "01 REQ-5.2 Acciones AVANZAR" ($resp.Body.accionesPermitidas -contains 'AVANZAR') "AVANZAR action"
    } else {
        Check "01 REQ-5.2 SiguienteEstado null en CERRADA" ($null -eq $resp.Body.siguienteEstado) "no next state"
        Check "01 REQ-5.2 Acciones REABRIR en CERRADA" ($resp.Body.accionesPermitidas -contains 'REABRIR') "REABRIR action"
    }
    
    $estadoActual = $estadoSiguiente
}

Write-Host "01 REQ-2.2: avanzando orden CERRADA (debe rechazar)..."
$resp = Invoke-Api -Method POST -Url "$baseUrl/$ordenId/avanzar" -Usuario 'asesor.ana'
Check "01 REQ-2.2 Avanzar CERRADA status 409" ($resp.Status -eq 409) "transition invalid"
Check "01 REQ-2.2 Codigo TRANSICION_INVALIDA" ($resp.Body.codigo -eq 'TRANSICION_INVALIDA') "error code"

Write-Host "01 REQ-3.1: reabriendo orden CERRADA..."
$reabrirRequest = @{ motivo = 'Falta pieza importante' }
$resp = Invoke-Api -Method POST -Url "$baseUrl/$ordenId/reabrir" -Body $reabrirRequest -Usuario 'asesor.ana'
Check "01 REQ-3.1 Reabrir status 200" ($resp.Status -eq 200) "reopen successful"
Check "01 REQ-3.1 Estado EN_REPARACION" ($resp.Body.estado -eq 'EN_REPARACION') "reopened state"

Write-Host "01 REQ-3.3: reabriendo orden EN_REPARACION (debe rechazar)..."
$resp = Invoke-Api -Method POST -Url "$baseUrl/$ordenId/reabrir" -Body $reabrirRequest -Usuario 'asesor.ana'
Check "01 REQ-3.3 Reabrir EN_REPARACION status 409" ($resp.Status -eq 409) "transition invalid"

Write-Host "01 REQ-3.2: reabriendo sin motivo..."
$resp = Invoke-Api -Method POST -Url "$baseUrl/$ordenId/reabrir" -Body @{} -Usuario 'asesor.ana'
Check "01 REQ-3.2 Sin motivo status 400" ($resp.Status -eq 400) "validation error"
Check "01 REQ-3.2 Codigo MOTIVO_REQUERIDO" ($resp.Body.codigo -eq 'MOTIVO_REQUERIDO') "error code"

Write-Host "01 REQ-2.3: GET orden inexistente..."
$resp = Invoke-Api -Method GET -Url "$baseUrl/inexistente-id" -Usuario 'asesor.ana'
Check "01 REQ-2.3 GET inexistente status 404" ($resp.Status -eq 404) "not found"
Check "01 REQ-2.3 Codigo ORDEN_NO_ENCONTRADA" ($resp.Body.codigo -eq 'ORDEN_NO_ENCONTRADA') "error code"

Write-Host ""
if ($failureCount -eq 0) {
    Write-Host 'RESULTADO: TODOS PASS'
    exit 0
}
Write-Host "RESULTADO: $failureCount FAIL"
exit 1
