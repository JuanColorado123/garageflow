$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
Add-Type -AssemblyName System.Net.Http
[System.Net.ServicePointManager]::DefaultConnectionLimit = 20

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.Timeout = [TimeSpan]::FromSeconds(15)

$failureCount = 0

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

Write-Host "03 REQ-1.1: obteniendo asesores..."
$resp = Invoke-Api -Method GET -Url 'http://localhost:8082/api/agenda/asesores'
Check "03 REQ-1.1 Obtener asesores" ($resp.Status -eq 200) "advisors retrieved"
Check "03 REQ-1.1 2 asesores" ($resp.Body.Count -eq 2) "two advisors"
if ($resp.Body.Count -ge 2) {
    Check "03 REQ-1.1 Cupo 2" ($resp.Body[0].cupoPorFranja -eq 2) "advisor 1 capacity"
    Check "03 REQ-1.1 Cupo 2" ($resp.Body[1].cupoPorFranja -eq 2) "advisor 2 capacity"
}

Write-Host "03 REQ-1.1: creando orden..."
$newOrder = @{
    clienteNombre = 'Roberto Perez'
    clienteTelefono = '+573214567890'
    vehiculoPlaca = 'DEF456'
    vehiculoDescripcion = 'Toyota Corolla'
    aseguradora = ''
    trabajo = 'Alineacion y balanceo'
}
$resp = Invoke-Api -Method POST -Url 'http://localhost:8081/api/ordenes' -Body $newOrder -Usuario 'asesor.ana'
$ordenId = $resp.Body.id

Write-Host "03 REQ-1.2: encontrando franja disponible para asesor 1..."
$franja = $null
$hoy = [DateTime]::Now
for ($diaOffset = 1; $diaOffset -le 30; $diaOffset++) {
    $fecha = $hoy.AddDays($diaOffset).DayOfWeek
    if ($fecha -eq [System.DayOfWeek]::Sunday) {
        continue
    }
    
    $fechaStr = $hoy.AddDays($diaOffset).ToString('yyyy-MM-dd')
    $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/disponibilidad?fecha=$fechaStr"
    
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

Check "03 REQ-1.2 Franja encontrada" ($null -ne $franja) "free slot found"

if ($franja) {
    Write-Host "03 REQ-2.3: enviando 6 reservas simultaneas..."
    
    $tasks = @()
    for ($i = 1; $i -le 6; $i++) {
        $citaRequest = @{
            ordenId = $ordenId
            asesorId = 1
            tipo = 'TRAER'
            franjaInicio = $franja.franjaInicio
        }
        
        $request = New-Object System.Net.Http.HttpRequestMessage
        $request.Method = 'POST'
        $request.RequestUri = 'http://localhost:8082/api/agenda/citas'
        $json = $citaRequest | ConvertTo-Json -Depth 5
        $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, 'application/json')
        $request.Headers.TryAddWithoutValidation('X-Usuario', 'asesor.ana') | Out-Null
        $request.Headers.TryAddWithoutValidation('X-Rol', 'ASESOR') | Out-Null
        
        $task = $httpClient.SendAsync($request)
        $tasks += $task
    }
    
    [System.Threading.Tasks.Task]::WaitAll($tasks)
    
    $created = 0
    $sinCupo = 0
    
    foreach ($task in $tasks) {
        $response = $task.Result
        $content = $response.Content.ReadAsStringAsync().Result
        $body = $null
        if ($content) {
            try {
                $body = $content | ConvertFrom-Json
            } catch {
                $body = $content
            }
        }
        
        if ([int]$response.StatusCode -eq 201) {
            $created++
        } elseif ([int]$response.StatusCode -eq 409 -and $body.codigo -eq 'SIN_CUPO') {
            $sinCupo++
        }
    }
    
    Check "03 REQ-2.3 2 citas creadas" ($created -eq 2) "two reservations created"
    Check "03 REQ-2.3 4 SIN_CUPO" ($sinCupo -eq 4) "four quota exceeded"
    
    Write-Host "03 REQ-1.2: verificando disponibilidad despues de reservas..."
    $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/disponibilidad?fecha=$($franja.franjaInicio.Split('T')[0])"
    
    if ($resp.Status -eq 200) {
        $franjaActualizada = $resp.Body | Where-Object { $_.asesorId -eq 1 -and $_.franjaInicio -eq $franja.franjaInicio }
        if ($franjaActualizada) {
            Check "03 REQ-1.2 Libres 0" ($franjaActualizada.libres -eq 0) "no free slots"
        } else {
            Check "03 REQ-1.2 Libres 0" $false "franja not found"
        }
    }
    
    Write-Host "03 REQ-3.1: obteniendo citas para cancelar..."
    $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId"
    Check "03 REQ-4.1 2 citas pendientes" ($resp.Body.Count -eq 2) "two appointments"
    
    if ($resp.Body.Count -ge 1) {
        $citaId = $resp.Body[0].id
        
        Write-Host "03 REQ-3.1: cancelando una cita..."
        $cancelRequest = @{ motivo = 'MANUAL' }
        $resp = Invoke-Api -Method POST -Url "http://localhost:8082/api/agenda/citas/$citaId/cancelar" -Body $cancelRequest
        Check "03 REQ-3.1 Cancelar status 200" ($resp.Status -eq 200) "cancellation successful"
        Check "03 REQ-3.1 Estado CANCELADA" ($resp.Body.estado -eq 'CANCELADA') "cancelled state"
        Check "03 REQ-3.1 Motivo MANUAL" ($resp.Body.motivoCancelacion -eq 'MANUAL') "manual cancellation"
        Check "03 REQ-3.1 Sin acciones" ($resp.Body.accionesPermitidas.Count -eq 0) "no actions after cancel"
        
        Write-Host "03 REQ-4.2: verificando disponibilidad tras cancelacion..."
        $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/disponibilidad?fecha=$($franja.franjaInicio.Split('T')[0])"
        
        if ($resp.Status -eq 200) {
            $franjaActualizada = $resp.Body | Where-Object { $_.asesorId -eq 1 -and $_.franjaInicio -eq $franja.franjaInicio }
            if ($franjaActualizada) {
                Check "03 REQ-4.2 Libres 1" ($franjaActualizada.libres -eq 1) "one free slot"
            }
        }
        
        Write-Host "03 REQ-3.2: cancelando otra vez (debe rechazar)..."
        $resp = Invoke-Api -Method POST -Url "http://localhost:8082/api/agenda/citas/$citaId/cancelar" -Body $cancelRequest
        Check "03 REQ-3.2 Cancelar NO pendiente status 409" ($resp.Status -eq 409) "conflict"
        Check "03 REQ-3.2 Codigo CITA_NO_PENDIENTE" ($resp.Body.codigo -eq 'CITA_NO_PENDIENTE') "error code"
    }
    
    Write-Host "03 REQ-2.4: POST con HH:30 (debe rechazar)..."
    $franjaInvalida = $franja.franjaInicio.Substring(0, 13) + ':30:00'
    $citaInvalida = @{
        ordenId = $ordenId
        asesorId = 1
        tipo = 'TRAER'
        franjaInicio = $franjaInvalida
    }
    $resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaInvalida
    Check "03 REQ-2.4 Franja invalida status 400" ($resp.Status -eq 400) "validation error"
    Check "03 REQ-2.4 Codigo FRANJA_INVALIDA" ($resp.Body.codigo -eq 'FRANJA_INVALIDA') "error code"
    
    Write-Host "03 REQ-2.9: POST con tipo invalido..."
    $citaMala = @{
        ordenId = $ordenId
        asesorId = 1
        tipo = 'LAVAR'
        franjaInicio = $franja.franjaInicio
    }
    $resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaMala
    Check "03 REQ-2.9 Tipo invalido status 400" ($resp.Status -eq 400) "validation error"
    Check "03 REQ-2.9 Codigo DATOS_INVALIDOS" ($resp.Body.codigo -eq 'DATOS_INVALIDOS') "error code"
    
    Write-Host "03 REQ-2.5: POST con orden inexistente..."
    $citaMala = @{
        ordenId = 'inexistente'
        asesorId = 1
        tipo = 'TRAER'
        franjaInicio = $franja.franjaInicio
    }
    $resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaMala
    Check "03 REQ-2.5 Orden inexistente status 404" ($resp.Status -eq 404) "not found"
    Check "03 REQ-2.5 Codigo ORDEN_NO_ENCONTRADA" ($resp.Body.codigo -eq 'ORDEN_NO_ENCONTRADA') "error code"
    
    Write-Host "03 REQ-4.1, REQ-4.2: listando citas..."
    $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId"
    Check "03 REQ-4.1 Citas ordenadas" ($resp.Body[0].franjaInicio -le $resp.Body[1].franjaInicio) "sorted by start"
    
    $pendiente = $resp.Body | Where-Object { $_.estado -eq 'PENDIENTE' }
    if ($pendiente) {
        Check "03 REQ-4.2 Pendiente tiene CANCELAR" ($pendiente.accionesPermitidas -contains 'CANCELAR') "cancel action"
    }
}

Write-Host ""
if ($failureCount -eq 0) {
    Write-Host 'RESULTADO: TODOS PASS'
    exit 0
}
Write-Host "RESULTADO: $failureCount FAIL"
exit 1
