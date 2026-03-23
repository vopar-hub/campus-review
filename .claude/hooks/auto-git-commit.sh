#!/bin/bash
# auto-git-commit.sh - 在文件修改后自动提交 git

# 从 stdin 读取 hook 输入
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

# 如果没有文件路径，直接退出
if [ -z "$FILE_PATH" ]; then
  exit 0
fi

# 切换到当前目录（项目根目录）
cd "$(pwd)" || exit 0
PROJECT_ROOT="$(pwd)"

# 检查是否是 git 仓库
if ! git rev-parse --git-dir > /dev/null 2>&1; then
  exit 0
fi

# 检查文件是否存在
if [ ! -f "$FILE_PATH" ]; then
  exit 0
fi

# 将 Windows 路径转换为 Unix 格式
# E:\_CODE_\campus-review -> /e/_CODE_/campus-review
FILE_PATH_UNIX=$(echo "$FILE_PATH" | sed 's/^\([A-Za-z]\):/\/\L\1/' | sed 's/\\/\//g')

# 获取相对路径
RELATIVE_PATH="${FILE_PATH_UNIX#$PROJECT_ROOT/}"

# 如果还是绝对路径，说明匹配失败
if [[ "$RELATIVE_PATH" == "$FILE_PATH_UNIX" ]]; then
  # 无法转换为相对路径，使用原始路径
  RELATIVE_PATH="$FILE_PATH_UNIX"
fi

# 根据文件类型生成提交信息前缀
COMMIT_PREFIX="chore"
if [[ "$RELATIVE_PATH" == *.java ]]; then
  COMMIT_PREFIX="refactor"
elif [[ "$RELATIVE_PATH" == *.xml ]]; then
  COMMIT_PREFIX="build"
elif [[ "$RELATIVE_PATH" == application*.yml ]] || [[ "$RELATIVE_PATH" == application*.properties ]]; then
  COMMIT_PREFIX="config"
elif [[ "$RELATIVE_PATH" == *.md ]]; then
  COMMIT_PREFIX="docs"
fi

# 检查是否是新增文件
IS_NEW_FILE="false"
if git status --porcelain "$RELATIVE_PATH" 2>/dev/null | grep -q "^A"; then
  IS_NEW_FILE="true"
fi

# 添加文件到 git
git add "$RELATIVE_PATH" 2>/dev/null

# 如果有变化，提交
if ! git diff --cached --quiet 2>/dev/null; then
  # 生成提交信息
  if [ "$IS_NEW_FILE" = "true" ]; then
    COMMIT_MSG="$COMMIT_PREFIX: 新增 $RELATIVE_PATH"
  else
    COMMIT_MSG="$COMMIT_PREFIX: 修改 $RELATIVE_PATH"
  fi

  git commit -m "$COMMIT_MSG" 2>/dev/null
fi

exit 0
