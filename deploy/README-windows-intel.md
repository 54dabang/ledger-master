# Ledger Windows Intel Docker 离线部署包

这个文档说明如何在 Windows Intel 电脑上离线运行 Ledger 项目。最终运行包包含：

- `ledger-app`：内置 `ledger-admin.jar` 和 JDK 11 运行环境。
- `ledger-mysql`：MySQL 8.0.36，首次启动空数据卷时自动导入本机导出的 `ledger`、`manager` 数据库。
- `redis:7.2-alpine`：Redis，密码默认 `12345678`。
- `ledger-nginx`：Nginx，内置本机 nginx 当前部署的前端静态文件。

## 包内容

离线包文件名类似：

```text
ledger-windows-intel-docker-3.9.0-linux-amd64.zip
```

解压后目录结构：

```text
ledger-windows-intel/
  .env
  README.md
  docker-compose.yml
  ledger-images-linux-amd64.tar
  load-images.ps1
  start.bat
  start.ps1
  stop.bat
  stop.ps1
```

`ledger-images-linux-amd64.tar` 已包含所有运行所需镜像，不需要 Windows 机器访问外网拉镜像。

## Windows 部署前准备

1. 使用 Windows Intel/x86_64 电脑。
2. 安装 Docker Desktop。
3. Docker Desktop 使用 Linux containers 模式。
4. 确认 Docker Desktop 已启动。
5. 确认本机端口 `8081`、`7070`、`3306`、`6379` 未被占用，或先修改 `.env` 里的端口。

检查 Docker：

```powershell
docker version
docker compose version
```

## Windows 离线启动

1. 将 `ledger-windows-intel-docker-*.zip` 拷贝到 Windows 电脑。
2. 解压 zip。
3. 进入解压后的 `ledger-windows-intel` 目录。
4. 双击 `start.bat`。

也可以在 PowerShell 中执行：

```powershell
cd .\ledger-windows-intel
.\start.ps1
```

`start.ps1` 会自动执行：

1. `docker load -i ledger-images-linux-amd64.tar`
2. 校验镜像平台。
3. `docker compose up -d`

启动完成后访问：

```text
http://localhost:8081
```

## 停止服务

```powershell
.\stop.ps1
```

或双击：

```text
stop.bat
```

停止服务不会删除 MySQL、Redis 和上传文件数据卷。

## 查看运行状态

```powershell
docker compose --env-file .env -f docker-compose.yml ps
```

查看后端日志：

```powershell
docker logs -f ledger-app
```

查看 Nginx 日志：

```powershell
docker logs -f ledger-nginx
```

查看 MySQL 日志：

```powershell
docker logs -f ledger-mysql
```

## 默认端口和密码

可在 `.env` 中修改：

```text
WEB_PORT=8081
APP_PORT=7070
MYSQL_PORT=3306
REDIS_PORT=6379
MYSQL_ROOT_PASSWORD=rootroot
REDIS_PASSWORD=12345678
```

修改 `.env` 后重新启动：

```powershell
.\stop.ps1
.\start.ps1
```

常用访问地址：

```text
前端页面: http://localhost:8081
后端接口: http://localhost:7070
MySQL: localhost:3306
Redis: localhost:6379
```

## 数据说明

MySQL 镜像内置了打包时从本机导出的数据库：

```text
ledger
manager
```

MySQL 数据只会在 `ledger_mysql_data` 数据卷第一次创建时导入。后续重启不会重复覆盖数据库。

如果需要清空 Windows 上已有数据并重新导入包内初始数据：

```powershell
docker compose --env-file .env -f docker-compose.yml down -v
.\start.ps1
```

注意：`down -v` 会删除该部署创建的 MySQL、Redis、上传文件和日志数据卷。

## 离线包校验

如果同时拿到了 `.sha256` 文件，可以在 Windows PowerShell 中校验：

```powershell
Get-FileHash .\ledger-windows-intel-docker-3.9.0-linux-amd64.zip -Algorithm SHA256
```

将输出值与 `.sha256` 文件中的第一列对比。

## 常见问题

### PowerShell 不允许执行脚本

使用 `start.bat` 启动，或在当前 PowerShell 会话中执行：

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\start.ps1
```

### 端口被占用

修改 `.env` 中的端口，例如：

```text
WEB_PORT=18081
APP_PORT=17070
MYSQL_PORT=13306
REDIS_PORT=16379
```

然后重新启动：

```powershell
.\start.ps1
```

访问地址也要同步改为：

```text
http://localhost:18081
```

### MySQL 没有重新导入数据

这是正常行为。MySQL 官方镜像只会在 `/var/lib/mysql` 为空时执行初始化 SQL。

需要重新导入时执行：

```powershell
docker compose --env-file .env -f docker-compose.yml down -v
.\start.ps1
```

### 页面打不开

按顺序检查：

```powershell
docker compose --env-file .env -f docker-compose.yml ps
docker logs ledger-nginx
docker logs ledger-app
```

确认浏览器访问的是 `.env` 中的 `WEB_PORT`：

```text
http://localhost:8081
```

### 后端启动失败

查看日志：

```powershell
docker logs -f ledger-app
```

重点检查 MySQL、Redis 是否健康：

```powershell
docker compose --env-file .env -f docker-compose.yml ps
```

## 本机 M4 打包说明

本项目当前本机是 Apple Silicon/M4，目标平台是 Windows Intel，所以不能直接保存本机默认镜像。默认镜像通常是 `linux/arm64`，Windows Intel 需要 `linux/amd64`。

代码更改后，在 Mac 本机执行这个脚本即可重新生成 Windows Intel 离线部署包：

```bash
./deploy/scripts/build-local-windows-intel-package.sh
```

脚本会：

1. 构建 `ledger-admin.jar`。
2. 使用 Buildx/QEMU 构建 `linux/amd64` 镜像。
3. 使用国内镜像加速源拉取基础镜像。
4. 在 `docker save` 前校验镜像平台必须是 `linux/amd64`。
5. 生成 `dist/ledger-windows-intel-docker-*.zip`。
6. 生成并校验 `dist/ledger-windows-intel-docker-*.zip.sha256`。
7. 默认执行 zip 完整性检查。

默认产物：

```text
dist/ledger-windows-intel-docker-3.9.0-linux-amd64.zip
dist/ledger-windows-intel-docker-3.9.0-linux-amd64.zip.sha256
dist/ledger-windows-intel/
```

### 常用打包命令

只重新打包后端代码和 Docker 离线包：

```bash
./deploy/scripts/build-local-windows-intel-package.sh
```

后端代码改了，但数据库和前端没有变时，用上面这条即可。

同时刷新本机 MySQL 数据库 dump：

```bash
REFRESH_DB=1 ./deploy/scripts/build-local-windows-intel-package.sh
```

同时同步本机 nginx 当前部署的前端静态文件：

```bash
REFRESH_FRONTEND=1 ./deploy/scripts/build-local-windows-intel-package.sh
```

数据库、前端、后端一起刷新并打包：

```bash
REFRESH_DB=1 REFRESH_FRONTEND=1 ./deploy/scripts/build-local-windows-intel-package.sh
```

如果已经手动构建过 jar，只想复用 `ledger-admin/target/ledger-admin.jar`：

```bash
SKIP_MAVEN=1 ./deploy/scripts/build-local-windows-intel-package.sh
```

可指定镜像 tag：

```bash
IMAGE_TAG=3.9.0-linux-amd64 ./deploy/scripts/build-local-windows-intel-package.sh
```

可指定 Docker Hub 镜像加速前缀：

```bash
DOCKER_MIRROR=docker.m.daocloud.io ./deploy/scripts/build-local-windows-intel-package.sh
```

默认 Redis 使用：

```text
docker.m.daocloud.io/library/redis:7.2-alpine
```

如需指定 Redis 来源：

```bash
REDIS_SOURCE_IMAGE=docker.m.daocloud.io/library/redis:7.2-alpine ./deploy/scripts/build-local-windows-intel-package.sh
```

跳过 zip 完整性检查：

```bash
SKIP_ZIP_TEST=1 ./deploy/scripts/build-local-windows-intel-package.sh
```

### 脚本参数

```text
IMAGE_TAG           镜像和 zip 使用的 tag，默认 3.9.0-linux-amd64
DOCKER_MIRROR       Docker Hub 镜像加速前缀，默认 docker.1ms.run
REDIS_SOURCE_IMAGE  Redis 镜像来源，默认 docker.m.daocloud.io/library/redis:7.2-alpine
REFRESH_DB          设为 1 时重新导出本机 MySQL 数据
REFRESH_FRONTEND    设为 1 时同步本机 nginx 前端静态文件
SKIP_MAVEN          设为 1 时跳过 Maven 构建并复用已有 jar
SKIP_ZIP_TEST       设为 1 时跳过 unzip -t 完整性检查
```

### 打包前检查

确认 Docker Desktop 已启动：

```bash
docker version
docker buildx ls
```

确认 Docker 支持 `linux/amd64`：

```bash
docker buildx ls
```

输出的 `PLATFORMS` 中应包含 `linux/amd64`。

### 打包后检查

校验 sha256：

```bash
cd dist
shasum -a 256 -c ledger-windows-intel-docker-3.9.0-linux-amd64.zip.sha256
```

查看离线包大小：

```bash
ls -lh dist/ledger-windows-intel-docker-3.9.0-linux-amd64.zip
```

## GitHub 一键打包

在 GitHub 页面进入 `Actions`，选择 `Build Windows Intel Docker Package`，点击 `Run workflow`。workflow 会：

1. 使用 JDK 11 构建 `ledger-admin/target/ledger-admin.jar`。
2. 构建 `linux/amd64` 的 `ledger-app`、`ledger-mysql`、`ledger-nginx` 镜像。
3. 拉取 `linux/amd64` 的 `redis:7.2-alpine`。
4. 在 `docker save` 前逐个校验镜像平台必须是 `linux/amd64`。
5. 上传 `ledger-windows-intel-docker-*.zip` artifact。

本机是 Apple Silicon/M4 时，不要直接把本机已有 Docker 镜像 `docker save` 给 Windows Intel 使用；本机镜像通常是 `linux/arm64`。以 GitHub Actions 产物为准。

如果必须在 M4 本机直接打包，可以用 Buildx/QEMU 强制构建 `linux/amd64`：

```bash
./deploy/scripts/build-local-windows-intel-package.sh
```

脚本会在 `docker save` 前校验所有镜像必须是 `linux/amd64`，产物在 `dist/ledger-windows-intel-docker-*.zip`。这种方式能用，但构建速度通常明显慢于 GitHub Actions。

## 更新本机数据和前端资产

在 Mac 本机仓库中更新数据库 dump：

```bash
./deploy/scripts/export-local-data.sh
```

同步本机 nginx 正在部署的前端静态文件：

```bash
./deploy/scripts/sync-local-nginx-assets.sh
```

然后提交 `deploy/docker/mysql/initdb/001-ledger-manager.sql.gz` 和 `deploy/docker/nginx/html/` 的变化，再在 GitHub Actions 运行打包 workflow。
