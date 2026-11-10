[CmdletBinding()]
param(
    [Parameter()]
    [string] $MongoUri = $env:CATEGORY_EXPORT_MONGO_URI,

    [Parameter()]
    [string] $Destination = (Join-Path $PSScriptRoot '..\src\main\resources\seed\categories.json'),

    [Parameter()]
    [switch] $Force,

    [Parameter()]
    [switch] $AllowEmpty
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($MongoUri)) {
    throw 'Mongo URI must be supplied with -MongoUri or CATEGORY_EXPORT_MONGO_URI.'
}

if (-not (Get-Command mongosh -ErrorAction SilentlyContinue)) {
    throw 'mongosh was not found in PATH.'
}

$destinationPath = [System.IO.Path]::GetFullPath($Destination)
$destinationDirectory = [System.IO.Path]::GetDirectoryName($destinationPath)
if (-not (Test-Path -LiteralPath $destinationDirectory -PathType Container)) {
    throw "Destination directory does not exist: $destinationDirectory"
}
if ((Test-Path -LiteralPath $destinationPath) -and -not $Force) {
    throw 'Destination already exists. Re-run with -Force only after explicitly confirming overwrite.'
}

$mongoScript = @'
const exportDb = connect(process.env.CATEGORY_EXPORT_MONGO_URI);
const rows = exportDb.getCollection("category").aggregate([
  { $match: { active: true } },
  { $sort: { _id: 1 } },
  { $project: { _id: 0, name: 1 } }
]).toArray();
print(EJSON.stringify(rows, { relaxed: true }));
'@

$previousUri = $env:CATEGORY_EXPORT_MONGO_URI
$temporaryPath = Join-Path $destinationDirectory ('.categories.{0}.tmp' -f [Guid]::NewGuid().ToString('N'))

try {
    $env:CATEGORY_EXPORT_MONGO_URI = $MongoUri
    $rawOutput = & mongosh --nodb --quiet --eval $mongoScript 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "mongosh failed with exit code $LASTEXITCODE. Connection details were suppressed."
    }

    $rows = @($rawOutput | Out-String | ConvertFrom-Json)
    if ($rows.Count -eq 0 -and -not $AllowEmpty) {
        throw 'Refusing to export an empty category collection. Use -AllowEmpty only for an intentional empty export.'
    }

    $names = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    $validated = [System.Collections.Generic.List[object]]::new()

    for ($index = 0; $index -lt $rows.Count; $index++) {
        $row = $rows[$index]
        $propertyNames = @($row.PSObject.Properties.Name | Sort-Object)
        if (($propertyNames -join ',') -ne 'name') {
            throw "Category at index $index must contain exactly name."
        }

        if ($null -eq $row.name -or $row.name -isnot [string] -or [string]::IsNullOrWhiteSpace($row.name)) {
            throw "Category at index $index has a missing or blank name."
        }
        if ($row.name -ne $row.name.Trim()) {
            throw "Category at index $index has surrounding whitespace in name."
        }
        if (-not $names.Add($row.name)) {
            throw "Category at index $index has a duplicate name ignoring case."
        }

        $validated.Add([ordered]@{
            name = $row.name
        })
    }

    $envelope = [ordered]@{
        schemaVersion = 1
        categories = $validated
    }
    $json = $envelope | ConvertTo-Json -Depth 5
    [System.IO.File]::WriteAllText($temporaryPath, "$json$([Environment]::NewLine)", [System.Text.UTF8Encoding]::new($false))

    if (Test-Path -LiteralPath $destinationPath) {
        Move-Item -LiteralPath $temporaryPath -Destination $destinationPath -Force
    } else {
        Move-Item -LiteralPath $temporaryPath -Destination $destinationPath
    }

    Write-Output "Exported $($validated.Count) categories to $destinationPath"
} finally {
    if (Test-Path -LiteralPath $temporaryPath) {
        Remove-Item -LiteralPath $temporaryPath -Force
    }
    $env:CATEGORY_EXPORT_MONGO_URI = $previousUri
}
