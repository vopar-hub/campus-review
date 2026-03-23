# auto-git-commit.ps1 - 在文件修改后自动提交 git

param(
    [Parameter(ValueFromPipeline = $true)]
    $InputData
)

# 从 stdin 读取 JSON 输入
if (-not $InputData) {
    $InputData = [Console]::In.ReadToEnd()
}

try {
    $JsonInput = $InputData | ConvertFrom-Json -ErrorAction SilentlyContinue
} catch {
    exit 0
}

if (-not $JsonInput) {
    exit 0
}

$FilePath = $JsonInput.tool_input.file_path

# 如果没有文件路径，直接退出
if (-not $FilePath) {
    exit 0
}

# 获取项目根目录
$ProjectRoot = (Get-Location).Path

# 检查是否是 git 仓库
$GitDir = Join-Path $ProjectRoot ".git"
if (-not (Test-Path $GitDir)) {
    exit 0
}

# 检查文件是否存在
if (-not (Test-Path $FilePath)) {
    exit 0
}

# 获取文件的相对路径
try {
    $RelativePath = (Resolve-Path $FilePath -Relative).TrimStart(".\").Replace("\", "/")
} catch {
    $RelativePath = $FilePath.Replace("\", "/")
}

# 根据文件类型生成提交信息前缀
$CommitPrefix = "chore"
if ($RelativePath -like "*.java") {
    $CommitPrefix = "refactor"
} elseif ($RelativePath -like "*.xml") {
    $CommitPrefix = "build"
} elseif (($RelativePath -like "application*.yml") -or ($RelativePath -like "application*.properties")) {
    $CommitPrefix = "config"
} elseif ($RelativePath -like "*.md") {
    $CommitPrefix = "docs"
}

# 检查是否是新增文件
$IsNewFile = $false
$GitStatus = git status --porcelain $RelativePath 2>$null
if ($GitStatus -match "^A\s") {
    $IsNewFile = $true
}

# 添加文件到 git
git add $RelativePath 2>$null

# 检查是否有变化需要提交
$DiffResult = git diff --cached --quiet 2>$null
if ($LASTEXITCODE -ne 0) {
    # 有变化，生成提交信息
    if ($IsNewFile) {
        $CommitMsg = "$CommitPrefix`: 新增 $RelativePath"
    } else {
        $CommitMsg = "$CommitPrefix`: 修改 $RelativePath"
    }

    # 执行提交
    git commit -m $CommitMsg 2>$null
}

exit 0
