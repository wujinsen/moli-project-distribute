#!/usr/bin/env bash
#
# Moli 微服务启停脚本（Linux，通用）
#
# 用法:
#   ./moli-service.sh user-center start
#   ./moli-service.sh gateway status
#   MOLI_SERVICE=knowledge ./moli-service.sh start
#
# 配置:
#   deploy/linux/{service}.env.example → 复制到 APP_HOME/conf/moli-{service}.env
#   或通过 MOLI_ENV_FILE 指定 env 文件
#
# 推荐目录（monorepo 检出到 /opt/moli-project-distribute）:
#   /opt/moli-project-distribute/moli-user-center/   jar + conf/ + application-pro.yml
#   /opt/moli-project-distribute/moli-gateway/
#   /opt/moli-project-distribute/moli-knowledge/     kb/ 在同仓库 moli-knowledge/kb

set -u

MOLI_ROOT="${MOLI_ROOT:-/opt/moli-project-distribute}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

declare -A SERVICE_APP_NAME=(
  [user-center]="user-center-server"
  [gateway]="moli-gateway"
  [knowledge]="knowledge-server"
)
declare -A SERVICE_JAR_PREFIX=(
  [user-center]="moli-user-center-server"
  [gateway]="moli-gateway"
  [knowledge]="moli-knowledge-server"
)
declare -A SERVICE_MODULE_DIR=(
  [user-center]="moli-user-center"
  [gateway]="moli-gateway"
  [knowledge]="moli-knowledge"
)

MOLI_SERVICE="${MOLI_SERVICE:-}"
ACTION="${1:-}"
if [[ -n "$MOLI_SERVICE" ]]; then
  :
elif [[ "$ACTION" =~ ^(user-center|gateway|knowledge)$ ]]; then
  MOLI_SERVICE="$ACTION"
  ACTION="${2:-}"
else
  MOLI_SERVICE="${MOLI_SERVICE:-user-center}"
fi

APP_NAME="${SERVICE_APP_NAME[$MOLI_SERVICE]:-moli-service}"
JAR_PREFIX="${SERVICE_JAR_PREFIX[$MOLI_SERVICE]:-moli}"
APP_HOME="${APP_HOME:-${MOLI_ROOT}/${SERVICE_MODULE_DIR[$MOLI_SERVICE]:-moli-${MOLI_SERVICE}}}"
JAR_FILE="${JAR_FILE:-}"
JAVA_CMD="${JAVA_CMD:-java}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-pro}"
SPRING_ARGS="${SPRING_ARGS:-}"
STOP_TIMEOUT="${STOP_TIMEOUT:-30}"
PID_FILE="${PID_FILE:-}"
LOG_DIR="${LOG_DIR:-}"
LOG_FILE="${LOG_FILE:-}"

load_env() {
  local env_file="${MOLI_ENV_FILE:-}"
  if [[ -z "$env_file" ]]; then
    for candidate in \
      "${APP_HOME}/conf/moli-${MOLI_SERVICE}.env" \
      "${APP_HOME}/conf/moli-server.env" \
      "${MOLI_ROOT}/conf/moli-${MOLI_SERVICE}.env" \
      "${SCRIPT_DIR}/moli-${MOLI_SERVICE}.env" \
      "${SCRIPT_DIR}/moli-${MOLI_SERVICE}.env.local"; do
      if [[ -f "$candidate" ]]; then
        env_file="$candidate"
        break
      fi
    done
  fi

  if [[ -n "$env_file" && -f "$env_file" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$env_file"
    set +a
    echo "[INFO] loaded env: $env_file"
  else
    echo "[INFO] env file not found for ${MOLI_SERVICE}, using defaults (APP_HOME=$APP_HOME)"
    echo "       tip: copy deploy/linux/${MOLI_SERVICE}.env.example to ${APP_HOME}/conf/moli-${MOLI_SERVICE}.env"
  fi

  MOLI_ROOT="${MOLI_ROOT:-/opt/moli-project-distribute}"
  APP_HOME="${APP_HOME:-${MOLI_ROOT}/${SERVICE_MODULE_DIR[$MOLI_SERVICE]:-moli-${MOLI_SERVICE}}}"
  APP_NAME="${APP_NAME:-${SERVICE_APP_NAME[$MOLI_SERVICE]}}"
  JAR_PREFIX="${JAR_PREFIX:-${SERVICE_JAR_PREFIX[$MOLI_SERVICE]}}"
  JAVA_CMD="${JAVA_CMD:-java}"
  JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai}"
  SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-pro}"
  SPRING_ARGS="${SPRING_ARGS:-}"
  STOP_TIMEOUT="${STOP_TIMEOUT:-30}"
  PID_FILE="${PID_FILE:-${APP_HOME}/run/${MOLI_SERVICE}.pid}"
  LOG_DIR="${LOG_DIR:-${APP_HOME}/logs}"
  LOG_FILE="${LOG_FILE:-${LOG_DIR}/${MOLI_SERVICE}.log}"

  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    JAVA_CMD="${JAVA_HOME}/bin/java"
  fi
}

resolve_jar() {
  if [[ -n "$JAR_FILE" && -f "$JAR_FILE" ]]; then
    return 0
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

  echo "[ERROR] cannot find jar under $APP_HOME (prefix=${JAR_PREFIX})"
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
  [[ -f "$PID_FILE" ]] || return 1
  local pid
  pid="$(tr -d '[:space:]' < "$PID_FILE")"
  [[ "$pid" =~ ^[0-9]+$ ]] || return 1
  echo "$pid"
}

find_running_pids() {
  resolve_jar 2>/dev/null || true
  local jar_name
  jar_name="$(basename "${JAR_FILE:-${JAR_PREFIX}.jar}")"
  if command -v pgrep >/dev/null 2>&1; then
    pgrep -f "[j]ava.*${jar_name}" 2>/dev/null || true
  else
    ps -ef 2>/dev/null | grep -E "[j]ava.*${jar_name}" | awk '{print $2}' || true
  fi
}

kill_pid_gracefully() {
  local pid="$1"
  [[ "$pid" =~ ^[0-9]+$ ]] || return 1
  if ! is_running "$pid"; then
    return 0
  fi
  echo "[INFO] stopping ${MOLI_SERVICE} (pid=$pid)"
  kill -15 "$pid" 2>/dev/null || true
  local waited=0
  while ((waited < STOP_TIMEOUT)); do
    is_running "$pid" || { echo "[OK] stopped (pid=$pid)"; return 0; }
    sleep 1
    waited=$((waited + 1))
  done
  echo "[WARN] SIGKILL pid=$pid"
  kill -9 "$pid" 2>/dev/null || true
  ! is_running "$pid"
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$(dirname "$PID_FILE")"
}

check_java() {
  command -v "$JAVA_CMD" >/dev/null 2>&1 || {
    echo "[ERROR] java not found: $JAVA_CMD"
    return 1
  }
  echo "[INFO] java: $($JAVA_CMD -version 2>&1 | head -n 1)"
}

preflight_checks() {
  if [[ "$SPRING_PROFILES_ACTIVE" == "pro" ]]; then
    if [[ ! -f "${APP_HOME}/application-pro.yml" ]]; then
      echo "[ERROR] missing ${APP_HOME}/application-pro.yml"
      echo "        copy deploy/application-pro/${MOLI_SERVICE}.yml.example -> application-pro.yml"
      return 1
    fi
    if [[ -z "${SPRING_DATASOURCE_PASSWORD:-}" || "${SPRING_DATASOURCE_PASSWORD}" == *"请替换"* ]]; then
      if [[ "$MOLI_SERVICE" != "gateway" ]]; then
        echo "[ERROR] SPRING_DATASOURCE_PASSWORD not set in conf/moli-${MOLI_SERVICE}.env"
        return 1
      fi
    fi
    if [[ "$MOLI_SERVICE" == "user-center" ]]; then
      if [[ -z "${SSO_SHARED_SECRET:-}" || "${SSO_SHARED_SECRET}" == *"请替换"* ]]; then
        echo "[WARN] SSO_SHARED_SECRET looks like placeholder"
      fi
    fi
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
    echo "[INFO] ${MOLI_SERVICE} already running (pid=$pid)"
    return 0
  fi

  [[ -d "$APP_HOME" ]] || { echo "[ERROR] APP_HOME not found: $APP_HOME"; return 1; }

  echo "[INFO] starting ${MOLI_SERVICE} (${APP_NAME})"
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
    echo "[OK] ${MOLI_SERVICE} started (pid=$new_pid)"
    return 0
  fi
  echo "[ERROR] start failed, see $LOG_FILE"
  rm -f "$PID_FILE"
  return 1
}

stop_server() {
  resolve_jar || true
  local stopped=0 pid scan_pid
  if pid="$(read_pid 2>/dev/null || true)" && is_running "$pid"; then
    kill_pid_gracefully "$pid" && stopped=1
  fi
  rm -f "$PID_FILE"
  while IFS= read -r scan_pid; do
    [[ -z "$scan_pid" ]] && continue
    is_running "$scan_pid" && kill_pid_gracefully "$scan_pid" && stopped=1
  done < <(find_running_pids 2>/dev/null || true)
  ((stopped == 1)) && return 0
  echo "[INFO] ${MOLI_SERVICE} is not running"
}

status_server() {
  resolve_jar || return 1
  local pid found=0 scan_pid
  if pid="$(read_pid 2>/dev/null || true)" && is_running "$pid"; then
    echo "[OK] ${MOLI_SERVICE} running (pid=$pid)"
    found=1
  fi
  while IFS= read -r scan_pid; do
    [[ -z "$scan_pid" ]] && continue
    if is_running "$scan_pid"; then
      echo "[OK] ${MOLI_SERVICE} running (pid=$scan_pid)"
      found=1
    fi
  done < <(find_running_pids 2>/dev/null || true)
  if ((found == 1)); then
    echo "     jar: $JAR_FILE"
    echo "     log: $LOG_FILE"
    return 0
  fi
  echo "[STOPPED] ${MOLI_SERVICE}"
  return 1
}

logs_server() {
  ensure_dirs
  local lines="${1:-}"
  [[ -f "$LOG_FILE" ]] || { echo "[WARN] no log: $LOG_FILE"; return 1; }
  if [[ -n "$lines" && "$lines" =~ ^[0-9]+$ ]]; then
    tail -n "$lines" "$LOG_FILE"
  else
    tail -f "$LOG_FILE"
  fi
}

usage() {
  cat <<EOF
Usage: $0 {user-center|gateway|knowledge} {start|stop|restart|status|logs [lines]}
   or: MOLI_SERVICE=gateway $0 start

Env: MOLI_ENV_FILE, APP_HOME, SPRING_PROFILES_ACTIVE=pro
EOF
}

main() {
  load_env
  case "${ACTION:-}" in
    start) start_server ;;
    stop) stop_server ;;
    restart) stop_server; start_server ;;
    status) status_server ;;
    logs) logs_server "${3:-${2:-}}" ;;
    *) usage; exit 1 ;;
  esac
}

main "$@"
