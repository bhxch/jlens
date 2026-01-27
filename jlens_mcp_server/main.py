import subprocess
import sys
import os
import glob
import urllib.request
import tempfile
from pathlib import Path

VERSION = "1.1.1"
REPO = "bhxch/jlens"
JAR_NAME_PREFIX = "jlens-mcp-server-"
CACHE_DIR = Path.home() / ".jlens" / "cache"

def find_local_jar():
    package_dir = Path(__file__).parent
    root_dir = package_dir.parent
    
    search_patterns = [
        str(package_dir / f"{JAR_NAME_PREFIX}*.jar"),
        str(root_dir / "target" / f"{JAR_NAME_PREFIX}*.jar"),
        str(package_dir / "target" / f"{JAR_NAME_PREFIX}*.jar"),
    ]
    
    for pattern in search_patterns:
        matches = glob.glob(pattern)
        matches = [m for m in matches if "original" not in m]
        if matches:
            return matches[0]
    return None

def download_jar(version):
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    dest = CACHE_DIR / f"{JAR_NAME_PREFIX}{version}.jar"
    url = f"https://github.com/{REPO}/releases/download/v{version}/{JAR_NAME_PREFIX}{version}.jar"
    
    print(f"JAR not found locally. Attempting to download version {version}...", file=sys.stderr)
    try:
        urllib.request.urlretrieve(url, dest)
        return str(dest)
    except Exception as e:
        print(f"Error downloading JAR: {e}", file=sys.stderr)
        return None

def main():
    args = sys.argv[1:]
    java_path = "java"
    jar_path = None
    mcp_args = []

    # Simple arg parsing
    i = 0
    while i < len(args):
        if args[i] == "--java-path" and i + 1 < len(args):
            java_path = args[i+1]
            i += 2
        elif args[i] == "--jar-path" and i + 1 < len(args):
            jar_path = args[i+1]
            i += 2
        else:
            mcp_args.append(args[i])
            i += 1

    # 1. Try local
    if not jar_path:
        jar_path = find_local_jar()

    # 2. Try cache
    if not jar_path:
        cached_jar = CACHE_DIR / f"{JAR_NAME_PREFIX}{VERSION}.jar"
        if cached_jar.exists():
            jar_path = str(cached_jar)

    # 3. Download
    if not jar_path:
        jar_path = download_jar(VERSION)

    if not jar_path:
        print("Error: Could not find or download jlens-mcp-server JAR file.", file=sys.stderr)
        sys.exit(1)

    # Check Java
    try:
        subprocess.run([java_path, "-version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print(f"Error: Java not found at '{java_path}'. Please ensure Java 25+ is installed.", file=sys.stderr)
        sys.exit(1)

    # Execute
    cmd = [java_path, "-jar", jar_path] + mcp_args
    try:
        process = subprocess.Popen(cmd)
        process.wait()
        sys.exit(process.returncode)
    except KeyboardInterrupt:
        process.terminate()
        sys.exit(0)

if __name__ == "__main__":
    main()
