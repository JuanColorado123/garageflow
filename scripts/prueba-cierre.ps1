$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
Add-Type -AssemblyName System.Net.Http

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

function Invoke-Docker {
    param([string[]]$Argumentos)
    
    $prevPref = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $output = & docker @Argumentos 2>&1 | ForEach-Object { "$_" }
    $code = $LASTEXITCODE
    $ErrorActionPreference = $prevPref
    
    return [pscustomobject]@{ Codigo = $code; Salida = ($output -join "`n") }
}

Write-Host "05 REQ-1.1: creando orden y avanzandola a REPARADA..."
$newOrder = @{
    clienteNombre = 'Miguel Sanchez'
    clienteTelefono = '+573334445555'
    vehiculoPlaca = 'GHI789'
    vehiculoDescripcion = 'Audi A4 2021'
    aseguradora = ''
    trabajo = 'Servicio de aire acondicionado'
}
$resp = Invoke-Api -Method POST -Url 'http://localhost:8081/api/ordenes' -Body $newOrder -Usuario 'asesor.ana'
$ordenId = $resp.Body.id

$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'asesor.ana'
$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'asesor.ana'
$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'asesor.ana'

Write-Host "05 REQ-1.1: encontrando franja para asesor 2..."
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
            if ($slot.asesorId -eq 2 -and $slot.libres -eq $slot.cupo) {
                $franja = $slot
                break
            }
        }
        if ($franja) { break }
    }
}

Check "05 REQ-1.1 Franja encontrada" ($null -ne $franja) "advisor 2 slot found"

if ($franja) {
    Write-Host "05 REQ-1.1: agendando cita A (TRAER)..."
    $citaA = @{
        ordenId = $ordenId
        asesorId = 2
        tipo = 'TRAER'
        franjaInicio = $franja.franjaInicio
    }
    $resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaA
    $citaAId = $resp.Body.id
    
    Write-Host "05 REQ-1.1: agendando cita B (RECOGER) en la misma franja..."
    $citaB = @{
        ordenId = $ordenId
        asesorId = 2
        tipo = 'RECOGER'
        franjaInicio = $franja.franjaInicio
    }
    $resp = Invoke-Api -Method POST -Url 'http://localhost:8082/api/agenda/citas' -Body $citaB
    $citaBId = $resp.Body.id
    
    Write-Host "05 REQ-1.1: cancelando cita B manualmente..."
    $cancelB = @{ motivo = 'MANUAL' }
    $resp = Invoke-Api -Method POST -Url "http://localhost:8082/api/agenda/citas/$citaBId/cancelar" -Body $cancelB
    
    Write-Host "04 REQ-1.1, REQ-1.2: esperando avisos en logs..."
    $logsAguardando = Esperar -Condicion {
        $result = Invoke-Docker -Argumentos @('compose', 'logs', '--no-color', 'ms-notificaciones')
        $lineas = $result.Salida -split "`n"
        
        $citaAAgendada = $lineas | Select-String -SimpleMatch -Pattern "WHATSAPP_SIMULADO cita=$citaAId " | Measure-Object
        $citaBAgendada = $lineas | Select-String -SimpleMatch -Pattern "WHATSAPP_SIMULADO cita=$citaBId " | Measure-Object
        $citaBCancelada = $lineas | Select-String -SimpleMatch -Pattern "WHATSAPP_SIMULADO cita=$citaBId " | Measure-Object
        
        ($citaAAgendada.Count -eq 1) -and ($citaBAgendada.Count -ge 1) -and ($citaBCancelada.Count -ge 2)
    } -Segundos 30
    
    Check "04 REQ-1.1 Aviso A agendada" $logsAguardando "WhatsApp sent for A"
    Check "04 REQ-1.2 Aviso B agendada y cancelada" $logsAguardando "WhatsApp sent for B"
    
    Write-Host "05 REQ-1.2, REQ-2.1: cerrando orden..."
    $resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'asesor.ana'
    
    Write-Host "05 REQ-1.2: esperando a que cita A sea cancelada con CIERRE_ORDEN..."
    $resultado = Esperar -Condicion {
        $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId"
        $citaAActual = $resp.Body | Where-Object { $_.id -eq $citaAId }
        $citaAActual -and $citaAActual.estado -eq 'CANCELADA' -and $citaAActual.motivoCancelacion -eq 'CIERRE_ORDEN'
    } -Segundos 20
    
    Check "05 REQ-1.2 Cita A cancelada por CIERRE_ORDEN" $resultado "A cancelled on close"
    
    Write-Host "05 REQ-1.4: verificando que cita B conserva MANUAL..."
    $resp = Invoke-Api -Method GET -Url "http://localhost:8082/api/agenda/citas?ordenId=$ordenId"
    $citaBActual = $resp.Body | Where-Object { $_.id -eq $citaBId }
    Check "05 REQ-1.4 Cita B sigue con MANUAL" ($citaBActual -and $citaBActual.motivoCancelacion -eq 'MANUAL') "B unchanged"
    
    Write-Host "05 REQ-2.1, REQ-2.2: verificando logs para WHATSAPP_OMITIDO..."
    $resultado = Esperar -Condicion {
        $result = Invoke-Docker -Argumentos @('compose', 'logs', '--no-color', 'ms-notificaciones')
        $lineas = $result.Salida -split "`n"
        
        $omitido = $lineas | Select-String -SimpleMatch -Pattern "WHATSAPP_OMITIDO cita=$citaAId" | Measure-Object
        $agendadaA = $lineas | Select-String -SimpleMatch -Pattern "WHATSAPP_SIMULADO cita=$citaAId " | Measure-Object
        
        ($omitido.Count -eq 1) -and ($agendadaA.Count -eq 1)
    } -Segundos 30
    
    Check "05 REQ-2.2 Aviso A omitido al cerrar" $resultado "omit notification on close"
    Check "05 REQ-2.1 Solo un WHATSAPP_SIMULADO de A" $resultado "single notification for A"
}

Write-Host ""
if ($failureCount -eq 0) {
    Write-Host 'RESULTADO: TODOS PASS'
    exit 0
}
Write-Host "RESULTADO: $failureCount FAIL"
exit 1
