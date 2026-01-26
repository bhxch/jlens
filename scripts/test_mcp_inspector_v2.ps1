# MCP Inspector Integration Test Script (Improved Version)
# Tests the javastub-mcp-server using MCP Inspector CLI

$ErrorActionPreference = "Continue"
$ConfigPath = "E:\repos\0000\javastub\mcp-inspector-config.json"
$ServerName = "javastub-mcp-server"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MCP Inspector Integration Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Initialize test results array
$script:TestResults = @()
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0

function Test-Tool {
    param(
        [string]$ToolName,
        [string]$Arguments,
        [string]$TestName,
        [scriptblock]$Validate
    )
    
    $script:TotalTests++
    Write-Host "[$script:TotalTests] Testing: $TestName..." -ForegroundColor Yellow
    
    try {
        $cmd = "npx @modelcontextprotocol/inspector --cli --config `\"$ConfigPath`\" --server $ServerName --method tools/call --tool-name $ToolName $Arguments"
        $output = Invoke-Expression $cmd 2>&1 | Out-String
        
        if (& $Validate $output) {
            $script:PassedTests++
            $script:TestResults += [PSCustomObject]@{
                Test = $TestName
                Status = "PASS"
                Tool = $ToolName
            }
            Write-Host "  ✓ PASS" -ForegroundColor Green
            return $true
        } else {
            $script:FailedTests++
            $script:TestResults += [PSCustomObject]@{
                Test = $TestName
                Status = "FAIL"
                Tool = $ToolName
                Output = $output
            }
            Write-Host "  ✗ FAIL" -ForegroundColor Red
            Write-Host "  Output: $output" -ForegroundColor Gray
            return $false
        }
    } catch {
        $script:FailedTests++
        $script:TestResults += [PSCustomObject]@{
            Test = $TestName
            Status = "ERROR"
            Tool = $ToolName
            Error = $_.Exception.Message
        }
        Write-Host "  ✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Test 1: List all tools
Write-Host "Test Suite 1: Server Initialization" -ForegroundColor Cyan
Write-Host "--------------------------------------" -ForegroundColor Cyan

$script:TotalTests++
Write-Host "[$script:TotalTests] Testing: List tools..." -ForegroundColor Yellow
try {
    $cmd = "npx @modelcontextprotocol/inspector --cli --config `\"$ConfigPath`\" --server $ServerName --method tools/list"
    $output = Invoke-Expression $cmd 2>&1 | Out-String
    
    if ($output -match '"tools":\s*\[' -and 
        $output -match 'inspect_java_class' -and 
        $output -match 'list_module_dependencies' -and 
        $output -match 'search_java_class' -and 
        $output -match 'build_module') {
        $script:PassedTests++
        $script:TestResults += [PSCustomObject]@{
            Test = "List tools"
            Status = "PASS"
            Tool = "server"
        }
        Write-Host "  ✓ PASS - All 4 tools found" -ForegroundColor Green
    } else {
        $script:FailedTests++
        $script:TestResults += [PSCustomObject]@{
            Test = "List tools"
            Status = "FAIL"
            Tool = "server"
            Output = $output
        }
        Write-Host "  ✗ FAIL - Not all tools found" -ForegroundColor Red
    }
} catch {
    $script:FailedTests++
    $script:TestResults += [PSCustomObject]@{
        Test = "List tools"
        Status = "ERROR"
        Tool = "server"
        Error = $_.Exception.Message
    }
    Write-Host "  ✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: inspect_java_class
Write-Host "Test Suite 2: inspect_java_class" -ForegroundColor Cyan
Write-Host "--------------------------------" -ForegroundColor Cyan

Test-Tool -ToolName "inspect_java_class" `
          -Arguments "--tool-arg className=java.util.List" `
          -TestName "Inspect java.util.List" `
          -Validate { param($out) $out -match 'className' -and $out -match 'java.util.List' -and $out -match 'isError":\s*false' }

Test-Tool -ToolName "inspect_java_class" `
          -Arguments "--tool-arg className=java.util.ArrayList" `
          -TestName "Inspect java.util.ArrayList" `
          -Validate { param($out) $out -match 'className' -and $out -match 'java.util.ArrayList' -and $out -match 'isError":\s*false' }

Test-Tool -ToolName "inspect_java_class" `
          -Arguments "--tool-arg className=java.lang.String" `
          -TestName "Inspect java.lang.String" `
          -Validate { param($out) $out -match 'className' -and $out -match 'java.lang.String' -and $out -match 'isError":\s*false' }

Write-Host ""

# Test 3: list_module_dependencies
Write-Host "Test Suite 3: list_module_dependencies" -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Cyan

Test-Tool -ToolName "list_module_dependencies" `
          -Arguments "--tool-arg pomFilePath=pom.xml" `
          -TestName "List dependencies with pom.xml" `
          -Validate { param($out) $out -match 'Dependencies' -and $out -match 'isError":\s*false' }

Write-Host ""

# Test 4: search_java_class
Write-Host "Test Suite 4: search_java_class" -ForegroundColor Cyan
Write-Host "-------------------------------" -ForegroundColor Cyan

Test-Tool -ToolName "search_java_class" `
          -Arguments "--tool-arg classNamePattern=*List* --tool-arg searchType=wildcard" `
          -TestName "Search for *List* pattern" `
          -Validate { param($out) $out -match 'results' -and $out -match 'totalResults' -and $out -match 'isError":\s*false' }

Test-Tool -ToolName "search_java_class" `
          -Arguments "--tool-arg classNamePattern=String --tool-arg searchType=prefix" `
          -TestName "Search for String prefix" `
          -Validate { param($out) $out -match 'results' -and $out -match 'isError":\s*false' }

Write-Host ""

# Test 5: build_module
Write-Host "Test Suite 5: build_module" -ForegroundColor Cyan
Write-Host "--------------------------" -ForegroundColor Cyan

Test-Tool -ToolName "build_module" `
          -Arguments "--tool-arg sourceFilePath=E:\repos\0000\javastub\src\main\java\io\github\bhxch\mcp\javastub\Main.java" `
          -TestName "Build module" `
          -Validate { param($out) $out -match 'isError":\s*false' }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests: $script:TotalTests" -ForegroundColor White
Write-Host "Passed: $script:PassedTests" -ForegroundColor Green
Write-Host "Failed: $script:FailedTests" -ForegroundColor $(if ($script:FailedTests -gt 0) { "Red" } else { "Green" })
Write-Host "Pass Rate: $([math]::Round(($script:PassedTests / $script:TotalTests) * 100, 2))%" -ForegroundColor $(if ($script:PassedTests -eq $script:TotalTests) { "Green" } else { "Yellow" })
Write-Host ""

# Export results
$ResultsFile = "E:\repos\0000\javastub\mcp_inspector_test_results.json"
$script:TestResults | ConvertTo-Json -Depth 3 | Out-File -FilePath $ResultsFile -Encoding UTF8
Write-Host "Results saved to: $ResultsFile" -ForegroundColor Gray

# Show detailed results
Write-Host ""
Write-Host "Detailed Test Results:" -ForegroundColor Cyan
$script:TestResults | Format-Table -AutoSize

Write-Host ""
if ($script:FailedTests -eq 0) {
    Write-Host "✓ All tests passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ Some tests failed. Please review the output above." -ForegroundColor Red
    exit 1
}