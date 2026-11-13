$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.Timeout = [TimeSpan]::FromSeconds(15)

$failureCount = 0

function Invoke-Api {
    param([string]$Method, [string]$Url, [hashtable]$Body, [string]$Usuario, [string]$Rol = '')
    
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

Write-Host "00 REQ-1.5: esperando a que http://localhost:4200 responda 200..."
$resultado = Esperar -Condicion {
    $resp = Invoke-Api -Method GET -Url 'http://localhost:4200/'
    $resp.Status -eq 200
} -Segundos 240

Check "00 REQ-1.5 HTTP 200 puerto 4200" $resultado "web responsive"

Write-Host "00 REQ-1.5: esperando a que http://localhost:15672 responda 200..."
$resultado = Esperar -Condicion {
    $resp = Invoke-Api -Method GET -Url 'http://localhost:15672/'
    $resp.Status -eq 200
} -Segundos 60

Check "00 REQ-1.5 HTTP 200 puerto 15672" $resultado "RabbitMQ console"

Write-Host "00 REQ-2.1: esperando a que los microservicios reporten UP..."
@(8081, 8082, 8083, 8084) | ForEach-Object {
    $puerto = $_
    $resultado = Esperar -Condicion {
        $resp = Invoke-Api -Method GET -Url "http://localhost:$puerto/actuator/health"
        $resp.Body.status -eq 'UP'
    } -Segundos 240
    
    Check "00 REQ-2.1 UP puerto $puerto" $resultado "health check"
}

Write-Host "00 REQ-4.1: verificando CORS en /api/ordenes..."
$request = New-Object System.Net.Http.HttpRequestMessage
$request.Method = 'OPTIONS'
$request.RequestUri = 'http://localhost:8081/api/ordenes'
$request.Headers.Add('Origin', 'http://localhost:4200')
$request.Headers.Add('Access-Control-Request-Method', 'POST')
$request.Headers.Add('Access-Control-Request-Headers', 'x-usuario,x-rol,content-type')
$response = $httpClient.SendAsync($request).Result
$valores = $null
$corsOk = $response.Headers.TryGetValues('Access-Control-Allow-Origin', [ref]$valores) -and ($valores -contains 'http://localhost:4200')
Check "00 REQ-4.1 CORS preflight /api/ordenes" $corsOk "Allow-Origin header present"

Write-Host "00 REQ-4.1: verificando CORS en /api/agenda/asesores..."
$request = New-Object System.Net.Http.HttpRequestMessage
$request.Method = 'OPTIONS'
$request.RequestUri = 'http://localhost:8082/api/agenda/asesores'
$request.Headers.Add('Origin', 'http://localhost:4200')
$request.Headers.Add('Access-Control-Request-Method', 'GET')
$request.Headers.Add('Access-Control-Request-Headers', 'x-usuario,x-rol,content-type')
$response = $httpClient.SendAsync($request).Result
$valores = $null
$corsOk = $response.Headers.TryGetValues('Access-Control-Allow-Origin', [ref]$valores) -and ($valores -contains 'http://localhost:4200')
Check "00 REQ-4.1 CORS preflight /api/agenda/asesores" $corsOk "Allow-Origin header present"

Write-Host "00 REQ-4.1: verificando CORS en /api/auditoria/historial..."
$request = New-Object System.Net.Http.HttpRequestMessage
$request.Method = 'OPTIONS'
$request.RequestUri = 'http://localhost:8083/api/auditoria/historial'
$request.Headers.Add('Origin', 'http://localhost:4200')
$request.Headers.Add('Access-Control-Request-Method', 'GET')
$request.Headers.Add('Access-Control-Request-Headers', 'x-usuario,x-rol,content-type')
$response = $httpClient.SendAsync($request).Result
$valores = $null
$corsOk = $response.Headers.TryGetValues('Access-Control-Allow-Origin', [ref]$valores) -and ($valores -contains 'http://localhost:4200')
Check "00 REQ-4.1 CORS preflight /api/auditoria/historial" $corsOk "Allow-Origin header present"

Write-Host ""
if ($failureCount -eq 0) {
    Write-Host 'RESULTADO: TODOS PASS'
    exit 0
}
Write-Host "RESULTADO: $failureCount FAIL"
exit 1
