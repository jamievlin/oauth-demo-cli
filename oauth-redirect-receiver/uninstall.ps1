#!/usr/bin/env pwsh

param($InstallLocation)

$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-Not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Output "Please run as administrator!"
    exit 1
}

Remove-Item $InstallLocation -Force -Recurse
Remove-Item -Path Registry::HKEY_CLASSES_ROOT\toauth\ -Recurse -Force
