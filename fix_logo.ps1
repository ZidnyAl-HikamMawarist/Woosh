Add-Type -AssemblyName System.Drawing

$inPath = "c:\Users\Thinkpad\AndroidStudioProjects\Woosh\app\src\main\res\drawable\logo.png"
$outPath = "c:\Users\Thinkpad\AndroidStudioProjects\Woosh\app\src\main\res\drawable\logo_square.png"

try {
    $img = [System.Drawing.Image]::FromFile($inPath)
    $maxDim = [math]::Max($img.Width, $img.Height)

    # Reduce padding dramatically to enlarge the logo by ~40%
    $paddedDim = [int]($maxDim * 1.06)

    $bmp = New-Object System.Drawing.Bitmap $paddedDim, $paddedDim
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::White)

    $x = [int](($paddedDim - $img.Width) / 2)
    $y = [int](($paddedDim - $img.Height) / 2)

    $g.DrawImage($img, $x, $y, $img.Width, $img.Height)

    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)

    $g.Dispose()
    $bmp.Dispose()
    $img.Dispose()
    Write-Host "Success: Generated logo_square.png"
} catch {
    Write-Host "Error: $_"
}
