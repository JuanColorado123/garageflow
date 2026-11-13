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

Write-Host "02 REQ-1.1: creando orden y avanzandola..."
$newOrder = @{
    clienteNombre = 'Ana Garcia'
    clienteTelefono = '+573112345678'
    vehiculoPlaca = 'OPQ789'
    vehiculoDescripcion = 'Honda Civic 2020'
    aseguradora = ''
    trabajo = 'Revisi adicional y alineacion'
}
$resp = Invoke-Api -Method POST -Url 'http://localhost:8081/api/ordenes' -Body $newOrder -Usuario 'asesor.ana'
$ordenId = $resp.Body.id

$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'asesor.luis'
$resp = Invoke-Api -Method POST -Url "http://localhost:8081/api/ordenes/$ordenId/avanzar" -Usuario 'asesor.luis'

Write-Host "02 REQ-1.1: esperando a que el historial tenga 3 cambios..."
$resultado = Esperar -Condicion {
    $resp = Invoke-Api -Method GET -Url "http://localhost:8083/api/auditoria/historial?ordenId=$ordenId"
    $resp.Body.Count -eq 3
} -Segundos 30

Check "02 REQ-1.1 Historial con 3 cambios" $resultado "audit trail complete"

if ($resultado) {
    $resp = Invoke-Api -Method GET -Url "http://localhost:8083/api/auditoria/historial?ordenId=$ordenId"
    
    Check "02 REQ-3.1 Primer cambio REGISTRADA" ($resp.Body[0].estadoNuevo -eq 'REGISTRADA') "initial state"
    Check "02 REQ-3.1 Primer estadoAnterior null" ($null -eq $resp.Body[0].estadoAnterior) "no previous state"
    Check "02 REQ-3.1 Primer usuario ana" ($resp.Body[0].usuario -eq 'asesor.ana') "creator"
    
    Check "02 REQ-3.1 Segundo cambio EN_DIAGNOSTICO" ($resp.Body[1].estadoNuevo -eq 'EN_DIAGNOSTICO') "second state"
    Check "02 REQ-3.1 Segundo usuario luis" ($resp.Body[1].usuario -eq 'asesor.luis') "second user"
    
    Check "02 REQ-3.1 Tercer cambio EN_REPARACION" ($resp.Body[2].estadoNuevo -eq 'EN_REPARACION') "third state"
    Check "02 REQ-3.1 Tercer usuario luis" ($resp.Body[2].usuario -eq 'asesor.luis') "third user"
}

$rootPassword = $env:MYSQL_ROOT_PASSWORD
if (-not $rootPassword) {
    $rootPassword = 'root'
}

$auditoriaPassword = $env:AUDITORIA_APP_PASSWORD
if (-not $auditoriaPassword) {
    $auditoriaPassword = 'auditoria'
}

Write-Host "02 REQ-2.1: intentando UPDATE en historial como root (debe rechazar)..."
$updateCmd = "UPDATE historial_estados SET usuario = 'alterado' WHERE orden_id = '$ordenId'"
$result = Invoke-Docker -Argumentos @('compose', 'exec', '-T', 'mysql-auditoria', 'mysql', "-uroot", "-p$rootPassword", 'auditoria_db', '-e', $updateCmd)
$updateRejected = $result.Codigo -ne 0 -and $result.Salida -like '*solo insercion*'
Check "02 REQ-2.1 UPDATE rechazado" $updateRejected "insert-only constraint"

Write-Host "02 REQ-2.1: intentando DELETE en historial como root (debe rechazar)..."
$deleteCmd = "DELETE FROM historial_estados WHERE orden_id = '$ordenId'"
$result = Invoke-Docker -Argumentos @('compose', 'exec', '-T', 'mysql-auditoria', 'mysql', "-uroot", "-p$rootPassword", 'auditoria_db', '-e', $deleteCmd)
$deleteRejected = $result.Codigo -ne 0 -and $result.Salida -like '*solo insercion*'
Check "02 REQ-2.1 DELETE rechazado" $deleteRejected "insert-only constraint"

Write-Host "02 REQ-2.2: intentando UPDATE como auditoria_app (debe rechazar por permisos)..."
$updateCmd = "UPDATE historial_estados SET usuario = 'alterado' WHERE orden_id = '$ordenId'"
$result = Invoke-Docker -Argumentos @('compose', 'exec', '-T', 'mysql-auditoria', 'mysql', "-uauditoria_app", "-p$auditoriaPassword", 'auditoria_db', '-e', $updateCmd)
$appUpdateRejected = $result.Codigo -ne 0
Check "02 REQ-2.2 UPDATE denied for app user" $appUpdateRejected "permission denied"

Write-Host "02 REQ-2.2: verificando SHOW GRANTS..."
$grantsCmd = "SHOW GRANTS FOR 'auditoria_app'@'%'"
$result = Invoke-Docker -Argumentos @('compose', 'exec', '-T', 'mysql-auditoria', 'mysql', "-uroot", "-p$rootPassword", 'auditoria_db', '-e', $grantsCmd)
$hasSelectInsert = $result.Salida -like '*SELECT*INSERT*' -and $result.Salida -like '*historial_estados*'
Check "02 REQ-2.2 Grants SELECT, INSERT" $hasSelectInsert "app user has correct permissions"

Write-Host "02 REQ-2.1: verificando que el historial sigue intacto..."
$resp = Invoke-Api -Method GET -Url "http://localhost:8083/api/auditoria/historial?ordenId=$ordenId"
Check "02 REQ-2.1 Historial inalterado 3 cambios" ($resp.Body.Count -eq 3) "audit trail unchanged"

if ($resp.Body.Count -eq 3) {
    Check "02 REQ-2.1 Usuarios originales intactos" (
        $resp.Body[0].usuario -eq 'asesor.ana' -and 
        $resp.Body[1].usuario -eq 'asesor.luis' -and 
        $resp.Body[2].usuario -eq 'asesor.luis'
    ) "users unchanged"
}

Write-Host ""
if ($failureCount -eq 0) {
    Write-Host 'RESULTADO: TODOS PASS'
    exit 0
}
Write-Host "RESULTADO: $failureCount FAIL"
exit 1
