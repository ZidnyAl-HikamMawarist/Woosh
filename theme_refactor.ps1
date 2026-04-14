$dir = "c:\Users\Thinkpad\AndroidStudioProjects\Woosh\app\src\main\java\com\example\woosh"

Get-ChildItem -Path $dir -Recurse -Filter "*.kt" | ForEach-Object {
    $text = Get-Content $_.FullName -Raw

    if ($text -match 'CrimsonRed') {
        # 1. Replace usages of CrimsonRed with ElegantDark
        $text = $text -replace 'CrimsonRed', 'ElegantDark'
        
        # 2. Fix imports to include PrimaryGold
        $text = $text -replace 'import com.example.woosh.ui.theme.ElegantDark', "import com.example.woosh.ui.theme.ElegantDark`r`nimport com.example.woosh.ui.theme.PrimaryGold"
        
        # 3. Add gold content color to buttons
        $text = $text -replace 'containerColor = ElegantDark\)', 'containerColor = ElegantDark, contentColor = PrimaryGold)'
        $text = $text -replace 'containerColor = if\(isRouteValid\) ElegantDark else Color\.Gray\)', 'containerColor = if(isRouteValid) ElegantDark else Color.Gray, contentColor = PrimaryGold)'
        
        # 4. Fix specific gradients
        $text = $text -replace 'Color\(0xFF8B0000\)', 'PrimaryGold'

        Set-Content -Path $_.FullName -Value $text
    }
}

Write-Host "Theme refactor applied successfully."
