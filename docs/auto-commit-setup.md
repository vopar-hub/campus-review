# 自动提交 Git 配置指南

## 方案 1：使用自动提交脚本（推荐）

### Windows
```bash
# 运行自动提交脚本
scripts\auto-commit.bat
```

### Linux/Mac
```bash
chmod +x scripts/auto-commit.sh
./scripts/auto-commit.sh
```

脚本会监视项目目录的文件变化，每 30 秒检查一次，检测到变化时自动提交。

---

## 方案 2：使用 Git 别名简化手动提交

将以下别名添加到 `~/.gitconfig`：

```ini
[alias]
    aa = !git add -A && git status
    ac = !git add -A && git commit -m
    aca = !git add -A && git commit -am
    acm = !git add -A && git commit
```

使用方法：
```bash
git aa              # 添加所有文件并查看状态
git ac "提交消息"    # 添加所有文件并提交
git aca "提交消息"   # 添加已跟踪文件并提交
git acm             # 添加所有文件，打开编辑器输入提交消息
```

---

## 方案 3：使用 VS Code 保存钩子（最自动化）

### 安装 VS Code 扩展
1. 安装 "Run on Save" 扩展（Emacsy 或类似）
2. 或使用 "Task Watcher" 扩展

### 配置 settings.json
在项目根目录创建 `.vscode/settings.json`：

```json
{
    "files.watcherExclude": {
        "**/.git/**": true
    },
    "editor.formatOnSave": true,
    "autoSave": "afterDelay",
    "autoSaveDelay": 1000
}
```

---

## 方案 4：使用 npm 包 watch-and-commit

```bash
# 安装依赖（需要 Node.js）
npm install -g @tooling/ci

# 或者使用 npm-watch
npm install -D npm-watch
```

---

## 注意事项

⚠️ **自动提交的缺点**：
- 提交消息不够具体，不利于代码审查
- 可能提交未完成的更改
- 增加 git 历史噪音

✅ **建议**：
- 开发阶段可以使用自动提交作为备份
- 重要功能完成后，手动整理提交历史（`git rebase -i`）
- 推送前确保提交消息清晰有意义
