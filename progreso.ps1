# GitHub Projects Autopilot - Asistente Local de Progreso
# Compatible con Windows PowerShell 5.1 y PowerShell 7+ (ASCII puro)

$ErrorActionPreference = 'Stop'

$RepoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }

function Test-RepoFile {
    param([string]$RelativePath)
    return (Test-Path -LiteralPath (Join-Path $RepoRoot $RelativePath) -PathType Leaf)
}

function Read-RepoFile {
    param([string]$RelativePath)
    $full = Join-Path $RepoRoot $RelativePath
    if (Test-Path -LiteralPath $full -PathType Leaf) {
        return Get-Content -LiteralPath $full -Raw -Encoding UTF8
    }
    return ''
}

function Get-RepoFiles {
    param([string]$RelativeDir)
    $full = Join-Path $RepoRoot $RelativeDir
    if (Test-Path -LiteralPath $full -PathType Container) {
        return @(Get-ChildItem -LiteralPath $full -Recurse -File)
    }
    return @()
}

$domainFiles = Get-RepoFiles 'src/main/java/cl/duoc/sonidovivo/domain'
$domainHasFrameworkImport = $false
foreach ($df in $domainFiles) {
    $raw = Get-Content -LiteralPath $df.FullName -Raw -Encoding UTF8
    if ($raw -match 'import\s+(org\.springframework|jakarta|org\.bson|com\.mongodb|oracle)\.') {
        $domainHasFrameworkImport = $true
        break
    }
}

$tasks = @(
    @{
        Id = 'T01'
        Title = 'Gobernanza Hexagonal, ADRs (0001-0003) y ArchUnit'
        Done = (
            (Test-RepoFile 'docs/architecture.md') -and
            (Test-RepoFile 'docs/adr/0001-hexagonal-modular-monolith.md') -and
            (Test-RepoFile 'docs/adr/0002-polyglot-persistence-oracle-mongo.md') -and
            (Test-RepoFile 'docs/adr/0003-jwt-stateless-security.md') -and
            ((Read-RepoFile 'src/test/java/cl/duoc/sonidovivo/architecture/HexagonalArchitectureTest.java') -match '@ArchTest')
        )
        Coach = 'Crea docs/architecture.md, los ADRs en docs/adr/ y HexagonalArchitectureTest.java con reglas @ArchTest.'
    },
    @{
        Id = 'T02'
        Title = 'Dominio Puro Java 21 (Modelos, Value Objects y Puertos Out)'
        Done = (
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/model/Product.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/model/Customer.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/model/SaleCommand.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/port/out/ProductRepositoryPort.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/port/out/DashboardMetricsPort.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/port/out/CustomerRepositoryPort.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/domain/port/out/OracleSalesPort.java') -and
            ($domainFiles.Count -gt 0) -and
            (-not $domainHasFrameworkImport)
        )
        Coach = 'Implementa modelos e interfaces port/out en domain/ usando Java 21 puro (cero Spring/Jakarta).'
    },
    @{
        Id = 'T03'
        Title = 'Casos de Uso y Servicios de Aplicacion (Inbound Ports y Tests)'
        Done = (
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/port/in/ProductUseCase.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/port/in/DashboardUseCase.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/port/in/CustomerUseCase.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/port/in/OracleIntegrationUseCase.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/service/ProductService.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/service/DashboardService.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/service/CustomerService.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/application/service/OracleIntegrationService.java') -and
            ((Get-RepoFiles 'src/test/java/cl/duoc/sonidovivo/application/service').Count -ge 4)
        )
        Coach = 'Implementa los 4 UseCases en application/port/in, servicios POJO en application/service y sus tests Mockito.'
    },
    @{
        Id = 'T04'
        Title = 'Adaptador de Persistencia MongoDB ($inc, $push, $aggregate)'
        Done = $(
            $pAd = Read-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/mongo/MongoProductRepositoryAdapter.java'
            $mAd = Read-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/mongo/MongoMetricsRepositoryAdapter.java'
            (
                (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/mongo/document/MongoProductDocument.java') -and
                (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/mongo/document/ResenaEmbeddable.java') -and
                ($pAd -match '\.inc\(') -and
                ($pAd -match '\.push\(') -and
                ($mAd -match 'Aggregation')
            )
        )
        Coach = 'Implementa MongoProductDocument, MongoProductRepositoryAdapter ($inc/$push) y MongoMetricsRepositoryAdapter ($aggregate).'
    },
    @{
        Id = 'T05'
        Title = 'Adaptador Oracle PL/SQL (PKG_SONIDO_VIVO) y Clientes'
        Done = $(
            $ora = Read-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/oracle/OraclePlSqlAdapter.java'
            (
                (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/oracle/OracleCustomerEntity.java') -and
                (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/out/persistence/oracle/OracleCustomerRepositoryAdapter.java') -and
                ($ora -match 'PKG_SONIDO_VIVO') -and
                ($ora -match 'SP_REGISTRAR_VENTA') -and
                ($ora -match 'SP_LIQUIDAR_CLIENTES') -and
                ($ora -match 'FN_CALCULAR_DESCUENTO')
            )
        )
        Coach = 'Implementa OracleCustomerEntity y OraclePlSqlAdapter invocando PKG_SONIDO_VIVO via SimpleJdbcCall.'
    },
    @{
        Id = 'T06'
        Title = 'Seguridad JWT Stateless, ROLE_ADMIN y Configuracion CORS'
        Done = (
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/config/SecurityConfig.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/config/JwtService.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/config/JwtAuthenticationFilter.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/config/CorsConfig.java') -and
            ((Read-RepoFile 'src/main/resources/application.yaml') -match '\$\{JWT_SECRET\}')
        )
        Coach = 'Configura SecurityConfig stateless, JwtService, JwtAuthenticationFilter y externaliza JWT_SECRET en application.yaml.'
    },
    @{
        Id = 'T07'
        Title = 'API REST Administrativa y Mapeo Global de Errores'
        Done = (
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/in/rest/AuthController.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/in/rest/AdminProductController.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/in/rest/AdminDashboardController.java') -and
            (Test-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/in/rest/AdminCustomerController.java') -and
            ((Read-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/in/rest/GlobalExceptionHandler.java') -match '@RestControllerAdvice') -and
            ((Get-RepoFiles 'src/main/java/cl/duoc/sonidovivo/infrastructure/adapter/in/rest/dto').Count -ge 8)
        )
        Coach = 'Expone los 4 controladores REST, DTOs record validados y GlobalExceptionHandler.'
    },
    @{
        Id = 'T08'
        Title = 'Script e Inicializacion MongoDB ($jsonSchema e Indices)'
        Done = $(
            $mScripts = @(Get-RepoFiles 'scripts/mongo') + @(Get-RepoFiles 'db/mongo') + @(Get-RepoFiles 'docker/mongo')
            $ok = $false
            foreach ($ms in $mScripts) {
                if ($ms.Extension -eq '.js') {
                    $raw = Get-Content -LiteralPath $ms.FullName -Raw -Encoding UTF8
                    if (($raw -match '\$jsonSchema') -and ($raw -match 'createIndex') -and ($raw -match 'idx_codigo_unique')) {
                        $ok = $true
                        break
                    }
                }
            }
            $ok
        )
        Coach = 'Crea scripts/mongo/init-sonido-vivo.js con el $jsonSchema de productos y los 4 indices (idx_codigo_unique, idx_cat_precio, idx_tags, idx_espec_tipo) segun docs/contracts/CONTRACT_DATABASE_MONGODB.md.'
    },
    @{
        Id = 'T09'
        Title = 'Workflow CI Maven (Tests + ArchUnit) y CORS Parametrizado'
        Done = $(
            $ciPath = if (Test-RepoFile '.github/workflows/ci-maven.yml') { '.github/workflows/ci-maven.yml' } elseif (Test-RepoFile '.github/workflows/ci.yml') { '.github/workflows/ci.yml' } else { '' }
            $ciRaw = if ($ciPath) { Read-RepoFile $ciPath } else { '' }
            $corsRaw = Read-RepoFile 'src/main/java/cl/duoc/sonidovivo/infrastructure/config/CorsConfig.java'
            $yamlRaw = Read-RepoFile 'src/main/resources/application.yaml'
            (($ciPath -ne '') -and ($ciRaw -match 'mvnw') -and (($corsRaw -match 'CORS_ALLOWED_ORIGINS|allowed-origins') -or ($yamlRaw -match 'CORS_ALLOWED_ORIGINS|allowed-origins')))
        )
        Coach = 'Crea .github/workflows/ci-maven.yml ejecutando ./mvnw test en JDK 21 y parametriza CORS en CorsConfig.java + application.yaml via ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}.'
    },
    @{
        Id = 'T10'
        Title = 'Empaquetado Dockerfile Non-Root, Compose y Smoke Test'
        Done = $(
            $df = Read-RepoFile 'Dockerfile'
            $hasFrom = $df -match '(?i)FROM\s+'
            $hasNonRoot = $df -match '(?im)^\s*USER\s+(?!root\b|0\b)\S+'
            $hasCompose = (Test-RepoFile 'docker-compose.yml') -or (Test-RepoFile 'compose.yaml')
            ($hasFrom -and $hasNonRoot -and $hasCompose)
        )
        Coach = 'Crea Dockerfile multi-stage non-root (USER spring) y docker-compose.yml con limites de memoria JVM, MongoDB, Oracle y verificacion /actuator/health.'
    }
)

$total = $tasks.Count
$doneCount = ($tasks | Where-Object { $_.Done }).Count
$pct = [math]::Round(($doneCount / $total) * 100)

$filledBars = [math]::Round(($pct / 100) * 20)
$emptyBars = 20 - $filledBars
$bar = ('#' * $filledBars) + ('-' * $emptyBars)

Write-Host ''
Write-Host '======================================================================' -ForegroundColor Cyan
Write-Host ' SONIDO VIVO BACKEND - AUTOPILOT DE PROGRESO LOCAL' -ForegroundColor Cyan
Write-Host '======================================================================' -ForegroundColor Cyan
Write-Host (" Avance Global: [{0}] {1}% ({2}/{3} Tareas)" -f $bar, $pct, $doneCount, $total) -ForegroundColor White
Write-Host '----------------------------------------------------------------------' -ForegroundColor DarkGray

$activeTask = $null

foreach ($t in $tasks) {
    if ($t.Done) {
        Write-Host (" [LISTO]     {0} - {1}" -f $t.Id, $t.Title) -ForegroundColor Green
    } elseif ($null -eq $activeTask) {
        $activeTask = $t
        Write-Host (" [EN CURSO]  {0} - {1}" -f $t.Id, $t.Title) -ForegroundColor Yellow
    } else {
        Write-Host (" [PENDIENTE] {0} - {1}" -f $t.Id, $t.Title) -ForegroundColor DarkGray
    }
}

Write-Host ''
Write-Host '----------------------------------------------------------------------' -ForegroundColor DarkGray
if ($null -ne $activeTask) {
    Write-Host (" QUE TOCA HACER AHORA -> {0}: {1}" -f $activeTask.Id, $activeTask.Title) -ForegroundColor Yellow
    Write-Host (" Guia: {0}" -f $activeTask.Coach) -ForegroundColor White
} else {
    Write-Host ' QUE TOCA HACER AHORA -> Todas las tareas del backlog estan LISTAS.' -ForegroundColor Green
}
Write-Host '======================================================================' -ForegroundColor Cyan
Write-Host ''
