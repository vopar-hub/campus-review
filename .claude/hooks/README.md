PowerShell 自动 git 提交钩子

## 配置完成

**Hook 脚本**: `.claude/hooks/auto-git-commit.ps1`
**配置文件**: `.claude/settings.local.json`

## 功能

每次 `Write` 或 `Edit` 工具修改文件后自动提交到 git。

## 提交信息格式

| 文件类型 | 前缀 | 示例 |
|---------|------|------|
| `.java` | `refactor` | `refactor: 修改 xxx.java` |
| `.xml` | `build` | `build: 修改 pom.xml` |
| `application*.yml/properties` | `config` | `config: 修改 application.yml` |
| `.md` | `docs` | `docs: 修改 README.md` |
| 新增文件 | `chore: 新增` | `chore: 新增 xxx.java` |
| 其他 | `chore` | `chore: 修改 xxx.txt` |

## 要求

- PowerShell 5.1+ (Windows 10/11 预装)
- 首次使用可能需要执行：`Set-ExecutionPolicy RemoteSigned -Scope CurrentUser`
