#!/usr/bin/env bash
# Submit HalfRotate to F-Droid: RFP issue and/or fdroiddata merge request.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GLAB="${GLAB:-/tmp/glab-install/bin/glab}"
PACKAGE="dev.pablo.halfrotate"
MODE="${1:-both}"  # rfp | mr | both

token() {
  if [[ -n "${GITLAB_TOKEN:-}" ]]; then
    echo "$GITLAB_TOKEN"
    return
  fi
  python3 - <<'PY' 2>/dev/null || true
import yaml, pathlib
cfg = pathlib.Path.home() / ".config/glab-cli/config.yml"
if cfg.exists():
    data = yaml.safe_load(cfg.read_text())
    t = data.get("hosts", {}).get("gitlab.com", {}).get("token")
    if t:
        print(t)
PY
}

TOKEN="$(token || true)"
if [[ -z "$TOKEN" ]]; then
  echo "Set GITLAB_TOKEN or run: glab auth login" >&2
  exit 1
fi

export GITLAB_TOKEN="$TOKEN"
export GLAB_TOKEN="$TOKEN"

submit_rfp() {
  local body
  body="$(sed '1,4d' "$ROOT/docs/FDROID_RFP.md")"
  curl -fsS --request POST \
    --header "PRIVATE-TOKEN: $TOKEN" \
    --header "Content-Type: application/json" \
    --data "$(python3 -c 'import json,sys; print(json.dumps({"title":"HalfRotate (dev.pablo.halfrotate)","description":sys.stdin.read(),"labels":["new-app"]}))' <<<"$body")" \
    "https://gitlab.com/api/v4/projects/fdroid%2Frfp/issues" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["web_url"])'
}

submit_mr() {
  local work="/tmp/fdroiddata-halfrotate-$$"
  rm -rf "$work"
  git clone --depth=1 "https://oauth2:${TOKEN}@gitlab.com/fdroid/fdroiddata.git" "$work"
  cd "$work"
  git remote add fork "https://oauth2:${TOKEN}@gitlab.com/pablogventura/fdroiddata.git" 2>/dev/null || true
  if ! git ls-remote fork &>/dev/null; then
    curl -fsS --request POST --header "PRIVATE-TOKEN: $TOKEN" \
      "https://gitlab.com/api/v4/projects/fdroid%2Ffdroiddata/fork" \
      -d "namespace=pablogventura" >/dev/null
    sleep 3
  fi
  git fetch fork master 2>/dev/null || git fetch "https://oauth2:${TOKEN}@gitlab.com/pablogventura/fdroiddata.git" master
  git checkout -b "$PACKAGE"
  cp "$ROOT/metadata/fdroid/${PACKAGE}.yml" "metadata/${PACKAGE}.yml"
  git add "metadata/${PACKAGE}.yml"
  git -c user.name="Pablo Ventura" -c user.email="pablo@users.noreply.github.com" \
    commit -m "New App: $PACKAGE"
  git push fork HEAD:"$PACKAGE"
  curl -fsS --request POST \
    --header "PRIVATE-TOKEN: $TOKEN" \
    --header "Content-Type: application/json" \
    --data "$(python3 -c 'import json; print(json.dumps({"source_branch":"'"$PACKAGE"'","target_branch":"master","title":"New App: '"$PACKAGE"'","description":"HalfRotate — limit auto-rotation to chosen orientations.\n\nMetadata draft: https://github.com/pablogventura/halfrotate/blob/main/metadata/fdroid/dev.pablo.halfrotate.yml"}))')" \
    "https://gitlab.com/api/v4/projects/fdroid%2Ffdroiddata/merge_requests" \
    | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["web_url"])'
}

case "$MODE" in
  rfp) submit_rfp ;;
  mr) submit_mr ;;
  both)
    echo "=== RFP ==="
    submit_rfp
    echo "=== MR ==="
    submit_mr
    ;;
  *) echo "Usage: $0 [rfp|mr|both]" >&2; exit 1 ;;
esac
