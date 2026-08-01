# Regenerate module graph Mermaid sources (from module-deps.json) and PNG outputs.
# Requires Node.js (npx downloads @mermaid-js/mermaid-cli on first run).
#
# Usage: .\scripts\render-module-graphs.ps1

$ErrorActionPreference = "Stop"
$scriptsDir = $PSScriptRoot

Write-Host "Generating Mermaid from module-deps.json ..."
& node (Join-Path $scriptsDir "generate-module-graphs.mjs")

Write-Host "Rendering PNGs ..."
& node (Join-Path $scriptsDir "render-module-graphs.mjs")

Write-Host "Done."
