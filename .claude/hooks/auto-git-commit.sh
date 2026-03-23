#!/bin/bash
# auto-git-commit.sh - 在文件修改后自动提交 git

# 从 stdin 读取 hook 输入
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
CONTENT=$(echo "$INPUT" | jq -r '.tool_input.content // empty')
TOOL_NAME=$(echo "$INPUT" | jq -r '.hook_event_name // empty')

# 如果没有文件路径，直接退出
if [ -z "$FILE_PATH" ]; then
  exit 0
fi

# 获取项目根目录
PROJECT_ROOT="$CLAUDE_PROJECT_DIR"

# 切换到项目根目录
cd "$PROJECT_ROOT" || exit 0

# 检查是否是 git 仓库
if ! git rev-parse --git-dir > /dev/null 2>&1; then
  exit 0
fi

# 检查文件是否存在
if [ ! -f "$FILE_PATH" ]; then
  exit 0
fi

# 获取文件的相对路径
RELATIVE_PATH="${FILE_PATH#$PROJECT_ROOT/}"

# 根据文件类型生成提交信息前缀
COMMIT_PREFIX="chore"
if [[ "$RELATIVE_PATH" == *".java" ]]; then
  if [[ "$CONTENT" == *"class "* ]] || [[ "$CONTENT" == *@* ]]; then
    COMMIT_PREFIX="feat"
  else
    COMMIT_PREFIX="refactor"
  fi
elif [[ "$RELATIVE_PATH" == *".xml" ]]; then
  COMMIT_PREFIX="build"
elif [[ "$RELATIVE_PATH" == *"application"*".yml" ]] || [[ "$RELATIVE_PATH" == *"application"*".properties" ]]; then
  COMMIT_PREFIX="config"
elif [[ "$RELATIVE_PATH" == *".md" ]]; then
  COMMIT_PREFIX="docs"
elif [[ "$RELATIVE_PATH" == *"test"* ]]; then
  COMMIT_PREFIX="test"
fi

# 添加文件到 git
git add "$RELATIVE_PATH" 2>/dev/null

# 如果有变化，提交
if ! git diff --cached --quiet 2>/dev/null; then
  # 获取当前时间戳
  TIMESTAMP=$(date "+%H:%M:%S")

  # 生成提交信息
  COMMIT_MSG="$COMMIT_PREFIX: 更新 $RELATIVE_PATH [$TIMESTAMP]"

  git commit -m "$COMMIT_MSG" 2>/dev/null
fi

exit 0
