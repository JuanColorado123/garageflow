$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.Timeout = [TimeSpan]::FromSeconds(15)

$failureCount = 0

function Invoke-Api {
    param([string]$Method, [string]$Url, [hashtable]$Body, [string]$Usuario = 'asesor.ana', [string]$Rol = '')
    
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

function Esperar {
    param([scriptblock]$Condicion, [int]$Segundos)
    
    $inicio = [DateTime]::UtcNow
    while ($true) {
        if (& $Condicion) {
            return $true
        }
        if (([DateTime]::UtcNow - $inicio).TotalSeconds -gt $Segundos) {
            return $false
        }
        Start-Sleep -Seconds 1
    }
}

Write-Host "07 REQ-1.3: verificando rechazo sin X-Rol..."
$resp = Invoke-Api -Method GET -Url 'http://localhost:8081/api/ordenes' -Usuario 'asesor.ana' -Rol ''
Check "07 REQ-1.3 GET ordenes sin rol" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

$resp = Invoke-Api -Method GET -Url 'http://localhost:8081/api/ordenes' -Usuario 'asesor.ana' -Rol 'JEFE'
Check "07 REQ-1.3 GET ordenes con rol JEFE" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

$resp = Invoke-Api -Method GET -Url 'http://localhost:8082/api/agenda/asesores' -Usuario 'asesor.ana' -Rol ''
Check "07 REQ-1.3 GET asesores sin rol" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

$resp = Invoke-Api -Method GET -Url 'http://localhost:8083/api/auditoria/historial' -Usuario 'asesor.ana' -Rol ''
Check "07 REQ-1.3 GET historial sin rol" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

Write-Host "07 REQ-2.1: setup ASESOR: creando orden..."
$newOrder = @{
    clienteNombre = 'Rosa Diaz'
    clienteTelefono = '+573005556677'
    vehiculoPlaca = 'ROL001'
    vehiculoDescripcion = 'Prueba roles'
    aseguradora = ''
    trabajo = 'Prueba de roles'
}
$resp = Invoke-Api -Method POST -Url 'http://localhost:8081/api/ordenes' -Body $newOrder -Usuario 'asesor.ana' -Rol 'ASESOR'
Check "07 REQ-2.1 POST orden ASESOR" ($resp.Status -eq 201) "order created"
$ordenId = $resp.Body.id

Write-Host "07 REQ-2.1: encontrando franja disponible para asesor 1..."
$franja = $null
$hoy = [DateTime]::Now
for ($diaOffset = 1; $diaOffset -le 30; $diaOffset++) {
    $fecha = $hoy.AddDays($diaOffset).DayOfWeek
    if ($fecha -eq [System.DayOfWeek]::Sunday) {
        continue
    }
    
    $fechaStr = $hoy.AddDays($diaOffset).ToString('yyyy-MM-dd')
    $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/disponibilidad?fecha=$fechaStr" -Usuario 'asesor.ana' -Rol 'ASESOR'
    
    if ($resp.Status -eq 200 -and $resp.Body.Count -gt 0) {
        foreach ($slot in $resp.Body) {
            if ($slot.asesorId -eq 1 -and $slot.libres -eq $slot.cupo) {
                $franja = $slot
                break
            }
        }
        if ($franja) { break }
    }
}

Check "07 REQ-2.1 Franja encontrada" ($null -ne $franja) "free slot found"

if ($franja) {
    Write-Host "07 REQ-2.1: agendando cita..."
    $citaRequest = @{
        ordenId = $ordenId
        asesorId = 1
        tipo = 'TRAER'
        franjaInicio = $franja.franjaInicio
    }
    $resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaRequest -Usuario 'asesor.ana' -Rol 'ASESOR'
    Check "07 REQ-2.1 POST cita ASESOR" ($resp.Status -eq 201) "appointment created"
    $citaId = $resp.Body.id
}

Write-Host "07 REQ-3.1: verificando acceso AUDITOR a GET..."
$resp = Invoke-Api -Method GET -Url 'http://localhost:8081/api/ordenes' -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.1 GET ordenes AUDITOR" ($resp.Status -eq 200) "200 OK"

$resp = Invoke-Api -Method GET -Url "http://localhost:8081/api/ordenes/$ordenId" -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.1 GET orden {id} AUDITOR" ($resp.Status -eq 200) "200 OK"

$resp = Invoke-Api -Method GET -Url 'http://localhost:8082/api/agenda/asesores' -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.1 GET asesores AUDITOR" ($resp.Status -eq 200) "200 OK"

$resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId" -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.1 GET citas AUDITOR" ($resp.Status -eq 200) "200 OK"

$resp = Invoke-Api -Method GET -Url 'http://localhost:8083/api/auditoria/historial' -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.1 GET historial AUDITOR" ($resp.Status -eq 200) "200 OK"

$resp = Invoke-Api -Method GET -Url "http://localhost:8083/api/auditoria/historial?ordenId=$ordenId" -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.1 GET historial {id} AUDITOR" ($resp.Status -eq 200) "200 OK"

Write-Host "07 REQ-3.3: verificando accionesPermitidas..."
$resp = Invoke-Api -Method GET -Url "http://localhost:8081/api/ordenes/$ordenId" -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.3 Orden AUDITOR accionesPermitidas vacio" ($resp.Body.accionesPermitidas.Count -eq 0) "empty actions"

$resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId" -Usuario 'auditor.marta' -Rol 'AUDITOR'
if ($resp.Body.Count -gt 0) {
    Check "07 REQ-3.3 Cita AUDITOR accionesPermitidas vacio" ($resp.Body[0].accionesPermitidas.Count -eq 0) "empty actions"
}

$resp = Invoke-Api -Method GET -Url "http://localhost:8081/api/ordenes/$ordenId" -Usuario 'asesor.ana' -Rol 'ASESOR'
Check "07 REQ-3.3 Orden ASESOR accionesPermitidas no vacio" ($resp.Body.accionesPermitidas.Count -gt 0 -and $resp.Body.accionesPermitidas -contains 'AVANZAR') "has AVANZAR"

$resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId" -Usuario 'asesor.ana' -Rol 'ASESOR'
if ($resp.Body.Count -gt 0) {
    Check "07 REQ-3.3 Cita ASESOR accionesPermitidas no vacio" ($resp.Body[0].accionesPermitidas.Count -gt 0 -and $resp.Body[0].accionesPermitidas -contains 'CANCELAR') "has CANCELAR"
}

Write-Host "07 REQ-3.2: verificando rechazo POST AUDITOR..."
$testOrder = @{
    clienteNombre = 'Test Rol'
    clienteTelefono = '+573001112233'
    vehiculoPlaca = 'ROL403'
    trabajo = 'Test'
}
$resp = Invoke-Api -Method POST -Url 'http://localhost:8081/api/ordenes' -Body $testOrder -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.2 POST ordenes AUDITOR" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.2 POST avanzar AUDITOR" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/reabrir" -Body @{motivo = 'x'} -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.2 POST reabrir AUDITOR" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

$citaRequest = @{
    ordenId = $ordenId
    asesorId = 1
    tipo = 'TRAER'
    franjaInicio = $franja.franjaInicio
}
$resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaRequest -Usuario 'auditor.marta' -Rol 'AUDITOR'
Check "07 REQ-3.2 POST citas AUDITOR" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

if ($citaId) {
    $resp = Invoke-Api -Method POST -Url "http://localhost:8082/api/agenda/citas/$citaId/cancelar" -Body @{motivo = 'TEST'} -Usuario 'auditor.marta' -Rol 'AUDITOR'
    Check "07 REQ-3.2 POST cancelar cita AUDITOR" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"
}

Write-Host "07 REQ-3.2: verificando que orden ROL403 no fue creada..."
$resp = Invoke-Api -Method GET -Url 'http://localhost:8081/api/ordenes' -Usuario 'admin.sofia' -Rol 'ADMIN'
$rol403Found = $resp.Body | Where-Object { $_.vehiculoPlaca -eq 'ROL403' }
Check "07 REQ-3.2 Orden ROL403 no existe" ($null -eq $rol403Found) "order not created"

$resp = Invoke-Api -Method GET -Url "http://localhost:8081/api/ordenes/$ordenId" -Usuario 'admin.sofia' -Rol 'ADMIN'
Check "07 REQ-3.2 Orden sigue en REGISTRADA" ($resp.Body.estado -eq 'REGISTRADA') "state unchanged"

Write-Host "07 REQ-2.3: verificando acceso ASESOR a auditoria..."
$resp = Invoke-Api -Method GET -Url 'http://localhost:8083/api/auditoria/historial' -Usuario 'asesor.ana' -Rol 'ASESOR'
Check "07 REQ-2.3 GET historial sin ordenId ASESOR" ($resp.Status -eq 403 -and $resp.Body.codigo -eq 'ROL_NO_PERMITIDO') "403 ROL_NO_PERMITIDO"

Write-Host "07 REQ-2.2: verificando acceso ASESOR con ordenId..."
$resp = Invoke-Api -Method GET -Url "http://localhost:8083/api/auditoria/historial?ordenId=$ordenId" -Usuario 'asesor.ana' -Rol 'ASESOR'
Check "07 REQ-2.2 GET historial con ordenId ASESOR" ($resp.Status -eq 200) "200 OK"

Write-Host "07 REQ-4.1: verificando acceso ADMIN..."
$resp = Invoke-Api -Method GET -Url 'http://localhost:8083/api/auditoria/historial' -Usuario 'admin.sofia' -Rol 'ADMIN'
Check "07 REQ-4.1 GET historial ADMIN" ($resp.Status -eq 200) "200 OK"

$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'admin.sofia' -Rol 'ADMIN'
Check "07 REQ-4.1 POST avanzar ADMIN" ($resp.Status -eq 200 -and $resp.Body.estado -eq 'EN_DIAGNOSTICO') "200 OK EN_DIAGNOSTICO"

if ($citaId) {
    $resp = Invoke-Api -Method POST -Url "http://localhost:8082/api/agenda/citas/$citaId/cancelar" -Body @{motivo = 'TEST'} -Usuario 'admin.sofia' -Rol 'ADMIN'
    Check "07 REQ-4.1 POST cancelar cita ADMIN" ($resp.Status -eq 200 -and $resp.Body.estado -eq 'CANCELADA') "200 OK CANCELADA"
}

Write-Host ""
if ($failureCount -eq 0) {
    Write-Host 'RESULTADO: TODOS PASS'
    exit 0
}
Write-Host "RESULTADO: $failureCount FAIL"
exit 1
