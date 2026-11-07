#requires -Version 5.1
<#
.SYNOPSIS
    Emrodev/IPVIO-Stream -> bu depo manuel senkron.

.DESCRIPTION
    GitHub Actions olmadan upstream guncellemelerini ceker.
    - Cakisma yoksa: merge commit olusturur (yerelde).
    - Cakisma varsa: merge'u iptal eder, cakisan dosyalari raporlar.
    -Push verilirse: sonucu origin/main'e gonderir.

.EXAMPLE
    .\scripts\update-from-upstream.ps1 -CheckOnly

.EXAMPLE
    .\scripts\update-from-upstream.ps1

.EXAMPLE
    .\scripts\update-from-upstream.ps1 -Push
#>
[CmdletBinding()]
param(
    [switch]$CheckOnly,
    [switch]$Push,
    [string]$UpstreamUrl = 'https://github.com/Emrodev/IPVIO-Stream.git',
    [string]$UpstreamBranch = 'main',
    [string]$TargetBranch = 'main'
)

$ErrorActionPreference = 'Stop'

function Write-Step([string]$Text) { Write-Host "`n==> $Text" -ForegroundColor Cyan }
function Fail([string]$Text) { Write-Host "HATA: $Text" -ForegroundColor Red; exit 1 }

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $repoRoot
try {
    if (-not (Test-Path (Join-Path $repoRoot '.git'))) { Fail "Git deposu bulunamadi: $repoRoot" }

    Write-Step 'Mevcut dal ve temizlik kontrolu'
    $branch = (git rev-parse --abbrev-ref HEAD).Trim()
    if ($branch -ne $TargetBranch) { Fail "Hedef dal '$TargetBranch' ama su an '$branch' dasin. Once: git switch $TargetBranch" }
    if (git status --porcelain) { Fail 'Calisma agacinda degisiklik var. Once commit et veya stash uygula (git stash).' }
    Write-Host "Dal: $branch"

    Write-Step 'Upstream remote'
    $remotes = git remote
    if ($remotes -contains 'upstream') { git remote set-url upstream $UpstreamUrl }
    else { git remote add upstream $UpstreamUrl }
    Write-Host "upstream -> $UpstreamUrl"

    Write-Step 'Fetch'
    git fetch --no-tags --prune upstream $UpstreamBranch
    if ($LASTEXITCODE -ne 0) { Fail 'Upstream fetch basarisiz (ag veya URL).' }

    $upstreamRef = "upstream/$UpstreamBranch"
    git merge-base --is-ancestor $upstreamRef HEAD 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host 'Depo guncel. Yeni commit yok.' -ForegroundColor Green
        return
    }

    Write-Step 'Yeni commitler'
    git --no-pager log --oneline "HEAD..$upstreamRef"

    if ($CheckOnly) {
        Write-Host "`nSadece kontrol istendi; degisiklik yapilmadi." -ForegroundColor Yellow
        return
    }

    Write-Step 'Deneme birlestirmesi'
    git merge --no-ff --no-commit $upstreamRef
    if ($LASTEXITCODE -ne 0) {
        $conflicted = @(git diff --name-only --diff-filter=U)
        git merge --abort 2>$null
        Write-Host "`nCakisma var; birlestirme iptal edildi." -ForegroundColor Yellow
        if ($conflicted.Count -gt 0) {
            Write-Host 'Cakisan dosyalar:' -ForegroundColor Yellow
            $conflicted | ForEach-Object { Write-Host "  - $_" }
        }
        Write-Host @'

Oneri:
  1. git switch -c upstream-sync/manuel
  2. git merge upstream/main
  3. Cakismalari coz (Turkce tarafi koru: DictTr*.kt, README.tr.md, values-tr/)
  4. git add -A ; git commit
  5. git switch main ; git merge upstream-sync/manuel
'@
        exit 2
    }

    if (git diff --cached --quiet) {
        Write-Host 'Birlestirilecek degisiklik yok.' -ForegroundColor Green
        return
    }

    $msg = "chore(upstream): Emrodev/IPVIO-Stream senkronu $(Get-Date -Format 'yyyy-MM-dd')"
    git commit -m $msg | Out-Null
    if ($LASTEXITCODE -ne 0) { Fail 'Commit olusturulamadi.' }
    Write-Host "Merge commit olusturuldu: $msg" -ForegroundColor Green

    if ($Push) {
        Write-Step "Push -> origin/$TargetBranch"
        git push origin "HEAD:$TargetBranch"
        if ($LASTEXITCODE -ne 0) { Fail 'Push basarisiz.' }
        Write-Host 'Push tamam.' -ForegroundColor Green
    }
    else {
        Write-Host "`nYerelde birlestirildi. Gonderleme icin: git push origin $TargetBranch" -ForegroundColor Yellow
    }
}
finally {
    Pop-Location
}
