#!/usr/bin/env pwsh

param($InstallLocation, $PythonLocation=$null, $FindByPythonConsole=$false)

if ($null -eq $InstallLocation) {
    Write-Output "Please specify an install location."
    Exit 1
}

$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-Not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Output "Please run as administrator!"
    exit 1
}

if ($null -eq $PythonLocation) {
    Write-Output "Python executable not specified. Will attempt to locate one"
    try {
        if ($FindByPythonConsole) {
            $PythonLocation = (Get-Command python.exe).Path
        } else {
            $PythonLocation = (Get-Command pythonw.exe).Path
        }
    } catch {
        Write-Output "Python executable not found. Cannot install"
        Exit 1
    }

    Write-Output "Found python at $PythonLocation"
}

Write-Output "Installing to $InstallLocation"

New-Item -ItemType Directory -Path $InstallLocation -Force >$null
Copy-Item -Path .\receiver.py -Destination $InstallLocation\receiver.py -Force


# Register toauth:// URL handler
$ToAuthRoot = "Registry::HKEY_CLASSES_ROOT\toauth"
New-Item -Path $ToAuthRoot -Force
New-ItemProperty -Name 'URL Protocol' -PropertyType String -Value "" -Path $ToAuthRoot

$ToAuthCommand = "$ToAuthRoot\shell\open\command"
New-Item -Path $ToAuthCommand -Force
Set-Item -Path $ToAuthCommand -Value "`"$PythonLocation`" `"$InstallLocation\receiver.py`" `"%1`""

Write-Output "Install complete."
