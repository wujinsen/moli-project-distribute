#!/usr/bin/env bash
#
# Moli 微服务启停脚本（Linux）
# 基于 deploy/linux/moli-server.sh，适配 moli-project-distribute 五服务目录
#
# 部署根: /opt/moli-project-distribute
# 方式 A（整仓）: JAR 在 moli-*/target/ 或 moli-*-server/target/
# 方式 B（扁平）: JAR 在 moli-*/ 根目录
#   moli-user-center/  jar + application-pro.yml + conf/moli-user-center.env
#   moli-gateway/
#   moli-knowledge/
#   moli-order/
#   moli-ai/          (AI · artifact moli-ai-server)
#   deploy/linux/moli-service.sh
#
# 用法:
#   ./deploy/linux/moli-service.sh user-center start
#   ./deploy/linux/moli-service.sh gateway status
#   ./deploy/linux/moli-service.sh knowledge logs 200
#   ./deploy/linux/moli-service.sh order restart
#   ./deploy/linux/moli-service.sh ai status

set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOLI_DEPLOY_ROOT="${MOLI_DEPLOY_ROOT:-/opt/moli-project-distribute}"

# ---------- 服务参数（勿与单服 moli-server 的 /opt/moli/backend 混淆）----------
SERVICE_KEY="${1:-}"
ACTION="${2:-}"

declare -A SVC_MODULE=(
  [user-center]="moli-user-center"
  [gateway]="moli-gateway"
  [knowledge]="moli-knowledge"
  [order]="moli-order"
  [ai]="moli-ai"
)
declare -A SVC_APP_NAME=(
  [user-center]="user-center-server"
  [gateway]="moli-gateway"
  [knowledge]="knowledge-server"
  [order]="order-server"
  [ai]="ai-server"
)
declare -A SVC_JAR_PREFIX=(
  [user-center]="moli-user-center-server"
  [gateway]="moli-gateway"
  [knowledge]="moli-knowledge-server"
  [order]="moli-order-server"
  [ai]="moli-ai-server"
)
declare -A SVC_PID_NAME=(
  [user-center]="user-center"
  [gateway]="gateway"
  [knowledge]="knowledge"
  [order]="order"
  [ai]="bi"
)
# 方式 A（整仓 git pull）：JAR 在 Maven target 子目录
declare -A SVC_MAVEN_TARGET=(
  [user-center]="moli-user-center-server/target"
  [gateway]="target"
  [knowledge]="moli-knowledge-server/target"
  [order]="moli-order-server/target"
  [ai]="moli-ai-server/target"
)

if [[ -z "$SERVICE_KEY" || -z "${SVC_MODULE[$SERVICE_KEY]:-}" ]]; then
  cat <<EOF
Usage: $0 {user-center|gateway|knowledge|order|ai} {start|stop|restart|status|logs [lines]}

Deploy root: ${MOLI_DEPLOY_ROOT}
Env:  \${APP_HOME}/conf/moli-<service>.env  (see deploy/linux/moli-*.env.example)
Java: JAVA_HOME in env, e.g. /usr/lib/jvm/java-11-amazon-corretto
EOF
  exit 1
fi

MODULE_DIR="${SVC_MODULE[$SERVICE_KEY]}"
APP_HOME="${MOLI_DEPLOY_ROOT}/${MODULE_DIR}"
APP_NAME="${SVC_APP_NAME[$SERVICE_KEY]}"
JAR_PREFIX="${SVC_JAR_PREFIX[$SERVICE_KEY]}"
PID_BASENAME="${SVC_PID_NAME[$SERVICE_KEY]}"

JAR_FILE="${JAR_FILE:-}"
JAVA_CMD="${JAVA_CMD:-java}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-pro}"
SPRING_ARGS="${SPRING_ARGS:-}"
STOP_TIMEOUT="${STOP_TIMEOUT:-30}"
PID_FILE=""
LOG_DIR=""
LOG_FILE=""

load_env() {
  local env_file="${MOLI_ENV_FILE:-}"
  if [[ -z "$env_file" ]]; then
    for candidate in \
      "${APP_HOME}/conf/moli-${SERVICE_KEY}.env" \
      "${MOLI_DEPLOY_ROOT}/conf/moli-${SERVICE_KEY}.env" \
      "${SCRIPT_DIR}/moli-${SERVICE_KEY}.env" \
      "${SCRIPT_DIR}/moli-${SERVICE_KEY}.env.local"; do
      if [[ -f "$candidate" ]]; then
        env_file="$candidate"
        break
      fi
    done
  fi

  if [[ -n "$env_file" && -f "$env_file" ]]; then
    set -a
    # Windows 上传的 .env 可能带 CRLF
    # shellcheck disable=SC1090
    source <(sed 's/\r$//' "$env_file")
    set +a
    echo "[INFO] loaded env: $env_file"
  else
    echo "[INFO] env file not found, using defaults (APP_HOME=$APP_HOME)"
    echo "       tip: copy deploy/linux/moli-${SERVICE_KEY}.env.example to ${APP_HOME}/conf/moli-${SERVICE_KEY}.env"
  fi

  # MOLI_ROOT 为旧键名，与 MOLI_DEPLOY_ROOT 等价
  MOLI_DEPLOY_ROOT="${MOLI_DEPLOY_ROOT:-${MOLI_ROOT:-/opt/moli-project-distribute}}"
  local canonical_home="${MOLI_DEPLOY_ROOT}/${MODULE_DIR}"
  if [[ -n "${APP_HOME:-}" && "$APP_HOME" != "$canonical_home" ]]; then
    echo "[WARN] APP_HOME in env is stale ($APP_HOME), using $canonical_home"
  fi
  APP_HOME="$canonical_home"
  APP_NAME="${APP_NAME:-${SVC_APP_NAME[$SERVICE_KEY]}}"
  JAR_PREFIX="${JAR_PREFIX:-${SVC_JAR_PREFIX[$SERVICE_KEY]}}"
  JAVA_CMD="${JAVA_CMD:-java}"
  JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai}"
  SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-pro}"
  SPRING_ARGS="${SPRING_ARGS:-}"
  STOP_TIMEOUT="${STOP_TIMEOUT:-30}"
  # 路径固定随 APP_HOME，避免 env 里残留 /opt/moli/* 旧目录
  PID_FILE="${APP_HOME}/run/${PID_BASENAME}.pid"
  LOG_DIR="${APP_HOME}/logs"
  LOG_FILE="${LOG_DIR}/${PID_BASENAME}.log"

  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    JAVA_CMD="${JAVA_HOME}/bin/java"
  elif [[ "$JAVA_CMD" == "java" ]] && ! command -v java >/dev/null 2>&1; then
    local j
    for j in \
      /usr/lib/jvm/java-11-amazon-corretto \
      /usr/lib/jvm/java-*-amazon-corretto \
      /usr/lib/jvm/java-*; do
      if [[ -x "${j}/bin/java" ]]; then
        JAVA_HOME="$j"
        JAVA_CMD="${j}/bin/java"
        echo "[INFO] auto-detected JAVA_HOME=$JAVA_HOME"
        break
      fi
    done
  fi
}

resolve_jar() {
  if [[ -n "$JAR_FILE" ]]; then
    if [[ -f "$JAR_FILE" ]]; then
      return 0
    fi
    echo "[ERROR] JAR_FILE not found: $JAR_FILE"
    return 1
  fi

  local candidates=(
    "${APP_HOME}/${JAR_PREFIX}.jar"
    "${APP_HOME}/${APP_NAME}.jar"
  )

  local jar
  for jar in "${candidates[@]}"; do
    if [[ -f "$jar" ]]; then
      JAR_FILE="$jar"
      return 0
    fi
  done

  local matched=()
  shopt -s nullglob
  matched=("${APP_HOME}/${JAR_PREFIX}-"*.jar)
  shopt -u nullglob

  if ((${#matched[@]} > 0)); then
    JAR_FILE="$(ls -1t "${matched[@]}" | head -n 1)"
    return 0
  fi

  # 整仓部署：JAR 在 moli-*/target/ 或 moli-*-server/target/
  local maven_rel="${SVC_MAVEN_TARGET[$SERVICE_KEY]:-}"
  if [[ -n "$maven_rel" ]]; then
    local maven_dir="${APP_HOME}/${maven_rel}"
    shopt -s nullglob
    matched=("${maven_dir}/${JAR_PREFIX}-"*.jar)
    shopt -u nullglob
    if ((${#matched[@]} > 0)); then
      JAR_FILE="$(ls -1t "${matched[@]}" | head -n 1)"
      echo "[INFO] using Maven target jar: $JAR_FILE"
      return 0
    fi
  fi

  echo "[ERROR] cannot find jar under $APP_HOME (flat layout or ${SVC_MAVEN_TARGET[$SERVICE_KEY]:-target})"
  echo "        set JAR_FILE in conf/moli-${SERVICE_KEY}.env or place ${JAR_PREFIX}-*.jar in APP_HOME"
  return 1
}

is_running() {
  local pid="$1"
  [[ -n "$pid" ]] || return 1
  kill -0 "$pid" 2>/dev/null || return 1

  if [[ -r "/proc/${pid}/cmdline" ]]; then
    local cmdline
    cmdline="$(tr '\0' ' ' < "/proc/${pid}/cmdline")"
    [[ "$cmdline" == *"${JAR_FILE}"* || "$cmdline" == *"${JAR_PREFIX}"* ]] || return 1
  fi
  return 0
}

read_pid() {
  if [[ ! -f "$PID_FILE" ]]; then
    return 1
  fi
  local pid
  pid="$(tr -d '[:space:]' < "$PID_FILE")"
  [[ "$pid" =~ ^[0-9]+$ ]] || return 1
  echo "$pid"
}

find_running_pids() {
  local jar_name="${JAR_FILE:-}"
  if [[ -z "$jar_name" ]]; then
    resolve_jar 2>/dev/null || true
  fi
  jar_name="$(basename "${JAR_FILE:-${JAR_PREFIX}.jar}")"

  local pids=()
  if command -v pgrep >/dev/null 2>&1; then
    local pid
    while IFS= read -r pid; do
      [[ -n "$pid" ]] && pids+=("$pid")
    done < <(pgrep -f "[j]ava.*${jar_name}" 2>/dev/null || true)
  else
    local line pid
    while IFS= read -r line; do
      pid="$(echo "$line" | awk '{print $2}')"
      [[ "$pid" =~ ^[0-9]+$ ]] && pids+=("$pid")
    done < <(ps -ef 2>/dev/null | grep -E "[j]ava.*${jar_name}" || true)
  fi

  if ((${#pids[@]} == 0)); then
    return 1
  fi

  printf '%s\n' "${pids[@]}" | sort -u
}

kill_pid_gracefully() {
  local pid="$1"
  [[ "$pid" =~ ^[0-9]+$ ]] || return 1
  if ! is_running "$pid"; then
    return 0
  fi

  echo "[INFO] stopping ${SERVICE_KEY} (pid=$pid)"
  kill -15 "$pid" 2>/dev/null || true

  local waited=0
  while ((waited < STOP_TIMEOUT)); do
    if ! is_running "$pid"; then
      echo "[OK] ${SERVICE_KEY} stopped (pid=$pid)"
      return 0
    fi
    sleep 1
    waited=$((waited + 1))
  done

  echo "[WARN] graceful stop timeout for pid=$pid, sending SIGKILL"
  kill -9 "$pid" 2>/dev/null || true
  if ! is_running "$pid"; then
    echo "[OK] ${SERVICE_KEY} force stopped (pid=$pid)"
    return 0
  fi
  return 1
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$(dirname "$PID_FILE")"
}

check_java() {
  if [[ -x "$JAVA_CMD" ]]; then
    :
  elif ! command -v "$JAVA_CMD" >/dev/null 2>&1; then
    echo "[ERROR] java not found: $JAVA_CMD"
    echo "        set JAVA_HOME or JAVA_CMD in ${APP_HOME}/conf/moli-${SERVICE_KEY}.env"
    echo "        example: JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto"
    return 1
  fi
  echo "[INFO] java: $($JAVA_CMD -version 2>&1 | head -n 1)"
  return 0
}

preflight_checks() {
  if [[ "$SPRING_PROFILES_ACTIVE" == "pro" ]]; then
    if [[ "$SERVICE_KEY" != "gateway" ]]; then
      if [[ -z "${SPRING_DATASOURCE_PASSWORD:-}" || "${SPRING_DATASOURCE_PASSWORD}" == *"请替换"* ]]; then
        echo "[ERROR] SPRING_DATASOURCE_PASSWORD is not set in conf/moli-${SERVICE_KEY}.env"
        return 1
      fi
    fi
    if [[ "$SERVICE_KEY" == "user-center" ]]; then
      if [[ -z "${SSO_SHARED_SECRET:-}" || "${SSO_SHARED_SECRET}" == *"请替换"* ]]; then
        echo "[WARN] SSO_SHARED_SECRET looks like placeholder, please change for production"
      fi
    fi
  fi

  if [[ ! -f "${APP_HOME}/application-${SPRING_PROFILES_ACTIVE}.yml" && ! -f "${APP_HOME}/application-pro.yml" ]]; then
    echo "[WARN] no external application-${SPRING_PROFILES_ACTIVE}.yml under $APP_HOME"
    echo "       using classpath config + environment variables only"
  fi

  if [[ -f "${APP_HOME}/application-pro.yml" ]]; then
    SPRING_ARGS="${SPRING_ARGS} --spring.config.additional-location=file:./application-pro.yml"
  fi

  return 0
}

start_server() {
  check_java || return 1
  preflight_checks || return 1
  resolve_jar || return 1
  ensure_dirs

  local pid
  if pid="$(read_pid 2>/dev/null || true)" && is_running "$pid"; then
    echo "[INFO] ${SERVICE_KEY} already running (pid=$pid)"
    return 0
  fi

  if [[ ! -d "$APP_HOME" ]]; then
    echo "[ERROR] APP_HOME not found: $APP_HOME"
    return 1
  fi

  echo "[INFO] starting ${SERVICE_KEY} (${APP_NAME})"
  echo "[INFO] APP_HOME=$APP_HOME"
  echo "[INFO] jar: $JAR_FILE"
  echo "[INFO] profile: $SPRING_PROFILES_ACTIVE"
  echo "[INFO] log: $LOG_FILE"

  cd "$APP_HOME" || return 1

  # shellcheck disable=SC2086
  nohup "$JAVA_CMD" $JAVA_OPTS -jar "$JAR_FILE" \
    --spring.profiles.active="$SPRING_PROFILES_ACTIVE" \
    $SPRING_ARGS >>"$LOG_FILE" 2>&1 &

  local new_pid=$!
  echo "$new_pid" >"$PID_FILE"
  sleep 2

  if is_running "$new_pid"; then
    echo "[OK] ${SERVICE_KEY} started (pid=$new_pid)"
    return 0
  fi

  echo "[ERROR] failed to start ${SERVICE_KEY}, see log: $LOG_FILE"
  rm -f "$PID_FILE"
  return 1
}

stop_server() {
  resolve_jar || true

  local stopped=0
  local pid

  if pid="$(read_pid 2>/dev/null || true)"; then
    if is_running "$pid"; then
      kill_pid_gracefully "$pid" && stopped=1
    else
      echo "[WARN] stale pid file (pid=$pid), will scan java process"
    fi
  elif [[ -f "$PID_FILE" ]]; then
    echo "[WARN] invalid pid file: $PID_FILE (empty or malformed), will scan java process"
  fi
  rm -f "$PID_FILE"

  local scan_pid
  while IFS= read -r scan_pid; do
    [[ -z "$scan_pid" ]] && continue
    if is_running "$scan_pid"; then
      kill_pid_gracefully "$scan_pid" && stopped=1
    fi
  done < <(find_running_pids 2>/dev/null || true)

  if ((stopped == 1)); then
    rm -f "$PID_FILE"
    return 0
  fi

  echo "[INFO] ${SERVICE_KEY} is not running"
  return 0
}

status_server() {
  resolve_jar || return 1

  local pid found=0
  if pid="$(read_pid 2>/dev/null || true)" && is_running "$pid"; then
    echo "[OK] ${SERVICE_KEY} is running (pid=$pid, source=pid file)"
    found=1
  fi

  local scan_pid
  while IFS= read -r scan_pid; do
    [[ -z "$scan_pid" ]] && continue
    if is_running "$scan_pid"; then
      echo "[OK] ${SERVICE_KEY} is running (pid=$scan_pid, source=process scan)"
      found=1
    fi
  done < <(find_running_pids 2>/dev/null || true)

  if ((found == 1)); then
    echo "     APP_HOME=$APP_HOME"
    echo "     jar: $JAR_FILE"
    echo "     profile: $SPRING_PROFILES_ACTIVE"
    echo "     log: $LOG_FILE"
    return 0
  fi

  echo "[STOPPED] ${SERVICE_KEY} is not running"
  if [[ -f "$PID_FILE" ]]; then
    echo "[WARN] stale pid file: $PID_FILE"
  fi
  return 1
}

logs_server() {
  ensure_dirs
  local lines="${1:-}"
  if [[ ! -f "$LOG_FILE" ]]; then
    echo "[WARN] log file not found: $LOG_FILE"
    return 1
  fi
  if [[ -n "$lines" && "$lines" =~ ^[0-9]+$ ]]; then
    tail -n "$lines" "$LOG_FILE"
  else
    tail -f "$LOG_FILE"
  fi
}

load_env

case "${ACTION:-}" in
  start) start_server ;;
  stop) stop_server ;;
  restart) stop_server; start_server ;;
  status) status_server ;;
  logs) logs_server "${3:-}" ;;
  *)
    echo "[ERROR] unknown action: ${ACTION:-}"
    exit 1
    ;;
esac
