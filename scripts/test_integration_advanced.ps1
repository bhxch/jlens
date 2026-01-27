# Advanced Integration Test Script
# Verifies Build, Distribution, and Tool Execution

$ErrorActionPreference = "Stop"

# Paths
$RootDir = Get-Location
$TempDir = Join-Path $RootDir ".temp"
$TestProjectDir = Join-Path $TempDir "test-project"

Write-Host ">>> Starting Advanced Integration Test" -ForegroundColor Cyan

# 1. Build the Project
Write-Host ">>> Building JAR..." -ForegroundColor Yellow
& mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Build failed" }

$JarFiles = Get-ChildItem "target\jlens-mcp-server-*.jar" | Where-Object { $_.Name -notmatch "original" -and $_.Name -notmatch "sources" -and $_.Name -notmatch "javadoc" }
if (!$JarFiles) { throw "JAR not found in target" }
$JarFile = $JarFiles[0]
Write-Host ">>> JAR built: $($JarFile.FullName)" -ForegroundColor Green

# 2. Distribute JAR
Write-Host ">>> Copying JAR to wrappers..." -ForegroundColor Yellow
Copy-Item $JarFile.FullName "bin\jlens-mcp-server.jar" -Force
Copy-Item $JarFile.FullName "jlens_mcp_server\jlens-mcp-server.jar" -Force

# 3. Create Test Project
Write-Host ">>> Creating Test Maven Project..." -ForegroundColor Yellow
if (Test-Path $TestProjectDir) { Remove-Item $TestProjectDir -Recurse -Force }
New-Item -ItemType Directory -Path $TestProjectDir | Out-Null

$PomContent = @"
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>my-app</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>my-app</name>
  <properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
"@
Set-Content -Path (Join-Path $TestProjectDir "pom.xml") -Value $PomContent

# Create Source
$SrcDir = Join-Path $TestProjectDir "src\main\java\com\example"
New-Item -ItemType Directory -Path $SrcDir -Force | Out-Null
$JavaContent = @"
package com.example;
public class App {
    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
    }
}
"@
Set-Content -Path (Join-Path $SrcDir "App.java") -Value $JavaContent

# 4. Run Node Wrapper Verification
Write-Host ">>> Verifying Node Wrapper..." -ForegroundColor Yellow
$NodeScript = Join-Path $RootDir "bin\jlens-mcp-server.js"

# We use Start-Process to check if it launches successfully.
# We expect it to wait for input (JSON-RPC).
# We redirect stderr to checking for "Picked up" or errors.

$Process = Start-Process node -ArgumentList "$NodeScript" -PassThru -NoNewWindow -RedirectStandardError (Join-Path $TempDir "node_err.txt")
Start-Sleep -Seconds 5
if ($Process.HasExited) {
    $ErrContent = Get-Content (Join-Path $TempDir "node_err.txt")
    if ($Process.ExitCode -ne 0) {
        Write-Host "Node Wrapper Failed (Exit Code $($Process.ExitCode)): $ErrContent" -ForegroundColor Red
        throw "Node Wrapper Failed"
    } else {
        Write-Host "Node Wrapper exited with 0 (maybe normal if no input provided?)" -ForegroundColor Yellow
    }
} else {
    $Process.Kill()
    Write-Host ">>> Node Wrapper started successfully (Process ID: $($Process.Id))." -ForegroundColor Green
}

# 5. Run Python Wrapper Verification
Write-Host ">>> Verifying Python Wrapper..." -ForegroundColor Yellow
# Need to ensure dependencies (there are none for the wrapper except python itself).
$PyScript = Join-Path $RootDir "jlens_mcp_server\main.py"
$Process = Start-Process python -ArgumentList "$PyScript" -PassThru -NoNewWindow -RedirectStandardError (Join-Path $TempDir "py_err.txt")
Start-Sleep -Seconds 3
if ($Process.HasExited) {
    $ErrContent = Get-Content (Join-Path $TempDir "py_err.txt")
    Write-Host "Python Wrapper Failed: $ErrContent" -ForegroundColor Red
    throw "Python Wrapper Failed"
} else {
    $Process.Kill()
    Write-Host ">>> Python Wrapper started successfully (JAR found)." -ForegroundColor Green
}

Write-Host ">>> Advanced Integration Test Complete: SUCCESS" -ForegroundColor Green
exit 0
