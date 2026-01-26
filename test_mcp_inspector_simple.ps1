# MCP Inspector Integration Test Script (Simple Version)
# Tests the javastub-mcp-server using MCP Inspector CLI

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MCP Inspector Integration Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$TestResults = @()
$PassCount = 0
$FailCount = 0

function Run-Test {
    param(
        [string]$TestName,
        [string]$Command,
        [scriptblock]$Validate
    )
    
    Write-Host "Testing: $TestName..." -ForegroundColor Yellow
    
    try {
        $output = cmd /c $Command 2>&1 | Out-String
        
        if (& $Validate $output) {
            $PassCount++
            $TestResults += [PSCustomObject]@{
                Test = $TestName
                Status = "PASS"
            }
            Write-Host "  ✓ PASS" -ForegroundColor Green
            return $true
        } else {
            $FailCount++
            $TestResults += [PSCustomObject]@{
                Test = $TestName
                Status = "FAIL"
                Output = $output
            }
            Write-Host "  ✗ FAIL" -ForegroundColor Red
            return $false
        }
    } catch {
        $FailCount++
        $TestResults += [PSCustomObject]@{
            Test = $TestName
            Status = "ERROR"
            Error = $_.Exception.Message
        }
        Write-Host "  ✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Test 1: List tools
Run-Test -TestName "List tools" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/list" `
        -Validate { param($out) $out -match 'inspect_java_class' -and $out -match 'list_module_dependencies' -and $out -match 'search_java_class' -and $out -match 'build_module' }

Write-Host ""

# Test 2: inspect_java_class
Write-Host "Test Suite: inspect_java_class" -ForegroundColor Cyan
Run-Test -TestName "Inspect java.util.List" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name inspect_java_class --tool-arg className=java.util.List" `
        -Validate { param($out) $out -match 'className' -and $out -match 'java.util.List' -and $out -match 'isError":\s*false' }

Run-Test -TestName "Inspect java.util.ArrayList" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name inspect_java_class --tool-arg className=java.util.ArrayList" `
        -Validate { param($out) $out -match 'className' -and $out -match 'java.util.ArrayList' -and $out -match 'isError":\s*false' }

Run-Test -TestName "Inspect java.lang.String" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name inspect_java_class --tool-arg className=java.lang.String" `
        -Validate { param($out) $out -match 'className' -and $out -match 'java.lang.String' -and $out -match 'isError":\s*false' }

Write-Host ""

# Test 3: list_module_dependencies
Write-Host "Test Suite: list_module_dependencies" -ForegroundColor Cyan
Run-Test -TestName "List dependencies with pom.xml" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name list_module_dependencies --tool-arg pomFilePath=pom.xml" `
        -Validate { param($out) $out -match 'Dependencies' -and $out -match 'isError":\s*false' }

Write-Host ""

# Test 4: search_java_class
Write-Host "Test Suite: search_java_class" -ForegroundColor Cyan
Run-Test -TestName "Search for *List* pattern" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name search_java_class --tool-arg classNamePattern=*List* --tool-arg searchType=wildcard" `
        -Validate { param($out) $out -match 'results' -and $out -match 'totalResults' -and $out -match 'isError":\s*false' }

Run-Test -TestName "Search for String prefix" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name search_java_class --tool-arg classNamePattern=String --tool-arg searchType=prefix" `
        -Validate { param($out) $out -match 'results' -and $out -match 'isError":\s*false' }

Write-Host ""

# Test 5: build_module
Write-Host "Test Suite: build_module" -ForegroundColor Cyan
Run-Test -TestName "Build module" `
        -Command "npx @modelcontextprotocol/inspector --cli --config E:\repos\0000\javastub\mcp-inspector-config.json --server javastub-mcp-server --method tools/call --tool-name build_module --tool-arg sourceFilePath=E:\repos\0000\javastub\src\main\java\io\github\bhxch\mcp\javastub\Main.java" `
        -Validate { param($out) $out -match 'isError":\s*false' }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests: $($TestResults.Count)" -ForegroundColor White
Write-Host "Passed: $PassCount" -ForegroundColor Green
Write-Host "Failed: $FailCount" -ForegroundColor $(if ($FailCount -gt 0) { "Red" } else { "Green" })
$PassRate = if ($TestResults.Count -gt 0) { [math]::Round(($PassCount / $TestResults.Count) * 100, 2) } else { 0 }
Write-Host "Pass Rate: $PassRate%" -ForegroundColor $(if ($PassCount -eq $TestResults.Count -and $TestResults.Count -gt 0) { "Green" } else { "Yellow" })
Write-Host ""

# Export results
$ResultsFile = "E:\repos\0000\javastub\mcp_inspector_test_results.json"
$TestResults | ConvertTo-Json -Depth 3 | Out-File -FilePath $ResultsFile -Encoding UTF8
Write-Host "Results saved to: $ResultsFile" -ForegroundColor Gray

# Show detailed results
Write-Host ""
Write-Host "Detailed Test Results:" -ForegroundColor Cyan
$TestResults | Format-Table -AutoSize

Write-Host ""
if ($FailCount -eq 0) {
    Write-Host "✓ All tests passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ Some tests failed. Please review the output above." -ForegroundColor Red
    exit 1
}